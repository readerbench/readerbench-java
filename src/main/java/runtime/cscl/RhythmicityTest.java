/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package runtime.cscl;

import data.Block;
import data.Lang;
import data.Word;
import data.cscl.Conversation;
import data.cscl.Participant;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import services.complexity.ComplexityIndices;
import static services.complexity.DataGathering.writeHeader;
import services.discourse.cohesion.CohesionGraph;
import services.semanticModels.ISemanticModel;
import static view.widgets.ReaderBenchView.LOGGER;

/**
 *
 * @author Cioaca Valentin-Sergiu
 */
public class RhythmicityTest {
    public static void processConversation(String processingPath, String saveLocation,
            String folderName, boolean writeHeader, List<ISemanticModel> models, Lang lang,
            boolean usePOSTagging, boolean computeDialogism) throws IOException {
        File dir = new File(processingPath);

        if (!dir.exists()) {
            throw new IOException("Inexistent Folder: " + dir.getPath());
        }

        File[] files = dir.listFiles((File pathname) -> {
            return pathname.getName().toLowerCase().endsWith(".xml");
        });

        if (writeHeader & folderName.equals("")) {
            writeHeader(saveLocation, lang, false);
        }

        for (File file : files) {
            LOGGER.log(Level.INFO, "Processing {0} file", file.getName());
            // Create file
            Conversation c = Conversation.load(file, models, lang, usePOSTagging);
            System.out.println("File: " + file + " participants: " + c.getParticipants());
            // determine complexity indices
            for (Participant p : c.getParticipants()) {
                // establish minimum criteria
                int noContentWords = 0;
                for (Block b : p.getSignificantContributions().getBlocks()) {
                    if (b != null) {
                        for (Entry<Word, Integer> entry : b.getWordOccurences().entrySet()) {
                            noContentWords += entry.getValue();
                        }
                    }
                }

                if (p.getSignificantContributions().getBlocks().size() >= 3 && noContentWords >= 50) {
                    // build cohesion graph for additional indices
                    CohesionGraph.buildCohesionGraph(p.getSignificantContributions());
                    ComplexityIndices.computeComplexityFactors(p.getSignificantContributions());
                }
            }
        }

    }
    
}
