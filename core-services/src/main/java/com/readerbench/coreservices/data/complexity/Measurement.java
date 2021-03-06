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
package com.readerbench.coreservices.data.complexity;

import java.io.Serializable;

/**
 * 
 * @author Mihai Dascalu
 */
public class Measurement implements Serializable{
	private static final long serialVersionUID = -3372230226203993836L;

	private double inputClass;
	private double[] measurementValues;

	public Measurement(double c, double[] m) {
		inputClass = c;
		measurementValues = m;
	}

	public double getInputClass() {
		return inputClass;
	}

	public void setInputClass(double inputClass) {
		this.inputClass = inputClass;
	}

	public double[] getMeasurementValues() {
		return measurementValues;
	}

	public void setMeasurementValues(double[] measurementValues) {
		this.measurementValues = measurementValues;
	}
}
