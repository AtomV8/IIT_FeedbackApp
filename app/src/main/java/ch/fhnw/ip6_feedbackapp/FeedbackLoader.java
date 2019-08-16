package ch.fhnw.ip6_feedbackapp;

import android.graphics.drawable.Drawable;
import android.renderscript.Sampler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class FeedbackLoader {
    public final static int MODE_FEEDBACKACTIVITY = 0;
    public final static int MODE_MYFEEDBACKFRAGMENT = 1;

    AppDetails appDetails;
    boolean thisVersionOnly;
    String userid;

    List<FeedbackEntryObject> feedbackCommunity;
    List<FeedbackEntryObject> listMyFeedback;
    FeedbackEntryObject myLatestFeedback;

    public FeedbackLoader(AppDetails appDetails, boolean thisVersionOnly, String userid) {
        this.appDetails = appDetails;
        this.thisVersionOnly = thisVersionOnly;
        this.userid = userid;
    }

    public void getFeedback(int mode) {
        if (mode == MODE_FEEDBACKACTIVITY) {
            getListMostPopular();
        } else {

        }
    }

    private void getListOwnFeedback() {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("QUERYSUCCESSFUL", "Worked");
                if (dataSnapshot.getChildrenCount() != 0) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        if (child.hasChild("feedbackDetails/complaint")) {
                            FeedbackDetails feedbackDetails = child.child("feedbackDetails").child("appDetails").getValue(FeedbackDetails.class);
                        } else {
                            RatingDetails ratingDetails = child.child("feedbackDetails").getValue(RatingDetails.class);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        Query query = FirebaseDatabase.getInstance().getReference()
                .equalTo(userid)
                .orderByKey()
                .limitToLast(5);
        query.addValueEventListener(valueEventListener);
    }

    private void getListMostPopular() {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("QUERYSUCCESSFUL", "Worked");
                if (dataSnapshot.getChildrenCount() != 0) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        if (child.hasChild("feedbackDetails/complaint")) {
                            FeedbackDetails feedbackDetails = child.child("feedbackDetails").child("appDetails").getValue(FeedbackDetails.class);
                        } else {
                            RatingDetails ratingDetails = child.child("feedbackDetails").getValue(RatingDetails.class);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        if (thisVersionOnly) {
            Query query = FirebaseDatabase.getInstance().getReference(appDetails.getPackageName().replace('.', ':'))
                    .equalTo(appDetails.getPackageName().replace('.', ':'))
                    .orderByChild("likes")
                    .limitToLast(5);
            query.addValueEventListener(valueEventListener);
        } else {
            Query query = FirebaseDatabase.getInstance().getReference(appDetails.getPackageName().replace('.', ':'))
                    .orderByChild("likes")
                    .limitToLast(5);
            query.addValueEventListener(valueEventListener);
        }
    }

    private void getOwnLatest() {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        Query query = FirebaseDatabase.getInstance().getReference(appDetails.getPackageName().replace('.', ':'))
                .orderByKey()
                .limitToLast(1);
        query.addValueEventListener(valueEventListener);
    }


    public class FeedbackEntryObject {
        FeedbackDetailsInterface feedbackDetails;
        boolean isRating;
        String username;
        Drawable userPic;

        FeedbackEntryObject(FeedbackDetailsInterface feedbackDetails, String username, boolean isRating) {
            this.feedbackDetails = feedbackDetails;
            this.username = username;
            this.isRating = isRating;
        }
    }
}
