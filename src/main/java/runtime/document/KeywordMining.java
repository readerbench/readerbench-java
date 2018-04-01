/*
 * Copyright 2016 ReaderBench.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package runtime.document;

import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.Lang;
import data.NGram;
import data.Word;
import data.discourse.Keyword;
import data.document.Document;
import data.document.MetaDocument;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import services.converters.Txt2XmlConverter;
import services.discourse.keywordMining.KeywordModeling;
import services.semanticModels.ISemanticModel;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import services.semanticModels.word2vec.Word2VecModel;
import webService.ReaderBenchServer;

/**
 *
 * @author Mihai Dascalu
 */
public class KeywordMining {

    static final Logger LOGGER = Logger.getLogger("");

    private final String processingPath;
    private final int noTopKeyWords;
    private final List<ISemanticModel> models;
    private final Lang lang;
    private final boolean usePOSTagging;
    private final boolean computeDialogism;
    private final boolean meta;

    public KeywordMining(String processingPath, int noTopKeyWords, List<ISemanticModel> models, Lang lang, boolean usePOSTagging, boolean computeDialogism, boolean meta) {
        this.processingPath = processingPath;
        this.noTopKeyWords = noTopKeyWords;
        this.models = models;
        this.lang = lang;
        this.usePOSTagging = usePOSTagging;
        this.computeDialogism = computeDialogism;
        this.meta = meta;
    }

    public Set<Keyword> getTopKeywords(List<Document> documents, int noTopKeyWords) {
        Set<Keyword> keywords = new TreeSet<>();

        for (Document d : documents) {
            List<Keyword> topics = KeywordModeling.getSublist(d.getTopics(), noTopKeyWords, false, false);
            for (Keyword t : topics) {
                keywords.add(t);
            }
        }
        return keywords;
    }

    public Map<Word, Double> getRelevance(Document d, Set<Word> keywords) {
        Map<Word, Double> keywordOccurrences = new TreeMap<>();

        List<Keyword> topics = d.getTopics();
        for (int i = 0; i < topics.size(); i++) {
            for (Word keyword : keywords) {
                //determine identical stem
                if (keyword.getLemma().equals(topics.get(i).getWord().getLemma())) {
                    keywordOccurrences.put(topics.get(i).getWord(), topics.get(i).getRelevance());
                }
            }
        }
        return keywordOccurrences;
    }

    public Map<Word, Integer> getIndex(Document d, Set<Word> keywords) {
        Map<Word, Integer> keywordOccurrences = new TreeMap<>();

        List<Keyword> topics = d.getTopics();
        for (int i = 0; i < topics.size(); i++) {
            for (Word keyword : keywords) {
                //determine identical stem
                if (keyword.getLemma().equals(topics.get(i).getWord().getLemma())) {
                    keywordOccurrences.put(topics.get(i).getWord(), i);
                }
            }
        }
        return keywordOccurrences;
    }

    public void saveSerializedFromXml() {
        File dir = new File(processingPath);

        if (!dir.exists()) {
            throw new RuntimeException("Inexistent Folder: " + dir.getPath());
        }

        File[] files = dir.listFiles((File pathname) -> {
            return pathname.getName().toLowerCase().endsWith(".xml");
        });

        for (File file : files) {
            File f = new File(file.getPath().replace(".xml", ".ser"));
            if (f.exists() && !f.isDirectory()) {
                continue;
            }
            LOGGER.log(Level.INFO, "Processing {0} file", file.getName());
            // Create file

            Document d;
            try {
                if (meta) {
                    d = MetaDocument.load(file, models, lang, usePOSTagging, MetaDocument.DocumentLevel.Subsection, 5);
                } else {
                    d = Document.load(file, models, lang, usePOSTagging);
                }
                d.save(AbstractDocument.SaveType.SERIALIZED);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Runtime error while processing {0}: {1} ...", new Object[]{file.getName(), e.getMessage()});
                Exceptions.printStackTrace(e);
            }
        }
    }

    public void generateKeywordsSers(List<String> acceptedKeywords, List<String> acceptedBigrams) {
        File dir = new File(processingPath);

        if (!dir.exists()) {
            throw new RuntimeException("Inexistent Folder: " + dir.getPath());
        }

        File[] files = dir.listFiles((File pathname) -> {
            return pathname.getName().toLowerCase().endsWith(".ser");
        });

        for (File file : files) {
            LOGGER.log(Level.INFO, "Processing {0} file", file.getName());
            Document d = null;
            try {
                d = (Document) AbstractDocument.loadSerializedDocument(file.getPath());

                KeywordModeling.determineKeywords(d);
                List<Keyword> keywords = d.getTopics();
                StringBuilder sbKeywords = new StringBuilder();
                StringBuilder sbKeywordsBigrams = new StringBuilder();
                StringBuilder sbKeywordsAccepted = new StringBuilder();
                StringBuilder sbKeywordsBigramsAccepted = new StringBuilder();
                StringBuilder sbKeywordsAcceptedAndBigrams = new StringBuilder();
                for (Keyword keyword : keywords) {
                    if (keyword.getElement() instanceof Word) {
                        sbKeywordsBigrams.append(keyword.getWord().getLemma()).append(" ");
                        sbKeywords.append(keyword.getWord().getLemma()).append(" ");
                        if (acceptedKeywords.contains(keyword.getWord().getLemma())) {
                            sbKeywordsBigramsAccepted.append(keyword.getWord().getLemma()).append(" ");
                            sbKeywordsAccepted.append(keyword.getWord().getLemma()).append(" ");
                            sbKeywordsAcceptedAndBigrams.append(keyword.getWord().getLemma()).append(" ");
                        }
                    } else {
                        sbKeywordsBigrams.append(keyword.getElement().toString()).append(" ");
                        sbKeywordsAcceptedAndBigrams.append(keyword.getElement().toString()).append(" ");
                        if (acceptedBigrams.contains(keyword.getElement().toString())) {
                            sbKeywordsBigramsAccepted.append(keyword.getElement().toString()).append(" ");
                        }
                    }
                }
                BufferedWriter bw = null;
                FileWriter fw = null;
                
                fw = new FileWriter(file.getPath().replace(".ser", "_keywords.txt"));
                bw = new BufferedWriter(fw);
                bw.write(sbKeywords.toString());
                sbKeywords.setLength(0);
                bw.close();
                fw.close();
                
                fw = new FileWriter(file.getPath().replace(".ser", "_keywords_bigrams.txt"));
                bw = new BufferedWriter(fw);
                bw.write(sbKeywordsBigrams.toString());
                sbKeywordsBigrams.setLength(0);
                bw.close();
                fw.close();
                
                fw = new FileWriter(file.getPath().replace(".ser", "_keywords_accepted.txt"));
                bw = new BufferedWriter(fw);
                bw.write(sbKeywordsAccepted.toString());
                sbKeywordsAccepted.setLength(0);
                bw.close();
                fw.close();
                
                fw = new FileWriter(file.getPath().replace(".ser", "_keywords_bigrams_accepted.txt"));
                bw = new BufferedWriter(fw);
                bw.write(sbKeywordsBigramsAccepted.toString());
                sbKeywordsBigramsAccepted.setLength(0);
                bw.close();
                fw.close();
                
                fw = new FileWriter(file.getPath().replace(".ser", "_keywords_accepted_and_bigrams.txt"));
                bw = new BufferedWriter(fw);
                bw.write(sbKeywordsAcceptedAndBigrams.toString());
                sbKeywordsAcceptedAndBigrams.setLength(0);
                bw.close();
                fw.close();
                
                /*AbstractDocumentTemplate templateKeywords = AbstractDocumentTemplate.getDocumentModel(sb.toString());
                AbstractDocument documentKeywords = new Document(file.getPath().replace(".ser", ".keywords.ser"), templateKeywords, models, lang, usePOSTagging);
                documentKeywords.save(AbstractDocument.SaveType.SERIALIZED);*/
            } catch (IOException | ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public void processTexts(boolean useSerialized, String outputFileName) {
        File dir = new File(processingPath);

        if (!dir.exists()) {
            throw new RuntimeException("Inexistent Folder: " + dir.getPath());
        }

        List<Document> documents = new ArrayList<>();

        if (useSerialized) {
            File[] files = dir.listFiles((File pathname) -> {
                return pathname.getName().toLowerCase().endsWith(".ser");
            });

            for (File file : files) {
                Document d = null;
                try {
                    d = (Document) AbstractDocument.loadSerializedDocument(file.getPath());
                    documents.add(d);
                    d.exportDocument();
                } catch (IOException | ClassNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        } else {
            File[] files = dir.listFiles((File pathname) -> {
                return pathname.getName().toLowerCase().endsWith(".xml");
            });

            for (File file : files) {
                LOGGER.log(Level.INFO, "Processing {0} file", file.getName());
                // Create file

                Document d;
                try {
                    if (meta) {
                        d = MetaDocument.load(file, models, lang, usePOSTagging, MetaDocument.DocumentLevel.Subsection, 5);
                    } else {
                        d = Document.load(file, models, lang, usePOSTagging);
                    }
                    //d.computeAll(computeDialogism);
                    //d.save(AbstractDocument.SaveType.NONE);
                    KeywordModeling.determineKeywords(d);
                    documents.add(d);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Runtime error while processing {0}: {1} ...", new Object[]{file.getName(), e.getMessage()});
                    Exceptions.printStackTrace(e);
                }
            }
        }

        //determing joint keywords
        Set<Keyword> keywords = getTopKeywords(documents, noTopKeyWords);

        try (BufferedWriter outRelevance = new BufferedWriter(new FileWriter(processingPath + "/" + outputFileName, true))) {
            StringBuilder csv = new StringBuilder("SEP=;\ntype;keyword;lemma;pos;relevance\n");
            for (Keyword keyword : keywords) {
                if (keyword.getElement() instanceof Word) {
                    csv.append("word;").append(keyword.getWord().getText()).append(";");
                } else if (keyword.getElement() instanceof NGram) {
                    NGram nGram = (NGram) keyword.getElement();
                    csv.append("ngram;");
                    for (Word w : nGram.getWords()) {
                        csv.append(w.getText()).append(" ");
                    }
                    csv.append(";");
                }
                csv.append(keyword.getWord().getLemma()).append(";");
                if (keyword.getElement() instanceof Word) {
                    csv.append(keyword.getWord().getPOS());
                } else {
                    NGram nGram = (NGram) keyword.getElement();
                    StringBuilder sb = new StringBuilder();
                    for (Word word : nGram.getWords()) {
                        sb.append(word.getPOS()).append("_");
                    }
                    String nGramLemmas = sb.toString();
                    sb.setLength(0);
                    csv.append(nGramLemmas.substring(0, nGramLemmas.length() - 1));
                }
                csv.append(";").append(keyword.getRelevance());
                outRelevance.write(csv.toString());
                outRelevance.newLine();
                outRelevance.flush();
                csv.setLength(0);
            }
            outRelevance.close();
        } catch (IOException ex) {
            LOGGER.severe("Runtime error while analyzing selected folder ...");
            Exceptions.printStackTrace(ex);
        }
    }

    public static void main(String[] args) {

        ReaderBenchServer.initializeDB();

        // Txt2XmlConverter.parseTxtFiles("", "resources/in/SciCorefCorpus/fulltexts", Lang.en, "UTF-8");
        LSA lsa = LSA.loadLSA("resources/config/EN/LSA/SciRef", Lang.en);
//        LDA lda = LDA.loadLDA("resources/config/EN/LDA/TASA", Lang.en);
        List<ISemanticModel> models = new ArrayList<>();
        models.add(lsa);
//        models.add(lda);
//
//        
        KeywordMining keywordMining = new KeywordMining("resources/in/SciCorefCorpus/fulltexts", 0, models, Lang.en, true, false, false);
        //keywordMining.saveSerializedFromXml();

        String keywordsString = "supply, reserve, clean, hybrid, clickthrough, miss, browse, regard, insert, multicast, shift, relax, overload, stream, quantify, delete, primitive, facilitate, degrade, leverage, virtual, conflict, attack, click, middleware, finish, appeal, secure, multiagent, preserve, forward, unify, substitute, ignore, stop, subject, switch, embed, keyword, wireless, span, academic, remark, sound, randomise, testbed, transmit, hash, waste, judge, decentralize, cache, break, fall, digital, dataset, queue, reverse, encounter, fine, shape, electronic, reward, project, collaborative, spend, american, sensor, impose, operate, combinatorial, bear, advertise, remote, hide, discount, trigger, economic, boolean, client, deep, augment, square, encourage, copy, adopt, complicate, double, correlate, analyse, automate, face, concentrate, contract, generalize, tune, engineer, retrieve, artificial, incentive, spread, enhance, intelligent, display, disjoint, default, guide, host, benchmark, delay, carry, favor, bias, representative, mobile, physical, retrieval, chain, encode, scalable, risk, refine, plot, equilibrium, block, national, survey, communicate, mix, trust, buy, middle, promise, cooperative, evidence, force, slow, briefly, peer, auction, verify, gather, discrete, concern, novel, center, outline, split, hope, comment, route, normalise, empty, neighbor, polynomial, surprise, monitor, coordinate, reflect, centralize, strategic, autonomous, simplify, intuitively, sample, flow, year, overhead, schedule, half, advance, max, statistical, characteristic, motivate, personal, score, filter, root, drive, sell, cluster, integrate, experience, probabilistic, transfer, overlap, wish, parallel, particularly, intend, sort, execute, speed, semantic, modify, adaptive, bring, abstract, exhibit, claim, wait, meet, exchange, demand, care, clearly, optimize, simulate, adjust, attribute, increment, extract, arise, simultaneously, realistic, balance, popular, seem, essentially, namely, player, consideration, record, fundamental, keep, presence, attention, social, potentially, public, past, complement, initially, submit, possibility, aggregate, internet, discover, notice, straightforward, theoretical, perspective, effectively, perfect, variant, pass, think, grow, exponential, live, obvious, conduct, bid, upper, distinguish, basis, specifically, factor, human, review, suppose, extreme, challenge, proceed, automatic, drop, robust, characterize, market, post, empirical, incorporate, interval, logical, arbitrary, request, author, label, document, answer, practical, early, minimize, possibly, distinct, quite, continue, message, output, completely, topic, target, recently, influence, literature, protocol, previously, online, objective, manner, underlie, nature, little, press, fast, connect, whole, kind, store, slightly, rank, meaning, relatively, efficiently, reveal, detect, free, understand, explain, establish, investigate, generally, content, theorem, explicitly, examine, number, load, reasonable, draw, especially, handle, highly, practice, check, explore, treat, procedure, impact, minimum, course, uniform, threshold, repeat, desire, equivalent, lack, calculate, technical, necessarily, yield, discussion, exploit, proof, accurate, relationship, bound, recall, enable, locate, proceeding, capture, international, setting, track, write, aspect, employ, game, major, index, typically, central, believe, currently, manage, similarly, access, accept, purpose, mention, comparison, vary, price, guarantee, sense, service, entire, exactly, account, requirement, separate, rule, illustrate, trade, file, theory, fail, combine, actually, layer, link"; // add keywords string here
        List<String> keywords = Arrays.asList(keywordsString.split("\\s*,\\s*"));

        String bigramsString = "result_use, use_set, use_model, use_use, use_result, use_measure, make_use, show_result, use_search, use_system, use_query, model_use, set_query, result_rank, use_rank, use_information, use_estimate, use_compute, use_function, set_use, result_search, use_approach, set_result, result_show, use_technique, use_control, study_use, allow_use, set_set, work_use, present_result, use_structure, approach_use, result_query, note_use, use_agent, result_set, use_algorithm, use_monitor, use_test, show_use, improve_use, show_figure, design_use, use_datum, use_study, use_method, use_construct, query_set, propose_use, use_metric, system_use, use_determine, use_increase, construct_use, use_feature, set_agent, use_mechanism, require_use, result_present, use_show, support_use, use_knowledge, use_work, use_sample, provide_use, use_syntax, use_identify, use_number, set_value, use_currently, obtain_use, term_query, use_update, use_analysis, use_strategy, use_design, measure_use, use_fact, use_extract, benefit_use, use_cluster, use_framework, set_datum, result_model, use_learn, describe_use, use_time, use_case, set_test, compute_use, suggest_use, consider_use, use_criteria, use_tool, use_combination, use_protocol, result_click, agent_autonomous, use_make, rank_use, use_filter, explore_use, use_reason, use_form, show_work, use_network, value_good, result_result, use_operator, relate_use, model_agent, use_source, use_capture, use_resource, use_statistic, model_result, use_mean, use_track, give_use, use_order, use_scale, need_information, set_base, use_numb, algorithm_use, system_base, use_dataset, implement_use, lead_use, show_example, use_transfer, yield_result, use_evaluate, use_widely, use_procedure, use_process, effect_use, use_user, use_balance, use_help, set_function, evaluate_use, use_application, use_provide, report_result, use_perform, enable_use, use_engine, need_model, use_equation, use_architecture, rank_result, model_query, show_table, document_rank, use_auction, use_type, use_cache, use_solution, use_definition, investigate_use, use_build, use_rule, provide_result, use_code, design_system, introduce_use, use_purpose, search_use, use_theory, use_retrieval, use_schedule, need_use, mean_use, use_train, give_result, reduce_use, train_set, use_check, model_model, use_environment, use_encode, use_term, process_query, set_state, ease_use, system_network, information_use, achieve_use, use_turn, use_scheme, use_title, use_call, use_improve, demonstrate_use, use_focus, click_result, use_problem, technique_use, represent_use, system_multiagent, form_use, function_value, solve_use, ensure_use, approach_model, use_route, set_bid, use_collection, use_compare, use_technology, time_run, set_node, use_prior, query_result, use_device, use_obtain, use_reduce, use_research, use_example, use_support, estimate_use, result_obtain, use_context, use_document, use_calculate, perform_good, measure_time, information_share, set_order, set_feature, use_corpus, show_value, use_hash, use_paper, return_result, use_implement, bid_agent, set_train, set_satisfy, set_variable, set_work, use_space, use_irf, use_range, discuss_use, use_section, system_set, yield_use, use_define, use_address, good_use, use_program, approximate_use, use_concept, use_evaluation, use_score, use_deal, focus_use, use_store, example_use, set_experiment, use_benchmark, time_process, query_search, set_give, set_process, use_summary, query_use, use_avoid, use_experiment, support_system, use_place, problem_set, model_system, use_representation, result_simulation, use_answer, increase_numb, use_allow, numb_increase, use_variant, use_testbed, depend_use, model_problem, use_view, advantage_use, use_change, set_search, model_user, use_due, value_agent, problem_match, present_model, use_base, use_detect, test_set, agent_system, refer_use, use_communication, build_use, combine_use, test_use, value_set, rank_document, use_component, process_information, compare_use, use_create, result_return, algorithm_rank, system_compute, set_parameter, increase_time, use_tune, use_match, use_need, result_experiment, datum_set, show_time, attempt_use, use_formulation, use_handle, use_task, use_property, set_term, use_distribution, set_correspond, use_point, train_use, algorithm_base, use_service, illustrate_use, work_system, use_factor, use_format, result_system, use_instance, update_set, use_formalism, use_way, system_rank, see_use, use_instead, use_value, indicate_result, result_base, result_page, set_system, use_name, auction_use, use_list, good_result, model_set, use_mode, model_network, use_formula, mechanism_use, consider_set, use_ensure, case_use, show_set, use_version, use_judgment, implement_function, approach_base, use_vector, result_case, learn_query, function_use, method_use, possible_use, use_index, set_good, share_information, base_model, involve_use, result_section, use_node, choose_use, use_language, use_image, obtain_result, example_show, address_problem, leverage_use, make_agent, use_attempt, numb_maximum, use_commonly, report_agent, assume_use, feature_use, time_start, support_query, present_use, need_user, bid_value, fit_use, use_implementation, condition_use, set_document, provide_set, use_simulate, need_set, use_feedback, result_figure, case_good, call_set, use_map, change_agent, use_comparison, use_abstraction, use_level, understand_good, affect_use, process_time, compare_result, use_smooth, solution_use, assume_set, use_gossip, perform_use, function_base, optimize_use, use_database, result_study, use_run, use_event, see_result, use_policy, metric_use, use_bodyweight, extend_result, use_observation, use_classifier, learn_use, way_use, use_good, set_size, experiment_use, result_performance, numb_agent, repeat_use, present_time, examine_use, value_compute, result_good, issue_use, algorithm_learn, issue_query, measure_performance, use_mediator, use_average, set_strategy, cover_set, give_set, set_information, include_use, answer_query, use_assumption, change_time, use_consider, use_quantify, order_result, limit_numb, result_method, use_represent, show_section, use_file, result_numb, model_rank, increase_use, note_set, set_model, use_scenario, result_approach, model_game, numb_fix, model_share, define_set, focus_paper, set_time, show_model, agent_advertise, present_user, reason_use, use_count, use_simulation, design_mechanism, improve_result, value_aggregate, work_future, use_machine, propose_model, set_edge, use_channel, use_baseline, process_set, match_query, query_value, use_denote, base_case, base_result, step_time, set_price, use_phrase, set_contain, learn_rank, use_computation, use_label, price_good, use_simplicity, exchange_information, able_use, set_procurement, set_run, system_search, avoid_use, present_section, use_notation, use_pagerank, update_use, describe_result, use_access, strategy_use, system_support, interest_user, use_limit, set_problem, use_pattern, model_form, address_use, apply_use, time_record, base_agent, use_pass, use_log, time_use, increase_set, time_average, relate_query, network_wireless, work_relate, use_history, use_sort, order_set, use_key, use_identifier, provide_support, use_decide, use_weight, size_set, equal_numb, provide_model, function_rank, agent_set, result_agent, simulate_use";
        List<String> bigrams = Arrays.asList(bigramsString.split("\\s*,\\s*"));

        keywordMining.generateKeywordsSers(keywords, bigrams);
//        keywordMining.processTexts(false, "keywords_lsa_lda_tasa_local.csv");
    }
}
