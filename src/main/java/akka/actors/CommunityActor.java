package akka.actors;

import akka.actor.UntypedActor;
import akka.messages.CommunityMessage;
import akka.messages.ConversationMessage;
import akka.messages.ConversationResponseMessage;
import com.readerbench.solr.entities.cscl.Community;
import com.readerbench.solr.entities.cscl.Contribution;
import com.readerbench.solr.entities.cscl.Conversation;
import com.readerbench.solr.services.SolrService;
import data.AbstractDocument;
import data.Block;
import data.Lang;
import data.Word;
import data.cscl.CSCLCriteria;
import data.cscl.CSCLIndices;
import data.cscl.Participant;
import data.cscl.Utterance;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;
import services.commons.VectorAlgebra;
import services.complexity.ComplexityIndices;
import services.discourse.CSCL.ParticipantEvaluation;
import services.discourse.cohesion.CohesionGraph;
import services.semanticModels.ISemanticModel;
import services.semanticModels.SimilarityType;
import services.solr.TestActors;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Created by Dorinela on 3/17/2017.
 */
public class CommunityActor extends UntypedActor{

    private static final Logger LOGGER = LoggerFactory.getLogger(CommunityActor.class);

    private static final String START_PROCESSING = "START_PROCESSING";

    private static final String SOLR_ADDRESS = "http://141.85.232.56:8983/solr/";
    private static final String SOLR_COLLECTION = "community";
    SolrService solrService = new SolrService(SOLR_ADDRESS, SOLR_COLLECTION, Integer.MAX_VALUE);

    List<AbstractDocument> abstractDocuments = new ArrayList<>();
    private static int CONVERSATION_NUMBER = 0;
    private static String FILENAME;
    private static String PATH = "resources/out";

    @Override
    public void preStart() {
        Long delay = computeDelayOfJob();

//        getContext().system().scheduler().scheduleOnce(
//                Duration.create(delay, TimeUnit.MILLISECONDS),
//                getSelf(), START_PROCESSING, getContext().dispatcher(), null);
    }

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof CommunityMessage) {
            LOGGER.info("Received CommunityMessage ...");
            String communityName = ((CommunityMessage) message).getCommunity();
            FILENAME = communityName + ".csv";

            /**
             * get conversations for a community
             */
            List<Conversation> conversations = solrService.getConversationsForCommunity(communityName);

            //todo - delete this because the participantAliasName should be in SOLR.
            int i = 1;
            for (Conversation conversation : conversations) {
                i = 1;
                for (Contribution c : conversation.getContributions()) {
                    c.setParticipantAliasName("Test " + i++);
                }

            }

            CONVERSATION_NUMBER = conversations.size();
            LOGGER.info("The number of conversations for community {} are {}", communityName, CONVERSATION_NUMBER);


            String LSA_PATH = "resources/config/EN/LSA/TASA_LAK";
            String LDA_PATH = "resources/config/EN/LDA/TASA_LAK";
            Lang LANGUAGE = Lang.en;
            Map<SimilarityType, String> modelPaths = new EnumMap<>(SimilarityType.class);
            modelPaths.put(SimilarityType.LSA, LSA_PATH);
            modelPaths.put(SimilarityType.LDA, LDA_PATH);
            //modelPaths.put(SimilarityType.WORD2VEC, pathToWORD2VEC);

            try {
                Long start = System.currentTimeMillis();

                LOGGER.info("Load vector models.");
                List<ISemanticModel> models = SimilarityType.loadVectorModels(modelPaths, LANGUAGE);

                for (Conversation conversation : conversations) {
                    ConversationMessage conversationMessage = new ConversationMessage(conversation, "resources/out");
                    /**
                     * send ConversationMessage to ConversationActor to process it
                     */
                    LOGGER.info("Send ConversationMessage to ConversationActor to process it ... ");
                    TestActors.akkaActorSystem.conversationActor.tell(conversationMessage, self());

                }


            } catch (Exception e) {
                LOGGER.info("Error in loading vector models!!!");
            }


        } else if (message instanceof ConversationResponseMessage) {
            LOGGER.info("Received ConversationResponseMessage ...");
            ConversationResponseMessage conversationResponseMessage = (ConversationResponseMessage) message;

            abstractDocuments.add(conversationResponseMessage.getAbstractDocument());
            if (abstractDocuments.size() == CONVERSATION_NUMBER) {
                LOGGER.info("End processing all conversations ...");
                LOGGER.info("Start processing document collection ...");
                processDocumentCollection(abstractDocuments, Lang.en, false, false, null, null, 0, 7);
                LOGGER.info("------------- End processing document collection --------- ");
            }

        } else {
            LOGGER.warn("Unhandled message.");
            unhandled(message);
        }

    }

    public void processDocumentCollection(List<AbstractDocument> abstractDocumentList, Lang lang,
                                                 boolean needsAnonymization, boolean useTextualComplexity, Date startDate,
                                                 Date endDate, int monthIncrement, int dayIncrement) {
        data.cscl.Community community = loadMultipleConversations(abstractDocumentList, lang, needsAnonymization, startDate,
                endDate, monthIncrement, dayIncrement);
        community.setPath(PATH);
        if (community != null) {
            community.computeMetrics(useTextualComplexity, true, true);
            community.export(PATH + "/" + FILENAME, true, true);
            //dc.generateParticipantView(rootPath + "/" + f.getName() + "_participants.pdf");
            //dc.generateParticipantViewD3(rootPath + "/" + f.getName() + "_d3.json");
            //community.generateParticipantViewSubCommunities("D:\\Facultate\\MASTER\\ReaderBench\\ReaderBench\\resources\\out\\" + "CallOfDuty_d3_");
            //community.generateConceptView("D:\\Facultate\\MASTER\\ReaderBench\\ReaderBench\\resources\\out\\" + "CallOfDuty_concepts.pdf");
        }
    }

    public data.cscl.Community loadMultipleConversations (List<AbstractDocument> abstractDocumentList, Lang lang,
                                                          boolean needsAnonymization, Date startDate, Date endDate,
                                                          int monthIncrement, int dayIncrement) {

        data.cscl.Community community = new data.cscl.Community(lang, needsAnonymization, startDate, endDate);
        for (AbstractDocument abstractDocument : abstractDocumentList) {
            community.getDocuments().add((data.cscl.Conversation) abstractDocument);
        }

        updateParticipantContributions(community);

        // create corresponding sub-communities
        Calendar cal = Calendar.getInstance();
        Date startSubCommunities = community.getFistContributionDate();
        cal.setTime(startSubCommunities);
        cal.add(Calendar.MONTH, monthIncrement);
        cal.add(Calendar.DATE, dayIncrement);
        Date endSubCommunities = cal.getTime();

        while (endSubCommunities.before(community.getLastContributionDate())) {
            community.getTimeframeSubCommunities()
                    .add(getSubCommunity(community, startSubCommunities, endSubCommunities));

            // update timeStamps
            startSubCommunities = endSubCommunities;
            cal.add(Calendar.MONTH, monthIncrement);
            cal.add(Calendar.DATE, dayIncrement);
            endSubCommunities = cal.getTime();
        }
        // create partial community with remaining contributions
        community.getTimeframeSubCommunities()
                .add(getSubCommunity(community, startSubCommunities, community.getLastContributionDate()));

        LOGGER.info("Finished creating {0} timeframe sub-communities spanning from {1} to {2}", new Object[]{community.getTimeframeSubCommunities().size(), community.getFistContributionDate(), community.getLastContributionDate()});

        return community;
    }

    private data.cscl.Community getSubCommunity(data.cscl.Community community, Date startSubCommunities,
                                                       Date endSubCommunities) {
        data.cscl.Community subCommunity = new data.cscl.Community(community.getPath(), community.getLanguage(),
                community.needsAnonymization(), startSubCommunities, endSubCommunities);
        for (data.cscl.Conversation c : community.getDocuments()) {
            subCommunity.getDocuments().add(c);
        }
        updateParticipantContributions(subCommunity);
        computeMetrics(subCommunity, false, false, false);
        return subCommunity;
    }

    public void computeMetrics(data.cscl.Community community, boolean useTextualComplexity, boolean modelTimeEvolution,
                               boolean additionalInfo) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
        if (community.getStartDate() != null && community.getEndDate() != null && community.getParticipants() != null
                && community.getParticipants().size() > 0) {
            LOGGER.info("Processing timeframe between {0} and {1} having {2} participants ...",
                    new Object[]{dateFormat.format(community.getStartDate()), dateFormat.format(community.getEndDate()),
                            community.getParticipants().size()});
        }

//        String fileName;
//        if (community.getStartDate() != null && community.getEndDate() != null) {
//            fileName = path + "/graph_" + dateFormat.format(startDate) + "_" + dateFormat.format(endDate);
//        } else {
//            fileName = path + "/graph_" + System.currentTimeMillis();
//        }

        //ParticipantEvaluation.performSNA(participants, participantContributions, true, fileName + ".pdf");
        // update surface statistics
        for (AbstractDocument d : community.getDocuments()) {
            Participant p = null;
            for (int i = 0; i < d.getBlocks().size(); i++) {
                if (d.getBlocks().get(i) != null) {
                    if (p == null) {
                        p = ((Utterance) d.getBlocks().get(i)).getParticipant();
                        Participant participantToUpdate = community.getParticipants().get(community.getParticipants().indexOf(p));
                        participantToUpdate.getIndices().put(CSCLIndices.NO_NEW_THREADS,
                                participantToUpdate.getIndices().get(CSCLIndices.NO_NEW_THREADS) + 1);
                        participantToUpdate.getIndices().put(CSCLIndices.NEW_THREADS_OVERALL_SCORE,
                                participantToUpdate.getIndices().get(CSCLIndices.NEW_THREADS_OVERALL_SCORE)
                                        + d.getScore());
                        participantToUpdate.getIndices().put(CSCLIndices.NEW_THREADS_CUMULATIVE_SOCIAL_KB,
                                participantToUpdate.getIndices().get(CSCLIndices.NEW_THREADS_CUMULATIVE_SOCIAL_KB)
                                        + VectorAlgebra.sumElements(((data.cscl.Conversation) d).getSocialKBEvolution()));
                        participantToUpdate.getIndices().put(CSCLIndices.NEW_THREADS_INTER_ANIMATION_DEGREE,
                                participantToUpdate.getIndices().get(CSCLIndices.NEW_THREADS_INTER_ANIMATION_DEGREE)
                                        + VectorAlgebra.sumElements(((data.cscl.Conversation) d).getVoicePMIEvolution()));
                        participantToUpdate.getIndices().put(CSCLIndices.AVERAGE_LENGTH_NEW_THREADS,
                                participantToUpdate.getIndices().get(CSCLIndices.AVERAGE_LENGTH_NEW_THREADS)
                                        + d.getBlocks().get(i).getText().length());
                        break;
                    }
                }
            }
        }

        community.getParticipants().stream().filter((p) -> (p.getIndices().get(CSCLIndices.NO_NEW_THREADS) != 0)).forEach((p) -> {
            p.getIndices().put(CSCLIndices.AVERAGE_LENGTH_NEW_THREADS,
                    p.getIndices().get(CSCLIndices.AVERAGE_LENGTH_NEW_THREADS)
                            / p.getIndices().get(CSCLIndices.NO_NEW_THREADS));
        });

       // export(fileName + ".csv", modelTimeEvolution, additionalInfo);

        if (useTextualComplexity) {

            // determine complexity indices
            for (Participant p : community.getParticipants()) {
                // establish minimum criteria
                int noContentWords = 0;
                for (Block b : p.getSignificantContributions().getBlocks()) {
                    if (b != null) {
                        for (Map.Entry<Word, Integer> entry : b.getWordOccurences().entrySet()) {
                            noContentWords += entry.getValue();
                        }
                    }
                }

                if (p.getSignificantContributions().getBlocks().size() >= community.getMinNoContributions() && noContentWords >= community.getMinNoContentWords()) {
                    // build cohesion graph for additional indices
                    CohesionGraph.buildCohesionGraph(p.getSignificantContributions());
                    ComplexityIndices.computeComplexityFactors(p.getSignificantContributions());
                }
            }
        }

        if (modelTimeEvolution) {
            modelEvolution(community);
        }
    }

    public void modelEvolution(data.cscl.Community community) {
        LOGGER.info("Modeling time evolution for {0} participants ...", community.getParticipants().size());
        for (CSCLIndices index : CSCLIndices.values()) {
            if (index.isUsedForTimeModeling()) {
                LOGGER.info("Modeling based on {0}", index.getDescription());
                int no = 0;
                for (Participant p : community.getParticipants()) {
                    // model time evolution of each participant
                    List<data.cscl.Community> timeframeSubCommunities = community.getTimeframeSubCommunities();
                    double[] values = new double[timeframeSubCommunities.size()];
                    for (int i = 0; i < timeframeSubCommunities.size(); i++) {
                        int localParticipantIndex = timeframeSubCommunities.get(i).getParticipants().indexOf(p);
                        if (localParticipantIndex != -1) {
                            values[i] = timeframeSubCommunities.get(i).getParticipants().get(localParticipantIndex)
                                    .getIndices().get(index);
                        }
                    }
                    if (++no % 100 == 0) {
                        LOGGER.info("Finished evaluating the time evolution of {0} participants", no);
                    }
                    for (CSCLCriteria crit : CSCLCriteria.values()) {
                        p.getLongitudinalIndices().put(
                                new AbstractMap.SimpleEntry<>(index, crit),
                                CSCLCriteria.getValue(crit, values));
                    }
                }
            }
        }
    }

    /**
     * Update participant contributions
     * @param community
     */
    private void updateParticipantContributions(data.cscl.Community community) {
        for (data.cscl.Conversation c : community.getDocuments()) {
            // update the community correspondingly
            for (Participant p : c.getParticipants()) {
                if (p.getContributions().getBlocks() != null && !p.getContributions().getBlocks().isEmpty()) {
                    int index = community.getParticipants().indexOf(p);
                    Participant participantToUpdate;
                    if (index >= 0) {
                        participantToUpdate = community.getParticipants().get(index);
                    } else {
                        participantToUpdate = new Participant(p.getName(), p.getAlias(), c);
                        community.getParticipants().add(participantToUpdate);
                    }

                    for (Block b : p.getContributions().getBlocks()) {
                        Utterance u = (Utterance) b;
                        // select contributions in imposed timeframe
                        if (u != null && u.isEligible(community.getStartDate(), community.getEndDate())) {
                            // determine first timestamp of considered contributions
                            if (community.getFistContributionDate() == null) {
                                community.setFistContributionDate(u.getTime());
                                LOGGER.info("Please check first contribution");
                            }
                            if (u.getTime().before(community.getFistContributionDate())) {
                                community.setFistContributionDate(u.getTime());
                            }
                            Calendar date = new GregorianCalendar(2010, Calendar.JANUARY, 1);
                            if (u.getTime().before(date.getTime())) {
                                LOGGER.info("Incorrect time! {0} / {1} : {2}", new Object[]{c.getPath(), u.getIndex(), u.getTime()});
                            }
                            if (u.getTime().after(new Date())) {
                                LOGGER.info("Incorrect time! {0} / {1} : {2}", new Object[]{c.getPath(), u.getIndex(), u.getTime()});
                            }

                            if (community.getLastContributionDate() == null) {
                                community.setLastContributionDate(u.getTime());
                            }
                            if (u.getTime().after(community.getLastContributionDate())) {
                                community.setLastContributionDate(u.getTime());
                            }
                            b.setIndex(-1);
                            Block.addBlock(participantToUpdate.getContributions(), b);
                            if (b.isSignificant()) {
                                Block.addBlock(participantToUpdate.getSignificantContributions(), b);
                            }

                            participantToUpdate.getIndices().put(CSCLIndices.NO_CONTRIBUTION,
                                    participantToUpdate.getIndices().get(CSCLIndices.NO_CONTRIBUTION) + 1);

                            for (Map.Entry<Word, Integer> entry : u.getWordOccurences().entrySet()) {
                                if (entry.getKey().getPOS() != null) {
                                    if (entry.getKey().getPOS().startsWith("N")) {
                                        participantToUpdate.getIndices().put(CSCLIndices.NO_NOUNS,
                                                participantToUpdate.getIndices().get(CSCLIndices.NO_NOUNS)
                                                        + entry.getValue());
                                    }
                                    if (entry.getKey().getPOS().startsWith("V")) {
                                        participantToUpdate.getIndices().put(CSCLIndices.NO_VERBS,
                                                participantToUpdate.getIndices().get(CSCLIndices.NO_VERBS)
                                                        + entry.getValue());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        double[][] participantContributions = new double[community.getParticipants().size()][community.getParticipants().size()];

        for (data.cscl.Conversation d : community.getDocuments()) {
            // determine strength of links
            for (int i = 0; i < d.getBlocks().size(); i++) {
                Utterance u = (Utterance) d.getBlocks().get(i);
                // select contributions in imposed timeframe
                if (u != null && u.isEligible(community.getStartDate(), community.getEndDate())) {
                    Participant p1 = u.getParticipant();
                    int index1 = community.getParticipants().indexOf(p1);
                    if (index1 >= 0) {
                        // participantContributions[index1][index1] += d
                        // .getBlocks().get(i).getCombinedScore();
                        Participant participantToUpdate = community.getParticipants().get(index1);
                        participantToUpdate.getIndices().put(CSCLIndices.SCORE,
                                participantToUpdate.getIndices().get(CSCLIndices.SCORE) + u.getScore());
                        participantToUpdate.getIndices().put(CSCLIndices.PERSONAL_KB,
                                participantToUpdate.getIndices().get(CSCLIndices.PERSONAL_KB) + u.getPersonalKB());
                        participantToUpdate.getIndices().put(CSCLIndices.SOCIAL_KB,
                                participantToUpdate.getIndices().get(CSCLIndices.SOCIAL_KB) + u.getSocialKB());

                        for (int j = 0; j < i; j++) {
                            if (d.getPrunnedBlockDistances()[i][j] != null) {
                                Participant p2 = ((Utterance) d.getBlocks().get(j)).getParticipant();
                                int index2 = community.getParticipants().indexOf(p2);
                                if (index2 >= 0) {
                                    // model knowledge building effect
                                    double addedKB = d.getBlocks().get(i).getScore() * d.getPrunnedBlockDistances()[i][j].getCohesion();
                                    participantContributions[index1][index2] += addedKB;
                                }
                            }
                        }
                    }
                }
            }

            community.setParticipantContributions(participantContributions);

            for (Participant p : d.getParticipants()) {
                if (community.getParticipants().contains(p)) {
                    Participant participantToUpdate = community.getParticipants().get(community.getParticipants().indexOf(p));
                    participantToUpdate.getIndices().put(CSCLIndices.INTER_ANIMATION_DEGREE,
                            participantToUpdate.getIndices().get(CSCLIndices.INTER_ANIMATION_DEGREE)
                                    + p.getIndices().get(CSCLIndices.INTER_ANIMATION_DEGREE));
                }
            }
        }
    }

    /**
     * compute the time until the job starts
     *
     * In this case the job will start every day at 12:00 PM
     *
     * @return
     */
    private Long computeDelayOfJob() {
        GregorianCalendar gCalendar = new GregorianCalendar();
        gCalendar.set(GregorianCalendar.HOUR_OF_DAY, 23);
        gCalendar.set(GregorianCalendar.MINUTE, 58);
        gCalendar.set(GregorianCalendar.SECOND, 0);
        Long delay = gCalendar.getTimeInMillis() - System.currentTimeMillis();

        if (delay < 0) {
            gCalendar.add(GregorianCalendar.DAY_OF_MONTH, 1);
            delay = gCalendar.getTimeInMillis() - System.currentTimeMillis();
        }

        return  delay;
    }
}
