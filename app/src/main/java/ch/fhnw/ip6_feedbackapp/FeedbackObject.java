package ch.fhnw.ip6_feedbackapp;

import com.google.firebase.auth.FirebaseUser;

public class FeedbackObject {
    private FeedbackDetails feedbackDetails;
    private DeviceData deviceData;
    private FirebaseUser user;

    public FeedbackObject(FeedbackDetails feedbackDetails, DeviceData deviceData, FirebaseUser user) {
        this.feedbackDetails = feedbackDetails;
        this.deviceData = deviceData;
        this.user = user;
    }

    private boolean isRating(){
        return feedbackDetails.isRating;
    }
}


