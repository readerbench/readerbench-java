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
package com.readerbench.datasourceprovider.data.cscl;

import java.util.ResourceBundle;

public enum CSCLIndices {
    NO_CONTRIBUTION(true, true),
    SCORE(true, true),
    SOCIAL_KB(true, true),
    INTER_ANIMATION_DEGREE(false, true),
    INDEGREE(true, true),
    OUTDEGREE(true, true),
    NO_NOUNS(false, true),
    NO_VERBS(false, true),
    NO_NEW_THREADS(false, false),
    AVERAGE_LENGTH_NEW_THREADS(false, false),
    NEW_THREADS_OVERALL_SCORE(false, false),
    NEW_THREADS_INTER_ANIMATION_DEGREE(false, false),
    NEW_THREADS_CUMULATIVE_SOCIAL_KB(false, false),
    PERSONAL_REGULARITY_ENTROPY(false, false),
    RHYTHMIC_INDEX(false, false),
    FREQ_MAX_INDEX(false, false),
    RHYTHMIC_COEFFICIENT(false, false);

    private final boolean isUsedForTimeModeling;
    private final boolean isIndividualStatsIndex;

    private CSCLIndices(boolean isUsedForTimeModeling, boolean isIndividualStatsIndex) {
        this.isUsedForTimeModeling = isUsedForTimeModeling;
        this.isIndividualStatsIndex = isIndividualStatsIndex;
    }

    public boolean isUsedForTimeModeling() {
        return isUsedForTimeModeling;
    }

    public boolean isIndividualStatsIndex() {
        return isIndividualStatsIndex;
    }

    public String getDescription() {
        return ResourceBundle.getBundle("utils.localization.CSCL_indices_descr").getString(this.name());
    }

    public String getAcronym() {
        return ResourceBundle.getBundle("utils.localization.CSCL_indices_acronyms").getString(this.name());
    }
}
