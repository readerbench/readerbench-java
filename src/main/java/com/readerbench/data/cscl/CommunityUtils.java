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

package com.readerbench.data.cscl;

import com.readerbench.data.Block;
import com.readerbench.data.Lang;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.readerbench.services.commons.Formatting;
import com.readerbench.services.commons.VectorAlgebra;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.clusterers.HierarchicalClusterer;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.EuclideanDistance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Standardize;
import weka.gui.hierarchyvisualizer.HierarchyVisualizer;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class CommunityUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommunityUtils.class);
    
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
            //exportCNAOnlyData(dc, rootPath+ "/clusterdata_" + f.getName() + ".csv",
            //        Arrays.asList(CSCLIndices.INDEGREE, CSCLIndices.OUTDEGREE));
            hierarchicalClustering(dc, rootPath + "/clustered_results_" + f.getName() + ".csv", rootPath);
            //dc.generateParticipantView(rootPath + "/" + f.getName() + "_participants.pdf");
        }
    }
    
    public static void exportPeriphericParticipantsIntegrationToXls(String path, List<String> header, List<List<String>> content) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("SE Analysis");
        int columnCount = 0;
        int rowNo = 0;
        Row rowOut = sheet.createRow(rowNo++);
        
        for (String s : header) {
            Cell cell = rowOut.createCell(columnCount++);
            cell.setCellValue(s);
        }
        
        for (List<String> interaction : content) {
            columnCount = 0;
            rowOut = sheet.createRow(rowNo++);
            
            for (String elem : interaction) {
                Cell cell = rowOut.createCell(columnCount++);
                cell.setCellValue(elem);
            }
        }

        // TODO check if path/file exists
        File f = new File(path);
        try (FileOutputStream outputStream = new FileOutputStream(path + "/peripherical_interactions_" + f.getName() + ".xlsx")) {
            workbook.write(outputStream);
        }
        catch (Exception e) {
            e.printStackTrace();
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
    
    private static String trimStringTo1024(String str) {
        return str.length() > 1025 ? str.substring(0, 1024) : str;
    }
    
    private static List<String> buildInteractionList(String title, String beggingText, String comment, String refComment, String nameOfParticipant) {
        List<String> l = new ArrayList<>(5);
        l.add(trimStringTo1024(title));
        l.add(trimStringTo1024(beggingText));
        l.add(trimStringTo1024(comment));
        l.add(trimStringTo1024(refComment));
        l.add(nameOfParticipant);
        
        return l;
    }
    
    public static void extractPeriphericalUsersInteractions(List<Participant> groupedParticipants, Community community, String path) {
        List<String> header = Arrays.asList(new String[]{"BlogPostTitle", "BeginningText", "CommentOfPOI", "CommentOfMemberOfCommunity", "PeriphericalParticipant"});
        List<List<String>> content = new ArrayList<>();
        LOGGER.info(groupedParticipants.toString());
        LOGGER.info(community.getParticipants().toString());
        for (Conversation conversation : community.getDocuments()) {
            boolean flag = false;
            for (Participant participantInConversation : conversation.getParticipants()) {
                if (groupedParticipants.contains(participantInConversation)
                        && groupedParticipants.get(groupedParticipants.indexOf(participantInConversation)) != null
                        && groupedParticipants.get(groupedParticipants.indexOf(participantInConversation))
                        .getParticipantGroup() != null
                        && groupedParticipants.get(groupedParticipants.indexOf(participantInConversation))
                        .getParticipantGroup().equals(ParticipantGroup.PERIPHERAL)) {
                    for (Block block : participantInConversation.getContributions().getBlocks()) {
                        Utterance u = (Utterance) block;
                        // add the parent comment along with the comment of POI
                        if (u.getRefBlock() != null) {
                            Participant participantOfParentComment = ((Utterance)u.getRefBlock()).getParticipant();
                            if (groupedParticipants.contains(participantOfParentComment)
                                && !groupedParticipants.get(groupedParticipants.indexOf(participantOfParentComment)).getParticipantGroup().equals(ParticipantGroup.PERIPHERAL)) {
                                List<String> refComment = buildInteractionList(conversation.getTitleText(), conversation.getText(), u.getText(), u.getRefBlock().getText(), participantInConversation.getName());
                                content.add(refComment);
                            }
                        }
                        
                        // add all the children
                        for (Utterance childUtterance : conversation.getAllChildrenOfAnUtterance(u)) {
                            Participant childOfParentComment = childUtterance.getParticipant();
                            if (groupedParticipants.contains(childOfParentComment)
                                && !groupedParticipants.get(groupedParticipants.indexOf(childOfParentComment)).getParticipantGroup().equals(ParticipantGroup.PERIPHERAL)) {
                                List<String> childComment = buildInteractionList(conversation.getTitleText(), conversation.getText(), u.getText(), childUtterance.getText(), participantInConversation.getName());
                                content.add(childComment);
                            }
                        }
                    } 
                }
            }
        }
        
        CommunityUtils.exportPeriphericParticipantsIntegrationToXls(path, header, content);
    }

    public static void hierarchicalClustering(Community community, String pathToFile, String rootPath){
        List<Participant> filteredParticipants = CommunityUtils.filterParticipants(community);
        System.out.println(filteredParticipants.size());
        System.out.println(filteredParticipants.toString());

        ClusterCommunity.performAglomerativeClusteringForCSCL(filteredParticipants, pathToFile);
        LOGGER.info("Finished Clustering");
        extractPeriphericalUsersInteractions(filteredParticipants, community, rootPath);
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
