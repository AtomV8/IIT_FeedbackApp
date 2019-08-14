package ch.fhnw.ip6_feedbackapp;

import java.sql.Timestamp;

public class FeedbackDetails {

    // General feedback
    public AppDetails appDetails;
    public boolean isRating;
    public boolean isComplaint;
    public boolean isIdea;
    public boolean isPraise;
    public String text;
    public float starRating;
    public Timestamp timestamp;
    /* TODO: IMAGE */

    // Privacy settings
    public boolean makePublic;
    public boolean publishAnonymously;
    public DeviceData deviceData;

    // Rating constructor
    public FeedbackDetails(AppDetails appDetails, boolean isRating, String text, boolean makePublic, boolean publishAnonymously, float stars, Timestamp timestamp){
        this.appDetails = appDetails;
        this.isRating = isRating;
        this.text = text;
        this.makePublic = makePublic;
        this.publishAnonymously = publishAnonymously;
        this.starRating = stars;
        this.timestamp = timestamp;
    }

    // Feedback constructor
    public FeedbackDetails(AppDetails appDetails, boolean isComplaint, boolean isIdea, boolean isPraise, String text, boolean makePublic, boolean publishAnonymously, Timestamp timestamp){
        this.appDetails = appDetails;
        this.isComplaint = isComplaint;
        this.isIdea = isIdea;
        this.isPraise = isPraise;
        this.text = text;
        this.makePublic = makePublic;
        this.publishAnonymously = publishAnonymously;
        this.timestamp = timestamp;
        /* TODO: IMAGE */
    }

}
