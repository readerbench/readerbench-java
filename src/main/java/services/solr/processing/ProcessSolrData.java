package services.solr.processing;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.converters.lifeConverter.Dialog;
import services.converters.lifeConverter.Turn;
import services.converters.lifeConverter.Utterance;
import services.solr.SolrService;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Dorinela on 3/18/2017.
 */
public class ProcessSolrData {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessSolrData.class);

    private SolrService solrService = new SolrService();
    private static Integer MAX_RECORDS_NUMBER = 30000;

    /**
     * Get data by community name
     * @param communityName
     * @return
     */
    private SolrDocumentList getDataByCommunityName(String communityName) {
        String query = "subreddit:" + communityName;
        SolrDocumentList solrDocuments = solrService.getDataByQuery(query, MAX_RECORDS_NUMBER);
        LOGGER.info("The number of SolrDocuments for query: " + "\"" + query + "\"" + " is: " +
                solrDocuments.getNumFound());

        return solrDocuments;
    }

    /**
     * Get all conversations for a discussion thread
     * @param solrDocuments
     * @return
     */
    private Map<String, List<SolrDocument>> processSolrDocuments(SolrDocumentList solrDocuments) {

        Map<String, List<SolrDocument>> submissions = new HashMap<>();

        for (SolrDocument solrDocument : solrDocuments) {
            String submissionId = (String) solrDocument.get("submission_id");

            if (!submissions.containsKey(submissionId)) {
                submissions.put(submissionId, new ArrayList<>());
            }

            List<SolrDocument> docs = submissions.get(submissionId);
            docs.add(solrDocument);
            submissions.put(submissionId, docs);
        }
        return submissions;

    }

    /**
     * Generate Dialog objects from solr documents
     * @param submissions
     * @return
     */
    private List<Dialog> generateDialogs (Map<String, List<SolrDocument>> submissions) {
        List<Dialog> dialogs = new ArrayList<>();
        for (Map.Entry<String, List<SolrDocument>> entry : submissions.entrySet()) {
            Dialog dialog = createDialogFromSolrDocuments(entry.getValue());
            dialogs.add(dialog);
        }
        return dialogs;

    }

    /**
     * Create Dialog Object from solr documents
     * @param solrDocuments - solr documents which represent one discussion thread
     * @return - Dialog
     */
    private Dialog createDialogFromSolrDocuments(List<SolrDocument> solrDocuments) {

        Dialog dialog = new Dialog();
        List<Turn> turns = new ArrayList<>();
        Set<String> participants = new HashSet<>();
        int i = 1;
        for (SolrDocument solrDocument : solrDocuments) {
            Utterance utterance = new Utterance();
            utterance.setMesg(solrDocument.get("comment_text").toString());
            utterance.setTime(convertTimestampToStringDate(Long.valueOf
                    (solrDocument.get("comment_created").toString()) * 1000));
            utterance.setRef(getRefId(solrDocument, solrDocuments));
            utterance.setGenid(i ++);

            Turn turn = new Turn();
            turn.setId(solrDocument.get("comment_author").toString());
            turn.setUtter(utterance);

            participants.add(solrDocument.get("comment_author").toString());

            turns.add(turn);
        }

        dialog.setBody(turns);
        dialog.setId(participants.size());

        return dialog;
    }

    /**
     * Find the index of parent for a specific solr document
     * @param solrDocument
     * @param solrDocuments
     * @return - the index of parent
     */
    private int getRefId(SolrDocument solrDocument, List<SolrDocument> solrDocuments) {
        String parentId = solrDocument.get("parent_id").toString();
        SolrDocument result = solrDocuments.stream()
                .filter(document -> document.get("comment_id").toString().equals(parentId))
                .findFirst().get();
        return solrDocuments.indexOf(result);

    }

    /**
     * Convert timestamp to "yyyy-MM-dd HH:mm:ss" date format
     * @param timestamp
     * @return
     */
    private String convertTimestampToStringDate(Long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

}
