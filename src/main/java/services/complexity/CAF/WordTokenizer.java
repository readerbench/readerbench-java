package services.complexity.CAF;
import java.text.Normalizer;
import java.util.regex.Pattern;

public class WordTokenizer {
	public static final String CEliminatedTokens = "[^a-zA-Z]";
	public static final Pattern DIACRITICS_AND_FRIENDS = Pattern
			.compile("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");

	public static String GetWords(String text) {
		return stripDiacritics(text.toLowerCase()).replaceAll(
				CEliminatedTokens, " ");
	}

	private static String stripDiacritics(String str) {
		str = Normalizer.normalize(str, Normalizer.Form.NFD);
		str = DIACRITICS_AND_FRIENDS.matcher(str).replaceAll("");
		return str;
	}
}
