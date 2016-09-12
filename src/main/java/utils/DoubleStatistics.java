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
package utils;

import java.util.DoubleSummaryStatistics;
import java.util.stream.Collector;

/**
 *
 * @author Stefan Ruseti
 */
public class DoubleStatistics extends DoubleSummaryStatistics {

    private double sumOfSquare = 0.0d;
    private double sumOfSquareCompensation; // Low order bits of sum
    private double simpleSumOfSquare; // Used to compute right sum for non-finite inputs

    @Override
    public void accept(double value) {
        super.accept(value);
        double squareValue = value * value;
        simpleSumOfSquare += squareValue;
        sumOfSquareWithCompensation(squareValue);
    }

    public DoubleStatistics combine(DoubleStatistics other) {
        super.combine(other);
        simpleSumOfSquare += other.simpleSumOfSquare;
        sumOfSquareWithCompensation(other.sumOfSquare);
        sumOfSquareWithCompensation(other.sumOfSquareCompensation);
        return this;
    }

    private void sumOfSquareWithCompensation(double value) {
        double tmp = value - sumOfSquareCompensation;
        double velvel = sumOfSquare + tmp; // Little wolf of rounding error
        sumOfSquareCompensation = (velvel - sumOfSquare) - tmp;
        sumOfSquare = velvel;
    }

    public double getSumOfSquare() {
        double tmp = sumOfSquare + sumOfSquareCompensation;
        if (Double.isNaN(tmp) && Double.isInfinite(simpleSumOfSquare)) {
            return simpleSumOfSquare;
        }
        return tmp;
    }

    public final double getStandardDeviation() {
        return getStandardDeviation(0);
    }

    public final double getStandardDeviation(double orElse) {
        long count = getCount();
        double _sumOfSquare = getSumOfSquare();
        double average = getAverage();
        if (count == 0) return orElse;
        return count > 1 ? Math.sqrt((_sumOfSquare - count * Math.pow(average, 2)) / (count - 1)) : 0.;
    }

    public static Collector<Double, ?, DoubleStatistics> collector() {
        return Collector.of(DoubleStatistics::new, DoubleStatistics::accept, DoubleStatistics::combine);
    }

}
