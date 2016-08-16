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
package runtime.optimalWeights;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import runtime.timeSeries.KeyStrokeLoginTimeSeries;

public class GeneticAlgorithm implements Runnable {
	static Logger logger = Logger.getLogger(KeyStrokeLoginTimeSeries.class);

	private static List<double[]> x;

	private static List<Integer> d;

	private int id;

	private CyclicBarrier barrier;

	public static final int MAX_NO_GENERATIONS = 1000;

	public static final int MAX_TRIES_CROSSOVER = 1000;

	private static final int NO_CHROMOSOMES_PER_GENERATION = 10000;

	// minimum percentage of best parents/children transfered to new population
	private static final double KEPT_PERCENTAGE = 0.2d;

	// percentage of consecutive generations with same best individual
	private static final double CONVERGENCE = 0.02d;

	// minimal distance between 2 individuals used for recombination
	private static double MIN_DISTANCE = Math.pow(1d / Chromosome.DIMENSION, 2);

	private static double interval = 0.25;

	private double total = 0;

	private double threshold[];

	private LinkedList<Chromosome> generation = new LinkedList<Chromosome>();

	private List<Chromosome> mostPromising;

	public GeneticAlgorithm(int id, List<Chromosome> mostPromising, CyclicBarrier barrier) {
		logger.info("Initializing population " + id);
		this.id = id;
		this.mostPromising = mostPromising;
		this.barrier = barrier;

		for (int i = 0; i < NO_CHROMOSOMES_PER_GENERATION; i++) {
			Chromosome indiv = new Chromosome();
			indiv.initialise_random();
			indiv.fitness(x, d);
			generation.add(indiv);
		}
	}

	public static void parseDataFile(String path) {
		File myFile = new File(path);

		x = new ArrayList<double[]>();
		d = new ArrayList<Integer>();

		try {
			FileInputStream fis = new FileInputStream(myFile);
			// Finds the workbook instance for XLSX file
			XSSFWorkbook myWorkBook = new XSSFWorkbook(fis);

			// Return first sheet from the XLSX workbook
			XSSFSheet mySheet = myWorkBook.getSheetAt(0);

			// Get iterator to all the rows in current sheet
			Iterator<Row> rowIterator = mySheet.iterator();

			// ignore header line
			if (rowIterator.hasNext())
				rowIterator.next();
			int line = 2;
			// Traversing over each row of XLSX file
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();

				if (row.getCell(41) == null || row.getCell(54) == null)
					continue;
				String response = row.getCell(41).getStringCellValue();
				if (response == null || !response.equals("OK"))
					continue;
				double constant = 1;

				double WCMC = java.lang.Math.log((double) row.getCell(54).getNumericCellValue());
				double LSA_CS = (double) row.getCell(57).getNumericCellValue();
				double LSA_PT = (double) row.getCell(58).getNumericCellValue();
				double LSA_TITLE = (double) row.getCell(56).getNumericCellValue();
				double W_IMPT = (double) row.getCell(72).getNumericCellValue();
				double W_PRIOR = (double) row.getCell(73).getNumericCellValue();
				double W_LATER = (double) row.getCell(74).getNumericCellValue();
				double W_NEW = (double) row.getCell(75).getNumericCellValue();

				// EN value
				int PSS = (int) row.getCell(40).getNumericCellValue();

				double[] entry = new double[] { constant, WCMC, LSA_CS, LSA_PT, LSA_TITLE, W_IMPT, W_PRIOR, W_LATER,
						W_NEW };
				x.add(entry);
				d.add(PSS);

				// print read line
				DecimalFormat formatter = new DecimalFormat("#.###");
				StringBuffer sb = new StringBuffer();
				double[] pred = new double[3];
				for (int i = 0; i < 3; i++) {
					pred[i] = 0;
					for (int j = 0; j < Chromosome.DIMENSION; j++) {
						pred[i] += entry[j] * Chromosome.DFA_COEFF[i][j];
					}
				}
				for (double e : entry)
					sb.append(formatter.format(e) + "\t");
				logger.info("Line:" + line + ": read " + sb + ">>\t" + formatter.format(pred[0]) + "\t/\t"
						+ formatter.format(pred[1]) + "\t/\t" + formatter.format(pred[2]) + "\t//\t" + PSS);
				line++;
			}
		} catch (Exception e) {
			logger.error("Error: " + e.getMessage());
			e.printStackTrace();
		}

		logger.info("Finished processing all rows...");
	}

	public void reinitialize() {
		logger.info("Reinitializing population " + id + "...");
		int i = 0;

		LinkedList<Chromosome> newGeneration = new LinkedList<Chromosome>();

		// keep best 20% of individuals
		for (; i < 0.2 * NO_CHROMOSOMES_PER_GENERATION; i++) {
			newGeneration.add(generation.get(i));
		}

		// 80% completely random
		for (; i < NO_CHROMOSOMES_PER_GENERATION; i++) {
			Chromosome indiv = new Chromosome();
			indiv.initialise_random();
			indiv.fitness(x, d);
			generation.add(indiv);
		}
	}

	public Chromosome selection() {
		double val = Math.random() * total;
		for (int i = 0; i < generation.size(); i++) {
			if (threshold[i] <= val && val < threshold[i + 1])
				return generation.get(i);
		}
		return null;
	}

	public void crossOver(LinkedList<Chromosome> generation) {
		Chromosome i1 = null, i2 = null;
		i1 = selection();
		i2 = selection();
		int sample = 0;
		while (i2.distanceFrom(i1) < MIN_DISTANCE && (sample++) < MAX_TRIES_CROSSOVER)
			i2 = selection();

		Chromosome new1 = new Chromosome();
		Chromosome new2 = new Chromosome();

		// Real Intermediate Recombination alpha in [-interval, 1 + interval]
		for (int i = 0; i < i1.getCoefficients().length; i++) {
			double alpha = Math.random() * (1 + 2 * interval) - interval;
			new1.getCoefficients()[i] = i1.getCoefficients()[i]
					+ alpha * (i2.getCoefficients()[i] - i1.getCoefficients()[i]);
		}

		for (int i = 0; i < i1.getCoefficients().length; i++) {
			double alpha = Math.random() * (1 + 2 * interval) - interval;
			new2.getCoefficients()[i] = i1.getCoefficients()[i]
					+ alpha * (i2.getCoefficients()[i] - i1.getCoefficients()[i]);
		}

		generation.add(mutation(new1));
		generation.add(mutation(new2));
	}

	public Chromosome mutation(Chromosome individual) {
		// mutated variable = variable ± range∑delta (+ sau - cu probabililate
		// egala)
		// range = 0.5*domeniu variabila
		// delta = sum(a(i) 2-i), a(i) = 1 cu probabilitate 1/m, altfel a(i) =
		// 0.

		double dom_var = Chromosome.MAX_THRESHOLD - Chromosome.MIN_THRESHOLD;
		double prob = 1 / (2 / Math.log10(2));

		for (int i = 0; i < individual.getCoefficients().length; i++) {
			double delta = 0;
			for (int j = 0; j < Math.round(1 / prob + 0.5); j++) {
				if (Math.random() < prob)
					delta += Math.pow(2, -j);
			}

			// randomly adjust
			if (Math.round(Math.random()) == 0)
				individual.getCoefficients()[i] += 0.5 * dom_var * delta;
			else
				individual.getCoefficients()[i] -= 0.5 * dom_var * delta;

			// fit constraint of [MIN_THRESHOLD; MAX_THRESHOLD]
			if (individual.getCoefficients()[i] > Chromosome.MAX_THRESHOLD)
				individual.getCoefficients()[i] = Chromosome.MAX_THRESHOLD;
			if (individual.getCoefficients()[i] < Chromosome.MIN_THRESHOLD)
				individual.getCoefficients()[i] = Chromosome.MIN_THRESHOLD;
		}
		individual.fitness(x, d);

		return individual;
	}

	public void newGeneration() {
		LinkedList<Chromosome> newGeneration = new LinkedList<Chromosome>();

		total = 0;
		threshold = new double[generation.size() + 1];

		for (int i = 0; i < generation.size(); i++) {
			threshold[i] = total;
			total += generation.get(i).fitness(x, d);
		}
		threshold[generation.size()] = total;

		while (newGeneration.size() < NO_CHROMOSOMES_PER_GENERATION) {
			crossOver(newGeneration);
		}

		Collections.sort(generation);
		Collections.sort(newGeneration);

		LinkedList<Chromosome> finalGeneration = new LinkedList<Chromosome>();

		int index1 = 0, index2 = 0;

		// add the best parents
		while (index1 < KEPT_PERCENTAGE * NO_CHROMOSOMES_PER_GENERATION) {
			finalGeneration.add(generation.get(index1));
			index1++;
		}

		// add the best children
		while (index2 < KEPT_PERCENTAGE * NO_CHROMOSOMES_PER_GENERATION) {
			finalGeneration.add(newGeneration.get(index2));
			index2++;
		}

		// add the overall best individuals
		while (finalGeneration.size() < (1 - KEPT_PERCENTAGE) * NO_CHROMOSOMES_PER_GENERATION) {
			if (generation.get(index1).getFitness() > newGeneration.get(index2).getFitness()) {
				finalGeneration.add(generation.get(index1));
				index1++;
			} else {
				finalGeneration.add(newGeneration.get(index2));
				index2++;
			}
		}
		Collections.sort(finalGeneration);

		generation = finalGeneration;
	}

	public void run() {
		try {
			int lastReinitialization = 0;
			double[] intermediate = new double[MAX_NO_GENERATIONS];
			for (int gen = 0; gen < MAX_NO_GENERATIONS; gen++) {
				// synchronize every 10 generations
				if (gen % 10 == 0) {
					barrier.await();
					Collections.sort(generation);
					synchronized (mostPromising) {
						mostPromising.add(generation.getFirst());
					}
					// remove worst individual
					generation.removeLast();
					barrier.await();
					// add one of the most promising individuals
					Chromosome c;
					synchronized (mostPromising) {
						c = mostPromising.get((int) (Math.random() * mostPromising.size()));
					}
					// deep copy
					Chromosome newC = new Chromosome();
					for (int i = 0; i < c.getCoefficients().length; i++)
						newC.getCoefficients()[i] = c.getCoefficients()[i];
					newC.setFitness(c.getFitness());
					generation.add(newC);
					barrier.await();
					synchronized (mostPromising) {
						mostPromising.clear();
					}

					Collections.sort(generation);
					barrier.await();
				}

				// run new generation
				newGeneration();

				intermediate[gen] = generation.getFirst().getFitness();

				// verify convergence
				if (gen > CONVERGENCE * MAX_NO_GENERATIONS + lastReinitialization) {
					boolean converged = true;
					for (int i = 1; i < Math.round(CONVERGENCE * MAX_NO_GENERATIONS + 0.5f); i++) {
						if (intermediate[gen] != intermediate[gen - i]) {
							converged = false;
							break;
						}
					}
					if (converged) {
						lastReinitialization = gen;
						reinitialize();
					}
				}
			}

			Collections.sort(generation);
			Chromosome best = generation.getFirst();

			synchronized (mostPromising) {
				mostPromising.add(generation.getFirst());
			}

			logger.info("Population " + id + ": Best overall individual:\n" + best);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
	}

	public LinkedList<Chromosome> getGeneratie() {
		return generation;
	}

	public void setGeneratie(LinkedList<Chromosome> generatie) {
		this.generation = generatie;
	}

	public static void main(String[] args) {
		parseDataFile("resources/in/iStart/ep_textscore_all.xlsx");
		int noPopulations = 7;

		Thread[] populations = new Thread[noPopulations];

		CyclicBarrier barrier = new CyclicBarrier(noPopulations);

		List<Chromosome> mostPromising = new ArrayList<Chromosome>();

		// create new populations
		for (int i = 0; i < noPopulations; i++) {
			GeneticAlgorithm GA = new GeneticAlgorithm(i, mostPromising, barrier);
			populations[i] = new Thread(GA);
			populations[i].start();
		}

		for (int i = 0; i < noPopulations; i++) {
			try {
				populations[i].join();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}

		Collections.sort(mostPromising);

		logger.info("Best overall chromosome:\n" + mostPromising.get(0));
	}
}