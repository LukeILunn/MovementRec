package uk.ac.aber.movementrecorder.Background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import uk.ac.aber.movementrecorder.UI.MainActivity;

import static android.content.Context.VIBRATOR_SERVICE;
import static uk.ac.aber.movementrecorder.Background.MessageHandler.MESSAGE_RESP;
import static uk.ac.aber.movementrecorder.UI.MainActivity.ServiceReceiver.SERVICE_RESP;

/**
 * Created by einar on 23/02/2018.
 */

public class SSManager implements SensorHandler.ISensorHandler{

    private SensorHandler sensorHandler;
    private DataStorage storage;
    private ConnectionHandler connectionHandler;
    private Handler toasthandler = new Handler(Looper.getMainLooper());
    private MessageHandler messageHandler;
    private MessageListener messageListener;

    private float lastSample = 0f;
    private int samplingTime;

    private Context serviceContext;

    private ActivityReceiver receiver;
    private boolean receiversRegisterd = false;

    private boolean activityRunning = true;



    public ISSManager issManager;

    public interface ISSManager {
        void onFinished();
    }

    /**
     * SSManager constructor</b>
     * Creates new ConnectionHandler</b>
     * Creates new DataStorage</b>
     * Creates new AccelerometerHandler
     *
     * @param serviceContext
     */
    public SSManager(Context serviceContext, MessageHandler messageHandler, ISSManager issManager, PowerManager.WakeLock wakeLock) {

        this.issManager = issManager;
        this.serviceContext = serviceContext;
        connectionHandler = new ConnectionHandler(serviceContext);
        storage = new DataStorage();
        sensorHandler = new SensorHandler(serviceContext, storage, this, wakeLock);
        this.messageHandler = messageHandler;

        registerReceivers();
    }

    private void registerReceivers() {
        if (receiversRegisterd)
            return;
        // Register broadcast receiver for service
        IntentFilter filter = new IntentFilter(ActivityReceiver.ACTIVITY_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ActivityReceiver();
        serviceContext.registerReceiver(receiver, filter);

        // Register broadcast receiver for messages
        IntentFilter messageFilter = new IntentFilter(MESSAGE_RESP);
        messageFilter.addCategory(Intent.CATEGORY_DEFAULT);
        messageListener = new MessageListener();
        serviceContext.registerReceiver(messageListener, messageFilter);

        receiversRegisterd = true;
    }

    public void unregisterReceivers() {
        if (!receiversRegisterd)
            return;
        serviceContext.unregisterReceiver(receiver);
        serviceContext.unregisterReceiver(messageListener);

        receiversRegisterd = false;
    }

    /**
     * Starts sampling data from the accelerometer
     * @param time
     */
    public void startSampling(int time, ArrayList<String> sensors) {
        samplingTime = time;
        sensorHandler.startSensor(time, sensors);
    }

    /**
     * Stops sampling </b>
     * Data will be sent
     */
    public void stopSampling(boolean processData) {
        sensorHandler.stopSensor(processData);
        startTimeCheckLogging();
    }

    /**
     * Called by AccelerometerHandler through interface
     * when finished with sampling of data.</b>
     * Calcultes magnitude and calls void sendData()
     */
    @Override
    public void processData() {
        vibrate(1000);
        sendInitialInfo();
    }

    @Override
    public void newData(float timeStamp) {
        lastSample = (float)(System.nanoTime() / 1000000);
    }

    final Handler handler = new Handler();
    final Runnable doggerLogger = new Runnable()
    {
        public void run()
        {
            try {
                if ((float)(System.nanoTime() / 1000000) - lastSample > 100) {
                   // vibrate(200);
                    // MISSING DATA!
                }
            }
            finally {
                handler.postDelayed(doggerLogger, 100);
            }
        }
    };

    void startTimeCheckLogging() {
        doggerLogger.run();
    }

    void stopTimeCheckLogging() {
        handler.removeCallbacks(doggerLogger);
    }

    /**
     * Vibrates watch for a duration set by ms.
     * @param ms
     */
    private void vibrate(int ms) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ((Vibrator) serviceContext.getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            ((Vibrator) serviceContext.getSystemService(VIBRATOR_SERVICE)).vibrate(ms);
        }
    }

    private void sendInitialInfo() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(SERVICE_RESP);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra("id", "message");
        broadcastIntent.putExtra("message", "finished");
        serviceContext.sendBroadcast(broadcastIntent);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        connectionHandler.sendInitialInfo(samplingTime, sdf.format(new Date()), storage.getStartRecTime(), storage.getStopRecTime(), Build.MANUFACTURER, Build.MODEL, Integer.toString(Build.VERSION.SDK_INT), Build.VERSION.RELEASE);
    }

    private void sendPartialData() {
//        connectionHandler.sendPartialData(storage.getPartialAccX(), storage.getPartialAccY(), storage.getPartialAccZ(), storage.getPartialAccTimestamp(),
//                storage.getPartialAccAccuracy(), storage.getPartialGyrX(), storage.getPartialGyrY(), storage.getPartialGyrZ(),
//                storage.getPartialGyrTimestamp(), storage.getPartialGyrAccuracy(), storage.isEmpty());


        connectionHandler.sendPartialDataTwo(storage.getAllPartial(), storage.getSensorNames(), storage.isEmpty());

        if(storage.isEmpty()) {
            storage.clear();
            toasthandler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(serviceContext,
                            "Data Sent",
                            Toast.LENGTH_SHORT).show();
                }
            });
            issManager.onFinished();
        }
    }

    public void clearStorage() {
        storage.clear();
    }

    public class MessageListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getStringExtra("id")){
                case "message":
                    if (intent.getStringExtra("message").equals("stop")) {
                        //stopSampling(false, true);
                        issManager.onFinished();
                    }
                    else if (intent.getStringExtra("message").equals("received")) {
                        sendPartialData();
                    }
                default:
                    break;
            }
        }
    }

    public class ActivityReceiver extends BroadcastReceiver {
        public static final String ACTIVITY_RESP = "uk.ac.aber.movementrecorder.intent.action.activitymessage";

        /**
         * @param context The Context in which the receiver is running.
         * @param intent  The Intent being received.
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getStringExtra("id")){
                case "message":
                    if (intent.getStringExtra("message").equals("stop")) {
                        messageHandler.send("stop");
                        stopSampling(true);
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

    public boolean isActivityRunning() {
        return activityRunning;
    }
}
