
package runtime.converters;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Cosmin
 */
public class IEEERefsEntriesSeparator {
    
    public static List<String> separate(String refSection) {
	List<String> sections = new ArrayList<String>();
	String[] splits = refSection.split("\n");
	
	
	String patternString1 = "^([0-9]+\\.\\s|\\[[0-9]+\\]\\s)";
	String patternString2 = "(?<!vol.)\\s([0-9]{1,2}\\.|\\[[0-9]{1,2}\\])\\s";
	
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
		currentSection += splits[i].substring(last_pos, matcher2.start());
		sections.add(currentSection);
		currentSection = "";
		last_pos = matcher2.start();
		while (matcher2.find()) {
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
