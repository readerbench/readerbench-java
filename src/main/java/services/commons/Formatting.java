package services.commons;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;

public class Formatting {
	public static Double formatNumber(double scoreValue) {
		DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
	    decimalFormatSymbols.setDecimalSeparator('.');
	    
		DecimalFormat formatter = new DecimalFormat("#.###");
		try {
			return formatter.parse(formatter.format(scoreValue)).doubleValue();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0.0;
	}
}
