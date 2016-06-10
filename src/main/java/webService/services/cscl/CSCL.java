package webService.services.cscl;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.AbstractDocumentTemplate.BlockTemplate;
import data.cscl.CSCLIndices;
import data.cscl.Conversation;
import data.cscl.Participant;
import services.commons.Formatting;
import webService.result.ResultCscl;
import webService.services.ConceptMap;

public class CSCL {
	
	public static org.w3c.dom.Document loadXMLFromString(String xml) throws Exception
	{
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    InputSource is = new InputSource(new StringReader(xml));
	    return builder.parse(is);
	}
	
	public static AbstractDocumentTemplate getConversationText(String conversationText) {
		
		AbstractDocumentTemplate contents = new AbstractDocumentTemplate();
		List<BlockTemplate> blocks = new ArrayList<BlockTemplate>();
		
		try {
			
			org.w3c.dom.Document dom = loadXMLFromString(conversationText);
			
			Element doc = dom.getDocumentElement();
			Element el = null;
			NodeList nl1 = null, nl2 = null;
	
			// reformat input accordingly to evaluation model
			nl1 = doc.getElementsByTagName("Turn");
			if (nl1 != null && nl1.getLength() > 0) {
				for (int i = 0; i < nl1.getLength(); i++) {
					el = (Element) nl1.item(i);
					BlockTemplate block = contents.new BlockTemplate();
					if (el.hasAttribute("nickname") && el.getAttribute("nickname").trim().length() > 0) {
						block.setSpeaker(el.getAttribute("nickname").trim());
					} else {
						block.setSpeaker("unregistered member");
					}
					
					nl2 = el.getElementsByTagName("Utterance");
					if (nl2 != null && nl2.getLength() > 0) {
						for (int j = 0; j < nl2.getLength(); j++) {
							el = (Element) nl2.item(j);
							if (el.getFirstChild() != null) {
								if (el.hasAttribute("time"))
									block.setTime(el.getAttribute("time"));
								if (el.hasAttribute("genid"))
									block.setId(Integer.parseInt(el.getAttribute("genid")));
								if (el.hasAttribute("ref")) {
									if (el.getAttribute("ref").isEmpty())
										block.setRefId(0);
									else
										try {
											block.setRefId(Integer.parseInt(el.getAttribute("ref")));
										} catch (Exception e) {
											block.setRefId(0);
										}
								}
								// String text = StringEscapeUtils.escapeXml(el
								// .getFirstChild().getNodeValue());
								String text = el.getFirstChild().getNodeValue();
								block.setContent(text);
								if (text.length() > 0
										&& !el.getFirstChild().getNodeValue().trim().equals("joins the room")
										&& !el.getFirstChild().getNodeValue().trim().equals("leaves the room")) {
									blocks.add(block);
								}
							}
						}
					}
				}
			}
			
			contents.setBlocks(blocks);
			return contents;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	public static ResultCscl getAll(AbstractDocument conversationDocument, Conversation c, double threshold) {
		
		return new ResultCscl(
				ConceptMap.getTopics(conversationDocument, threshold),
				ParticipantInteraction.buildParticipantGraph(c),
				ParticipantEvolution.buildParticipantEvolutionData(c),
				Collaboration.buildSocialKBGraph(c),
				Collaboration.buildVoiceOverlapGraph(c),
				CSCL.getCsclIndices(c),
				CSCL.getCsclIndicesDescription(c)
			);
		
	}
	
	public static HashMap<String, HashMap<String, Double>> getCsclIndices(Conversation c) {
		
		HashMap<String, HashMap<String, Double>> indices = new HashMap<String, HashMap<String, Double>>();
		
		// print participant statistics
		if (c.getParticipants().size() > 0) {
			//out.write("\nParticipant involvement and interaction\n");
			//out.write("Participant name");
			//for (CSCLIndices CSCLindex : CSCLIndices.values())
				//out.write(CSCLindex.getDescription());
			for (Participant p : c.getParticipants()) {
				HashMap<String, Double> hm = new HashMap<String, Double>();
				//out.write(p.getName().replaceAll(",", "").replaceAll("\\s+", " "));
				for (CSCLIndices index : CSCLIndices.values()) {
					//out.write("," + p.getIndices().get(index));
					hm.put(index.toString(), Formatting.formatNumber(p.getIndices().get(index)));
				}
				indices.put(p.getName(), hm);
			}
			// print interaction matrix
			//out.write("Interaction matrix\n");
			//for (Participant p : c.getParticipants())
				//out.write("," + p.getName().replaceAll(",", "").replaceAll("\\s+", " "));
			//out.write("\n");
			/*Iterator<Participant> it = c.getParticipants().iterator();
			int i = 0;
			while (it.hasNext()) {
				Participant part = it.next();
				//out.write(part.getName().replaceAll(",", "").replaceAll("\\s+", " "));
				for (int j = 0; j < c.getParticipants().size(); j++) {
					//out.write("," + Formatting.formatNumber(c.getParticipantContributions()[i][j]));
				}
				i++;
				//out.write("\n");
			}*/
		}
		
		return indices;
		
	}
	
	public static HashMap<String, String> getCsclIndicesDescription(Conversation c) {
		
		HashMap<String, String> hm = new HashMap<String, String>();
		
		for (CSCLIndices index : CSCLIndices.values()) {
			hm.put(index.toString(), index.getDescription());
		}
		
		return hm;
	}

}