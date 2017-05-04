package akka.messages;

import data.AbstractDocument;

/**
 * Created by Dorinela on 5/4/2017.
 */
public class ConversationResponseMessage {

    private AbstractDocument abstractDocument;

    public ConversationResponseMessage(AbstractDocument abstractDocument) {
        this.abstractDocument = abstractDocument;
    }

    public AbstractDocument getAbstractDocument() {
        return abstractDocument;
    }

    @Override
    public String toString() {
        return "ConversationResponseMessage{" +
                "abstractDocument=" + abstractDocument +
                '}';
    }
}


