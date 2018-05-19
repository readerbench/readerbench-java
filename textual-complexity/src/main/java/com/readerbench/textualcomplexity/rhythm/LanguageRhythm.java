/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.textualcomplexity.rhythm;

import com.readerbench.coreservices.data.cscl.Conversation;
import com.readerbench.coreservices.data.cscl.Participant;
import com.readerbench.coreservices.data.cscl.Utterance;
import com.readerbench.coreservices.data.AbstractDocument;
import com.readerbench.coreservices.data.Block;
import com.readerbench.coreservices.data.Sentence;
import com.readerbench.coreservices.data.Word;
import com.readerbench.coreservices.nlp.wordlists.SyllabifiedDictionary;
import com.readerbench.textualcomplexity.ComplexityIndex;
import com.readerbench.coreservices.rhythm.RhythmTool;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author valentin.sergiu.cioaca@gmail.com
 */

public class LanguageRhythm extends ComplexityIndex {

    protected static SyllabifiedDictionary syllCMUDict_en = null;

    public LanguageRhythm() {
        super(null);
    }

    @Override
    public double compute(AbstractDocument d) {
        List<Integer> rhythmicIndices = new ArrayList<>();
        int infRhythmicLimit, supRhythmicLimit, rhythmicDiameter;
        
        infRhythmicLimit = Integer.MAX_VALUE;
        rhythmicDiameter = supRhythmicLimit = 0;
        for (Block b : d.getBlocks()) {
            if (null == b) {
                continue;
            }
            for (Sentence s : b.getSentences()) {
                List<Word> unit = s.getAllWords();
                List<Integer> rhythmicStructure = RhythmTool.getRhythmicStructureSM(unit);
                if (rhythmicStructure.isEmpty()) {
                    continue;
                }
                int min = Collections.min(rhythmicStructure);
                int max = Collections.max(rhythmicStructure);
                infRhythmicLimit = Math.min(infRhythmicLimit, min);
                supRhythmicLimit = Math.max(supRhythmicLimit, max);
                rhythmicDiameter = Math.max(rhythmicDiameter, max - min);
                int unitLength = unit.size();
                int rhythmicLength = rhythmicStructure.size();
                int unitRhythmicIndex = RhythmTool.calcRhythmicIndexSM(unitLength, rhythmicLength);
                rhythmicIndices.add(unitRhythmicIndex);
            }
        }
        Map<Integer, Long> counts = rhythmicIndices.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        System.out.println(counts);
        System.out.println("The rhythmic lower edge of language: " + infRhythmicLimit);
        System.out.println("The rhythmic upper edge of language: " + supRhythmicLimit);
        System.out.println("The rhythmic diameter of language: " + rhythmicDiameter);
        return Collections.max(rhythmicIndices);
    }
    
    public static void findMissingWords(AbstractDocument d) {
        for (Block b : d.getBlocks()) {
            if (null == b) {
                continue;
            }
            for (Sentence s : b.getSentences()) {
                RhythmTool.getRhythmicStructureSM(s.getAllWords());
            }
        }
    }
    
    public void testNewMethod(AbstractDocument d) {
        Map<String, Integer> phonemesFrequency = new TreeMap<>();
        Map<Integer, Integer> cntSyllables = new TreeMap<>();
        int deviations = 0;
        
        for (Block b : d.getBlocks()) {
            if (null == b) continue;
            for (Sentence s : b.getSentences()) {
                System.out.println(s);
                List<Word> unit = s.getAllWords();
                String SN = RhythmTool.getNumericalSystem(unit, phonemesFrequency);
                deviations += RhythmTool.calcPossibleDeviations(SN);
                int NT = (SN.charAt(0) == '!') ? SN.length()-1 : SN.length();
                int NA = RhythmTool.getArithmeticNumber(SN, cntSyllables);
                System.out.println("Sistemul numeric: " + SN);
                System.out.println("Numar tonic: " + NT);
                System.out.println("Numar aritmetic: " + NA);
//               break;
            }
        }
        
        
        System.out.println("Phonemes frequency: ");
        for (Map.Entry<String, Integer> entry : phonemesFrequency.entrySet()) {
            System.out.println(entry.getKey() + "\t" + entry.getValue());
        }
        System.out.println();
        int totalNumber = cntSyllables.values().stream().reduce(0, Integer::sum);
        System.out.println("Syllabic frequencies");
        DecimalFormat df = new DecimalFormat("#.##");
        double maxFreq = 0.0;
        int maxKey = 0;
        for (Map.Entry<Integer, Integer> entry : cntSyllables.entrySet()) {
            double syllFreq = 1.0 * entry.getValue() / totalNumber;
            if (syllFreq > maxFreq) {
                maxFreq = syllFreq;
                maxKey = entry.getKey();
            }
            System.out.println(entry.getKey() + "\t" + totalNumber +
                                                "\t" + entry.getValue() + 
                                                "\t" + df.format(syllFreq));
        }
        System.out.println();
        System.out.println("Deviations: " + deviations);
        System.out.println("Keys: " + cntSyllables.get(maxKey-1) + " "
                                    + cntSyllables.get(maxKey) + " "
                                    + cntSyllables.get(maxKey + 1));
    }

    public void evaluateParticipantsRhythmicity(Conversation chat) {
        Map<Participant, List<Sentence>> interventionsPerParticipant = new HashMap<>();
        if (chat.getParticipants().size() > 0) {
            for (Block b : chat.getBlocks()) {
                if (null != b) {
                    Participant p = ((Utterance) b).getParticipant();
                    if (!interventionsPerParticipant.containsKey(p)) {
                        interventionsPerParticipant.put(p, new ArrayList<>());
                    }
                    interventionsPerParticipant.get(p).addAll(b.getSentences());
                }
            }
        }

        // calculate rhythmic structures and indices
        Map<Participant, Map<Integer, Long>> rhythmicIndicesPerParticipant = new TreeMap<>();
        for (Map.Entry<Participant, List<Sentence>> entry : interventionsPerParticipant.entrySet()) {
            List<Integer> rhythmicIndices = new ArrayList<>(); 
            for (Sentence s : entry.getValue()) {
                List<Word> unit = s.getAllWords();
                List<Integer> rhythmicStructure = RhythmTool.getRhythmicStructureSM(unit);
                if (rhythmicStructure.isEmpty()) {
                    continue;
                }
                int unitLength = unit.size();
                int rhythmicLength = rhythmicStructure.size();
                int unitRhythmicIndex = RhythmTool.calcRhythmicIndexSM(unitLength, rhythmicLength);
                rhythmicIndices.add(unitRhythmicIndex);
            }
            rhythmicIndicesPerParticipant.put(entry.getKey(), 
                rhythmicIndices.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting())));
        }
        
        System.out.println(rhythmicIndicesPerParticipant);
//        List<AbstractDocument> participantInterventions = new ArrayList<>();
//        for (Participant part : chat.getParticipants()) {
//            participantInterventions.add(part.getSignificantContributions());
////            System.out.println(part.getName());
////            System.out.println(part.getSignificantContributions());
//        }
//        System.out.println("SIZE SIZE: " + participantInterventions.size());
//        for (AbstractDocument d : participantInterventions) {
//            System.out.println(d.getBlocks());
//        }
    }
}

//        Map<Participant, List<Sentence>> contributionsPerParticipant = new HashMap<>();
//        for (Block b : d.getBlocks()) {
//            Participant p = ((Utterance) b).getParticipant();
//            if (!contributionsPerParticipant.containsKey(p)) {
//                contributionsPerParticipant.put(p, new ArrayList<>());
//            }
//            contributionsPerParticipant.get(p).addAll(b.getSentences());
//        }
//        SyllabifiedCMUDict syllabifiedCMUDict = new SyllabifiedCMUDict("resources/config/EN/word lists/syllabified_cmudict.txt");
//        int superiorRhythmicLimit, inferiorRhythmicLimit, rhythmicLanguageDiameter; 
//        Map<Integer, Integer> chatRhythmicIndices = new HashMap<>();
//        Map<Participant, Map<Integer, Integer>> rhythmicIndicesPerParticipant = new TreeMap<>();
//        for (Map.Entry<Participant, List<Sentence>> entry : contributionsPerParticipant.entrySet()) {
////            System.out.println("PARTICIPANT: " + entry.getKey().getName());
//            Map<Integer, Integer> indicesCount = new TreeMap<>();
//            for (Sentence s : entry.getValue()) {
////                System.out.println(s);
//                int rhythmicIndex = Rhythm.calculateRhythmIndexSM(s.getAllWords(), syllabifiedCMUDict);
//                Integer count = indicesCount.get(rhythmicIndex);          
//                indicesCount.put(rhythmicIndex, (null==count) ? 1 : count+1);
////                System.out.println("Rhythm index: " + rhythmicIndex);
//            }
//            rhythmicIndicesPerParticipant.put(entry.getKey(), indicesCount);
//            indicesCount.entrySet().forEach((entryRhythm) -> {
//                Integer count = chatRhythmicIndices.get(entryRhythm.getKey());
//                chatRhythmicIndices.put(entryRhythm.getKey(), (null==count) ? entryRhythm.getValue() : count + entryRhythm.getValue());
//            });
////            chatRhythmicIndices.forEach((k, v) -> indicesCount.merge(k, v, (v1, v2) -> v1 + v2));
//        }
//        
//        System.out.println(chatRhythmicIndices);
//        System.out.println(rhythmicIndicesPerParticipant);

