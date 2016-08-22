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
package services.libsvm;

public class svm_model implements java.io.Serializable {
	private static final long serialVersionUID = -8642637231196646922L;
	public svm_parameter param; // parameter
	public int nr_class; // number of classes, = 2 in regression/one class svm
	public int l; // total #SV
	public svm_node[][] SV; // SVs (SV[l])
	public double[][] sv_coef; // coefficients for SVs in decision functions
								// (sv_coef[k-1][l])
	public double[] rho; // constants in decision functions (rho[k*(k-1)/2])
	public double[] probA; // pariwise probability information
	public double[] probB;

	// for classification only
	public int[] label; // label of each class (label[k])
	public int[] nSV; // number of SVs for each class (nSV[k])
	// nSV[0] + nSV[1] + ... + nSV[k-1] = l
};
