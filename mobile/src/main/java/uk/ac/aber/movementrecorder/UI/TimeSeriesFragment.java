package uk.ac.aber.movementrecorder.UI;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import uk.ac.aber.movementrecorder.Communication.MessageHandler;
import uk.ac.aber.movementrecorder.Communication.MessageService;
import uk.ac.aber.movementrecorder.Data.SamplingService;
import uk.ac.aber.movementrecorder.R;

import static uk.ac.aber.movementrecorder.UI.MainActivity.PREFS_NAME;
import static uk.ac.aber.movementrecorder.Communication.MessageHandler.MESSAGE_RESP;
import static uk.ac.aber.movementrecorder.UI.SensorFragment.NUMBERPHONESENSORS;
import static uk.ac.aber.movementrecorder.UI.SensorFragment.NUMBERWATCHSENSORS;
import static uk.ac.aber.movementrecorder.UI.TimeSeriesFragment.ServiceReceiver.TS_RESP;

/**
 * Created by einar on 11/03/2018.
 */

public class TimeSeriesFragment extends Fragment implements MessageHandler.IMessageHandler{

    private Button btStart;
    private Button btStop;
    private TextView tvInfo;
    private String participantNumber = "";
    private ServiceReceiver receiver;
    private MessageListener messageReceiver;
    private String pYob;
    private String pWeight;
    private String pHeight;
    private boolean receiversRegisterd = false;
    private MessageHandler messageHandler;
    private EditText miscTest;
    private String miscText;

    private InputMethodManager imm;

    private SpinnerActivity spinManager;
    private Spinner sActivity;
    private ArrayAdapter activityAd;
    private Spinner sDurMins;
    private ArrayAdapter durMinsAd;
    private Integer[] durMins;
    private Spinner sDurSecs;
    private ArrayAdapter durSecsAd;
    private Integer[] durSecs;
    private Spinner sTrialNo;
    private ArrayAdapter trialNoAd;
    private Integer[] pTrialNos;
    private Spinner sWatchHand;
    private ArrayAdapter watchHandAd;
    private boolean misc = false;

    String activity = "";
    int duration = 0;
    int trialNo = 0;
    String watchHand = "";


    /**
     * Click listener for the buttons in the fragment
     */
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btStart:
                    duration = (Integer)sDurMins.getSelectedItem();
                    duration *= 60;
                    duration += (Integer)sDurSecs.getSelectedItem();
                    trialNo = (Integer)sTrialNo.getSelectedItem();
                    watchHand = sWatchHand.getSelectedItem().toString();
                    if (startCheck()) {
                        runRemoteStartDialog();
                    }

                    break;
                case R.id.btStop:
                    stop(true);
                    break;
                default:
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.time_series_fragment, container, false);

        spinManager = new SpinnerActivity();
        messageHandler = new MessageHandler(getActivity(), this);
        btStart = view.findViewById(R.id.btStart);
        btStart.setOnClickListener(clickListener);
        btStop = view.findViewById(R.id.btStop);
        btStop.setOnClickListener(clickListener);

        sActivity = view.findViewById(R.id.testType);
        activityAd = ArrayAdapter.createFromResource(getContext(), R.array.actArray,
                android.R.layout.simple_spinner_item);
        activityAd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sActivity.setAdapter(activityAd);
        sActivity.setOnItemSelectedListener(spinManager);

        sDurMins = view.findViewById(R.id.dMins);
        durMins = populateSpinnerArray(10, true);
        durMinsAd = new ArrayAdapter<Integer>(getContext(), android.R.layout.simple_spinner_item, durMins);
        durMinsAd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sDurMins.setAdapter(durMinsAd);
        sDurMins.setOnItemSelectedListener(spinManager);

        sDurSecs = view.findViewById(R.id.dSecs);
        durSecs = populateSpinnerArray(59, true);
        durSecsAd = new ArrayAdapter<Integer>(getContext(), android.R.layout.simple_spinner_item, durSecs);
        durSecsAd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sDurSecs.setAdapter(durSecsAd);
        sDurSecs.setOnItemSelectedListener(spinManager);

        sTrialNo = view.findViewById(R.id.trialNo);
        pTrialNos = populateSpinnerArray(20, false);
        trialNoAd = new ArrayAdapter<Integer>(getContext(), android.R.layout.simple_spinner_item, pTrialNos);
        trialNoAd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sTrialNo.setAdapter(trialNoAd);
        sTrialNo.setOnItemSelectedListener(spinManager);

        sWatchHand = view.findViewById(R.id.watchHand);
        watchHandAd = ArrayAdapter.createFromResource(getContext(), R.array.watArray,
                android.R.layout.simple_spinner_item);
        watchHandAd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sWatchHand.setAdapter(watchHandAd);
        sWatchHand.setOnItemSelectedListener(spinManager);

        tvInfo = view.findViewById(R.id.tvInfo);
        miscTest = view.findViewById(R.id.miscTest);

        btStart.setEnabled(false);
        miscTest.setInputType(InputType.TYPE_NULL);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void registerReceivers() {
        if (receiversRegisterd)
            return;

        // Register broadcast receiver for service
        IntentFilter filter = new IntentFilter(ServiceReceiver.TS_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ServiceReceiver();
        getActivity().registerReceiver(receiver, filter);

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

        getActivity().unregisterReceiver(receiver);
        getActivity().unregisterReceiver(messageReceiver);

        receiversRegisterd = false;
    }

    @Override
    public void onResume() {
        setData();
        super.onResume();
    }

    public void setData() {

        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.participantNumber = prefs.getString(getString(R.string.participantid_key), null);
        pYob = prefs.getString(getString(R.string.yob_key), null);
        pHeight = prefs.getString(getString(R.string.height_key), null);
        pWeight = prefs.getString(getString(R.string.weight_key), null);

        if (isServiceRunning(SamplingService.class)) {
            // Service is running
            btStart.setEnabled(false);
            btStop.setEnabled(true);
            miscText = "Sampling: " + participantNumber + " " + activity + " " + "Trial " + Integer.toString(trialNo);

            tvInfo.setText(miscText);

            // Disable radio buttons
//            for(int i = 0; i< radioGroup.getChildCount(); i++) {
//                radioGroup.getChildAt(i).setEnabled(false);

//            }
        }
        else {
            if (participantNumber == null || pYob == null || pHeight == null || pWeight == null) {
                btStart.setEnabled(false);
                tvInfo.setText("Missing participant data.\nAdd the data by going to \"Participant Data\"");
            } else {
                btStart.setEnabled(true);
                tvInfo.setText(participantNumber);
            }
        }
    }

    private boolean[] getPhoneSensorsToSample() {
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean[] bSensors = new boolean[NUMBERPHONESENSORS];

        bSensors[0] = prefs.getBoolean(getString(R.string.phone_acc_key), false);
        bSensors[1] = prefs.getBoolean(getString(R.string.phone_gyr_key), false);
        bSensors[2] = prefs.getBoolean(getString(R.string.phone_mag_key), false);

        return bSensors;
    }

    private boolean[] getWatchSensorsToSample() {
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean[] bSensors = new boolean[NUMBERWATCHSENSORS];

        bSensors[0] = prefs.getBoolean(getString(R.string.watch_acc_key), false);
        bSensors[1] = prefs.getBoolean(getString(R.string.watch_gyr_key), false);
        bSensors[2] = prefs.getBoolean(getString(R.string.watch_mag_key), false);
        bSensors[3] = prefs.getBoolean(getString(R.string.watch_hrt_key), false);

        return bSensors;
    }

    private void runRemoteStartDialog() {
        // Alert dialog asking whether to start sampling from watch
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Do you want to start the sampling from the watch?\nIf you click yes, you have to touch start on the watch to start the sampling.")
                .setTitle("Remote Start");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (!isServiceRunning(MessageService.class)) {
                    getActivity().startService(new Intent(getActivity(), MessageService.class));
                }
                registerReceivers();
                miscText = "Waiting for start command from watch";
                tvInfo.setText(miscText);
                messageHandler.send("remote_start");
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (!isServiceRunning(MessageService.class)) {
                    getActivity().startService(new Intent(getActivity(), MessageService.class));
                }
                registerReceivers();
                start();
            }
        });
        builder.create().show();
    }

    /*private boolean checkRadiobuttons() {
        activity = "";
        duration = 0;

        switch (radioGroup.getCheckedRadioButtonId()) {
            case -1:
                Toast.makeText(getActivity(), "Please check a radio button", Toast.LENGTH_SHORT).show();
                return false;
            case R.id.rbCS:
                duration = 45;
                activity = "CS";
                break;
            case R.id.rbTUG:
                duration = 45;
                activity = "TUG";
                break;
            case R.id.rbWalk:
                duration = 365;
                activity = "6MW";
                break;
            default:
                return false;
        }
        return true;
    }*/

    private boolean startCheck() {
        boolean[] phonebSensors = getPhoneSensorsToSample();
        if (!phonebSensors[0] && !phonebSensors[1] && !phonebSensors[2]) {
            Toast.makeText(getActivity(), "Please select sensors in the sensor menu.", Toast.LENGTH_LONG).show();
            return false;
        }
        boolean[] watchbSensors = getWatchSensorsToSample();
        if (!watchbSensors[0] && !watchbSensors[1] && !watchbSensors[2] && !watchbSensors[3]) {
            Toast.makeText(getActivity(), "Please select sensors in the sensor menu.", Toast.LENGTH_LONG).show();
            return false;
        }
        if(misc)
            activity = miscTest.getText().toString();
            if(activity.equals("Please enter activity") || activity.equals("")){
                Toast.makeText(getActivity(), "Please enter the name of the activity", Toast.LENGTH_LONG).show();
                return false;
            }

        if(duration == 0){
            Toast.makeText(getActivity(), "Please choose a duration greater than 0.", Toast.LENGTH_LONG).show();
            return false;
        }
        //if(!checkRadiobuttons())
          //  return false;

        return true;
    }

    private void start() {

        btStart.setEnabled(false);
        btStop.setEnabled(true);
        miscText = "Sampling: " + participantNumber + " " + activity + " " + "Trial " + Integer.toString(trialNo);

        tvInfo.setText(miscText);

        // Disable radio buttons
//        for(int i = 0; i< radioGroup.getChildCount(); i++) {
//            radioGroup.getChildAt(i).setEnabled(false);
//        }

        Intent serviceIntent = new Intent(getActivity(), SamplingService.class);
        serviceIntent.putExtra("P-ID", participantNumber);
        serviceIntent.putExtra("ACTIVITY", activity);
        serviceIntent.putExtra("DURATION", duration);
        serviceIntent.putExtra("WATCH_HAND", watchHand);
        serviceIntent.putExtra("TRIAL_NO", trialNo);
        serviceIntent.putExtra("YOB", pYob);
        serviceIntent.putExtra("WEIGHT", pWeight);
        serviceIntent.putExtra("HEIGHT", pHeight);
        getActivity().startService(serviceIntent);
    }

    /**
     * Stops sampling
     */
    private void stop(boolean forcestop) {
        if (forcestop) {
            miscText = "Stopped: " + participantNumber;
            tvInfo.setText(miscText);
            messageHandler.send("stop");
            stopService();
        }
        btStart.setEnabled(true);
        btStop.setEnabled(true);
        // Enable radio buttons
//        for(int i = 0; i< radioGroup.getChildCount(); i++) {
//            radioGroup.getChildAt(i).setEnabled(true);
//        }
        unregisterReceivers();

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(TS_RESP);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra("id", "message");
        broadcastIntent.putExtra("message", "destroy");
        getActivity().sendBroadcast(broadcastIntent);
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

    /**
     * Stops the current service
     */
    private void stopService() {
        getActivity().stopService(new Intent(getActivity(), SamplingService.class));
    }

    private Integer[] populateSpinnerArray(int size, boolean withZero){
        Integer[] arr;
        if (withZero) {
            arr = new Integer[size+1];
            for (int i = 0 ; i <= size ; i++) {
                arr[i] = i;
            }
        } else {
            arr = new Integer[size];
            for (int i = 1 ; i <=size ; i++){
                arr[i-1] = i;
            }
        }
            return arr;
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
                    if (intent.getStringExtra("message").toLowerCase().contains("start")) {
                        // Watch sent start
                        start();
                    }
                    else if (intent.getStringExtra("message").toLowerCase().contains("stop")) {
                        // Watch sent stop
                        stop(false);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public class ServiceReceiver extends BroadcastReceiver {
        public static final String TS_RESP = "uk.ac.aber.movementrecorder.intent.action.ssmessage";

        /**
         * @param context The Context in which the receiver is running.
         * @param intent  The Intent being received.
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getStringExtra("id")){
                case "out":
                    tvInfo.setText(intent.getStringExtra("out"));
                    break;
                case "message":
                    if (intent.getStringExtra("message").equals("upload_complete")) {
                        tvInfo.setText("Upload Complete");
                        stop(false);
                    }
                    else if (intent.getStringExtra("message").equals("message fail")) {
                        tvInfo.setText(intent.getStringExtra("error"));
                        stop(false);
                    }
                    else if (intent.getStringExtra("message").equals("error")) {
                        tvInfo.setText(intent.getStringExtra("error"));
                        stop(false);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public class SpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            miscTest.clearFocus();
            if(parent.toString().contains("testType")){
                String find = parent.getItemAtPosition(pos).toString();
                find = find.toLowerCase();
                switch(find.charAt(0)){
                    case 'c':
                        sDurMins.setSelection(0);
                        sDurSecs.setSelection(45);
                        miscText = "";
                        miscTest.setText(miscText);
                        miscTest.setInputType(InputType.TYPE_NULL);
                        activity = "CST";
                        break;
                    case 't':
                        sDurMins.setSelection(0);
                        sDurSecs.setSelection(45);
                        miscText = "";
                        miscTest.setText(miscText);
                        miscTest.setInputType(InputType.TYPE_NULL);
                        activity = "TUG";
                        break;
                    case '6':
                        sDurMins.setSelection(6);
                        sDurSecs.setSelection(15);
                        miscText = "";
                        miscTest.setText(miscText);
                        miscTest.setInputType(InputType.TYPE_NULL);
                        activity = "6MW";
                        break;
                    case 'o':
                        misc = true;
                        sDurMins.setSelection(0);
                        sDurSecs.setSelection(0);
                        activity = "MSC";
                        miscTest.setInputType(InputType.TYPE_CLASS_TEXT);
                        miscText = "Please enter activity";
                        miscTest.setText(miscText);
                        break;
                    default:
                        break;
                }
            } //else if (parent.toString().contains("durMins")){
//                miscTest.clearFocus();
//            } else if (parent.toString().contains("durSecs")){
//                miscTest.clearFocus();
//            } else if (parent.toString().contains("trialNo")){
//                miscTest.clearFocus();
//            } else if (parent.toString().contains("watchHand")){
//                miscTest.clearFocus();
//            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }
    }
}
