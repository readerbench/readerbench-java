/**
 * 
 */
package services.cscl;

import org.apache.log4j.BasicConfigurator;

import data.cscl.Community;
import edu.cmu.lti.jawjaw.pobj.Lang;
import view.widgets.ReaderBenchView;
import webService.ReaderBenchServer;

/**
 * @author mihaidascalu
 *
 */
public class CommunityTest {
	public static void main(String[] args) {
		BasicConfigurator.configure();

		ReaderBenchServer.initializeDB();

		ReaderBenchView.adjustToSystemGraphics();

		Community.processAllFolders("resources/in/blogs_Nic/1 year", "", false, "resources/config/LSA/tasa_en",
				"resources/config/LDA/tasa_en", Lang.eng, true, true, null, null, 0, 7);
		// String path = "resources/in/MOOC/forum_posts&comments";
		// SerialCorpusAssessment.processCorpus(path,
		// "resources/config/LSA/tasa_lak_en",
		// "resources/config/LDA/tasa_lak_en", Lang.eng, true, false, null,
		// null, true);
		// Long startDate = 1383235200L;
		// Long startDate = 1383843600L; new Date(startDate * 1000)
		// Community.processDocumentCollection(path, false, null, null, 0, 7);
	}
}
