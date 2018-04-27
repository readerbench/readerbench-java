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
package com.readerbench.datasourceprovider.commons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;

public class Formatting {

    private static final Logger LOGGER = LoggerFactory.getLogger(Formatting.class);

    public static Double formatNumber(double scoreValue) {
        return Formatting.formatNumber(scoreValue, 3);
    }

    public static Double formatNumber(double scoreValue, int decimals) {
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');

        StringBuilder sbDecimalFormat = new StringBuilder("#.");
        for (int i = 0; i < decimals; i++) {
            sbDecimalFormat.append('#');
        }
        DecimalFormat formatter = new DecimalFormat(sbDecimalFormat.toString());
        try {
            return formatter.parse(formatter.format(scoreValue)).doubleValue();
        } catch (ParseException e) {
            LOGGER.error(e.getMessage());
        }
        return 0.0;
    }

}
