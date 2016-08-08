package services.comprehensionModel.utils.indexer.graphStruct;

import data.Word;

public class CMNodeDO implements Comparable<CMNodeDO> {

    private final Word word;
    private final CMNodeType nodeType;
    private boolean isActive;

    public CMNodeDO(Word word, CMNodeType nodeType) {
        this.word = word;
        this.nodeType = nodeType;
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
        if (!(obj instanceof CMNodeDO)) {
            return false;
        }
        CMNodeDO node = (CMNodeDO) obj;
        return this.getWord().equals(node.getWord());
    }

    @Override
    public int hashCode() {
        return this.word.getLemma().hashCode();
    }

    @Override
    public int compareTo(CMNodeDO otherNode) {
        return this.word.getLemma().compareTo(otherNode.word.getLemma());
    }

    @Override
    public String toString() {
        return this.word.getLemma();
    }
}
