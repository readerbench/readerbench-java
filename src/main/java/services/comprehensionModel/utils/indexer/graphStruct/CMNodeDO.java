package services.comprehensionModel.utils.indexer.graphStruct;

import data.Word;

public class CMNodeDO implements Comparable<CMNodeDO> {
	public CMNodeType nodeType;
	public Word word;
	
	@Override
	public boolean equals(Object obj) {
		CMNodeDO node = (CMNodeDO) obj;
		return this.word.getLemma().equals(node.word.getLemma());
	}
	public int hashCode() {
        return this.word.getLemma().hashCode();
    }
	@Override
	public int compareTo(CMNodeDO otherNode) {
		return this.word.getLemma().compareTo(otherNode.word.getLemma());
	}
	public String toString() {
		return this.word.getLemma();
	}
}