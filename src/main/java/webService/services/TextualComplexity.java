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
package webService.services;

import java.util.ArrayList;
import java.util.List;

import data.AbstractDocument;
import data.Lang;
import services.commons.Formatting;
import services.complexity.ComplexityIndices;
import services.complexity.IComplexityFactors;
import services.complexity.discourse.ConnectivesComplexity;
import services.complexity.discourse.DialogismStatisticsComplexity;
import services.complexity.discourse.DialogismSynergyComplexity;
import services.complexity.discourse.DiscourseComplexity;
import services.complexity.discourse.LexicalCohesionComplexity;
import services.complexity.discourse.SemanticCohesionComplexity;
import services.complexity.lexicalChains.LexicalChainsComplexity;
import services.complexity.surface.EntropyComplexity;
import services.complexity.surface.LengthComplexity;
import services.complexity.surface.SurfaceStatisticsComplexity;
import services.complexity.syntax.POSComplexity;
import services.complexity.syntax.PronounsComplexity;
import services.complexity.syntax.TreeComplexity;
import webService.result.*;

public class TextualComplexity {

	private AbstractDocument d;
	private Lang lang;
	private boolean posTagging;
	private boolean computeDialogism;
	private List<IComplexityFactors> list;

	public TextualComplexity(Lang lang, boolean posTagging, boolean computeDialogism) {
		this.lang = lang;
		this.posTagging = posTagging;
		this.computeDialogism = computeDialogism;
		initializeList();
	}

	public TextualComplexity(AbstractDocument d, Lang lang, boolean posTagging, boolean computeDialogism) {
		this.d = d;
		this.lang = lang;
		this.posTagging = posTagging;
		this.computeDialogism = computeDialogism;
		initializeList();
	}

	public List<IComplexityFactors> getList() {
		return list;
	}

	public void setList(List<IComplexityFactors> list) {
		this.list = list;
	}

	public Lang getLang() {
		return lang;
	}

	public void setLang(Lang lang) {
		this.lang = lang;
	}

	private void initializeList() {
		list = new ArrayList<IComplexityFactors>();
		list.add(new LengthComplexity());
		list.add(new SurfaceStatisticsComplexity());
		list.add(new EntropyComplexity());
		list.add(new PronounsComplexity());
		list.add(new ConnectivesComplexity(lang));
		list.add(new DiscourseComplexity());
		list.add(new SemanticCohesionComplexity(1));
		list.add(new SemanticCohesionComplexity(3));
		list.add(new SemanticCohesionComplexity(4));

		if (posTagging) {
			list.add(new POSComplexity());
			list.add(new TreeComplexity());
		}

		if (computeDialogism == true) {
			list.add(new LexicalChainsComplexity());
			list.add(new LexicalCohesionComplexity());
			list.add(new DialogismStatisticsComplexity());
			list.add(new DialogismSynergyComplexity());
		}
	}

	/**
	 * Get values for all textual complexity indices applied on the entire
	 * document
	 *
	 * @param query
	 * @return List of sentiment values per entity
	 */
	public List<ResultTextualComplexity> getComplexityIndices() {

		List<ResultTextualComplexity> resultsComplexity = new ArrayList<ResultTextualComplexity>();

		d.setComplexityIndices(new double[ComplexityIndices.NO_COMPLEXITY_INDICES]);

		// complexity indices computation
		for (IComplexityFactors f : list) {
			f.computeComplexityFactors(d);
		}

		// complexity indices save to result list
		for (IComplexityFactors f : list) {
			List<ResultValence> localResults = new ArrayList<ResultValence>();
			for (int i : f.getIDs())
				localResults.add(new ResultValence(ComplexityIndices.TEXTUAL_COMPLEXITY_INDEX_DESCRIPTIONS[i],
						Formatting.formatNumber(d.getComplexityIndices()[i])));
			resultsComplexity.add(new ResultTextualComplexity(f.getClassName(), localResults));
		}

		/*
		 * List<ResultValence> localResults; for (IComplexityFactors
		 * complexityClass : ComplexityIndices.TEXTUAL_COMPLEXITY_FACTORS) {
		 * localResults = new ArrayList<ResultValence>(); for (int id :
		 * complexityClass.getIDs()) { localResults.add(new
		 * ResultValence(ComplexityIndices.TEXTUAL_COMPLEXITY_INDEX_DESCRIPTIONS
		 * [id], Formatting.formatNumber(queryDoc.getComplexityIndices()[id])));
		 * } resultsComplexity.add(new
		 * ResultTextualComplexity(complexityClass.getClassName(),
		 * localResults)); }
		 */

		return resultsComplexity;
	}

}
