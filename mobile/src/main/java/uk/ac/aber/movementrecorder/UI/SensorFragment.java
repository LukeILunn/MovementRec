package uk.ac.aber.movementrecorder.UI;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import uk.ac.aber.movementrecorder.Communication.MessageHandler;
import uk.ac.aber.movementrecorder.Communication.MessageService;
import uk.ac.aber.movementrecorder.R;

import static uk.ac.aber.movementrecorder.Communication.MessageHandler.MESSAGE_RESP;
import static uk.ac.aber.movementrecorder.UI.MainActivity.PREFS_NAME;
import static uk.ac.aber.movementrecorder.UI.TimeSeriesFragment.ServiceReceiver.TS_RESP;

/**
 * Created by einar on 16/07/2018.
 */

public class SensorFragment extends Fragment implements MessageHandler.IMessageHandler{

    private ImageButton ibEditPhoneSensors;
    private ImageButton ibEditWatchSensors;
    private Button btGetAllSensors;
    private TextView tvSFWatchSensorInfo;
    private TextView tvSFPhoneSensorInfo;
    private static boolean[] phoneChecked;
    private static boolean[] watchChecked;
    private ArrayList<String> chosenPhoneSensors = new ArrayList<String>();
    private ArrayList<String> chosenWatchSensors = new ArrayList<String>();
    private boolean watchSensorPressed = false;
    private String[] WatchSensors;
    private MessageListener messageReceiver;
    private MessageHandler messageHandler;
    private boolean receiversRegisterd = false;
    private ProgressDialog nDialog;
    private boolean progressCanceled = false;
    ArrayList<String> aWatchSensors = new ArrayList<>();

    public static final int NUMBERPHONESENSORS = 3;
    public static final int NUMBERWATCHSENSORS = 4;

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ibSFEditPhoneSensor:
                    openPhoneSensorPickerDialog();
                    break;
                case R.id.tvSFPhoneSensorInfo:
                    openPhoneSensorPickerDialog();
                    break;
                case R.id.ibSFEditWatchSensor:
                    getAvailableWatchSensors();
                    //messageHandler.send("sensor_info");
                    break;
                case R.id.tvSFWatchSensorInfo:
                    getAvailableWatchSensors();
                    //messageHandler.send("sensor_info");
                    break;
                case R.id.btGetAllSensors:
                    showAllSensors();
                    break;
                default:
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sensor_fragment, container, false);

        ibEditPhoneSensors = view.findViewById(R.id.ibSFEditPhoneSensor);
        ibEditPhoneSensors.setOnClickListener(clickListener);
        ibEditWatchSensors = view.findViewById(R.id.ibSFEditWatchSensor);
        ibEditWatchSensors.setOnClickListener(clickListener);
        btGetAllSensors = view.findViewById(R.id.btGetAllSensors);
        btGetAllSensors.setOnClickListener(clickListener);
        tvSFWatchSensorInfo = view.findViewById(R.id.tvSFWatchSensorInfo);
        tvSFWatchSensorInfo.setOnClickListener(clickListener);
        tvSFPhoneSensorInfo = view.findViewById(R.id.tvSFPhoneSensorInfo);
        tvSFPhoneSensorInfo.setOnClickListener(clickListener);


        phoneChecked = new boolean[NUMBERPHONESENSORS];
        watchChecked = new boolean[NUMBERWATCHSENSORS];

        messageHandler = new MessageHandler(getActivity(), this);

        return view;
    }

    @Override
    public void onResume() {

        if (!isServiceRunning(MessageService.class)) {
            getActivity().startService(new Intent(getActivity(), MessageService.class));
        }
        registerReceivers();

        String s = "";
        boolean[] bSensors = getPhoneValues();
        phoneChecked[0] = bSensors[0];
        phoneChecked[1] = bSensors[1];
        phoneChecked[2] = bSensors[2];

        final String[] sensors = getAvailablePhoneSensors();
        printSelectedPhoneSensors(sensors);
        setChosenPhoneSensors(sensors);

        boolean[] wbSensors = getWatchValues();
        watchChecked[0] = wbSensors[0];
        watchChecked[1] = wbSensors[1];
        watchChecked[2] = wbSensors[2];
        watchChecked[3] = wbSensors[3];

        printSelectedWatchSensors(new String[] {"Accelerometer","Gyroscope","Magnetometer","Heart Rate"});
        setChosenWatchSensors(new String[] {"Accelerometer","Gyroscope","Magnetometer","Heart Rate"});


        super.onResume();
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onPause() {
        unregisterReceivers();

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(TS_RESP);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra("id", "message");
        broadcastIntent.putExtra("message", "destroy");
        getActivity().sendBroadcast(broadcastIntent);

        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void openWatchSensorPickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = getActivity().getLayoutInflater().inflate(R.layout.sensor_picker_list, null);
        builder.setView(view);
        final AlertDialog pickerDialog = builder.create();

        final String[] sensors = aWatchSensors.toArray(new String[0]);

        final ListPickerAdapter listPickerAdapter = new ListPickerAdapter(getContext(), sensors, chosenWatchSensors);
        ListView lvSensorList = view.findViewById(R.id.lvSensors);
        lvSensorList.setAdapter(listPickerAdapter);


        ImageButton ibSensorSelectDone = view.findViewById(R.id.ibSensorPickDone);
        ibSensorSelectDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                watchChecked = listPickerAdapter.getCheckboxstate();
                setChosenWatchSensors(sensors);
                printSelectedWatchSensors(sensors);
                storeWatchValues(sensors);
                pickerDialog.dismiss();
            }
        });
        pickerDialog.show();
    }

    private void openPhoneSensorPickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = getActivity().getLayoutInflater().inflate(R.layout.sensor_picker_list, null);
        builder.setView(view);
        final AlertDialog pickerDialog = builder.create();

        final String[] sensors = getAvailablePhoneSensors();

        final ListPickerAdapter listPickerAdapter = new ListPickerAdapter(getContext(), sensors, chosenPhoneSensors);
        ListView lvSensorList = view.findViewById(R.id.lvSensors);
        lvSensorList.setAdapter(listPickerAdapter);


        ImageButton ibSensorSelectDone = view.findViewById(R.id.ibSensorPickDone);
        ibSensorSelectDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneChecked = listPickerAdapter.getCheckboxstate();
                setChosenPhoneSensors(sensors);
                printSelectedPhoneSensors(sensors);
                storePhoneValues(sensors);
                pickerDialog.dismiss();
            }
        });
        pickerDialog.show();
    }

    /**
     * Stores the data in shared preferences for PHONE
     */
    private void storePhoneValues(String[] sensors) {
        SharedPreferences.Editor editor = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();

        Boolean acc = false, gyr = false, mag = false;

        for (int i = 0; i<sensors.length; i++) {
            if (sensors[i].equals("Accelerometer")) {
                editor.putBoolean(getString(R.string.phone_acc_key), phoneChecked[i]);
                acc = true;
            }
            else if (sensors[i].equals("Gyroscope")) {
                editor.putBoolean(getString(R.string.phone_gyr_key), phoneChecked[i]);
                gyr = true;
            }
            else if (sensors[i].equals("Magnetometer")) {
                editor.putBoolean(getString(R.string.phone_mag_key), phoneChecked[i]);
                mag = true;
            }
        }

        if (!acc)
            editor.putBoolean(getString(R.string.phone_acc_key), false);
        if (!gyr)
            editor.putBoolean(getString(R.string.phone_gyr_key), false);
        if (!mag)
            editor.putBoolean(getString(R.string.phone_mag_key), false);

        editor.commit();
    }

    private boolean[] getPhoneValues() {
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean[] bSensors = new boolean[3];

        bSensors[0] = prefs.getBoolean(getString(R.string.phone_acc_key), false);
        bSensors[1] = prefs.getBoolean(getString(R.string.phone_gyr_key), false);
        bSensors[2] = prefs.getBoolean(getString(R.string.phone_mag_key), false);

        return bSensors;
    }

    /**
     * Stores the data in shared preferences for WATCH
     */
    private void storeWatchValues(String[] sensors) {
        SharedPreferences.Editor editor = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();

        Boolean acc = false, gyr = false, mag = false, hrt = false;

        for (int i = 0; i<sensors.length; i++) {
            if (sensors[i].equals("Accelerometer")) {
                editor.putBoolean(getString(R.string.watch_acc_key), watchChecked[i]);
                acc = true;
            }
            else if (sensors[i].equals("Gyroscope")) {
                editor.putBoolean(getString(R.string.watch_gyr_key), watchChecked[i]);
                gyr = true;
            }
            else if (sensors[i].equals("Magnetometer")) {
                editor.putBoolean(getString(R.string.watch_mag_key), watchChecked[i]);
                mag = true;
            }
            else if (sensors[i].equals("Heart Rate")) {
                editor.putBoolean(getString(R.string.watch_hrt_key), watchChecked[i]);
                hrt = true;
            }
        }

        if (!acc)
            editor.putBoolean(getString(R.string.watch_acc_key), false);
        if (!gyr)
            editor.putBoolean(getString(R.string.watch_gyr_key), false);
        if (!mag)
            editor.putBoolean(getString(R.string.watch_mag_key), false);
        if (!hrt)
            editor.putBoolean(getString(R.string.watch_hrt_key), false);

        editor.commit();
    }

    private boolean[] getWatchValues() {
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean[] bSensors = new boolean[NUMBERWATCHSENSORS];

        bSensors[0] = prefs.getBoolean(getString(R.string.watch_acc_key), false);
        bSensors[1] = prefs.getBoolean(getString(R.string.watch_gyr_key), false);
        bSensors[2] = prefs.getBoolean(getString(R.string.watch_mag_key), false);
        bSensors[3] = prefs.getBoolean(getString(R.string.watch_hrt_key), false);

        return bSensors;
    }

    private void printSelectedPhoneSensors(String[] sensors) {
        String s = "";
        for (int i = 0; i<sensors.length; i++) {
            if (phoneChecked[i]) {
                s += sensors[i] + "\n";
            }
        }
        if (s.equals(""))
            tvSFPhoneSensorInfo.setText("No sensors selected");
        else
            tvSFPhoneSensorInfo.setText(s);
    }

    private void setChosenPhoneSensors(String[] sensors) {
        chosenPhoneSensors.clear();
        for (int i = 0; i<sensors.length; i++) {
            if (phoneChecked[i]) {
                chosenPhoneSensors.add(sensors[i]);
            }
        }
    }

    private void printSelectedWatchSensors(String[] sensors) {
        String s = "";
        for (int i = 0; i<sensors.length; i++) {
            if (watchChecked[i]) {
                s += sensors[i] + "\n";
            }
        }
        if (s.equals(""))
            tvSFWatchSensorInfo.setText("No sensors selected");
        else
            tvSFWatchSensorInfo.setText(s);
    }

    private void setChosenWatchSensors(String[] sensors) {
        chosenWatchSensors.clear();
        for (int i = 0; i<sensors.length; i++) {
            if (watchChecked[i]) {
                chosenWatchSensors.add(sensors[i]);
            }
        }
    }


    private String[] getAvailablePhoneSensors() {
        PackageManager manager = getContext().getPackageManager();
        ArrayList<String> sensors = new ArrayList<String>();
        if (manager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER))
            sensors.add("Accelerometer");
        if (manager.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE))
            sensors.add("Gyroscope");
        if (manager.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS))
            sensors.add("Magnetometer");

        return sensors.toArray(new String[0]);
    }

    private void getAvailableWatchSensors() {
        messageHandler.send("sensor_info");

        progressCanceled = false;

        nDialog = new ProgressDialog(getActivity());
        nDialog.setMessage("Loading..");
        nDialog.setTitle("Getting Sensors");
        nDialog.setIndeterminate(false);
        nDialog.setCancelable(true);
        nDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                progressCanceled = true;
            }
        });
        nDialog.show();
    }

    private void setAvailableWatchSensors(String sSensors) {
        aWatchSensors.clear();
        if (progressCanceled) {
            progressCanceled = false;
            return;
        }

        if (nDialog != null)
            nDialog.dismiss();

        if (sSensors == "")
            return;

        StringBuilder s = new StringBuilder(sSensors);

        int i = s.indexOf(":");
        if (i != -1) {
            s.delete(0, i + 1);
        }

        while (s.toString().contains(",")) {
            i = s.indexOf(",");
            aWatchSensors.add(s.substring(0, i));
            s.delete(0, i + 1);
//            if (i+1 >= s.length())
//                break;
        }
    }

    private ArrayList<String> getAllAvailablePhoneSensors() {
        ArrayList<String> sensors = new ArrayList<String>();

        SensorManager mSensorManager= (SensorManager) getContext().getSystemService(getContext().SENSOR_SERVICE);
        List<Sensor> msensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        for (Sensor s:msensorList) {
            sensors.add(s.getName());
        }

        return sensors;
    }

    private void showAllSensors() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = getActivity().getLayoutInflater().inflate(R.layout.sensor_list_dialog, null);
        builder.setView(view);
        final AlertDialog pickerDialog = builder.create();

        TextView tvAllSensors = view.findViewById(R.id.tvAllSensors);
        tvAllSensors.setMovementMethod(new ScrollingMovementMethod());
        ArrayList<String> allSensors = getAllAvailablePhoneSensors();
        StringBuilder stringBuilder = new StringBuilder("Available sensors:\n");
        for (String s:allSensors) {
            stringBuilder.append("--" + s + "\n");
        }
        tvAllSensors.setText(stringBuilder.toString());
        pickerDialog.show();
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


    private void registerReceivers() {
        if (receiversRegisterd)
            return;

        // Register broadcast receiver for message
        IntentFilter messageFilter = new IntentFilter(MESSAGE_RESP);
        messageFilter.addCategory(Intent.CATEGORY_DEFAULT);
        messageReceiver = new MessageListener();
        getActivity().registerReceiver(messageReceiver, messageFilter);

        receiversRegisterd = true;
    }

    private void unregisterReceivers() {
        if (!receiversRegisterd)
            return;

        getActivity().unregisterReceiver(messageReceiver);
        receiversRegisterd = false;
    }

    public class MessageListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getStringExtra("id")){
                case "message":
                    if (intent.getStringExtra("message").toLowerCase().contains("sensor_info")) {
                        // Watch sent start
                        setAvailableWatchSensors(intent.getStringExtra("message"));
                        openWatchSensorPickerDialog();
                    }
                    break;
                default:
                    break;
            }
        }
    }

}
