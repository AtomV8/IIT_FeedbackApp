package ch.fhnw.ip6_feedbackapp;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.accessibility.AccessibilityEvent;

import androidx.core.app.NotificationCompat;

import static ch.fhnw.ip6_feedbackapp.MainActivity.CHANNEL_ID;

public class AppDetectionService extends AccessibilityService {

    public static final int TYPE_DEFAULT = 0;
    public static final int TYPE_FEEDBACK = 1;
    public static final int TYPE_RATING = 2;

    public static final int SOURCE_FEEDBACK_APP = 0;
    public static final int SOURCE_NOTIFICATION = 1;

    private static final String NOTIFICATION_CONTENT_TITLE = "Jetzt Feedback geben zu:";

    private static final String ACTION_FEEDBACK_TITLE = "FEEDBACK";
    private static final String ACTION_RATING_TITLE = "BEWERTUNG";

    private String foregroundAppName = "Feedback App";
    private String foregroundAppPackageName = "ch.fhnw.ip6_feedbackapp";


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!intent.getBooleanExtra("ACTIVATE", true)){
            //disableSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    // Call updateNotification when service starts to get the current app
    // (Otherwise it will only show the notification once the next AccessibilityEvent is triggered)
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        updateNotification();
    }

    // Refresh the notification when the current activity changes
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            // Only update the notification if the current app is NOT a system app
            if (event.getPackageName() != null && !(event.getPackageName().toString().equalsIgnoreCase("com.android.systemui"))) {
                String currentPackageName = (String) event.getPackageName();
                PackageManager packageManager = getApplicationContext().getPackageManager();
                String appName = "";
                try {
                    appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(foregroundAppPackageName, PackageManager.GET_META_DATA));
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                // Update appName and packageName and then trigger a notification refresh
                foregroundAppName = appName;
                foregroundAppPackageName = currentPackageName;
                updateNotification();
            }
        }
    }

    @Override
    public void onInterrupt() {}

    private void updateNotification(){
            // Open current app's page in feedback app if notification main body clicked
            Intent notificationIntent = new Intent(this, FeedbackActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            notificationIntent.putExtra("App", foregroundAppPackageName);
            notificationIntent.putExtra("Type", TYPE_DEFAULT);
            notificationIntent.putExtra("Source", SOURCE_NOTIFICATION);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Feedback to current app if button FEEDBACK clicked
            Intent feedbackIntent = new Intent(this, FeedbackActivity.class);
            feedbackIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            feedbackIntent.putExtra("App", foregroundAppPackageName);
            feedbackIntent.putExtra("Type", TYPE_FEEDBACK);
            feedbackIntent.putExtra("Source", SOURCE_NOTIFICATION);
            PendingIntent pendingFeedbackIntent = PendingIntent.getActivity(this, 1, feedbackIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Rating for the current app if button RATING clicked
            Intent ratingIntent = new Intent(this, FeedbackActivity.class);
            ratingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ratingIntent.putExtra("App", foregroundAppPackageName);
            ratingIntent.putExtra("Type", TYPE_RATING);
            ratingIntent.putExtra("Source", SOURCE_NOTIFICATION);
            PendingIntent pendingRatingIntent = PendingIntent.getActivity(this, 2, ratingIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Build the notification
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(NOTIFICATION_CONTENT_TITLE)
                    .setContentText(foregroundAppName)
                    .setSmallIcon(R.drawable.ic_stat_onesignal_default)
                    .setContentIntent(pendingIntent)
                    .setColor(getResources().getColor(R.color.colorPrimary))
                    // Add the action buttons (FEEDBACK / RATING)
                    .addAction(R.mipmap.ic_launcher, ACTION_FEEDBACK_TITLE, pendingFeedbackIntent)
                    .addAction(R.mipmap.ic_launcher, ACTION_RATING_TITLE, pendingRatingIntent)
                    .build();
            startForeground(1, notification);
    }

}
