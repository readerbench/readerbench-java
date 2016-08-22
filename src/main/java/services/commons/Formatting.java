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
