package uk.ac.aber.movementrecorder.Background;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;

import static android.content.Context.VIBRATOR_SERVICE;

/**
 * Created by einar on 30/01/2018.
 */

public class ConnectionHandler {
    private Context context;
    private static final String PATH = "/tremor_tracker_message";

    /**
     * ConnectionHandler constructor. </b>
     * Creates interface.</b>
     * Starts the listener for messages</b>
     * @param context
     */
    public ConnectionHandler(Context context) {
        this.context = context;
        //startMessageListener();
    }

    public void sendInitialInfo(int sampleTime, String timendate, float startRecTime, float stopRecTime, String manufacturer, String model, String version, String release) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/tremor-allaxis");
        putDataMapRequest.getDataMap().putFloat("startRecTime", startRecTime);
        putDataMapRequest.getDataMap().putFloat("stopRecTime", stopRecTime);
        //putDataMapRequest.getDataMap().putInt("sample_time", sampleTime);
        putDataMapRequest.getDataMap().putString("timendate", timendate);
        putDataMapRequest.getDataMap().putBoolean("initial", true);
        putDataMapRequest.getDataMap().putString("manufacturer", manufacturer);
        putDataMapRequest.getDataMap().putString("model", model);
        putDataMapRequest.getDataMap().putString("version", version);
        putDataMapRequest.getDataMap().putString("release", release);

        PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
        putDataRequest.setUrgent(); //Instant

        Task<DataItem> dataItemTask = Wearable.getDataClient(context).putDataItem(putDataRequest);
        dataItemTask.addOnSuccessListener(new OnSuccessListener<DataItem>() {
            @Override
            public void onSuccess(DataItem dataItem) {
                // Data sent
                //wakeLock.release();
                //vibrate(50);
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Data not sent
                        //wakeLock.release();
                        vibrate(2000);
                    }
                });
    }

    public void sendPartialData(float[] accx, float[] accy, float[] accz, float[] acct, ArrayList<Integer> acca,
                            float[] gyrx, float[] gyry, float[] gyrz, float[] gyrt, ArrayList<Integer> gyra, final boolean done) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/tremor-allaxis");
        putDataMapRequest.getDataMap().putFloatArray("accx", accx);
        putDataMapRequest.getDataMap().putFloatArray("accy", accy);
        putDataMapRequest.getDataMap().putFloatArray("accz", accz);
        putDataMapRequest.getDataMap().putFloatArray("acct", acct);
        putDataMapRequest.getDataMap().putIntegerArrayList("acca", acca);
        putDataMapRequest.getDataMap().putFloatArray("gyrx", gyrx);
        putDataMapRequest.getDataMap().putFloatArray("gyry", gyry);
        putDataMapRequest.getDataMap().putFloatArray("gyrz", gyrz);
        putDataMapRequest.getDataMap().putFloatArray("gyrt", gyrt);
        putDataMapRequest.getDataMap().putIntegerArrayList("gyra", gyra);
        putDataMapRequest.getDataMap().putBoolean("done", done);
        putDataMapRequest.getDataMap().putBoolean("initial", false);

        PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
        putDataRequest.setUrgent(); //Instant

        Task<DataItem> dataItemTask = Wearable.getDataClient(context).putDataItem(putDataRequest);
        dataItemTask.addOnSuccessListener(new OnSuccessListener<DataItem>() {
            @Override
            public void onSuccess(DataItem dataItem) {
                // Data sent
                //wakeLock.release();
                if(done)
                    vibrate(50);
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Data not sent
                        //wakeLock.release();
                        vibrate(2000);
                    }
                });
    }




    public void sendPartialDataTwo (ArrayList<float[]> allPartial, ArrayList<String> sensorNames, final boolean done) {

        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/tremor-allaxis");
//        putDataMapRequest.getDataMap().putFloatArray("accx", allPartial.get(0));
//        putDataMapRequest.getDataMap().putFloatArray("accy", allPartial.get(1));
//        putDataMapRequest.getDataMap().putFloatArray("accz", allPartial.get(2));
//        putDataMapRequest.getDataMap().putFloatArray("acct", allPartial.get(3));
//
//        putDataMapRequest.getDataMap().putFloatArray("gyrx", allPartial.get(4));
//        putDataMapRequest.getDataMap().putFloatArray("gyry", allPartial.get(5));
//        putDataMapRequest.getDataMap().putFloatArray("gyrz", allPartial.get(6));
//        putDataMapRequest.getDataMap().putFloatArray("gyrt", allPartial.get(7));
//
//        putDataMapRequest.getDataMap().putFloatArray("magx", allPartial.get(8));
//        putDataMapRequest.getDataMap().putFloatArray("magy", allPartial.get(9));
//        putDataMapRequest.getDataMap().putFloatArray("magz", allPartial.get(10));
//        putDataMapRequest.getDataMap().putFloatArray("magt", allPartial.get(11));
//
//        putDataMapRequest.getDataMap().putFloatArray("hrt", allPartial.get(12));
//        putDataMapRequest.getDataMap().putFloatArray("hrtt", allPartial.get(13));
//
//        String[] names = {  "accx", "accy", "accz", "acct",
//                            "gyrx", "gyry", "gyrz", "gyrt",
//                            "magx", "magy", "magz", "magt",
//                            "hrt", "hrtt"};

        ArrayList<String> sNames = new ArrayList<>();

        if (sensorNames.contains("acc")) {
            sNames.add("accx");
            sNames.add("accy");
            sNames.add("accz");
            sNames.add("acct");
        }
        if (sensorNames.contains("gyr")) {
            sNames.add("gyrx");
            sNames.add("gyry");
            sNames.add("gyrz");
            sNames.add("gyrt");
        }
        if (sensorNames.contains("mag")) {
            sNames.add("magx");
            sNames.add("magy");
            sNames.add("magz");
            sNames.add("magt");
        }
        if (sensorNames.contains("hrt")) {
            sNames.add("hrt");
            sNames.add("hrtt");
        }

        for (int i = 0; i < allPartial.size(); i++) {
            putDataMapRequest.getDataMap().putFloatArray(sNames.get(i), allPartial.get(i));
        }

        putDataMapRequest.getDataMap().putBoolean("done", done);
        putDataMapRequest.getDataMap().putBoolean("initial", false);

        PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
        putDataRequest.setUrgent(); //Instant

        Task<DataItem> dataItemTask = Wearable.getDataClient(context).putDataItem(putDataRequest);
        dataItemTask.addOnSuccessListener(new OnSuccessListener<DataItem>() {
            @Override
            public void onSuccess(DataItem dataItem) {
                // Data sent
                //wakeLock.release();
                if(done)
                    vibrate(50);
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Data not sent
                        //wakeLock.release();
                        vibrate(2000);
                    }
                });


    }


    /**
     * Vibrates watch for a duration set by ms.
     * @param ms
     */
    private void vibrate(int ms) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ((Vibrator) context.getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            ((Vibrator) context.getSystemService(VIBRATOR_SERVICE)).vibrate(ms);
        }
    }
}
