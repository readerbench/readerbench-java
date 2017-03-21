package akka.messages;

import services.converters.lifeConverter.Dialog;

import java.util.List;

/**
 * Created by Dorinela on 3/21/2017.
 */
public class DialogMessage {

    private List<Dialog> dialogs;

    public DialogMessage(List<Dialog> dialogs) {
        this.dialogs = dialogs;
    }

    public List<Dialog> getDialogs() {
        return dialogs;
    }

    public void setDialogs(List<Dialog> dialogs) {
        this.dialogs = dialogs;
    }

    @Override
    public String toString() {
        return "DialogMessage{" +
                "dialogs=" + dialogs +
                '}';
    }
}
