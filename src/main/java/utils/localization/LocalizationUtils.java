package utils.localization;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;


public class LocalizationUtils {
	private static HashMap<Locale, String> localeStringMap = new HashMap<Locale, String>();
	private static HashMap<String, Locale> stringLocaleMap = new HashMap<String, Locale>();
	
	static {
		localeStringMap.put(Locale.ENGLISH, "English");
		localeStringMap.put(Locale.FRENCH, "French");
		
		stringLocaleMap.put("English", Locale.ENGLISH);
		stringLocaleMap.put("French", Locale.FRENCH);
	}
	
	public static Locale LOADED_LOCALE = Locale.ENGLISH;
	static {
		String language = ResourceBundle.getBundle("utils.localization.settings").getString("SelectedLanguage");
		Locale detectedLanguage = stringLocaleMap.get(language);
		LOADED_LOCALE = detectedLanguage;
		Locale.setDefault(LOADED_LOCALE);
		ResourceBundle.clearCache();
	}
	
	public static final int TITLE 	= 0;
	public static final int TEXT	= 1;
	
	public static void saveLocaleInBundle(Locale locale) {
		String localeToSave = localeStringMap.get(locale);
		
		try {
			FileInputStream in = new FileInputStream("src/main/resources/utils/localization/settings.properties");
			Properties props = new Properties();
			props.load(in);
			in.close();
	
			FileOutputStream out = new FileOutputStream("src/main/resources/utils/localization/settings.properties");
			props.setProperty("SelectedLanguage", localeToSave);
			props.store(out, null);
			out.close();
			
			LOADED_LOCALE = locale;
			Locale.setDefault(LOADED_LOCALE);
			ResourceBundle.clearCache();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getLocalizedString (Class<?> inClass, int type, String key) {
		if (key == null || key.length() == 0) {
			return "";
		}
		
		String fullKey = inClass.getSimpleName() + "." + key + ".";
		switch (type) {
		case TITLE:
			fullKey += "title";
			break;
		case TEXT:
			fullKey += "text";
			break;
		default:
			break;
		}

		String text = ResourceBundle.getBundle("utils.localization.messages").getString(fullKey);
		if(text == null || text.length() == 0) {
			return key;
		}
		return text;
	}
	
	public static String getTranslation(String text) {
		return ResourceBundle.getBundle("utils.localization.translations").getString(text);
	}
}