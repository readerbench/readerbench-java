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
package com.readerbench.services.comprehensionModel.utils;

/**
 *
 * @author ionutparaschiv
 */
public class AoAMetric {
    private double weightedAvg;
    private double avg;
    private double weightedIdfAvg;
    
    public AoAMetric() {
        this.weightedAvg = 0.0;
        this.avg = 0.0;
        this.weightedIdfAvg = 0.0;
    }
    
    public double getWeightedAvg() {
        return this.weightedAvg;
    }
    public void setWeightedAvg(double weightedAvg) {
        this.weightedAvg = weightedAvg;
    }
    
    public double getAvg() {
        return this.avg;
    }
    public void setAvg(double avg) {
        this.avg = avg;
    }
    
    public double getWeightedIdfAvg() {
        return this.weightedIdfAvg;
    }
    public void setWeightedIdfAvg(double weightedIdfAvg) {
        this.weightedIdfAvg = weightedIdfAvg;
    }
}