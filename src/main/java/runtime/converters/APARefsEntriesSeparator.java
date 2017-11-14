/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package runtime.converters;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Cosmin
 */
public class APARefsEntriesSeparator {
    public static List<String> separate(String refSection) {
	List<String> sections = new ArrayList<String>();
	String[] splits = refSection.split("\n");
	
	
	String patternString1 = "^\\s*[A-ZĂÎÂȘȚ][a-zA-Z-ăîâșțĂÎÂȘȚ']+,\\s[A-ZăîâșțĂÎÂȘȚ]\\.";
	String patternString2 = "(?<![A-Z])\\.\\s+[A-ZĂÎÂȘȚ][a-zA-Z-ăîâșțĂÎÂȘȚ']+,\\s[A-ZăîâșțĂÎÂȘȚ]\\.";
	
	Pattern pattern1 = Pattern.compile(patternString1);
	Pattern pattern2 = Pattern.compile(patternString2);
	//Matcher matcher1 = pattern1.matcher(refText);
	String currentSection = "";
	for (int i = 0; i < splits.length; i++) {
	    Matcher matcher1 = pattern1.matcher(splits[i]);
	    Matcher matcher2 = pattern2.matcher(splits[i]);
	    if (matcher1.find()) {
		//System.out.println(splits[i]);
		if (currentSection != "") {
		    sections.add(currentSection);
		    currentSection = "";
		    
		}
	    } 
	    int last_pos = 0;
	    if (matcher2.find()) {
		System.out.println("MATCHER " + matcher2.group(0));
		currentSection += splits[i].substring(last_pos, matcher2.start());
		sections.add(currentSection);
		currentSection = "";
		last_pos = matcher2.start();
		while (matcher2.find()) {
		    System.out.println("MATCHER " + matcher2.group(0));
		    currentSection += splits[i].substring(last_pos, matcher2.start());
		    sections.add(currentSection);
		    currentSection = "";
		    last_pos = matcher2.start();


		}
		currentSection += splits[i].substring(last_pos);
	    } else {
		currentSection += splits[i];

	    }
		
	    
	}
	if (currentSection != "") {
	    sections.add(currentSection);
	}
	
	return sections;
	
	
    }
    
}
