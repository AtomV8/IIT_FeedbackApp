package ch.fhnw.ip6_feedbackapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.audiofx.Equalizer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.chip.Chip;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

import static ch.fhnw.ip6_feedbackapp.AppDetectionService.SOURCE_FEEDBACK_APP;
import static ch.fhnw.ip6_feedbackapp.AppDetectionService.TYPE_DEFAULT;
import static ch.fhnw.ip6_feedbackapp.AppDetectionService.TYPE_FEEDBACK;
import static ch.fhnw.ip6_feedbackapp.AppDetectionService.TYPE_RATING;

import static ch.fhnw.ip6_feedbackapp.AppDetectionService.SOURCE_NOTIFICATION;
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
        Log.d("CALLTYPE", Integer.toString(callType));
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

        // Initialize the feedback popup
        feedbackButton = (Button) findViewById(R.id.btnFeedback);
        feedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FEEDBACKBUTTONPRESSED", "feedback alert dialogue should pop up now");
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

                Button buttonSend = mView.findViewById(R.id.feedbackDialogueButtonSend);
                buttonSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (pendingFeedback == null) {
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
                        } else {
                            Toast.makeText(getApplicationContext(), "Letztes Feedback noch in Bearbeitung...", Toast.LENGTH_LONG).show();
                        }
                        feedbackDialog.cancel();
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
                Log.d("RATINGBUTTONPRESSED", "rating alert dialogue should pop up now");
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

    public void getFeedback(View v){
        FeedbackLoader fl = new FeedbackLoader(appDetails, false);
        fl.getFeedback(FeedbackLoader.MODE_FEEDBACKACTIVITY);
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
                            Log.d("LOCATIONDETECTED", "LD");
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
                            Log.d("LOCATIONREQUESTED", "LR");
                        }
                    } else {
                        locationManager.requestSingleUpdate(criteria, locationListener, looper);
                        Log.d("LOCATIONREQUESTED", "LR");
                    }
                }
            });

        }
    }

    class FeedbackSender extends Thread {

        FeedbackObject feedbackObject;
        FeedbackDetails feedbackDetails;
        RatingDetails ratingDetails;
        String userID;

        boolean isRating;

        FeedbackSender() {
            Log.d("FEEDBACKSENDER ERSTELLT", "Standard Konstruktor");
            userID = FeedbackActivity.this.userid;
            Log.d("USERIDINFEEDBACKSENDER", userID);
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
            Log.d("LOCATIONRECEIVED", "GPS Location Received");
            feedbackObject.getDeviceData().setLatitude(lat);
            Log.d("LOCATIONRECEIVED", "Latitude is " + Double.toString(lat));
            feedbackObject.getDeviceData().setLongitude(lon);
            Log.d("LOCATIONRECEIVED", "Longitude is " + Double.toString(lon));
            // OK now send the feedback
            sendToDB();
        }


        public void gatherData() {
            DeviceData deviceData = new DeviceData(FeedbackActivity.this);
            if (isRating) {
                feedbackObject = new FeedbackObject(ratingDetails, deviceData, userID);
            } else {
                feedbackObject = new FeedbackObject(feedbackDetails, deviceData, userID);
            }

            // Wait for location to be detected if that setting is enabled
            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
            if (sharedPreferences.getBoolean(SHARED_PREFERENCES_GPS_LOCATION, true)) {
                GPSLocationHelper gpsLocationHelper = new GPSLocationHelper();
                Log.d("GPSLOCATIONREQUESTED", "GPS Location Requested");
            } else {
                // Otherwise send the feedback straight away
                sendToDB();
            }
        }

        public void sendToDB() {
            Log.d("DATABASECALL", "Datenbankverbindung wird hergestellt");
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference ref = db.getReference(appDetails.getPackageName().replace('.',':'));
            if(isRating){
                ref.push().setValue(feedbackObject);
            }else{
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
