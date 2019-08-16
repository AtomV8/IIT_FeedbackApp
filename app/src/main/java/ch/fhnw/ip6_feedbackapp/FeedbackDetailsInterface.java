package ch.fhnw.ip6_feedbackapp;


import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public abstract class FeedbackDetailsInterface {

    // General feedback
    private AppDetails appDetails;
    private String text;
    private String timestamp;
    private int likes;

    // Privacy settings
    private boolean makePublic;
    private boolean publishAnonymously;

    public FeedbackDetailsInterface() {
    }

    public FeedbackDetailsInterface(AppDetails appDetails, String text, Timestamp timestamp, boolean makePublic, boolean publishAnonymously) {
        this.appDetails = appDetails;
        this.text = text;
        this.timestamp = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(timestamp);
        this.makePublic = makePublic;
        this.publishAnonymously = publishAnonymously;
        likes = 0;
    }

    public AppDetails getAppDetails() {
        return appDetails;
    }

    public String getText() {
        return text;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public boolean isMakePublic() {
        return makePublic;
    }

    public boolean isPublishAnonymously() {
        return publishAnonymously;
    }

    public int getLikes() {
        return likes;
    }

}
