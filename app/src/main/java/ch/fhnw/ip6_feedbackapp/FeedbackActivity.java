package ch.fhnw.ip6_feedbackapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.chip.Chip;
import com.google.firebase.FirebaseApp;
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


public class FeedbackActivity extends AppCompatActivity {

    private static final int IMAGE_PICK_CODE = 777;
    private static final int PERMISSION_CODE = 888;

    String callPackageName;
    int callType;
    int callSource;

    Button feedbackButton;
    Button ratingButton;

    ImageButton imagePickerButton;
    ImageView imageThumbnail;
    TextView imageAppendText;

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
    private void buildAppPage(final String packageName, int type, int source){
        final String appName = getAppName(packageName);
        String appVersionNumber = getAppVersion(packageName);
        // Set app icon and title
        TextView appTitle = findViewById(R.id.appPageTitle);
        appTitle.setText(appName);
        TextView appVersion = findViewById(R.id.appVersionNumber);
        appVersion.setText(appVersionNumber);
        ImageView appIcon = findViewById(R.id.imageViewAppIcon);
        Drawable icon = getAppIcon(packageName);
        if(icon != null){
            appIcon.setImageDrawable(getAppIcon(packageName));
        }else {
            appIcon.setImageResource(R.drawable.ic_unknown_app);
        }

        // Initialize the feedback popup
        feedbackButton = (Button) findViewById(R.id.btnFeedback);
        feedbackButton.setOnClickListener(new View.OnClickListener(){
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
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                                // Request permission
                                String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                                requestPermissions(permissions, PERMISSION_CODE);
                            }else{
                                pickImageFromGallery();
                            }
                        }else{
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
                if(callSource == SOURCE_NOTIFICATION  && callType != TYPE_DEFAULT){
                    buttonCancel.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View view) {
                            feedbackDialog.cancel();
                            finish();
                        }
                    });
                    // Otherwise just close the dialog
                }else {
                    buttonCancel.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View view) {
                            feedbackDialog.cancel();
                        }
                    });
                }
                Button buttonSend = mView.findViewById(R.id.feedbackDialogueButtonSend);
                buttonSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AppDetails appDetails = new AppDetails(packageName, getAppName(packageName),null, getAppVersion(packageName));

                        FeedbackDetails feedbackDetails = new FeedbackDetails(
                                appDetails,
                                chipComplaint.isChecked(),
                                chipIdea.isChecked(),
                                chipPraise.isChecked(),
                                feedbackText.getText().toString(),
                                checkBoxPublic.isChecked(),
                                checkBoxAnonymous.isChecked(),
                                new Timestamp(new Date().getTime())
                                );

                        //FeedbackObject feedbackObject = new FeedbackObject();
                        FirebaseDatabase db = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = db.getReference("test");
                        myRef.setValue(feedbackDetails);

                        /*


                        TODO



                         */

                    }
                });

                feedbackDialog.show();
            }
        });

        // Initialize the rating popup
        ratingButton = (Button) findViewById(R.id.btnRating);
        ratingButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.d("RATINGBUTTONPRESSED", "rating alert dialogue should pop up now");
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(FeedbackActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.rating_dialogue, null);
                TextView title = (TextView) mView.findViewById(R.id.ratingDialogueTitle);
                title.setText(appName + " - Bewertung");

                mBuilder.setView(mView);
                final AlertDialog ratingDialog = mBuilder.create();
                Button buttonCancel = mView.findViewById(R.id.ratingDialogueButtonCancel);
                // // Go back to the previous app if the source was a notification click on one of the action buttons
                if(callSource == SOURCE_NOTIFICATION && callType != TYPE_DEFAULT){
                    buttonCancel.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View view) {
                            ratingDialog.cancel();
                            finish();
                        }
                    });
                    // Otherwise just close the dialog
                }else {
                    buttonCancel.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View view) {
                            ratingDialog.cancel();
                        }
                    });
                }
                ratingDialog.show();
            }
        });

        // Show according popup if intent came from a notification click
        if(callSource == SOURCE_NOTIFICATION){
            if(type == TYPE_FEEDBACK){
                feedbackButton.callOnClick();
            }
            else if(type == TYPE_RATING){
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
        switch(requestCode){
            case PERMISSION_CODE: {
                if(grantResults.length >= 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    pickImageFromGallery();
                }else {
                    Toast.makeText(this, "Zugriff verweigert", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE){
            imageThumbnail.setImageURI(data.getData());
            imageThumbnail.setVisibility(View.VISIBLE);
            imageAppendText.setText("Screenshot ändern");
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    // Helper method to get app name
    private String getAppName(String packageName){
        PackageManager packageManager = getApplicationContext().getPackageManager();
        String appName = "";
        try {
            appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appName;
    }

    // Helper method to get app icon
    private Drawable getAppIcon(String packageName){
        try
        {
            Drawable appIcon = getPackageManager().getApplicationIcon(packageName);
            return appIcon;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    // Helper method to get app version
    private String getAppVersion(String packageName){
        PackageManager packageManager = getApplicationContext().getPackageManager();
        String appVersion = "";
        try {
            PackageInfo pInfo  = packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA);
            appVersion = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appVersion;
    }


}
