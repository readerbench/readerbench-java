package webService.services.essayFeedback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by robert botarleanu on 05-Jun-17.
 */
public class EFResult {
    public static final String TYPE_DOCUMENT = "doc";
    public static final String TYPE_PARAGRAPH = "par";
    public static final String TYPE_SENTENCE = "sen";

    private List<List<String>> analyzedText;
    private List<String> components;
    private List<String> feedbackMessages;
    private List<String> feedbackType;
    private List<Integer> feedbackIndexParagraph;
    private List<Integer> feedbackIndexPhrase;

    private List<Integer> feedbackSeverityLevel;
    public static final int NUMBER_OF_SEVERITY_LEVELS = 5;

    public EFResult() {
        super();
        this.analyzedText = new ArrayList<>();
        this.components = new ArrayList<>();
        this.feedbackMessages = new ArrayList<>();
        this.feedbackType = new ArrayList<>();
        this.feedbackIndexParagraph = new ArrayList<>();
        this.feedbackIndexPhrase = new ArrayList<>();
        this.feedbackSeverityLevel = new ArrayList<>();
    }

    public List<List<String>> getAnalyzedText() {
        return analyzedText;
    }

    public void setAnalyzedText(List<List<String>> analyzedText) {
        this.analyzedText = analyzedText;
    }

    public List<String> getComponents() {
        return components;
    }

    public void setComponents(List<String> components) {
        this.components = components;
    }

    public List<String> getFeedbackMessages() {
        return feedbackMessages;
    }

    public void setFeedbackMessages(List<String> feedbackMessages) {
        this.feedbackMessages = feedbackMessages;
    }

    public List<String> getFeedbackType() {
        return feedbackType;
    }

    public void setFeedbackType(List<String> feedbackType) {
        this.feedbackType = feedbackType;
    }

    public List<Integer> getFeedbackIndexParagraph() {
        return feedbackIndexParagraph;
    }

    public void setFeedbackIndexParagraph(List<Integer> feedbackIndexParagraph) {
        this.feedbackIndexParagraph = feedbackIndexParagraph;
    }

    public List<Integer> getFeedbackIndexPhrase() {
        return feedbackIndexPhrase;
    }

    public void setFeedbackIndexPhrase(List<Integer> feedbackIndexPhrase) {
        this.feedbackIndexPhrase = feedbackIndexPhrase;
    }

    public List<Integer> getFeedbackSeverityLevel() {
        return feedbackSeverityLevel;
    }

    public void setFeedbackSeverityLevel(List<Integer> feedbackSeverityLevel) {
        this.feedbackSeverityLevel = feedbackSeverityLevel;
    }

    public static int getNumberOfSeverityLevels() {
        return NUMBER_OF_SEVERITY_LEVELS;
    }
}
