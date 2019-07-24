package uk.ac.aber.movementrecorder.Data;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.widget.Toast;

import com.google.android.gms.wearable.DataMap;

import uk.ac.aber.movementrecorder.Communication.ConnectionHandler;
import uk.ac.aber.movementrecorder.Communication.MessageHandler;
import uk.ac.aber.movementrecorder.R;
import uk.ac.aber.movementrecorder.UI.MainActivity;

import static uk.ac.aber.movementrecorder.Communication.MessageHandler.MESSAGE_RESP;
import static uk.ac.aber.movementrecorder.UI.MainActivity.PREFS_NAME;
import static uk.ac.aber.movementrecorder.UI.NotificationHandler.CHANNEL_ID;
import static uk.ac.aber.movementrecorder.UI.SensorFragment.NUMBERPHONESENSORS;
import static uk.ac.aber.movementrecorder.UI.SensorFragment.NUMBERWATCHSENSORS;
import static uk.ac.aber.movementrecorder.UI.TimeSeriesFragment.ServiceReceiver.TS_RESP;

/**
 * Created by einar on 11/06/2018.
 */

public class SamplingService extends Service implements ConnectionHandler.IConnectionHandler, SensorHandler.ISensorHandler, MessageHandler.IMessageHandler{

    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private ConnectionHandler connectionHandler;
    private DataHandler dataHandler;
    private MessageHandler messageHandler;

    private String pYob;
    private String pWeight;
    private String pHeight;

    private SensorHandler sensorHandler;
    private DataMap dataMap;
    private boolean dataFromWatch = false;
    private String participantID = "";
    private String activity = "";
    private String watchHand = "";
    private int trialNo;
    private int samplingSeconds;
    private PowerManager.WakeLock wakeLock;
    private ServiceReceiver receiver;
    private MessageListener messageReceiver;
    private boolean receiversRegisterd = false;
    private boolean activityRunning = true;

    private static final boolean JSONFILE = true;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        dataHandler = new DataHandler(getApplicationContext()); // this.getContext()
        sensorHandler = new SensorHandler(getApplicationContext(), this);
        connectionHandler = new ConnectionHandler(getApplicationContext(), this);
        messageHandler = new MessageHandler(getApplicationContext(), this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        PowerManager powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Sampling");
        wakeLock.acquire();

        startForeground(1, createNotification());

        registerReceivers();

        connectionHandler.resume();
        participantID = intent.getStringExtra("P-ID");
        activity = intent.getStringExtra("ACTIVITY");
        watchHand = intent.getStringExtra("WATCH_HAND");
        trialNo = intent.getIntExtra("TRIAL_NO", -1);
        samplingSeconds = intent.getIntExtra("DURATION", -1);
        pYob = intent.getStringExtra("YOB");
        pHeight = intent.getStringExtra("HEIGHT");
        pWeight = intent.getStringExtra("WEIGHT");


        start();

        return START_NOT_STICKY;
    }

    private void registerReceivers() {
        // Register broadcast receiver for service
        if (receiversRegisterd)
            return;

        IntentFilter filter = new IntentFilter(ServiceReceiver.PHONE_SS_ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ServiceReceiver();
        this.registerReceiver(receiver, filter);

        // Register broadcast receiver for message
        IntentFilter messageFilter = new IntentFilter(MESSAGE_RESP);
        messageFilter.addCategory(Intent.CATEGORY_DEFAULT);
        messageReceiver = new MessageListener();
        registerReceiver(messageReceiver, messageFilter);

        receiversRegisterd = true;
    }

    private void unregisterReceivers() {
        if (!receiversRegisterd)
            return;
        unregisterReceiver(receiver);
        unregisterReceiver(messageReceiver);

        receiversRegisterd = false;
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_tremor)
                .setContentTitle("Tremor Tracker")
                .setContentText("Working...")
                .setContentIntent(pendingIntent).build();

        return notification;
    }

    private void stopRecording() {
        sensorHandler.stopSensor(true);
    }

    @Override
    public void onDestroy() {

        stopForeground(true);

        super.onDestroy();
    }

    @Override
    public void dataReceived(DataMap data) {
        this.dataMap = data;

        if(data.getBoolean("initial")){
            initialDataReceived();
        }
        else {
            partialDataReceived();
        }

        processData();
    }

    private void initialDataReceived() {
        dataHandler.setDatasetInfo(dataMap);
        messageHandler.send("received");
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(TS_RESP);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra("id", "out");
        broadcastIntent.putExtra("out", "Receiving data from watch");
        this.sendBroadcast(broadcastIntent);
    }

    private void partialDataReceived() {
        dataHandler.addDataFromWatch(dataMap);
        if (!dataMap.getBoolean("done")) {
            dataFromWatch = false;
            messageHandler.send("received");
        }
        else {
            dataFromWatch = true;
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(TS_RESP);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra("id", "out");
            broadcastIntent.putExtra("out", "All data received");
            this.sendBroadcast(broadcastIntent);
        }
    }

    @Override
    public void captureFinished(boolean success) {
//        Intent broadcastIntent = new Intent();
//        broadcastIntent.setAction(ACTION_RESP);
//        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
//        broadcastIntent.putExtra("id", "out");
//        broadcastIntent.putExtra("out", "Phone finished, waiting for watch");
//        this.sendBroadcast(broadcastIntent);
        processData();
    }

    /**
     * Vibrates watch for a duration set by ms.
     * @param ms
     */
    private void vibrate(int ms) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ((Vibrator) this.getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            ((Vibrator) this.getSystemService(VIBRATOR_SERVICE)).vibrate(ms);
        }
    }

    private void processData() {
        if(sensorHandler.hasFinished() && dataFromWatch) {
            dataFromWatch = false;

            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(TS_RESP);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra("id", "out");

            if (dataHandler.getAccXList().size() <= 0 && dataHandler.getGyrXList().size() <= 0 && dataHandler.getMagXList().size() <= 0 && dataHandler.getHrtList().size() <= 0) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Error: Missing data from watch!", Toast.LENGTH_LONG).show();
                    }
                });
                broadcastIntent.putExtra("out", "Error: Missing data from watch!");
                this.sendBroadcast(broadcastIntent);
                return;
            }
            if (sensorHandler.getAccXArray().size() <= 0 && sensorHandler.getGyrXArray().size() <= 0 && sensorHandler.getMagXArray().size() <= 0) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Error: Missing data from phone!", Toast.LENGTH_LONG).show();
                    }
                });
                broadcastIntent.putExtra("out", "Error: Missing data from phone!");
                this.sendBroadcast(broadcastIntent);
                return;
            }
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Data received and uploading...", Toast.LENGTH_SHORT).show();
                }
            });
            broadcastIntent.putExtra("out", "Data received\nUploading...");
            this.sendBroadcast(broadcastIntent);


            dataHandler.allDataReceived(participantID, activity, watchHand, trialNo, dataMap, sensorHandler.getAllData(),
                    sensorHandler.getAllTimestamps(), sensorHandler.getAllAccuracy(),
                    sensorHandler.getStartRecTime(), sensorHandler.getStopRecTime(), JSONFILE, pYob, pWeight, pHeight, getPhoneSensorsToSample(), getBoolWatchSensorsToSample());

            sensorHandler.clearAllSensorValues();

        }
    }

    private boolean[] getPhoneSensorsToSample() {
        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean[] bSensors = new boolean[NUMBERPHONESENSORS];

        bSensors[0] = prefs.getBoolean(getString(R.string.phone_acc_key), false);
        bSensors[1] = prefs.getBoolean(getString(R.string.phone_gyr_key), false);
        bSensors[2] = prefs.getBoolean(getString(R.string.phone_mag_key), false);

        return bSensors;
    }

    private boolean[] getBoolWatchSensorsToSample() {
        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean[] bSensors = new boolean[NUMBERWATCHSENSORS];

        bSensors[0] = prefs.getBoolean(getString(R.string.watch_acc_key), false);
        bSensors[1] = prefs.getBoolean(getString(R.string.watch_gyr_key), false);
        bSensors[2] = prefs.getBoolean(getString(R.string.watch_mag_key), false);
        bSensors[3] = prefs.getBoolean(getString(R.string.watch_hrt_key), false);

        return bSensors;
    }

    private String getWatchSensorsToSample() {
        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String s = "";

        if (prefs.getBoolean(getString(R.string.watch_acc_key), false)) {
            s += "acc,";
        }
        if (prefs.getBoolean(getString(R.string.watch_gyr_key), false)) {
            s += "gyr,";
        }
        if (prefs.getBoolean(getString(R.string.watch_mag_key), false)) {
            s += "mag,";
        }
        if (prefs.getBoolean(getString(R.string.watch_hrt_key), false)) {
            s += "hrt,";
        }

        return s;
    }

    /**
     * Sends a start message to the watch with the seconds
     * for the interval
     */
    private void start() {

        if (samplingSeconds < 0) {
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(TS_RESP);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra("id", "message");
            broadcastIntent.putExtra("message", "error");
            broadcastIntent.putExtra("error", "Error: Seconds are less then zero");
            this.sendBroadcast(broadcastIntent);
            return;
        }
        vibrate(50);
        //int seconds = Integer.parseInt(etSeconds.getText().toString());
        dataFromWatch = false;

        //messageHandler.send("start-" + etSeconds.getText().toString());
        sensorHandler.startSensor(samplingSeconds, getPhoneSensorsToSample());
        messageHandler.send("start:" + getWatchSensorsToSample() + "-" + Integer.toString(samplingSeconds));

    }



    private void endSession() {
        // Only destroy the message listener if the activity is closed
        if (!activityRunning) {
            // Destroy messagelistener
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(TS_RESP);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra("id", "message");
            broadcastIntent.putExtra("message", "destroy");
            this.sendBroadcast(broadcastIntent);
        }

        unregisterReceivers();
        sensorHandler.stopSensor(false);

        messageHandler.send("stop");
        connectionHandler.pause();

        if(wakeLock != null)
            wakeLock.release();

        stopForeground(true);
        stopSelf();
    }

    @Override
    public void messageSent(String message) {

    }

    @Override
    public void messageFailed(String error) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(TS_RESP);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra("id", "message");
        broadcastIntent.putExtra("message", "message fail");
        broadcastIntent.putExtra("error", error);
        this.sendBroadcast(broadcastIntent);
    }

    @Override
    public void onMessageReceived(String message) {

    }

    public class MessageListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getStringExtra("id")){
                case "message":
                    if (intent.getStringExtra("message").toLowerCase().contains("stop")) {
                        // Watch sent stop
                        stopRecording();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public class ServiceReceiver extends BroadcastReceiver {
        public static final String PHONE_SS_ACTION_RESP = "uk.ac.aber.movementrecorder.intent.action.samplingservicemessage";

        /**
         * @param context The Context in which the receiver is running.
         * @param intent  The Intent being received.
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getStringExtra("id")){
                case "message":
                    if (intent.getStringExtra("message").equals("sending_done")) {
                        endSession();
                    }
                    else if (intent.getStringExtra("message").equals("running")) {
                        activityRunning = true;
                    }
                    else if (intent.getStringExtra("message").equals("closing")) {
                        activityRunning = false;
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
