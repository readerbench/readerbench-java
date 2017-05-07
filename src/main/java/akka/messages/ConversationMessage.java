package akka.messages;

import com.readerbench.solr.entities.cscl.Conversation;

/**
 * Created by Dorinela on 5/3/2017.
 */
public class ConversationMessage {

    private Conversation conversation;
    private String path;

    public ConversationMessage(Conversation conversation) {
        this.conversation = conversation;
    }

    public ConversationMessage(Conversation conversation, String path) {
        this.conversation = conversation;
        this.path = path;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "ConversationMessage{" +
                "conversation=" + conversation +
                ", path='" + path + '\'' +
                '}';
    }
}
