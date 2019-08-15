package ch.fhnw.ip6_feedbackapp;

import com.google.firebase.auth.FirebaseUser;

public class FeedbackObject {
    private FeedbackDetails feedbackDetails;
    private DeviceData deviceData;
    private String userid;

    public FeedbackObject(FeedbackDetails feedbackDetails, DeviceData deviceData, String uid) {
        this.feedbackDetails = feedbackDetails;
        this.deviceData = deviceData;
        this.userid = uid;
    }

    private boolean isRating(){
        return feedbackDetails.isRating;
    }
}


