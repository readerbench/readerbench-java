package akka.messages;

/**
 * Created by Dorinela on 3/17/2017.
 */
public class CommunityMessage {

    private String community;

    public CommunityMessage(String community) {
        this.community = community;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    @Override
    public String toString() {
        return "CommunityMessage{" +
                "community='" + community + '\'' +
                '}';
    }
}
