package ch.fhnw.ip6_feedbackapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import static ch.fhnw.ip6_feedbackapp.AppDetectionService.SOURCE_NOTIFICATION;
import static ch.fhnw.ip6_feedbackapp.AppDetectionService.TYPE_DEFAULT;
import static ch.fhnw.ip6_feedbackapp.AppDetectionService.TYPE_FEEDBACK;
import static ch.fhnw.ip6_feedbackapp.AppDetectionService.TYPE_RATING;
import static ch.fhnw.ip6_feedbackapp.SettingsFragment.SHARED_PREFERENCES_GPS_LOCATION;
import static ch.fhnw.ip6_feedbackapp.SettingsFragment.SHARED_PREFERENCES_NAME;


public class FeedbackActivity extends AppCompatActivity {

    private static final int IMAGE_PICK_CODE = 777;
    private static final int PERMISSION_CODE = 888;
    private AppCompatActivity activity;

    String callPackageName;
    int callType;
    int callSource;

    Button feedbackButton;
    Button ratingButton;

    ImageButton imagePickerButton;
    ImageView imageThumbnail;
    TextView imageAppendText;

    FeedbackSender pendingFeedback;

    AppDetails appDetails;

    private Handler mainHandler = new Handler();

    String userid;

    FeedbackLoader feedbackLoader;

    ListView communityFeedbackListView;
    TextView communityFeedbackListEmptyTextView;

    LinearLayout ownFeedbackLinLay;
    TextView ownFeedbackEmptyTextView;

    CheckBox checkBoxOnlyCurrentVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarFeedback);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent caller = getIntent();
        callPackageName = caller.getStringExtra("App");
        callType = caller.getIntExtra("Type", 0);
        callSource = caller.getIntExtra("Source", 0);
        activity = this;
        appDetails = new AppDetails(callPackageName, this);
        userid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Do the following to prevent a crash from happening while getting bluetooth data
        // See https://stackoverflow.com/questions/5920578/bluetoothadapter-getdefaultadapter-throwing-runtimeexception-while-not-in-acti
        BluetoothAdapter.getDefaultAdapter();

        buildAppPage(callPackageName, callType, callSource);
    }

    @Override
    protected void onResume() {
        removeFeedback();
        feedbackLoader = new FeedbackLoader(this, appDetails, userid);
        feedbackLoader.start();
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Initialize the layout
    private void buildAppPage(final String packageName, int type, int source) {
        final String appName = appDetails.getAppName();
        checkBoxOnlyCurrentVersion = findViewById(R.id.checkBoxOnlyCurrentVersion);
        checkBoxOnlyCurrentVersion.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                removeFeedback();
                feedbackLoader = new FeedbackLoader(FeedbackActivity.this, appDetails, userid);
                feedbackLoader.start();
            }
        });
        String appVersionNumber = appDetails.getAppVersion();
        // Set app icon and title
        TextView appTitle = findViewById(R.id.appPageTitle);
        appTitle.setText(appName);
        TextView appVersion = findViewById(R.id.appVersionNumber);
        appVersion.setText(appVersionNumber);
        ImageView appIcon = findViewById(R.id.imageViewAppIcon);
        Drawable icon = appDetails.getAppIcon();
        if (icon != null) {
            appIcon.setImageDrawable(icon);
        } else {
            appIcon.setImageResource(R.drawable.ic_unknown_app);
        }
        communityFeedbackListView = findViewById(R.id.listViewCommunityFeedback);
        communityFeedbackListEmptyTextView = findViewById(R.id.textViewNoFeedbackCommunity);

        ownFeedbackLinLay = findViewById(R.id.linLayFeedbackOwn);
        ownFeedbackEmptyTextView = findViewById(R.id.textViewNoFeedbackOwn);

        // Initialize the feedback popup
        feedbackButton = (Button) findViewById(R.id.btnFeedback);
        feedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(FeedbackActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.feedback_dialogue, null);
                TextView title = (TextView) mView.findViewById(R.id.feedbackDialogueTitle);
                title.setText(appName + " - Feedback");

                final Chip chipComplaint = (Chip) mView.findViewById(R.id.chipComplaint);
                chipComplaint.setChipIcon(getResources().getDrawable(R.mipmap.ic_dislike_round));
                final Chip chipIdea = (Chip) mView.findViewById(R.id.chipIdea);
                chipIdea.setChipIcon(getResources().getDrawable(R.mipmap.ic_idea_round));
                final Chip chipPraise = (Chip) mView.findViewById(R.id.chipPraise);
                chipPraise.setChipIcon(getResources().getDrawable(R.mipmap.ic_like_round));
                final EditText feedbackText = mView.findViewById(R.id.FeedbackDialogueDescriptionText);
                final CheckBox checkBoxPublic = mView.findViewById(R.id.checkBoxFeedbackDialoguePublic);
                final CheckBox checkBoxAnonymous = mView.findViewById(R.id.checkBoxFeedbackDialogueAnonymous);

                imagePickerButton = mView.findViewById(R.id.screenshotAppendButton);
                imagePickerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Check permission
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                                // Request permission
                                String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                                requestPermissions(permissions, PERMISSION_CODE);
                            } else {
                                pickImageFromGallery();
                            }
                        } else {
                            pickImageFromGallery();
                        }
                    }
                });
                imageThumbnail = mView.findViewById(R.id.screenshotThumbnail);
                imageAppendText = mView.findViewById(R.id.appendScreenshotTitle);

                mBuilder.setView(mView);
                final AlertDialog feedbackDialog = mBuilder.create();


                // Cancel button logic
                Button buttonCancel = mView.findViewById(R.id.feedbackDialogueButtonCancel);
                // Go back to the previous app if the source was a notification click on one of the action buttons
                if (callSource == SOURCE_NOTIFICATION && callType != TYPE_DEFAULT) {
                    buttonCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            feedbackDialog.cancel();
                            finish();
                        }
                    });
                    // Otherwise just close the dialog
                } else {
                    buttonCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            feedbackDialog.cancel();
                        }
                    });
                }

                // Remove the app icon from appdetails because that cannot be sent to firebase db
                final AppDetails appDetailsToSend = FeedbackActivity.this.appDetails;
                appDetailsToSend.setAppIcon(null);


                // Send button logic
                Button buttonSend = mView.findViewById(R.id.feedbackDialogueButtonSend);
                buttonSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (pendingFeedback == null) {
                            if (feedbackText.getText().toString().matches("")) {
                                Toast.makeText(getApplicationContext(), "Bitte schreiben Sie einen kurzen Text zu ihrem Feedback", Toast.LENGTH_LONG).show();
                            } else {
                                FeedbackDetails feedbackDetails = new FeedbackDetails(
                                        FeedbackActivity.this.appDetails,
                                        feedbackText.getText().toString(),
                                        new Timestamp(new Date().getTime()),
                                        checkBoxPublic.isChecked(),
                                        checkBoxAnonymous.isChecked(),
                                        chipComplaint.isChecked(),
                                        chipIdea.isChecked(),
                                        chipPraise.isChecked()
                                );
                                pendingFeedback = new FeedbackSender(feedbackDetails);
                                pendingFeedback.start();
                                feedbackDialog.cancel();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Letztes Feedback noch in Bearbeitung...", Toast.LENGTH_LONG).show();
                        }

                    }
                });

                feedbackDialog.show();
            }
        });

        // Initialize the rating popup
        ratingButton = (Button) findViewById(R.id.btnRating);
        ratingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(FeedbackActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.rating_dialogue, null);
                TextView title = (TextView) mView.findViewById(R.id.ratingDialogueTitle);
                title.setText(appName + " - Bewertung");


                final EditText ratingText = mView.findViewById(R.id.ratingDialogueDescriptionText);
                final CheckBox checkBoxPublic = mView.findViewById(R.id.checkBoxRatingDialoguePublic);
                final CheckBox checkBoxAnonymous = mView.findViewById(R.id.checkBoxRatingDialogueAnonymous);
                final RatingBar starRating = mView.findViewById(R.id.ratingDialogueRatingBar);

                mBuilder.setView(mView);
                final AlertDialog ratingDialog = mBuilder.create();
                Button buttonCancel = mView.findViewById(R.id.ratingDialogueButtonCancel);
                // // Go back to the previous app if the source was a notification click on one of the action buttons
                if (callSource == SOURCE_NOTIFICATION && callType != TYPE_DEFAULT) {
                    buttonCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ratingDialog.cancel();
                            finish();
                        }
                    });
                    // Otherwise just close the dialog
                } else {
                    buttonCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ratingDialog.cancel();
                        }
                    });
                }
                // Remove the app icon from appdetails because that cannot be sent to firebase db
                final AppDetails appDetailsToSend = FeedbackActivity.this.appDetails;
                appDetailsToSend.setAppIcon(null);

                Button buttonSend = mView.findViewById(R.id.ratingDialogueButtonSend);
                buttonSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (pendingFeedback == null) {
                            RatingDetails feedbackDetails = new RatingDetails(
                                    appDetailsToSend,
                                    ratingText.getText().toString(),
                                    new Timestamp(new Date().getTime()),
                                    checkBoxPublic.isChecked(),
                                    checkBoxAnonymous.isChecked(),
                                    starRating.getRating()
                            );
                            pendingFeedback = new FeedbackSender(feedbackDetails);
                            pendingFeedback.start();
                        } else {
                            Toast.makeText(getApplicationContext(), "Letztes Feedback noch in Bearbeitung...", Toast.LENGTH_LONG).show();
                        }
                        ratingDialog.cancel();
                    }
                });
                ratingDialog.show();
            }
        });

        // Show according popup if intent came from a notification click
        if (callSource == SOURCE_NOTIFICATION) {
            if (type == TYPE_FEEDBACK) {
                feedbackButton.callOnClick();
            } else if (type == TYPE_RATING) {
                ratingButton.callOnClick();
            }
        }

    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length >= 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery();
                } else {
                    Toast.makeText(this, "Zugriff verweigert", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            imageThumbnail.setImageURI(data.getData());
            imageThumbnail.setVisibility(View.VISIBLE);
            imageAppendText.setText("Screenshot ändern");
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    // Helper class to get gps coordinates
    class GPSLocationHelper {
        private LocationManager locationManager;
        private LocationListener locationListener;
        private Context context;
        private double latitude;
        private double longitude;


        public GPSLocationHelper() {
            this.context = FeedbackActivity.this;
            getLocation();
        }


        public void getLocation() {
            runOnUiThread(new Runnable() {
                public void run() {
                    locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

                    locationListener = new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            pendingFeedback.receiveGPSCoordinates(latitude, longitude);
                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {

                        }

                        @Override
                        public void onProviderEnabled(String provider) {

                        }

                        @Override
                        public void onProviderDisabled(String provider) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    };

                    Criteria criteria = new Criteria();
                    criteria.setAccuracy(Criteria.ACCURACY_FINE);
                    criteria.setPowerRequirement(Criteria.POWER_LOW);
                    criteria.setAltitudeRequired(false);
                    criteria.setBearingRequired(false);
                    criteria.setSpeedRequired(false);
                    criteria.setCostAllowed(true);
                    criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
                    criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);

                    final Looper looper = null;

                    // Check if the permission is set
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            locationManager.requestSingleUpdate(criteria, locationListener, looper);
                        }
                    } else {
                        locationManager.requestSingleUpdate(criteria, locationListener, looper);
                    }
                }
            });
        }
    }

    // Callback function that is called once the FeedbackLoader has loaded the feedback entries
    public void onReceiveFeedback(ArrayList<FeedbackLoader.FeedbackEntryObject> feedbackEntryObjects) {
        // Only proceed if list has more than one element
        if (feedbackEntryObjects.size() > 1) {
            // First prepare the community feedback
            ArrayList<FeedbackLoader.FeedbackEntryObject> communityFeedbackEntries = feedbackEntryObjects;
            // Remove feedback for other versions if checkbox "only current version" is checked
            if (FeedbackActivity.this.checkBoxOnlyCurrentVersion.isChecked()) {
                ArrayList<FeedbackLoader.FeedbackEntryObject> wrongVersion = new ArrayList<>();
                for (FeedbackLoader.FeedbackEntryObject feo : communityFeedbackEntries) {
                    if (feo.feedbackDetails.getAppDetails().getAppVersion() != appDetails.getAppVersion()) {
                        wrongVersion.add(feo);
                    }
                }
                communityFeedbackEntries.remove(wrongVersion);
            }
            // After filtering check again if list has more than one element
            if (communityFeedbackEntries.size() > 1) {
                // Copy this list to use it later for the own feedback
                ArrayList<FeedbackLoader.FeedbackEntryObject> listForOwnFeedback = communityFeedbackEntries;
                // Sort by number of likes, then timestamp descending
                Collections.sort(communityFeedbackEntries);
                // Insert the feedback into the view
                CommunityFeedbackListAdapter communityFeedbackListAdapter = new CommunityFeedbackListAdapter(this, communityFeedbackEntries);
                communityFeedbackListView.setAdapter(communityFeedbackListAdapter);
                communityFeedbackListView.setVisibility(View.VISIBLE);
                communityFeedbackListEmptyTextView.setVisibility(View.GONE);

                // Now prepare the own latest feedback
                for (FeedbackLoader.FeedbackEntryObject feo : listForOwnFeedback) {
                    ArrayList<FeedbackLoader.FeedbackEntryObject> wrongUser = new ArrayList<>();
                    if (feo.getUserid() != userid) {
                        wrongUser.add(feo);
                    }
                    listForOwnFeedback.remove(wrongUser);
                }
                Collections.sort(listForOwnFeedback, new Comparator<FeedbackLoader.FeedbackEntryObject>() {
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
                });
                FeedbackLoader.FeedbackEntryObject latestOwnFeedback = listForOwnFeedback.get(0);
                putOwnFeedbackIntoView(latestOwnFeedback);

            } else if (communityFeedbackEntries.size() == 1) {
                CommunityFeedbackListAdapter communityFeedbackListAdapter = new CommunityFeedbackListAdapter(this, feedbackEntryObjects);
                communityFeedbackListView.setAdapter(communityFeedbackListAdapter);
                communityFeedbackListView.setVisibility(View.VISIBLE);
                communityFeedbackListEmptyTextView.setVisibility(View.GONE);

                if (communityFeedbackEntries.get(0).userid == userid) {
                    FeedbackLoader.FeedbackEntryObject latestOwnFeedback = communityFeedbackEntries.get(0);
                    putOwnFeedbackIntoView(latestOwnFeedback);
                }
            }
        } else if (feedbackEntryObjects.size() == 1) {
            CommunityFeedbackListAdapter communityFeedbackListAdapter = new CommunityFeedbackListAdapter(this, feedbackEntryObjects);
            communityFeedbackListView.setAdapter(communityFeedbackListAdapter);
            communityFeedbackListView.setVisibility(View.VISIBLE);
            communityFeedbackListEmptyTextView.setVisibility(View.GONE);

            if (feedbackEntryObjects.get(0).userid == userid) {
                FeedbackLoader.FeedbackEntryObject latestOwnFeedback = feedbackEntryObjects.get(0);
                putOwnFeedbackIntoView(latestOwnFeedback);
            }
        }
    }

    private void removeFeedback() {
        communityFeedbackListView.setAdapter(null);
        if (ownFeedbackEmptyTextView.getVisibility() != View.VISIBLE) {
            ownFeedbackLinLay.removeViewAt(1);
        }
    }

    private void putOwnFeedbackIntoView(FeedbackLoader.FeedbackEntryObject latestOwnFeedback) {
        // Prepare the view
        ownFeedbackEmptyTextView.setVisibility(View.GONE);
        LayoutInflater layoutInflater = LayoutInflater.from(FeedbackActivity.this);
        View entry = layoutInflater.inflate(R.layout.feedback_entry, null);
        TextView userName = entry.findViewById(R.id.textViewUsername);
        TextView timeStamp = entry.findViewById(R.id.textViewTimeStamp);
        TextView likeCounter = entry.findViewById(R.id.textViewLikeCounter);
        ImageView dislikeIcon = entry.findViewById(R.id.dislikeIcon);
        ImageView ideaIcon = entry.findViewById(R.id.ideaIcon);
        ImageView likeIcon = entry.findViewById(R.id.likeIcon);
        RatingBar starsBar = entry.findViewById(R.id.ratingBar);
        TextView feedbackText = entry.findViewById(R.id.feedbackText);
        CheckBox checkBoxLike = entry.findViewById(R.id.checkBoxLike);
        final TextView invisibleFeedbackID = entry.findViewById(R.id.textViewInvisibleFeedbackID);
        FeedbackDetailsInterface feedbackDetails = latestOwnFeedback.feedbackDetails;

        // Put the feedback ID in an invisible textfield (needed to make the like feature work)
        invisibleFeedbackID.setText(latestOwnFeedback.feedbackID);

        // Initialize UI elements
        boolean isRating = latestOwnFeedback.isRating;
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


        boolean anonymous = latestOwnFeedback.feedbackDetails.isPublishAnonymously();
        if (anonymous) {
            userName.setText("Anonym");
        } else {
            userName.setText(latestOwnFeedback.username);
        }
        timeStamp.setText(latestOwnFeedback.feedbackDetails.getTimestamp());
        likeCounter.setText(Integer.toString(latestOwnFeedback.feedbackDetails.getLikes()));
        String description = latestOwnFeedback.feedbackDetails.getText();
        if (description != "" && description != null) {
            feedbackText.setText(description);
        } else {
            feedbackText.setVisibility(View.GONE);
        }

        checkBoxLike.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {

                } else {
                    //likeFeedback(invisibleFeedbackID.getText().toString());
                }
            }
        });
        ownFeedbackLinLay.addView(entry);
    }

    // List adapter for list of community feedback
    class CommunityFeedbackListAdapter extends ArrayAdapter<FeedbackLoader.FeedbackEntryObject> {

        Context context;
        ArrayList<FeedbackLoader.FeedbackEntryObject> communityFeedbackList;

        CommunityFeedbackListAdapter(Context c, ArrayList<FeedbackLoader.FeedbackEntryObject> feedbackEntryObjects) {
            super(c, R.layout.feedback_entry);
            this.communityFeedbackList = feedbackEntryObjects;
            this.context = c;
        }

        // Method that is called when the communityFeedbackList gets updated
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            // Get the row elements for the view
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            View row = layoutInflater.inflate(R.layout.feedback_entry, parent, false);
            TextView userName = row.findViewById(R.id.textViewUsername);
            TextView timeStamp = row.findViewById(R.id.textViewTimeStamp);
            TextView likeCounter = row.findViewById(R.id.textViewLikeCounter);
            ImageView dislikeIcon = row.findViewById(R.id.dislikeIcon);
            ImageView ideaIcon = row.findViewById(R.id.ideaIcon);
            ImageView likeIcon = row.findViewById(R.id.likeIcon);
            RatingBar starsBar = row.findViewById(R.id.ratingBar);
            TextView feedbackText = row.findViewById(R.id.feedbackText);
            CheckBox checkBoxLike = row.findViewById(R.id.checkBoxLike);
            final TextView invisibleFeedbackID = row.findViewById(R.id.textViewInvisibleFeedbackID);
            FeedbackDetailsInterface feedbackDetails = communityFeedbackList.get(position).feedbackDetails;

            // Put the feedback ID in an invisible textfield (needed to make the like feature work)
            invisibleFeedbackID.setText(communityFeedbackList.get(position).feedbackID);

            // Fill in the rows
            boolean isRating = communityFeedbackList.get(position).isRating;
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

            boolean anonymous = communityFeedbackList.get(position).feedbackDetails.isPublishAnonymously();
            if (anonymous) {
                userName.setText("Anonym");
            } else {
                userName.setText(communityFeedbackList.get(position).username);
            }
            timeStamp.setText(communityFeedbackList.get(position).feedbackDetails.getTimestamp());
            likeCounter.setText(Integer.toString(communityFeedbackList.get(position).feedbackDetails.getLikes()));
            String description = communityFeedbackList.get(position).feedbackDetails.getText();
            if (description != "" && description != null) {
                feedbackText.setText(description);
            } else {
                feedbackText.setVisibility(View.GONE);
            }

            checkBoxLike.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (isChecked) {

                    } else {
                        //likeFeedback(invisibleFeedbackID.getText().toString());
                    }
                }
            });
            return row;
        }

        public void likeFeedback(String feedbackID) {
            // Put a new like object into firebase db
            final FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference refUserLikes = db.getReference("likes" + "/" + userid);
            refUserLikes.push().setValue(feedbackID);

            // Increment likes count on feedback object in firebase db
            DatabaseReference upvotesRef = db.getReference(callPackageName + "/" + feedbackID + "/feedbackDetails/likes");
            upvotesRef.runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                    Integer likesCurrent = mutableData.getValue(Integer.class);
                    mutableData.setValue(likesCurrent + 1);

                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                }
            });
        }

        public void dislikeFeedback(String feedbackID) {
            final FirebaseDatabase db = FirebaseDatabase.getInstance();


        }

        public void checkIfLiked(final String feedbackID) {
            final FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference ref = db.getReference("likes" + "/" + userid);
            ValueEventListener eventListener = new ValueEventListener() {
                boolean likeExists = false;

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    boolean likeExists = dataSnapshot.hasChild(feedbackID);
                    onReceiveLikeExists(likeExists);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    onReceiveLikeExists(likeExists);
                }
            };
            ref.addListenerForSingleValueEvent(eventListener);
        }

        public void onReceiveLikeExists(boolean exists) {
        }

        @Override
        public int getCount() {
            return communityFeedbackList.size();
        }
    }

    class FeedbackSender extends Thread {

        FeedbackObject feedbackObject;
        FeedbackDetails feedbackDetails;
        RatingDetails ratingDetails;
        String userID;

        boolean isRating;

        FeedbackSender() {
            userID = FeedbackActivity.this.userid;
        }

        FeedbackSender(FeedbackDetails feedbackDetails) {
            this();
            this.feedbackDetails = feedbackDetails;
        }

        FeedbackSender(RatingDetails ratingDetails) {
            this();
            this.ratingDetails = ratingDetails;
            isRating = true;
        }

        // Get gps coordinates from gpsLocationHelper
        public void receiveGPSCoordinates(double lat, double lon) {
            feedbackObject.getDeviceData().setLatitude(lat);
            feedbackObject.getDeviceData().setLongitude(lon);
            // OK now send the feedback
            sendToDB();
        }


        public void gatherData() {
            String username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
            DeviceData deviceData = new DeviceData(FeedbackActivity.this);
            if (isRating) {
                feedbackObject = new FeedbackObject(ratingDetails, deviceData, userID, username);
            } else {
                feedbackObject = new FeedbackObject(feedbackDetails, deviceData, userID, username);
            }

            // Wait for location to be detected if that setting is enabled
            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
            if (sharedPreferences.getBoolean(SHARED_PREFERENCES_GPS_LOCATION, true)) {
                GPSLocationHelper gpsLocationHelper = new GPSLocationHelper();
            } else {
                // Otherwise send the feedback straight away
                sendToDB();
            }
        }

        public void sendToDB() {
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference ref = db.getReference(appDetails.getPackageName().replace('.', ':'));
            if (isRating) {
                ref.push().setValue(feedbackObject);
            } else {
                ref.push().setValue(feedbackObject);
            }
            FeedbackActivity.this.pendingFeedback = null;
        }

        @Override
        public void run() {
            gatherData();
        }
    }
}
