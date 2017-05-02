package akka.messages;

import com.readerbench.solr.entities.cscl.Conversation;

/**
 * Created by Dorinela on 5/3/2017.
 */
public class ConversationMessage {

    private Conversation conversation;

    public ConversationMessage(Conversation conversation) {
        this.conversation = conversation;
    }

    public Conversation getConversation() {
        return conversation;
    }

    @Override
    public String toString() {
        return "ConversationMessage{" +
                "conversation=" + conversation +
                '}';
    }
}
