package ch.fhnw.ip6_feedbackapp;

import java.sql.Timestamp;

public class RatingDetails extends FeedbackDetailsInterface {

    float rating;

    public RatingDetails() {
    }

    public RatingDetails(AppDetails appDetails, String text, Timestamp timestamp, boolean makePublic, boolean publishAnonymously, float rating) {
        super(appDetails, text, timestamp, makePublic, publishAnonymously);
        this.rating = rating;
    }

    public float getRating() {
        return rating;
    }
}
