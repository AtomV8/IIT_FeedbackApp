package ch.fhnw.ip6_feedbackapp;

public class FeedbackObject {
    private FeedbackDetailsInterface feedbackDetails;
    private DeviceData deviceData;
    private String userid;
    private String username;

    public FeedbackObject(FeedbackDetailsInterface feedbackDetails, DeviceData deviceData, String uid, String username) {
        this.feedbackDetails = feedbackDetails;
        this.deviceData = deviceData;
        this.userid = uid;
        this.username = username;
    }

    public FeedbackDetailsInterface getFeedbackDetails() {
        return feedbackDetails;
    }

    public void setFeedbackDetails(FeedbackDetailsInterface feedbackDetails) {
        this.feedbackDetails = feedbackDetails;
    }

    public DeviceData getDeviceData() {
        return deviceData;
    }

    public void setDeviceData(DeviceData deviceData) {
        this.deviceData = deviceData;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}


