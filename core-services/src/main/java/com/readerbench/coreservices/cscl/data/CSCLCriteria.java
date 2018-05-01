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
package com.readerbench.coreservices.cscl.data;

import com.readerbench.coreservices.commons.VectorAlgebra;

import java.util.ResourceBundle;

public enum CSCLCriteria {
	AVERAGE, STDEV, SLOPE, ENTROPY, UNIFORMITY, PEAK_CHAT_FRAME, LOCAL_EXTREME, RECURRENCE_AVERAGE, RECURRENCE_STDEV, ;

	public String getDescription() {
		return ResourceBundle.getBundle("utils.localization.CSCL_criteria").getString(this.name());
	}

	/**
	 * Apply a certain criteria on the input vector
	 * 
	 * @param crit
	 * @param v
	 * @return
	 */
	public static double getValue(CSCLCriteria crit, double[] v) {
		switch (crit) {
		case AVERAGE:
			return VectorAlgebra.avg(v);
		case STDEV:
			return VectorAlgebra.stdev(v);
		case SLOPE:
			return VectorAlgebra.slope(v);
		case ENTROPY:
			return VectorAlgebra.entropy(v);
		case UNIFORMITY:
			return VectorAlgebra.uniformity(v);
		case LOCAL_EXTREME:
			return VectorAlgebra.localExtremeDetection(v);
		case RECURRENCE_AVERAGE:
			return VectorAlgebra.avg(VectorAlgebra.getRecurrence(v));
		case RECURRENCE_STDEV:
			return VectorAlgebra.stdev(VectorAlgebra.getRecurrence(v));
                case PEAK_CHAT_FRAME:
                    return VectorAlgebra.peakOnChatFrame(v);
		default:
			return -1;
		}
	}
}
