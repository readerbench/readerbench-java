/*************************************************************************
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 *  Copyright 2016 Adobe Systems Incorporated
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by all applicable intellectual property
 * laws, including trade secret and copyright laws.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 **************************************************************************/

package data.cscl;

import weka.clusterers.HierarchicalClusterer;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.EuclideanDistance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Standardize;
import weka.gui.hierarchyvisualizer.HierarchyVisualizer;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.*;

import data.Lang;
import services.commons.Formatting;
import services.commons.VectorAlgebra;

public class CommunityUtils {

    public static List<Participant> filterParticipants(Community community){
        return community.getParticipants()
                .parallelStream()
                .filter(p -> p.getIndices().get(CSCLIndices.OUTDEGREE) != 0)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public static void processDocumentCollectionForClustering(String rootPath, boolean needsAnonymization,
                                                              boolean useTextualComplexity, Date startDate, Date endDate, int monthIncrement, int dayIncrement) {
        Community dc = Community.loadMultipleConversations(rootPath, Lang.en, needsAnonymization, startDate, endDate,
                monthIncrement, dayIncrement);
        if (dc != null) {
            dc.computeMetrics(useTextualComplexity, true, true);
            File f = new File(rootPath);
            exportCNAOnlyData(dc, rootPath+ "/clusterdata_" + f.getName() + ".csv",
                    Arrays.asList(CSCLIndices.INDEGREE, CSCLIndices.OUTDEGREE));
            hierarchicalClustering(dc, rootPath + "/clustered_results_" + f.getName() + ".csv");
            dc.generateParticipantView(rootPath + "/" + f.getName() + "_participants.pdf");
        }
    }

    public static void exportCNAOnlyData(Community community, String pathToFile, List<CSCLIndices> csclIndicesList) {
        try {
            File output = new File(pathToFile);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"),
                    32768);

            for (CSCLIndices CSCLindex : csclIndicesList) {
                out.write("Participant," + CSCLindex.getDescription() + "(" + CSCLindex.getAcronym() + ")");
            }

            for (int index = 0; index < community.getParticipants().size(); index++) {
                Participant p = community.getParticipants().get(index);
                if(p.getIndices().get(CSCLIndices.OUTDEGREE) != 0) {
                    out.write("\n");
                    out.write(p.getName().replaceAll(",", "").replaceAll("\\s+", " "));
                    for (CSCLIndices CSCLindex : csclIndicesList) {
                        out.write("," + Formatting.formatNumber(p.getIndices().get(CSCLindex)));
                    }

                }
            }
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void hierarchicalClustering(Community community, String pathToFile){
        List<Participant> filteredParticipants = CommunityUtils.filterParticipants(community);
        System.out.println(filteredParticipants.size());
        System.out.println(filteredParticipants.toString());

        ClusterCommunity.performAglomerativeClusteringForCSCL(filteredParticipants, pathToFile);
    }


    public static List<ParticipantNormalized> normalizeParticipantsData(List<Participant> participants){

        double[] indegree = new double[participants.size()];
        double[] outdegree = new double[participants.size()];
        double[] eccentricity = new double[participants.size()];


        for(int i = 0; i < participants.size(); i++){
            indegree[i] = participants.get(i).getIndices().get(CSCLIndices.INDEGREE);
            outdegree[i] = participants.get(i).getIndices().get(CSCLIndices.OUTDEGREE);
            eccentricity[i] = participants.get(i).getIndices().get(CSCLIndices.ECCENTRICITY);
        }

        double[] normalizedIndegree = VectorAlgebra.softmax(indegree);
        double[] normalizedOutdegree = VectorAlgebra.softmax(outdegree);
        double[] normalizedEccentricity = VectorAlgebra.softmax(eccentricity);

        List<ParticipantNormalized> normalizeds = new ArrayList<>();
        for(int i = 0; i < participants.size(); i++){
            ParticipantNormalized p = new ParticipantNormalized(normalizedIndegree[i], normalizedOutdegree[i]);
            p.setName(participants.get(i).getName());
            normalizeds.add(p);
        }

        return normalizeds;
    }



    public static void hierarchicClustering(Community community, String rootPath){
        try {
            List<Participant> filteredParticipants = CommunityUtils.filterParticipants(community);
            HierarchicalClusterer hc = new HierarchicalClusterer();

            hc.setOptions(new String[] {"-L", "WARD"});
            hc.setDebug(true);
            hc.setNumClusters(3);
            hc.setDistanceFunction(new EuclideanDistance());
            hc.setDistanceIsBranchLength(true);


            ArrayList<Attribute> attributes = new ArrayList<Attribute>();
            attributes.add(new Attribute("In-Degree"));
            attributes.add(new Attribute("Out-degree"));
            attributes.add(new Attribute("Eccentricity"));
            Instances data = new Instances("Participants", attributes, filteredParticipants.size());


            for(Participant p: filteredParticipants){
                DenseInstance denseInstance = new DenseInstance(1.0, new double[]{
                        Formatting.formatNumber(p.getIndices().get(CSCLIndices.INDEGREE)),
                        Formatting.formatNumber(p.getIndices().get(CSCLIndices.OUTDEGREE)),
                        Formatting.formatNumber(p.getIndices().get(CSCLIndices.ECCENTRICITY))});
                data.add(denseInstance);
            }

            Standardize normalize = new Standardize();
            normalize.setInputFormat(data);
            Instances normalizedData = Filter.useFilter(data, normalize);

            hc.buildClusterer(normalizedData);
            System.out.println(hc.graph());

//            for(int i = 0; i < normalizedData.numInstances(); i++){
//                System.out.println(normalizedData.get(i).value(0) + ", " + normalizedData.get(i).value(1) +
//                        ", " + normalizedData.get(i).value(2) + ", " + normalizedData.get(i).value(3));
//            }

            hc.setPrintNewick(false);
            System.out.println(hc.graph());

            JFrame mainFrame = new JFrame("Clustering CNA Test");
            mainFrame.setSize(600, 400);
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            Container content = mainFrame.getContentPane();
            content.setLayout(new GridLayout(1, 1));

            HierarchyVisualizer visualizer = new HierarchyVisualizer(hc.graph());
            content.add(visualizer);

            mainFrame.setVisible(false);


            processClusterData(hc, data, normalizedData, rootPath);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    public static void processClusterData(HierarchicalClusterer hc, Instances data, Instances normalizedData, String rootPath) throws Exception {
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();
        attributes.add(new Attribute("In-Degree"));
        attributes.add(new Attribute("Out-degree"));
        attributes.add(new Attribute("Eccentricity"));
        Instances dataCluster1 = new Instances("Clustered 1", attributes, data.numInstances());
        Instances dataCluster2 = new Instances("Clustered 2", attributes, data.numInstances());
        Instances dataCluster3 = new Instances("Clustered 3", attributes, data.numInstances());

        for(int i = 0; i < normalizedData.numInstances(); i++){
            int cluster = hc.clusterInstance(normalizedData.get(i));
            System.out.print(cluster + " " + data.get(i).value(0) + " " + data.get(i).value(1) +
                    " " + data.get(i).value(2) + " | ");
            for(double d: hc.distributionForInstance(normalizedData.get(i))){
                System.out.print(d + " ");
            }
            System.out.println();

            switch (cluster){
                case 0:
                    dataCluster1.add(data.get(i));
                    break;
                case 1:
                    dataCluster2.add(data.get(i));
                    break;
                case 2:
                    dataCluster3.add(data.get(i));
                    break;
                default:
                    break;

            }
        }

        exportClusteredDataToCsv(rootPath, hc, dataCluster1);
        exportClusteredDataToCsv(rootPath, hc, dataCluster2);
        exportClusteredDataToCsv(rootPath, hc, dataCluster3);
    }


    private static void exportClusteredDataToCsv(String pathToFile, HierarchicalClusterer hc, Instances instances) {
        try {
            File output = new File(pathToFile);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output, true), "UTF-8"),
                    32768);

            out.write(instances.relationName() + "\n");
            out.write("Mean - Indegree,Mean - Outdegree, Mean - Eccentricity\n");
            out.write(instances.meanOrMode(0) + "," + instances.meanOrMode(1) + "," + instances.meanOrMode(2) +
                    "," + instances.meanOrMode(2) + "\n");

            for (int i = 0; i < instances.size(); i++) {
                out.write(instances.get(i).value(0) + "," + instances.get(i).value(1) + "," +
                        instances.get(i).value(2) + "\n");
            }

            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {

        }
    }


}
