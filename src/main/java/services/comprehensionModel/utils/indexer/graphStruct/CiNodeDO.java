package services.comprehensionModel.utils.indexer.graphStruct;

import data.Word;

public class CiNodeDO implements Comparable<CiNodeDO> {
	public CiNodeType nodeType;
	public Word word;
	
	@Override
	public boolean equals(Object obj) {
		CiNodeDO node = (CiNodeDO) obj;
		return this.word.getLemma().equals(node.word.getLemma());
	}
	public int hashCode() {
        return this.word.getLemma().hashCode();
    }
	@Override
	public int compareTo(CiNodeDO otherNode) {
		return this.word.getLemma().compareTo(otherNode.word.getLemma());
	}
	public String toString() {
		return this.word.getLemma();
	}
}