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
package runtime.converters;

import data.Lang;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import services.nlp.lemmatizer.StaticLemmatizer;


public class DutchDecomposition {

    private static HashSet<String> dictionary = null;
    private static HashMap<String, Composition> result = null;
    private static HashMap<Integer, HashSet<String>> evidence = null;

    private static class Composition {

        public boolean isComposed = false;
        public String rule = "";
        //public int componentsNumber = 0;
    }

    public static void computeDecomposition(String word) {
        //Prefixe
        if (word.length() > 4) {
            String first4 = word.substring(0, 4 <= word.length() ? 4 : word.length());
            String last4 = word.substring(4 <= word.length() ? 4 : 0);
            if (first4.equals("anti") && dictionary.contains(last4)) {
                result.get(word).isComposed = true;
                result.get(word).rule = StaticLemmatizer.lemmaStatic(first4, Lang.nl) + "+" + StaticLemmatizer.lemmaStatic(last4, Lang.nl);
                return;//continue;
            }
        }

        if (word.length() > 3) {
            String first3 = word.substring(0, 3 <= word.length() ? 3 : word.length());
            String last3 = word.substring(3 <= word.length() ? 3 : 0);
            if ((first3.equals("her") && dictionary.contains(last3))
                    || (first3.equals("ont") && dictionary.contains(last3))
                    || (first3.equals("ver") && dictionary.contains(last3))
                    || (first3.equals("des") && dictionary.contains(last3))) {
                result.get(word).isComposed = true;
                result.get(word).rule = StaticLemmatizer.lemmaStatic(first3, Lang.nl) + "+" + StaticLemmatizer.lemmaStatic(last3, Lang.nl);
                return;//continue;
            }
        }

        if (word.length() > 2) {
            String first2 = word.substring(0, 2 <= word.length() ? 2 : word.length());
            String last2 = word.substring(2 <= word.length() ? 2 : 0);
            if ((first2.equals("on") || first2.equals("de")) && dictionary.contains(last2)) {
                result.get(word).isComposed = true;
                result.get(word).rule = StaticLemmatizer.lemmaStatic(first2, Lang.nl) + "+" + StaticLemmatizer.lemmaStatic(last2, Lang.nl);
                return;//continue;
            }
        }

        //Terminatii
        if (word.charAt(word.length() - 1) == 's' && dictionary.contains(word.substring(0, word.length() - 1))) {
            result.get(word).isComposed = true;
            result.get(word).rule = StaticLemmatizer.lemmaStatic(word.substring(0, word.length() - 1), Lang.nl) ;//+ "+s";
            return;//continue;
        }

        if (word.length() > 2 && word.substring(word.length() - 2, word.length()).equals("en") && !word.substring(0, word.length() - 2).equals("") && dictionary.contains(word.substring(0, word.length() - 2))) {
            result.get(word).isComposed = true;
            result.get(word).rule = StaticLemmatizer.lemmaStatic(word.substring(0, word.length() - 2), Lang.nl) ;//+ "+en";
            return;//continue;
        }

        if (word.length() > 3) {
            String first = word.substring(0, word.length() - 3);
            String last3 = word.substring(word.length() - 3);
            if (dictionary.contains(first) && last3.equals("ing")) {
                result.get(word).isComposed = true;
                result.get(word).rule = StaticLemmatizer.lemmaStatic(first, Lang.nl) + "+" + StaticLemmatizer.lemmaStatic(last3, Lang.nl);
                return;
            }
        }

        if (word.length() > 4) {
            String first = word.substring(0, word.length() - 4);
            String last4 = word.substring(word.length() - 4);
            if (dictionary.contains(first) && (last4.equals("baar") || last4.equals("heid"))) {
                result.get(word).isComposed = true;
                result.get(word).rule = StaticLemmatizer.lemmaStatic(first, Lang.nl) + "+" + StaticLemmatizer.lemmaStatic(last4, Lang.nl);
                return;
            }
        }

        //Terminatii de verbe
        //Present
        if (word.length() > 3) {
            String stem = word.substring(0, word.length() - 1);
            char end = word.charAt(word.length() - 1);
            if (end == 't' && dictionary.contains(stem + "en") && !stem.equals("")) {
                result.get(word).isComposed = true;
                result.get(word).rule = StaticLemmatizer.lemmaStatic(stem, Lang.nl) + "+" + StaticLemmatizer.lemmaStatic(end+"", Lang.nl);
                return;//continue;
            }
        }

        //Simple past
        HashSet<Character> tVerbs = new HashSet<Character>();
        tVerbs.add('t');
        tVerbs.add('h');
        tVerbs.add('f');
        tVerbs.add('c');
        tVerbs.add('k');
        tVerbs.add('s');
        tVerbs.add('p');
        if (word.length() > 2) {
            String stem = word.substring(0, word.length() - 2);
            String end = word.substring(word.length() - 2, word.length());
            if ((end.equals("te") || end.equals("de")) && dictionary.contains(stem + "en") && !stem.equals("")) {
                result.get(word).isComposed = true;
                result.get(word).rule = StaticLemmatizer.lemmaStatic(stem, Lang.nl) + "+" + StaticLemmatizer.lemmaStatic(end, Lang.nl);
                return;//continue;
            }
        }

        if (word.length() > 3) {
            String stem = word.substring(0, word.length() - 3);
            String end = word.substring(word.length() - 3, word.length());
            if ((end.equals("ten") || end.equals("den")) && dictionary.contains(stem + "en") && !stem.equals("")) {
                result.get(word).isComposed = true;
                result.get(word).rule = StaticLemmatizer.lemmaStatic(stem, Lang.nl) + "+" + StaticLemmatizer.lemmaStatic(end, Lang.nl);
                return;//continue;
            }
        }

        //Past participle
        if (word.length() > 3) {
            String begin = word.substring(0, 2);
            String stem = word.substring(2, word.length() - 1);
            char end = word.charAt(word.length() - 1);
            if (begin.equals("ge") && dictionary.contains(stem + "en") && !stem.equals("") && (end == 't' || end == 'd')) {
                result.get(word).isComposed = true;
                result.get(word).rule = "ge+" + StaticLemmatizer.lemmaStatic(stem, Lang.nl) + "+" + StaticLemmatizer.lemmaStatic(end+"", Lang.nl);
                return;//continue;
            }
        }

        if (word.length() > 2) {
            String first2 = word.substring(0, 2);
            String last2 = word.substring(2);
            if ((first2.equals("be") && dictionary.contains(last2))
                    || (first2.equals("be") && dictionary.contains(last2))
                    || (first2.equals("er") && dictionary.contains(last2))) {
                result.get(word).isComposed = true;
                result.get(word).rule = StaticLemmatizer.lemmaStatic(first2, Lang.nl) + "+" + StaticLemmatizer.lemmaStatic(last2, Lang.nl);
                return;//continue;
            }
        }

        //Descompunere
        for (int j = 3; j < word.length() - 2; j++) {//
            String a = word.substring(0, j);
            String b = word.substring(j, word.length());
            if (dictionary.contains(a) && dictionary.contains(b)) {
                result.get(word).isComposed = true;
                result.get(word).rule = StaticLemmatizer.lemmaStatic(a, Lang.nl) + "+" + StaticLemmatizer.lemmaStatic(b, Lang.nl);
                return;//break;
            } else if ((dictionary.contains(a.substring(0, a.length() - 1)) && a.charAt(a.length() - 1) == 's' && dictionary.contains(b))) {
                result.get(word).isComposed = true;
                result.get(word).rule = StaticLemmatizer.lemmaStatic(a.substring(0, a.length() - 1), Lang.nl) + "+" /*"+s+"*/ + StaticLemmatizer.lemmaStatic(b, Lang.nl);
                return;//break;
            } else if ((dictionary.contains(a.substring(0, a.length() - 2)) && a.substring(a.length() - 2, a.length()).equals("en") && dictionary.contains(b))) {
                result.get(word).isComposed = true;
                result.get(word).rule = StaticLemmatizer.lemmaStatic(a.substring(0, a.length() - 2), Lang.nl) + "+" /*"+en+"*/ + StaticLemmatizer.lemmaStatic(b, Lang.nl);
                return;//break;
            }
        }

        if (result.get(word).isComposed) {
            return;//continue;
        }
    }

    public static void calculate3(String word) {

        ArrayList<String> between = new ArrayList<String>();
        between.add("");
        between.add("s");
        between.add("en");

        //Descompunere din 3
        for (int j = 4; j < word.length() - 4; j++) {//2
            for (int k = j + 4; k < word.length() - 1; k++) {//2
                String wordA = word.substring(0, j);
                String wordB = word.substring(j, k);
                String wordC = word.substring(k);
                //System.out.println(wordA + " + " + wordB + " + " + wordC + " = " + word);
                for (String a : between) {
                    for (String b : between) {
                        String wordAA = wordA.substring(0, wordA.length() - a.length());
                        String wordBB = wordB.substring(0, wordB.length() - b.length());
                        if (a.equals("") && b.equals("") && dictionary.contains(wordA) && dictionary.contains(wordB) && dictionary.contains(wordC)) {
                            result.get(word).isComposed = true;
                            result.get(word).rule = StaticLemmatizer.lemmaStatic(wordA, Lang.nl) + "+" + StaticLemmatizer.lemmaStatic(wordB, Lang.nl) + "+" + StaticLemmatizer.lemmaStatic(wordC, Lang.nl);
                            return;
                        }
                        if (a.equals("") && dictionary.contains(wordA) && dictionary.contains(wordBB) && wordB.endsWith(b) && dictionary.contains(wordC)) {
                            result.get(word).isComposed = true;
//                            result.get(word).rule = wordA + "+" + wordBB + "+" + b + "+" + wordC;
                            result.get(word).rule = StaticLemmatizer.lemmaStatic(wordA, Lang.nl) + "+" + StaticLemmatizer.lemmaStatic(wordBB, Lang.nl) + "+" + StaticLemmatizer.lemmaStatic(wordC, Lang.nl);
                            return;
                        }
                        if (b.equals("") && dictionary.contains(wordAA) && wordA.endsWith(a) && dictionary.contains(wordB) && dictionary.contains(wordC)) {
                            result.get(word).isComposed = true;
//                            result.get(word).rule = wordA.substring(0, wordA.length() - a.length()) + "+" + a + "+" + wordB + "+" + wordC;
                            result.get(word).rule = StaticLemmatizer.lemmaStatic(wordA.substring(0, wordA.length() - a.length()), Lang.nl) + "+" + StaticLemmatizer.lemmaStatic(wordB, Lang.nl) + "+" + StaticLemmatizer.lemmaStatic(wordC, Lang.nl);
                            return;
                        }
                        if (dictionary.contains(wordAA) && wordA.endsWith(a) && dictionary.contains(wordBB) && wordB.endsWith(b) && dictionary.contains(wordC)) {
                            result.get(word).isComposed = true;
//                            result.get(word).rule = wordA + "+" + a + "+" + wordBB + "+" + b + "+" + wordC;
                            result.get(word).rule = StaticLemmatizer.lemmaStatic(wordA, Lang.nl) + "+" + StaticLemmatizer.lemmaStatic(wordBB, Lang.nl) + "+" + StaticLemmatizer.lemmaStatic(wordC, Lang.nl);
                            return;
                        }
                    }
                }
            }
        }
    }

//	public static String ngram(String composition){
//		String result ="";
//		
//		StringTokenizer st = new StringTokenizer(composition," +");
//		
//		ArrayList<String> words = new ArrayList<String>();
//		
//		while(st.hasMoreTokens()){
//			words.add(st.nextToken());
//		}
//		
//		boolean[][] ok = new boolean[words.size()][words.size()];
//		for(int i=0; i<words.size(); i++){
//			ok[0][i] = true;
//		}
//		
//		for(int i=1; i<words.size()-1; i++){
//			String[] aux = new String[words.size()-i];
//			for(int j=0; j<words.size()-i; j++){
//				aux[j] = "";
//				for(int k=j; k<=j+i; k++){
//					aux[j] += words.get(k);
//				}
//				if(dictionary.contains(aux[j])){
//					ok[j][i] = true;
//				}
//			}
//			boolean ok2 = true;
//			for(int j=0; j<words.size()-i; j++){
//				if(!dictionary.contains(aux[j])){
//					ok2 = false;
//					break;
//				}
//			}
//		}
//		
//		return result;
//	}
    public static void main(String[] args) {
        BufferedReader br = null;
        BufferedWriter bw = null;

        int WORD2LIMIT = 9;
        int WORD3LIMIT = 20;
        int SEGMENT = 12;
        String TYPE = "Direct"; // || "SEGMENT"+SEGMENT+"_Recursive[OneLevel]"
        
        try {
            br = new BufferedReader(new FileReader(new File("resources/config/NL/word lists/dict_nl.txt"))); //dict_nl.txt sample.txt
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("C:/DutchDecomposition/NL_Decomposition_threshold"+WORD2LIMIT+"_"+WORD3LIMIT+"_"+TYPE+".txt"),"UTF-8")); //CompositionRulesNL.txt sampleOut.txt
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String line = null;
        dictionary = new HashSet<String>();
        result = new HashMap<String, Composition>();
        evidence = new HashMap<Integer, HashSet<String>>();
        for (int i = 0; i < 42; i++) {
            evidence.put(i, new HashSet<String>());
        }

        try {
            while ((line = br.readLine()) != null) {
                dictionary.add(line);
                String lema = StaticLemmatizer.lemmaStatic(line, Lang.nl);
                evidence.get(lema.length()).add(lema);
                result.put(lema, new Composition());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = WORD2LIMIT; i <= 41; i++) {
            for (String word : evidence.get(i)) {
                computeDecomposition(word);
            }
        }

//		System.out.println("---");
//		computeDecomposition("duurzaambouwenbeleid");
//		System.out.println(result.get("duurzaambouwenbeleid").rule);
//		System.out.println("---");
        for (int i = WORD3LIMIT; i <= 41; i++) { //6
            for (String word : evidence.get(i)) {
                if (!result.get(word).isComposed) {
                    calculate3(word);
                }
            }
        }

        //Recursive decomposition One level
//		for(int i=41; i>=WORD2LIMIT; i--){
        //Recursive decomposition
//		for(int i=WORD2LIMIT; i<=41; i++){
//			for(String word : evidence.get(i)){
//				Composition c = result.get(word);
//				if(c.isComposed){
//					StringBuilder newRule = new StringBuilder("");
//					StringTokenizer st = new StringTokenizer(c.rule,"+\n ");
//					while(st.hasMoreTokens()){
//						String unit = st.nextToken();
//						Composition unitC = result.get(unit);
//						if(unitC == null || unit.length() < SEGMENT){//Segment
//							newRule.append(unit+"+");
//							continue;
//						}
//						if(unitC.isComposed){
//							newRule.append(unitC.rule + "+");
//						}else{
//							newRule.append(unit+"+");
//						}
//					}
//					c.rule = newRule.toString();
//					c.rule = c.rule.substring(0, c.rule.length()-1);
//				}
//			}
//		}
        try {
            TreeMap<String, Composition> aux = new TreeMap<String, Composition>();
            HashMap<Integer, TreeSet<String>> notComposed = new HashMap<Integer, TreeSet<String>>();
            HashMap<Integer, TreeMap<String, Composition>> results = new HashMap<Integer, TreeMap<String, Composition>>();

            for (Map.Entry<String, Composition> pair : result.entrySet()) {
                aux.put(pair.getKey(), pair.getValue());
            }

            for (int i = 0; i <= 41; i++) {
                results.put(i, new TreeMap<String, Composition>());
                notComposed.put(i, new TreeSet<String>());
            }

            for (int i = 0; i <= 41; i++) {
                for (String word : evidence.get(i)) {
                    TreeMap<String, Composition> set = results.get(i);
                    if (aux.get(word).isComposed) {
                        set.put(word, aux.get(word));
                    } else {
                        notComposed.get(i).add(word);
                    }
                }
            }

            for (int i = 0; i <= 41; i++) {
                for (Map.Entry<String, Composition> pair : results.get(i).entrySet()) {
                    Composition c = pair.getValue();
                    bw.write(pair.getKey() + " ::: " + c.rule + "\n");
                }
                if (results.get(i).entrySet().size() != 0) {
                    bw.write("\n\n");
                }
            }

            for (int i = 0; i <= 41; i++) {
                for (String word : notComposed.get(i)) {
                    bw.write(word + "\n");
                }
                if (notComposed.get(i).size() != 0) {
                    bw.write("\n\n");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        };

        try {
            br.close();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
