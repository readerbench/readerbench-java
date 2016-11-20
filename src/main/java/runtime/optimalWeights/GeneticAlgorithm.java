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
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openide.util.Exceptions;

public class GeneticAlgorithm implements Runnable {

    static final Logger LOGGER = Logger.getLogger("");

    private static List<double[]> x;

    private static List<Integer> d;

    private final int id;

    private final CyclicBarrier barrier;

    public static final int MAX_NO_GENERATIONS = 1000;

    public static final int MAX_TRIES_CROSSOVER = 1000;

    private static final int NO_CHROMOSOMES_PER_GENERATION = 10000;

    // minimum percentage of best parents/children transfered to new population
    private static final double KEPT_PERCENTAGE = 0.2d;

    // percentage of consecutive generations with same best individual
    private static final double CONVERGENCE = 0.02d;

    // minimal distance between 2 individuals used for recombination
    private static final double MIN_DISTANCE = Math.pow(1d / Chromosome.DIMENSION, 2);

    private static final double INTERVAL = 0.25;

    private double total = 0;

    private double threshold[];

    private List<Chromosome> generation = new ArrayList<>();

    private final List<Chromosome> mostPromising;

    public GeneticAlgorithm(int id, List<Chromosome> mostPromising, CyclicBarrier barrier) {
        LOGGER.log(Level.INFO, "Initializing population {0}", id);
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

        x = new ArrayList<>();
        d = new ArrayList<>();

        try {
            FileInputStream fis = new FileInputStream(myFile);
            // Finds the workbook instance for XLSX file
            XSSFWorkbook myWorkBook = new XSSFWorkbook(fis);

            // Return first sheet from the XLSX workbook
            XSSFSheet mySheet = myWorkBook.getSheetAt(0);

            // Get iterator to all the rows in current sheet
            Iterator<Row> rowIterator = mySheet.iterator();

            // ignore header line
            if (rowIterator.hasNext()) {
                rowIterator.next();
                rowIterator.next();
            }
            int line = 2;
            // Traversing over each row of XLSX file
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                if (row.getCell(41) == null || row.getCell(54) == null) {
                    continue;
                }
                String response = row.getCell(41).getStringCellValue();
                if (response == null || !response.equals("OK")) {
                    continue;
                }
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

                double[] entry = new double[]{constant, WCMC, LSA_CS, LSA_PT, LSA_TITLE, W_IMPT, W_PRIOR, W_LATER, W_NEW};
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
                for (double e : entry) {
                    sb.append(formatter.format(e)).append("\t");
                }
                LOGGER.log(Level.INFO, "Line:{0}: read {1}>>\t{2}\t/\t{3}\t/\t{4}\t//\t{5}", new Object[]{line, sb, formatter.format(pred[0]), formatter.format(pred[1]), formatter.format(pred[2]), PSS});
                line++;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error: {0}", e.getMessage());
            Exceptions.printStackTrace(e);
        }

        LOGGER.info("Finished processing all rows...");
    }

    public void reinitialize() {
        LOGGER.log(Level.INFO, "Reinitializing population {0}...", id);
        int i = 0;

        List<Chromosome> newGeneration = new ArrayList<>();

        // keep best 20% of individuals
        for (; i < 0.2 * NO_CHROMOSOMES_PER_GENERATION; i++) {
            newGeneration.add(generation.get(i));
        }

        // 80% completely random
        for (; i < NO_CHROMOSOMES_PER_GENERATION; i++) {
            Chromosome indiv = new Chromosome();
            indiv.initialise_random();
            indiv.fitness(x, d);
            newGeneration.add(indiv);
        }
        generation = newGeneration;
    }

    public Chromosome selection() {
        double val = Math.random() * total;
        for (int i = 0; i < generation.size(); i++) {
            if (threshold[i] <= val && val < threshold[i + 1]) {
                return generation.get(i);
            }
        }
        return null;
    }

    public void crossOver(List<Chromosome> generation) {
        Chromosome i1, i2;
        i1 = selection();
        i2 = selection();
        int sample = 0;
        while (i2.distanceFrom(i1) < MIN_DISTANCE && (sample++) < MAX_TRIES_CROSSOVER) {
            i2 = selection();
        }

        Chromosome new1 = new Chromosome();
        Chromosome new2 = new Chromosome();

        // Real Intermediate Recombination alpha in [-interval, 1 + interval]
        for (int i = 0; i < i1.getCoefficients().length; i++) {
            double alpha = Math.random() * (1 + 2 * INTERVAL) - INTERVAL;
            new1.getCoefficients()[i] = i1.getCoefficients()[i]
                    + alpha * (i2.getCoefficients()[i] - i1.getCoefficients()[i]);
        }

        for (int i = 0; i < i1.getCoefficients().length; i++) {
            double alpha = Math.random() * (1 + 2 * INTERVAL) - INTERVAL;
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
                if (Math.random() < prob) {
                    delta += Math.pow(2, -j);
                }
            }

            // randomly adjust
            if (Math.round(Math.random()) == 0) {
                individual.getCoefficients()[i] += 0.5 * dom_var * delta;
            } else {
                individual.getCoefficients()[i] -= 0.5 * dom_var * delta;
            }

            // fit constraint of [MIN_THRESHOLD; MAX_THRESHOLD]
            if (individual.getCoefficients()[i] > Chromosome.MAX_THRESHOLD) {
                individual.getCoefficients()[i] = Chromosome.MAX_THRESHOLD;
            }
            if (individual.getCoefficients()[i] < Chromosome.MIN_THRESHOLD) {
                individual.getCoefficients()[i] = Chromosome.MIN_THRESHOLD;
            }
        }
        individual.fitness(x, d);

        return individual;
    }

    public void newGeneration() {
        List<Chromosome> newGeneration = new ArrayList<>();

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

        List<Chromosome> finalGeneration = new ArrayList<>();

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

    @Override
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
                        mostPromising.add(generation.get(0));
                    }
                    // remove worst individual
                    generation.remove(generation.size() - 1);
                    barrier.await();
                    // add one of the most promising individuals
                    Chromosome c;
                    synchronized (mostPromising) {
                        c = mostPromising.get((int) (Math.random() * mostPromising.size()));
                    }
                    // deep copy
                    Chromosome newC = new Chromosome();
                    System.arraycopy(c.getCoefficients(), 0, newC.getCoefficients(), 0, c.getCoefficients().length);
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

                intermediate[gen] = generation.get(0).getFitness();

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
            Chromosome best = generation.get(0);

            synchronized (mostPromising) {
                mostPromising.add(generation.get(0));
            }

            LOGGER.log(Level.INFO, "Population {0}: Best overall individual:\n{1}", new Object[]{id, best});
        } catch (InterruptedException | BrokenBarrierException ex) {
            Exceptions.printStackTrace(ex);
            LOGGER.severe(ex.getMessage());
        }
    }

    public List<Chromosome> getGeneratie() {
        return generation;
    }

    public void setGeneratie(LinkedList<Chromosome> generatie) {
        this.generation = generatie;
    }

    public static void main(String[] args) {
        parseDataFile("resources/in/iStart/ep_textscore_all.xlsx");
        int noPopulations = 10;

        Thread[] populations = new Thread[noPopulations];

        CyclicBarrier barrier = new CyclicBarrier(noPopulations);

        List<Chromosome> mostPromising = new ArrayList<>();

        // create new populations
        for (int i = 0; i < noPopulations; i++) {
            GeneticAlgorithm GA = new GeneticAlgorithm(i, mostPromising, barrier);
            populations[i] = new Thread(GA);
            populations[i].start();
        }

        for (int i = 0; i < noPopulations; i++) {
            try {
                populations[i].join();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        Collections.sort(mostPromising);

        LOGGER.log(Level.INFO, "Best overall chromosome:\n{0}", mostPromising.get(0));
    }
}
