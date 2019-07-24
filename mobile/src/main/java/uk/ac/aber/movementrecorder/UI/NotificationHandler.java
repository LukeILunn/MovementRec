package uk.ac.aber.movementrecorder.UI;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import uk.ac.aber.movementrecorder.R;

/**
 * Created by einar on 23/02/2018.
 */

public class NotificationHandler extends Application{

    public static final String CHANNEL_ID = "com.doggertech.movementrecorder";
    public static final String CHANNEL_NAME = "TremorChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    /**
     * Source:
     * Google, “Create a Notification,” 2018. [Online]. Available: https://developer.android.com/training/notify-user/build-notification. [Accessed: 29-Apr-2018].
     *
     * Creates a notification channel for the notification to run on.
     */
    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Channel for notifications from TremorTracker");
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    /**
     * Source:
     * Google, “Create a Notification,” 2018. [Online]. Available: https://developer.android.com/training/notify-user/build-notification. [Accessed: 29-Apr-2018].
     *
     * Builds and returns a notification for the TremorTracker
     * using the channel set above.
     * @param context
     * @return Notification
     */
    public Notification notificationBuilder(Context context) {

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentIntent(contentIntent)
            .setWhen(System.currentTimeMillis())
            .setContentTitle("Tremor Tracker")
            .setContentText("Working...")
            .setSmallIcon(R.drawable.ic_tremor);

        return builder.build();
    }

}
