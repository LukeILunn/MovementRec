package uk.ac.aber.movementrecorder.Background;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import uk.ac.aber.movementrecorder.R;
import uk.ac.aber.movementrecorder.UI.MainActivity;

import static uk.ac.aber.movementrecorder.Background.SSManager.ActivityReceiver.ACTIVITY_RESP;
import static uk.ac.aber.movementrecorder.UI.NotificationHandler.CHANNEL_ID;

/**
 * Created by einar on 21/02/2018.
 */

public class SCService extends Service implements MessageHandler.IMessageHandler, SSManager.ISSManager{

    private SSManager ssManager;
    private PowerManager.WakeLock wakeLock;
    private MessageHandler messageHandler;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        messageHandler = new MessageHandler(this, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startForeground(1, createNotification());

        Log.d("Service", "Service started");

        if (wakeLock != null) {
            if (wakeLock.isHeld()) {

                ssManager.startSampling(intent.getIntExtra("STIME", 30), intent.getStringArrayListExtra("SENSORS"));

                return START_NOT_STICKY;
            }
        }

        PowerManager powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Sampling");
        wakeLock.acquire();
        ssManager = new SSManager(this, messageHandler, this, wakeLock);

        ssManager.startSampling(intent.getIntExtra("STIME", 30), intent.getStringArrayListExtra("SENSORS"));

        //startHelloLogging();

        return START_NOT_STICKY;
    }

    final Handler handler = new Handler();
    final Runnable r = new Runnable()
    {
        public void run()
        {
            try {
                Log.d("Hello", "\nStill running!\n");
            }
            finally {
                handler.postDelayed(r, 5000);
            }
        }
    };

    void startHelloLogging() {
        r.run();
    }

    void stopHelloLogging() {
        handler.removeCallbacks(r);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d("WARNING", "\n\nLOW MEMORY\n\n");
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_working)
                .setContentTitle("Tremor Tracker")
                .setContentText("Working...")
                .setContentIntent(pendingIntent).build();

        return notification;
    }

    /**
     * Stops the service, stops listening to messages and releases wakelock before destroying service.
     */
    @Override
    public void onDestroy() {
        //endSession();

        stopForeground(true);

        Log.d("Service", "Service destroyed");
        stopHelloLogging();

        super.onDestroy();
    }

    @Override
    public void messageSent(String message) {

    }

    @Override
    public void messageFailed(String error) {

    }

    @Override
    public void onMessageReceived(String message) {

    }

    @Override
    public void onFinished() {

        endSession();

        stopForeground(true);
        stopSelf();
    }

    private void endSession() {
        // Only destroy the message listener if the activity is closed
        if (!ssManager.isActivityRunning()) {
            // Destroy messagelistener
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(ACTIVITY_RESP);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra("id", "message");
            broadcastIntent.putExtra("message", "destroy");
            this.sendBroadcast(broadcastIntent);
        }

        ssManager.unregisterReceivers();
        ssManager.stopSampling(false);

        ssManager.stopTimeCheckLogging();

        ssManager.clearStorage();

        if(wakeLock != null)
            wakeLock.release();
    }
}
