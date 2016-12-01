package gma;

import data.discourse.Keyword;
import gma.models.Cell;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import view.widgets.chat.models.WikiResult;

public class WikiSearchUtils {

	private static int MAX_SEARCH_RESULTS_PER_SEARCH = 3;

	public static ArrayList<WikiResult> gmaWikiSearch(
			ArrayList<Cell> conceptsToSearch) {

		ArrayList<WikiResult> wikiResults = new ArrayList<WikiResult>();

		// (1) search for base concepts separately
		for (Cell cell : conceptsToSearch) {
			mergeResults(wikiResults, search(cell.word.getLemma()));
		}

		// (2) run a search with the full list of input concepts
		String searchInput = "";
		for (Cell cell : conceptsToSearch) {
			searchInput += " " + cell.word.getLemma();
		}
		mergeResults(wikiResults, search(searchInput));

		// (3) search for base concepts + related concepts found by the GMA
		// algorithm
		for (Cell cell : conceptsToSearch) {
			searchInput = cell.word.getLemma();
			if (cell.getCorrelatedCells() != null) {
				for (Cell corelatedCell : cell.getCorrelatedCells()) {
					searchInput += " " + corelatedCell.word.getLemma();
				}
				mergeResults(wikiResults, search(searchInput));
			}
		}

		return wikiResults;

	}
	
	public static ArrayList<WikiResult> baseWikiSearch(List<Keyword> conceptsToSearch){
		ArrayList<WikiResult> wikiResults = new ArrayList<WikiResult>();

		// (1) search for base concepts separately
		for (Keyword topic : conceptsToSearch) {
			mergeResults(wikiResults, search(topic.getWord().getLemma()));
		}

		// (2) run a search with the full list of input concepts
		String searchInput = "";
		for (Keyword topic : conceptsToSearch) {
			searchInput += " " + topic.getWord().getLemma();
		}
		mergeResults(wikiResults, search(searchInput));
		
		return wikiResults;

	}

	private static ArrayList<WikiResult> mergeResults(
			ArrayList<WikiResult> wikiResults, ArrayList<WikiResult> newResults) {
		boolean match;
		for (WikiResult newRes : newResults) {
			match = false;
			for (WikiResult res : wikiResults) {
				if (newRes.equals(res)) {
					match = true;
					break;
				}
			}

			if (!match) {
				wikiResults.add(newRes);
			}

		}

		return wikiResults;
	}

	// NO-UCD
	private ArrayList<String> getSearchedConcepts(ArrayList<Cell> inputCells) {
		ArrayList<String> conceptsToSearch = new ArrayList<String>();
		String lemma;
		for (Cell cell : inputCells) {
			lemma = cell.word.getLemma();
			if (!conceptsToSearch.contains(lemma)) {
				conceptsToSearch.add(lemma);
			}
			if (cell.getCorrelatedCells() != null) {
				for (Cell relatedCell : cell.getCorrelatedCells()) {
					lemma = relatedCell.word.getLemma();
					if (!conceptsToSearch.contains(lemma)) {
						conceptsToSearch.add(lemma);
					}
				}
			}

		}

		return conceptsToSearch;
	}

	private static ArrayList<WikiResult> search(String conceptToSearch) {
		ArrayList<String> singleItemArray = new ArrayList<String>();
		singleItemArray.add(conceptToSearch);
		return search(singleItemArray);
	}

	private static ArrayList<WikiResult> search(
			ArrayList<String> conceptsToSearch) {
		ArrayList<WikiResult> results = null;

		String url = getSearchUrl(conceptsToSearch);

		URL obj;
		try {
			obj = new URL(url);

			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// optional default is GET
			con.setRequestMethod("GET");

			// add request header
			// con.setRequestProperty("User-Agent", USER_AGENT);

			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + url);
//			System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			//System.out.println(response.toString());
			results = getResultsFromJson(response.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO: handle exception
		}

		return results;
	}

	private static String getSearchUrl(ArrayList<String> conceptsToSearch) {
		// "http://en.wikipedia.org/w/api.php?action=query&titles=bethoven|mozart|book|Ludwig&continue=";
		// https://en.wikipedia.org/w/api.php?action=query&format=json&list=search&continue=&srsearch=
		String charset = "UTF-8";
		String url = "https://en.wikipedia.org/w/api.php?action=query&format=json&list=search&continue=&srsearch=";
		try {
			url += URLEncoder.encode(conceptsToSearch.get(0), charset);
			for (int i = 1; i < conceptsToSearch.size(); i++) {
				url += " "
						+ URLEncoder.encode(conceptsToSearch.get(i), charset);
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(url);
		return url;
	}

	private static ArrayList<WikiResult> getResultsFromJson(String jsonString)
			throws JSONException {
		JSONObject jsonObj = new JSONObject(jsonString);
		JSONArray resultsJsonArray = (jsonObj.getJSONObject("query"))
				.getJSONArray("search");
		ArrayList<WikiResult> results = new ArrayList<WikiResult>();
		for (int i = 0; i < MAX_SEARCH_RESULTS_PER_SEARCH; i++) {
			if (i >= resultsJsonArray.length()) {
				break;
			}
			String title = ((JSONObject) resultsJsonArray.get(i))
					.getString("title");
			String snippet = ((JSONObject) resultsJsonArray.get(i))
					.getString("snippet");
			results.add(new WikiResult(title, snippet));

		}

		return results;
	}

	// private void getValidConceptsFromJson(String jsonString)
	// throws JSONException {
	// JSONObject jsonObj = new JSONObject(jsonString);
	// JSONObject pagesObj = (jsonObj.getJSONObject("query"))
	// .getJSONObject("pages");
	// validConcepts = new ArrayList<String>();
	//
	// Iterator<?> keys = pagesObj.keys();
	//
	// while (keys.hasNext()) {
	// String key = (String) keys.next();
	// if (!key.equalsIgnoreCase("-1")
	// && pagesObj.get(key) instanceof JSONObject) {
	// JSONObject pageObj = pagesObj.getJSONObject(key);
	// validConcepts.add(pageObj.getString("title"));
	// }
	// }
	// }

	// private void getResultsLinks() {
	// resultsLinks = new ArrayList<String>();
	// for (String conceptLema : validConcepts) {
	// resultsLinks.add("http://en.wikipedia.org/wiki/" + conceptLema);
	// }
	// }

}
