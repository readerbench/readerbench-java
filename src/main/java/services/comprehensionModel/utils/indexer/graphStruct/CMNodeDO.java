package services.comprehensionModel.utils.indexer.graphStruct;

import data.Word;

public class CMNodeDO implements Comparable<CMNodeDO> {

	private Word word;
	private CMNodeType nodeType;
	private boolean isActive;
	
	public CMNodeDO(Word word, CMNodeType nodeType) {
		this.word = word;
		this.nodeType = nodeType;
		this.isActive = false;
	}

    public Word getWord() {
        return word;
    }

    public CMNodeType getNodeType() {
        return nodeType;
    }
	
    public boolean isActive() {
        return isActive;
    }

    public void activate() {
        isActive = true;
    }

    public void deactivate() {
        isActive = false;
    }
	
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