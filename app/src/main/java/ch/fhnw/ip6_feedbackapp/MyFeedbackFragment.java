package ch.fhnw.ip6_feedbackapp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MyFeedbackFragment extends Fragment {

    ListView listView;
    ProgressBar progressBar;
    SwipeRefreshLayout refreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_feedback, container, false);
        listView = view.findViewById(R.id.myFeedbackList);
        refreshLayout = view.findViewById(R.id.swiperefreshMyFeedbackList);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                FeedbackLoader feedbackLoader = new FeedbackLoader(MyFeedbackFragment.this);
                feedbackLoader.start();
            }
        });
        progressBar = view.findViewById(R.id.myFeedbackListProgressBar);
        progressBar.setVisibility(View.VISIBLE);
        return view;
    }

    @Override
    public void onResume() {
        FeedbackLoader feedbackLoader = new FeedbackLoader(this);
        feedbackLoader.start();
        super.onResume();
    }

    public void onReceiveFeedback(ArrayList<FeedbackLoader.FeedbackEntryObject> list) {
        ArrayList<FeedbackLoader.FeedbackEntryObject> feedbackEntryObjects = list;
        // Remove all entries from the list that have not been submitted by this user
        ArrayList<FeedbackLoader.FeedbackEntryObject> wrongUser = new ArrayList<>();
        for (FeedbackLoader.FeedbackEntryObject feo : feedbackEntryObjects) {
            Log.d("USERIDERROR", feo.getUserid() + " " + feo.username);
            if (!feo.getUserid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                wrongUser.add(feo);
            }
        }
        feedbackEntryObjects.removeAll(wrongUser);
        // Sort the feedback by timestamp
        if (feedbackEntryObjects.size() > 1) {
            Comparator<FeedbackLoader.FeedbackEntryObject> c = new Comparator<FeedbackLoader.FeedbackEntryObject>() {
                @Override
                public int compare(FeedbackLoader.FeedbackEntryObject feedbackEntryObject, FeedbackLoader.FeedbackEntryObject t1) {
                    try {
                        Long timeStampThis = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(feedbackEntryObject.feedbackDetails.getTimestamp()).getTime();
                        Long timeStampOther = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(t1.feedbackDetails.getTimestamp()).getTime();
                        return timeStampThis.compareTo(timeStampOther);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return 0;
                }
            };
            Collections.sort(feedbackEntryObjects, c.reversed());
        }
        refreshLayout.setRefreshing(false);
        progressBar.setVisibility(View.GONE);
        FeedbackListAdapter feedbackListAdapter = new FeedbackListAdapter(getActivity(), feedbackEntryObjects);
        listView.setAdapter(feedbackListAdapter);
    }

    // List adapter for list of installed apps
    class FeedbackListAdapter extends ArrayAdapter<String> {

        Context context;
        ArrayList<FeedbackLoader.FeedbackEntryObject> feedbackEntryObjects;

        FeedbackListAdapter(Context c, ArrayList<FeedbackLoader.FeedbackEntryObject> feedbackEntryObjects) {
            super(c, R.layout.apps_list_entry);
            this.feedbackEntryObjects = feedbackEntryObjects;
            this.context = c;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            // Get the row elements for the view
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            View row = layoutInflater.inflate(R.layout.own_feedback_entry, parent, false);
            ImageView appImage = row.findViewById(R.id.imageViewUserProfilePic);
            TextView appTitle = row.findViewById(R.id.textViewAppTitle);
            TextView appVersion = row.findViewById(R.id.textViewAppVersion);
            TextView timeStamp = row.findViewById(R.id.textViewTimeStamp);
            TextView likeCounter = row.findViewById(R.id.textViewLikeCounter);
            ImageView dislikeIcon = row.findViewById(R.id.dislikeIcon);
            ImageView ideaIcon = row.findViewById(R.id.ideaIcon);
            ImageView likeIcon = row.findViewById(R.id.likeIcon);
            RatingBar starsBar = row.findViewById(R.id.ratingBar);
            TextView feedbackText = row.findViewById(R.id.feedbackText);
            CheckBox checkBoxLike = row.findViewById(R.id.checkBoxLike);
            FeedbackDetailsInterface feedbackDetails = feedbackEntryObjects.get(position).feedbackDetails;

            // Fill in the rows and initialize UI elements
            appVersion.setText(feedbackEntryObjects.get(position).feedbackDetails.getAppDetails().getAppVersion());
            try {
                appImage.setImageDrawable(getContext().getPackageManager().getApplicationIcon(feedbackEntryObjects.get(position).feedbackDetails.getAppDetails().getPackageName()));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            appTitle.setText(feedbackEntryObjects.get(position).feedbackDetails.getAppDetails().getAppName());

            boolean isRating = feedbackEntryObjects.get(position).isRating;
            if (isRating) {
                starsBar.setVisibility(View.VISIBLE);
                starsBar.setRating(((RatingDetails) feedbackDetails).getRating());
            } else {
                if (((FeedbackDetails) feedbackDetails).isPraise()) {
                    likeIcon.setVisibility(View.VISIBLE);
                }
                if (((FeedbackDetails) feedbackDetails).isComplaint()) {
                    dislikeIcon.setVisibility(View.VISIBLE);
                }
                if (((FeedbackDetails) feedbackDetails).isIdea()) {
                    ideaIcon.setVisibility(View.VISIBLE);
                }
            }
            timeStamp.setText(feedbackEntryObjects.get(position).feedbackDetails.getTimestamp());
            likeCounter.setText(Integer.toString(feedbackEntryObjects.get(position).feedbackDetails.getLikes()));
            String description = feedbackEntryObjects.get(position).feedbackDetails.getText();
            if (description != "" && description != null) {
                feedbackText.setText(description);
            } else {
                feedbackText.setVisibility(View.GONE);
            }

            checkBoxLike.setEnabled(false);
            return row;
        }

        @Override
        public int getCount() {
            return feedbackEntryObjects.size();
        }
    }
}
