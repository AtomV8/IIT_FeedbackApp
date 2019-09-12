package ch.fhnw.ip6_feedbackapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final String CHANNEL_ID = "Feedback App Channel";
    public static final String CHANNEL_NAME = "Feedback App - Service um laufende App zu ermitteln";

    private boolean isServiceRunning = false;

    private DrawerLayout drawer;
    private TextView labelUserName;
    private TextView labelUserEmail;
    private ImageView imageUserPic;


    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    public FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    Intent i = new Intent(getApplicationContext(), LoginChoiceActivity.class);
                    startActivity(i);
                } else {
                    if (!isServiceRunning) {
                        createNotificationChannel();
                        startService();
                    }
                    mUser = user;
                }
            }
        };

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        labelUserName = header.findViewById(R.id.userName);
        labelUserEmail = header.findViewById(R.id.userEmail);
        imageUserPic = header.findViewById(R.id.userPic);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MyAppsFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_myApps);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent i = new Intent(getApplicationContext(), LoginChoiceActivity.class);
            startActivity(i);
        }else{
            updateUI();
        }
    }

    public void updateUI() {
        labelUserEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        labelUserName.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
    }

    public void logout(View view) {
        stopService();
        FirebaseAuth.getInstance().signOut();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_myApps:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MyAppsFragment()).commit();
                break;
            case R.id.nav_myFeedback:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MyFeedbackFragment()).commit();
                break;
            case R.id.nav_settings:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).commit();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthStateListener);
    }

    // Start the accessibility service
    public void startService() {
        Intent serviceIntent = new Intent(this, AppDetectionService.class);
        serviceIntent.putExtra("ACTIVATE", true);
        startService(serviceIntent);
        isServiceRunning = true;
    }

    // Stop the accessibility service
    public void stopService() {
        Intent serviceIntent = new Intent(this, AppDetectionService.class);
        removeNotificationChannel();
        serviceIntent.putExtra("ACTIVATE", false);
        startService(serviceIntent);
        isServiceRunning = false;
    }

    // Create a notification channel for devices running Android 8.0 or higher
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.setShowBadge(false);
            serviceChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(serviceChannel);
        }
    }

    // Remove the notification channel for devices running Android 8.0 or higher
    private void removeNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.deleteNotificationChannel(CHANNEL_ID);
        }
    }

}
