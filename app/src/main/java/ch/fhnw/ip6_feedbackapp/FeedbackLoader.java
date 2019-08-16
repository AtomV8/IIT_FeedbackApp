package ch.fhnw.ip6_feedbackapp;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class FeedbackLoader extends Thread {
    public final static int MODE_FEEDBACKACTIVITY = 0;
    public final static int MODE_MYFEEDBACKFRAGMENT = 1;

    AppDetails appDetails;
    String userid;
    int mode;
    FeedbackActivity callerFeedbackActivity;
    MyFeedbackFragment callerMyFeedbackFragment;
    String username;

    ArrayList<FeedbackEntryObject> allFeedbackForApp;

    // Constructor for usage in FeedbackActivity
    public FeedbackLoader(FeedbackActivity caller, AppDetails appDetails, String userid) {
        this.callerFeedbackActivity = caller;
        this.appDetails = appDetails;
        this.userid = userid;
        this.mode = MODE_FEEDBACKACTIVITY;
        this.username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
    }

    // Constructor for usage in MyFeedbackFragment
    public FeedbackLoader(MyFeedbackFragment caller) {
        this.callerMyFeedbackFragment = caller;
        this.userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.mode = MODE_MYFEEDBACKFRAGMENT;
        this.username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
    }

    // Method that is called when FeedbackLoader.start() is called
    public void getFeedback() {
        if (mode == MODE_FEEDBACKACTIVITY) {
            getAllFeedbackForApp();
        } else {
            getAllFeedback();
        }
    }

    // Get a list with all feedback entries for an app (used for FeedbackActivity)
    private void getAllFeedbackForApp() {
        final ArrayList<FeedbackEntryObject> list = new ArrayList<>();
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() != 0) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        String username = child.child("username").getValue(String.class);
                        // Distinguish between feedback and rating
                        if (child.hasChild("feedbackDetails/complaint")) {
                            FeedbackDetails feedbackDetails = child.child("feedbackDetails").getValue(FeedbackDetails.class);
                            if (feedbackDetails.isMakePublic()) {
                                // Only add an entry to the list if public is enabled
                                list.add(new FeedbackEntryObject(feedbackDetails, username, false, child.getKey(), userid));
                            }
                        } else {
                            RatingDetails ratingDetails = child.child("feedbackDetails").getValue(RatingDetails.class);
                            if (ratingDetails.isMakePublic()) {
                                // Only add an entry to the list if public is enabled
                                list.add(new FeedbackEntryObject(ratingDetails, username, true, child.getKey(), userid));
                            }
                        }
                    }
                    setFeedbackCommunity(list);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        // Get all feedback for this app
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(appDetails.getPackageName().replace('.', ':'));
        ref.addValueEventListener(valueEventListener);
    }

    // Get all feedback entries from the db (used for FeedbackActivity)
    private void getAllFeedback() {
        final ArrayList<FeedbackEntryObject> list = new ArrayList<>();
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() != 0) {
                    for (DataSnapshot appPkg : dataSnapshot.getChildren()) {
                        for (DataSnapshot feedback : appPkg.getChildren()) {
                            String username = feedback.child("username").getValue(String.class);
                            // Distinguish between feedback and rating
                            if (feedback.hasChild("feedbackDetails/complaint")) {
                                FeedbackDetails feedbackDetails = feedback.child("feedbackDetails").getValue(FeedbackDetails.class);
                                list.add(new FeedbackEntryObject(feedbackDetails, username, false, feedback.getKey(), userid));
                            } else {
                                RatingDetails ratingDetails = feedback.child("feedbackDetails").getValue(RatingDetails.class);
                                list.add(new FeedbackEntryObject(ratingDetails, username, true, feedback.getKey(), userid));
                            }
                        }
                    }
                    setFeedbackCommunity(list);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        // Get all feedback
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.addValueEventListener(valueEventListener);
    }

    private void setFeedbackCommunity(ArrayList<FeedbackEntryObject> feedbackCommunity) {
        this.allFeedbackForApp = feedbackCommunity;
        if (mode == MODE_FEEDBACKACTIVITY) {
            callerFeedbackActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callerFeedbackActivity.onReceiveFeedback(allFeedbackForApp);
                }
            });
        } else {
            callerMyFeedbackFragment.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callerMyFeedbackFragment.onReceiveFeedback(allFeedbackForApp);
                }
            });
        }
    }

    // Run method, called by FeedbackActivity or MyFeedbackFragment
    @Override
    public void run() {
        getFeedback();
    }

    public class FeedbackEntryObject implements Comparable<FeedbackEntryObject> {
        FeedbackDetailsInterface feedbackDetails;
        boolean isRating;
        String username;
        Drawable userPic;
        String feedbackID;
        String userid;

        FeedbackEntryObject() {
        }

        FeedbackEntryObject(FeedbackDetailsInterface feedbackDetails, String username, boolean isRating, String feedbackID, String userid) {
            this.feedbackDetails = feedbackDetails;
            this.username = username;
            this.isRating = isRating;
            this.feedbackID = feedbackID;
            this.userid = userid;
        }

        public void setFeedbackDetails(FeedbackDetailsInterface feedbackDetails) {
            this.feedbackDetails = feedbackDetails;
        }

        public void setRating(boolean rating) {
            isRating = rating;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setUserPic(Drawable userPic) {
            this.userPic = userPic;
        }

        public void setFeedbackID(String feedbackID) {
            this.feedbackID = feedbackID;
        }

        public String getUserid() {
            return userid;
        }

        public void setUserid(String userid) {
            this.userid = userid;
        }

        @Override
        public int compareTo(FeedbackEntryObject other) {
            Integer likesThis = feedbackDetails.getLikes();
            Integer likesOther = other.feedbackDetails.getLikes();
            Integer compareLikes = likesThis.compareTo(likesOther);
            if (compareLikes != 0) {
                return compareLikes;
            }
            try {
                Long timeStampThis = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(feedbackDetails.getTimestamp()).getTime();
                Long timeStampOther = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(other.feedbackDetails.getTimestamp()).getTime();
                return timeStampThis.compareTo(timeStampOther);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return compareLikes;
        }
    }
}
