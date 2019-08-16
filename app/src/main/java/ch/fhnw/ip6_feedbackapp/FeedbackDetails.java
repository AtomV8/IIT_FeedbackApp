package ch.fhnw.ip6_feedbackapp;

import java.sql.Timestamp;

public class FeedbackDetails extends FeedbackDetailsInterface {

    private boolean isComplaint;
    private boolean isIdea;
    private boolean isPraise;

    public FeedbackDetails() {
    }

    public FeedbackDetails(AppDetails appDetails, String text, Timestamp timestamp, boolean makePublic, boolean publishAnonymously, boolean isComplaint, boolean isIdea, boolean isPraise) {
        super(appDetails, text, timestamp, makePublic, publishAnonymously);
        this.isComplaint = isComplaint;
        this.isIdea = isIdea;
        this.isPraise = isPraise;
    }

    public boolean isComplaint() {
        return isComplaint;
    }

    public boolean isIdea() {
        return isIdea;
    }

    public boolean isPraise() {
        return isPraise;
    }

    public void setComplaint(boolean complaint) {
        isComplaint = complaint;
    }

    public void setIdea(boolean idea) {
        isIdea = idea;
    }

    public void setPraise(boolean praise) {
        isPraise = praise;
    }

}
