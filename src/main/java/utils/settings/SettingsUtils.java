package utils.settings;

import java.util.ResourceBundle;

public class SettingsUtils {
	public static String getReaderBenchVersion() {
		String text = ResourceBundle.getBundle("utils.readerbench").getString("ReaderBenchVersion");
		if(text == null || text.length() == 0) {
			return "";
		}
		return text;
	}
}
