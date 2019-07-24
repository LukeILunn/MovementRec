package uk.ac.aber.movementrecorder.UI;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import androidx.wear.widget.BoxInsetLayout;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import uk.ac.aber.movementrecorder.Background.MessageHandler;
import uk.ac.aber.movementrecorder.Background.MessageService;
import uk.ac.aber.movementrecorder.R;
import uk.ac.aber.movementrecorder.Background.SCService;

import static uk.ac.aber.movementrecorder.Background.MessageHandler.MESSAGE_RESP;
import static uk.ac.aber.movementrecorder.Background.SSManager.ActivityReceiver.ACTIVITY_RESP;
import static uk.ac.aber.movementrecorder.UI.MainActivity.ServiceReceiver.SERVICE_RESP;

public class MainActivity extends WearableActivity implements MessageHandler.IMessageHandler{

    private TextView infoText;
    private TextView tvStartStop;
    private TextView tvHead;
    private BoxInsetLayout bilBack;
    private int background = -1000;
    private boolean remoteStartVar = false;
    private ServiceReceiver serviceReceiver;
    private MessageListener messageListener;
    private MessageHandler messageHandler;
    private boolean receiversRegisterd = false;
    private int samplingTime = 0;

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.bilBack:
                    if (remoteStartVar)
                        messageHandler.send("start");
                    else
                        stopRecording();
                    break;
                case R.id.tvStartStop:
                    if (remoteStartVar)
                        messageHandler.send("start");
                    else
                        stopRecording();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvHead = findViewById(R.id.tvHead);

        tvStartStop = findViewById(R.id.tvStartStop);
        tvStartStop.setVisibility(View.GONE);

        bilBack = findViewById(R.id.bilBack);

        infoText = (TextView) findViewById(R.id.tvInfo);
        infoText.setText("Not active");

        messageHandler = new MessageHandler(this, this);

        // Start message service
        //this.startService(new Intent(this, MessageService.class));

        // Register broadcast receiver for service

        // Enables Always-on
        setAmbientEnabled();
    }

    private void registerReseivers() {
        if (receiversRegisterd)
            return;
        IntentFilter serviceFilter = new IntentFilter(SERVICE_RESP);
        serviceFilter.addCategory(Intent.CATEGORY_DEFAULT);
        serviceReceiver = new ServiceReceiver();
        this.registerReceiver(serviceReceiver, serviceFilter);

        // Register broadcast receiver for messages
        IntentFilter messageFilter = new IntentFilter(MESSAGE_RESP);
        messageFilter.addCategory(Intent.CATEGORY_DEFAULT);
        messageListener = new MessageListener();
        this.registerReceiver(messageListener, messageFilter);
        receiversRegisterd = true;
    }

    private void unregisterReceivers() {
        if(!receiversRegisterd)
            return;
        unregisterReceiver(serviceReceiver);
        unregisterReceiver(messageListener);
        receiversRegisterd = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!isServiceRunning(MessageService.class)) {
            this.startService(new Intent(this, MessageService.class));
        }

        // Let the service know that the app is in the foreground
        if (isServiceRunning(SCService.class)) {
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(ACTIVITY_RESP);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra("id", "message");
            broadcastIntent.putExtra("message", "running");
            this.sendBroadcast(broadcastIntent);

            setRemoteStop();
        }
        else {
            if (background != -1000) {
                ViewGroup viewGroup = (ViewGroup) ((ViewGroup) (findViewById(android.R.id.content))).getChildAt(0);
                viewGroup.setBackgroundColor(background);
                tvStartStop.setVisibility(View.GONE);
                tvHead.setVisibility(View.VISIBLE);
            }
        }

        registerReseivers();
    }

    @Override
    protected void onStop() {
        if (!isServiceRunning(SCService.class)) {
            // Destroy messagelistener
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(ACTIVITY_RESP);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra("id", "message");
            broadcastIntent.putExtra("message", "destroy");
            this.sendBroadcast(broadcastIntent);
        }
        else {
            // Let the service know that the app is no longer in the foreground
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(ACTIVITY_RESP);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra("id", "message");
            broadcastIntent.putExtra("message", "closing");
            this.sendBroadcast(broadcastIntent);
        }

        unregisterReceivers();
        super.onStop();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    // ForegroundService requires API Level 26. Minimum in this app is 23

    /**
     * Starts the service SCService
     */
    private void mainStartService(ArrayList<String> sensors) {

        // UI

//        if (remoteStartVar) {
//            ViewGroup viewGroup = (ViewGroup) ((ViewGroup) (findViewById(android.R.id.content))).getChildAt(0);
//            viewGroup.setBackgroundColor(background);
//        }
//        stopButton.setVisibility(View.VISIBLE);
//        startButton.setVisibility(View.GONE);
//        tvStartStop.setVisibility(View.GONE);
//        tvHead.setVisibility(View.VISIBLE);

        // SERVICE

        Intent serviceIntent = new Intent(this, SCService.class);
        serviceIntent.putExtra("STIME", samplingTime);
        serviceIntent.putExtra("SENSORS", sensors);
        remoteStartVar = false;
        this.startService(serviceIntent);

        //startService(new Intent(this, SCService.class));
        infoText.setText("Active");

        setRemoteStop();
    }

    /**
     * Stops the service SCService
     */
    private void mainStopService() {
        stopService(new Intent(this, SCService.class));
        infoText.setText("Not active");
    }

    /**
     * Stops recording
     */
    private void stopRecording() {
        finishSession();

        // Tells SSManager to stop recording
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ACTIVITY_RESP);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra("id", "message");
        broadcastIntent.putExtra("message", "stop");
        this.sendBroadcast(broadcastIntent);
    }

    private void setRemoteStart() {

        // UI

        if (!bilBack.hasOnClickListeners())
            bilBack.setOnClickListener(clickListener);
        if (!tvStartStop.hasOnClickListeners())
            tvStartStop.setOnClickListener(clickListener);

        ViewGroup viewGroup = (ViewGroup) ((ViewGroup) (findViewById(android.R.id.content))).getChildAt(0);
        if (background == -1000) {
            ColorDrawable bkgn = (ColorDrawable) viewGroup.getBackground();
            background = bkgn.getColor();
        }
        viewGroup.setBackgroundColor(Color.GREEN);

        tvStartStop.setText("Touch to start");
        tvStartStop.setVisibility(View.VISIBLE);
        tvHead.setVisibility(View.GONE);
        infoText.setVisibility(View.GONE);

        // VAR

        remoteStartVar = true;
    }

    private void setRemoteStop() {

        if (!bilBack.hasOnClickListeners())
            bilBack.setOnClickListener(clickListener);
        if (!tvStartStop.hasOnClickListeners())
            tvStartStop.setOnClickListener(clickListener);

        ViewGroup viewGroup = (ViewGroup) ((ViewGroup) (findViewById(android.R.id.content))).getChildAt(0);
        if (background == -1000) {
            ColorDrawable bkgn = (ColorDrawable) viewGroup.getBackground();
            background = bkgn.getColor();
        }
        viewGroup.setBackgroundColor(Color.RED);

        tvStartStop.setText("Touch to stop");
        tvStartStop.setVisibility(View.VISIBLE);
        tvHead.setVisibility(View.GONE);
        infoText.setVisibility(View.GONE);
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private String getAvailableSensors() {
        PackageManager manager = getPackageManager();
        StringBuilder sensors = new StringBuilder();
        if (manager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER))
            sensors.append("Accelerometer,");
        if (manager.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE))
            sensors.append("Gyroscope,");
        if (manager.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS))
            sensors.append("Magnetometer,");
        if (manager.hasSystemFeature(PackageManager.FEATURE_SENSOR_HEART_RATE))
            sensors.append("Heart Rate,");

        return sensors.toString();
    }

    private ArrayList<String> getSensorsToSampleFrom(String message) {
        StringBuilder s = new StringBuilder(message);
        int sensorCount = StringUtils.countMatches(message, ",");
        ArrayList<String> sensors = new ArrayList<>();

        s.delete(0, s.indexOf(":")+1);

        for (int i = 0; i < sensorCount; i++) {
            int indexnum = s.indexOf(",");
            sensors.add(s.substring(0, indexnum));
            s.delete(0, indexnum + 1);
        }

        return sensors;
    }

    private void finishSession() {
        ViewGroup viewGroup = (ViewGroup) ((ViewGroup) (findViewById(android.R.id.content))).getChildAt(0);
        viewGroup.setBackgroundColor(background);
        tvStartStop.setVisibility(View.GONE);
        tvHead.setVisibility(View.VISIBLE);

        if (bilBack.hasOnClickListeners())
            bilBack.setOnClickListener(null);
        if (tvStartStop.hasOnClickListeners())
            tvStartStop.setOnClickListener(null);
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

    public class MessageListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getStringExtra("id")){
                case "message":
                    if(intent.getStringExtra("message").toLowerCase().contains("start")) {
                        if (intent.getStringExtra("message").toLowerCase().contains("remote_start")) {
                            setRemoteStart();
                        }
                        else {
                            vibrate(200);
                            String message = intent.getStringExtra("message");
                            String samplingTimeString = message.substring(message.indexOf('-') + 1, message.length());
                            samplingTime = Integer.parseInt(samplingTimeString);
                            mainStartService(getSensorsToSampleFrom(message));
                        }
                    }
                    else if (intent.getStringExtra("message").equals("sensor_info")) {
                        // Get sensor info
                        messageHandler.send("sensor_info:" + getAvailableSensors());
                    }
                    else if (intent.getStringExtra("message").equals("stop")) {
                        finishSession();
                    }
                    break;
                default:
                    break;
            }
        }
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

    public class ServiceReceiver extends BroadcastReceiver {
        public static final String SERVICE_RESP = "uk.ac.aber.movementrecorder.intent.action.ssmessage";

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getStringExtra("id")){
                case "out":
                    infoText.setText(intent.getStringExtra("out"));
                    break;
                case "message":
                    if (intent.getStringExtra("message").equals("finished")) {
                        finishSession();
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
