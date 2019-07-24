package uk.ac.aber.movementrecorder.UI;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import uk.ac.aber.movementrecorder.R;

/**
 * Created by einar on 23/02/2018.
 */


public class NotificationHandler extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
    }

    /**Source:
     * Google, “Create and Manage Notification Channels  |  Android Developers,” 2018. [Online]. Available: https://developer.android.com/training/notify-user/channels. [Accessed: 29-Apr-2018].
     *
     * Creates a notification channel for the notification to run on.
     * @param context
     */



    public static final String CHANNEL_ID = "com.doggertech.movementrecorder";
    public static final String CHANNEL_NAME = "TremorChannel";

    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //CharSequence channelName = "TremorChannel";
            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
                    //(NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    /**
     * Source:
     * Google, “Create a Notification,” 2018. [Online]. Available: https://developer.android.com/training/notify-user/build-notification. [Accessed: 29-Apr-2018].
     *
     * Builds a notification for TremorTracker using the channel above
     * @param context
     * @return
     */
    public Notification notificationBuilder(Context context) {
        Intent notificationIntent = new Intent(context, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(context, "1")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.mipmap.ic_launcher))
                .setContentTitle("Tremor Tracker")
                .setContentText("Working...")
                .setContentIntent(pendingIntent).build();

        return notification;
    }

}
