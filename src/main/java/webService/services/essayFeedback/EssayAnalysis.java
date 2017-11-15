package webService.services.essayFeedback;

import data.*;
import data.document.Document;
import services.pca.EssayMeasurementsPCA;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndices;
import services.semanticModels.ISemanticModel;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

/**
 * Created by Robert Botarleanu on 03-Jun-17.
 * Computes the scores of a document from the loadings, means and stdevs.
 */
public class EssayAnalysis {

    private Map<String, Double> means;
    private Map<String, Double> sdevs;
    private Map<String, ArrayList<EssayMeasurementsPCA.MetricValue>> loadings;
    private List<ISemanticModel> models;
    private Map<String, Double> aoaDictionary;
    private Lang lang;
    JSONObject rules;

    public EssayAnalysis(Lang lang, List<ISemanticModel> models,
                         String loadingsPath, String metricMeansPath,
                         String metricStdevPath, String rulesPath,
                         String aoaPath
    ) {
        super();
        // Read the data
        this.loadings = EssayMeasurementsPCA.parseCSV(loadingsPath);
        this.means = EssayMeasurementsPCA.parseZscoreCSV(metricMeansPath);
        this.sdevs = EssayMeasurementsPCA.parseZscoreCSV(metricStdevPath);
        this.lang = lang;
        this.models = models;
        this.aoaDictionary = EssayMeasurementsPCA.parseAoA(aoaPath);

        org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();
        try {
            Reader reader = new FileReader(rulesPath);
            this.rules = (JSONObject) parser.parse(reader);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Double> computeScores(AbstractDocument d) {
        Map<String, Double> scores = new LinkedHashMap<>();

        // get the values for each index that is used for the component scores
        Map<String, Double> values = new LinkedHashMap<>();
        Map<ComplexityIndex, Double> dValues = d.getComplexityIndices();
        for (ComplexityIndex c : dValues.keySet()) {
            String name = "RB." + c.getAcronym();
            if (means.containsKey(name)) {
                Double sd = sdevs.get(name);
                Double mean = means.get(name);
                Double value = c.compute(d);
                Double v = (value - mean) / sd;
                values.put(name, v);
            }
        }

        values.put("Content.words",
                (d.getNoContentWords() * 1. - means.get("Content.words")) / sdevs.get("Content.words"));

        // Compute the scores for each component
        List<String> headers = new ArrayList<>(loadings.keySet());
        List<String> metrics = new ArrayList<>();
        loadings.get(headers.get(0)).forEach(mv -> metrics.add(mv.toString()));

        for (int i = 1; i < headers.size(); ++i) {
            String component = headers.get(i);
            Double score = 0.;
            for (int j = 0; j < metrics.size(); ++j) {
                try {
                    Double loading = Double.parseDouble(loadings.get(component).get(j).toString());
                    String m = metrics.get(j).replace("\"", "");
                    score += loading * values.get(m);
                    if (component.replace("\"", "").equals("V6")) {
                        System.out.println(m + ": " + values.get(m) + " x " + loading);
                    }
                } catch (NumberFormatException e) {
                    continue; // empty loading
                }
            }
            scores.put(component.replace("\"", ""), score);
            if (component.replace("\"", "").equals("V6")) {
                System.out.println("V6 score");
                System.out.println(score);
            }
        }

        return scores;
    }

    private static String choice(String[] population) {
        List<String> copy = Arrays.asList(population);
        Collections.shuffle(copy);

        return copy.get(0);
    }

    class Rule {
        String id;
        String name;
        String type;
        double min;
        double max;
        String[] feedbackMessagesLow;
        String[] feedbackMessagesHigh;

        Rule(JSONObject rule) {
            super();
            id = (String) rule.get("id");
            name = (String) rule.get("name");
            type = (String) rule.get("type");
            min = new Double(rule.get("min").toString());
            max = new Double(rule.get("max").toString());
//            JSONArray componentFeedbackObj = (JSONArray) rule.get("feedbackMessages");
//            feedbackMessages = new String[componentFeedbackObj.size()];
//            for (int i = 0; i < componentFeedbackObj.size(); ++i) {
//                feedbackMessages[i] = (String) componentFeedbackObj.get(i);
//            }
//
            JSONArray componentFeedbackLowObj = (JSONArray) rule.get("feedbackMessagesLow");
            JSONArray componentFeedbackHighObj = (JSONArray) rule.get("feedbackMessagesHigh");

            feedbackMessagesLow = new String[componentFeedbackLowObj.size()];
            feedbackMessagesHigh = new String[componentFeedbackHighObj.size()];

            for (int i = 0; i < componentFeedbackLowObj.size(); ++i) {
                feedbackMessagesLow[i] = (String) componentFeedbackLowObj.get(i);
            }

            for (int i = 0; i < componentFeedbackHighObj.size(); ++i) {
                feedbackMessagesHigh[i] = (String) componentFeedbackHighObj.get(i);
            }
        }

        private int getSeverityLevel(double score) {
            double mean = (min + max) / 2;
            double stdev = (max - mean) / 2 * 2;

            double dist = Math.min(Math.abs(score - min), Math.abs(score - max));
            if (dist >= stdev) {
                return EFResult.NUMBER_OF_SEVERITY_LEVELS;
            } else {
                int closestLevel = 0;
                double closestMark = Double.MAX_VALUE;
                for (int i = 0; i <= EFResult.NUMBER_OF_SEVERITY_LEVELS; ++i) {
                    double point = stdev / EFResult.NUMBER_OF_SEVERITY_LEVELS * i;
                    double d = Math.abs(dist - point);
                    if (d < closestMark) {
                        closestMark = d;
                        closestLevel = i;
                    }
                }
                return closestLevel;
            }
        }

        private int getCorrectnessLevel(double score) {
            double mean = (min + max) / 2;
            double stdev = (max - mean);

            int closestLevel = -5;
            double closestMark = Double.MAX_VALUE;
            int index = -10;

            for (double k = -1; k <= 1; k += 0.2, index++) {
                double mark = mean + k * stdev;
                double dist = Math.abs(score - mark);
                if (dist < closestMark) {
                    closestMark = dist;
                    closestLevel = index;
                }
            }

            return Math.min(closestLevel, -1);
        }
    }

    private Map<String, Double> getIndices(AbstractDocument d) {
        Map<String, Double> indices = new LinkedHashMap<>();
        indices.put("Paragraphs", (double) d.getNoBlocks());
        indices.put("Sentences", (double) d.getNoSentences());
        indices.put("Words", (double) d.getNoWords());
        indices.put("Content words", (double) d.getNoContentWords());
        for (ComplexityIndex ci : d.getComplexityIndices().keySet()) {
            indices.put(ci.getAcronym(), d.getComplexityIndices().get(ci));
        }
        return indices;
    }

    public EFResult analyzeDocument(AbstractDocument d) {
        EFResult res = new EFResult();
        Map<String, Double> scores = computeScores(d);

        JSONArray documentRules = (JSONArray) rules.get("document");
        JSONArray paragraphRules = (JSONArray) rules.get("paragraph");
        JSONArray phraseRules = (JSONArray) rules.get("phrase");
        JSONArray wordRules = (JSONArray) rules.get("word");


        Map<String, Double> documentIndices = getIndices(d);

        for (Object ruleObject : documentRules) {
            Rule rule = new Rule((JSONObject) ruleObject);
            double score = 0;
            if (rule.type.equals("component")) {
                score = scores.get(rule.id);
            } else {
                score = documentIndices.get(rule.id);
            }
            if (score < rule.min || score > rule.max) {
                res.getComponents().add(rule.name);
                if (score < rule.min) {
                    res.getFeedbackMessages().add(choice(rule.feedbackMessagesLow));
                } else {
                    res.getFeedbackMessages().add(choice(rule.feedbackMessagesHigh));
                }
                res.getFeedbackType().add(EFResult.TYPE_DOCUMENT);
                res.getFeedbackIndexPhrase().add(-1);
                res.getFeedbackIndexParagraph().add(-1);
                res.getFeedbackSeverityLevel().add(rule.getSeverityLevel(score));
            } else {
                res.getComponents().add(rule.name);
                res.getFeedbackMessages().add("No problems found.");
                res.getFeedbackType().add(EFResult.TYPE_DOCUMENT);
                res.getFeedbackIndexPhrase().add(-1);
                res.getFeedbackIndexParagraph().add(-1);
                res.getFeedbackSeverityLevel().add(rule.getCorrectnessLevel(score));
            }
        }


        for (int i = 0; i < d.getBlocks().size(); ++i) {
            Block block = d.getBlocks().get(i);
            List<String> blockText = new ArrayList<>();
            res.getAnalyzedText().add(blockText);

            AbstractDocumentTemplate template = AbstractDocumentTemplate.getDocumentModel(block.getText());
            AbstractDocument dBlock = new Document(null, template, models, lang, true);
            for (Object ruleObject : paragraphRules) {
                Rule rule = new Rule((JSONObject) ruleObject);
                double score = getParagraphScore(dBlock, rule.id);
                if (score < rule.min || score > rule.max) {
                    res.getComponents().add(rule.name);
                    if (score < rule.min) {
                        res.getFeedbackMessages().add(choice(rule.feedbackMessagesLow));
                    } else {
                        res.getFeedbackMessages().add(choice(rule.feedbackMessagesHigh));
                    }
                    res.getFeedbackIndexParagraph().add(i);
                    res.getFeedbackIndexPhrase().add(-1);
                    res.getFeedbackType().add(EFResult.TYPE_PARAGRAPH);
                    res.getFeedbackSeverityLevel().add(rule.getSeverityLevel(score));
                } else {
                    res.getComponents().add(rule.name);
                    res.getFeedbackMessages().add("No problems found.");
                    res.getFeedbackIndexParagraph().add(i);
                    res.getFeedbackIndexPhrase().add(-1);
                    res.getFeedbackType().add(EFResult.TYPE_PARAGRAPH);
                    res.getFeedbackSeverityLevel().add(rule.getCorrectnessLevel(score));
                }
            }

            for (int j = 0; j < block.getSentences().size(); ++j) {
                Sentence sentence = block.getSentences().get(j);
                blockText.add(sentence.getText());
                template = AbstractDocumentTemplate.getDocumentModel(sentence.getText());
                AbstractDocument dSentence = new Document(null, template, models, lang, true);
                for (Object ruleObject : phraseRules) {
                    Rule rule = new Rule((JSONObject) ruleObject);
                    double score = getSentenceScore(dSentence, rule.id);
                    if (score < rule.min || score > rule.max) {
                        res.getComponents().add(rule.name);
                        if (score < rule.min) {
                            res.getFeedbackMessages().add(choice(rule.feedbackMessagesLow));
                        } else {
                            res.getFeedbackMessages().add(choice(rule.feedbackMessagesHigh));
                        }
                        res.getFeedbackIndexParagraph().add(i);
                        res.getFeedbackIndexPhrase().add(j);
                        res.getFeedbackType().add(EFResult.TYPE_SENTENCE);
                        res.getFeedbackSeverityLevel().add(rule.getSeverityLevel(score));
                    } else {
                        res.getComponents().add(rule.name);
                        res.getFeedbackMessages().add("No problems found.");
                        res.getFeedbackIndexParagraph().add(i);
                        res.getFeedbackIndexPhrase().add(j);
                        res.getFeedbackType().add(EFResult.TYPE_SENTENCE);
                        res.getFeedbackSeverityLevel().add(rule.getCorrectnessLevel(score));
                    }
                }
            }
        }

        return res;
    }

    private double getSentenceScore(AbstractDocument dSentence, String id) {
        if (id.equals("Words")) {
            return dSentence.getNoWords();
        }
        if (id.equals("Content words")) {
            return dSentence.getNoContentWords();
        }
        if (id.equals("AoA")) {
            return getAverageAoA(dSentence);
        }
        for (ComplexityIndex ci : ComplexityIndices.getIndices(lang)) {
            if (ci.getAcronym().equals(id)) {
                return ci.compute(dSentence);
            }
        }

        return Double.NaN;
    }

    private double getAverageAoA(AbstractDocument dSentence) {
        double s = 0;
        int count = 0;
        List<Word> wordsInSentence = dSentence.getBlocks().get(0).getSentences().get(0).getAllWords();
        if (wordsInSentence.size() == 0) return 0;
        for (Word w: wordsInSentence) {
            String word = w.getLemma();
            if (aoaDictionary.containsKey(word)) {
                s += aoaDictionary.get(word);
                count += 1;
            }
        }

        return count != 0 ? s / count : 0;
    }

    private double getParagraphScore(AbstractDocument dBlock, String id) {
        if (id.equals("Sentences")) {
            return dBlock.getSentencesInDocument().size();
        }
        if (id.equals("Words")) {
            return dBlock.getNoWords();
        }
        if (id.equals("Content words")) {
            return dBlock.getNoContentWords();
        }
        for (ComplexityIndex ci : ComplexityIndices.getIndices(lang)) {
            if (ci.getAcronym().equals(id)) {
                return ci.compute(dBlock);
            }
        }

        return Double.NaN;
    }
}
