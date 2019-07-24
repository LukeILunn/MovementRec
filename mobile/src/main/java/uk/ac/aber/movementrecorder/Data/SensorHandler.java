package uk.ac.aber.movementrecorder.Data;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;
import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by einar on 08/06/2018.
 */

public class SensorHandler implements SensorEventListener {

    private final SensorManager mSensorManager;
    private final Sensor mAccelerometer;
    private final Sensor mGyro;
    private final Sensor mMag;
    private int samplingTime;
    private boolean firstRecFlag;
    private float startRecTime;
    private float stopRecTime;
    private long initTimeStamp;
    private Context context;
    private boolean finishRecFlag;

    private ArrayList<Float> accXArray = new ArrayList<Float>();
    private ArrayList<Float> accYArray = new ArrayList<Float>();
    private ArrayList<Float> accZArray = new ArrayList<Float>();
    private ArrayList<Float> accTimeStamp = new ArrayList<Float>();
    private ArrayList<Integer> accAccuracy = new ArrayList<Integer>();
    private ArrayList<Float> gyrXArray = new ArrayList<Float>();
    private ArrayList<Float> gyrYArray = new ArrayList<Float>();
    private ArrayList<Float> gyrZArray = new ArrayList<Float>();
    private ArrayList<Float> gyrTimeStamp = new ArrayList<Float>();
    private ArrayList<Integer> gyrAccuracy = new ArrayList<Integer>();
    private ArrayList<Float> magXArray = new ArrayList<Float>();
    private ArrayList<Float> magYArray = new ArrayList<Float>();
    private ArrayList<Float> magZArray = new ArrayList<Float>();
    private ArrayList<Float> magTimeStamp = new ArrayList<Float>();
    private ArrayList<Integer> magAccuracy = new ArrayList<Integer>();

    private SensorHandler.ISensorHandler iSensorHandler;


    public interface ISensorHandler {
        void captureFinished(boolean success);
    }

    /**
     * SensorHandler constructor.
     * @param context
     */
    public SensorHandler(Context context, ISensorHandler iSensorHandler) {
        this.iSensorHandler = iSensorHandler;
        mSensorManager = (SensorManager)context.getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mMag = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        firstRecFlag = true;
        finishRecFlag = false;
        this.context = context;
    }

    /**
     * Called by SensorEventListener every time data has been sampled.
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {

        if(firstRecFlag) {
            startRecTime = (float)(System.nanoTime() / 1000000);
            initTimeStamp = event.timestamp;
            firstRecFlag=false;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accXArray.add(event.values[0]);
            accYArray.add(event.values[1]);
            accZArray.add(event.values[2]);
            accTimeStamp.add((float)((event.timestamp - initTimeStamp) / 1000000)); // (in milli-seconds)
            accAccuracy.add(event.accuracy);
        }
        else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gyrXArray.add(event.values[0]);
            gyrYArray.add(event.values[1]);
            gyrZArray.add(event.values[2]);
            gyrTimeStamp.add((float)((event.timestamp - initTimeStamp) / 1000000)); // (in milli-seconds)
            gyrAccuracy.add(event.accuracy);
        }
        else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magXArray.add(event.values[0]);
            magYArray.add(event.values[1]);
            magZArray.add(event.values[2]);
            magTimeStamp.add((float)((event.timestamp - initTimeStamp) / 1000000)); // (in milli-seconds)
            magAccuracy.add(event.accuracy);
        }

        stopRecTime = (float)(System.nanoTime() / 1000000);
        if ((stopRecTime - startRecTime) >= samplingTime)
            stopSensor(true);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * Stops sampling data.</b>
     * Calls processData() in IAccelerometerHandler
     */
    public void stopSensor(boolean processData) {
        mSensorManager.unregisterListener(this);
        firstRecFlag=true;
        finishRecFlag = true;
        if(processData) {
            iSensorHandler.captureFinished(finishRecFlag);
        }
    }

    /**
     * Starts sampling data from accelerometer
     * @param samplingDuration (in seconds)
     */
    public void startSensor(int samplingDuration, boolean[] sensorsToUse) {
        finishRecFlag = false;
        samplingTime = samplingDuration * 1000; // in milli-seconds

        int sensorDelay = SensorManager.SENSOR_DELAY_FASTEST;
//        if (samplingDuration > 45) {
//            sensorDelay = SensorManager.SENSOR_DELAY_UI;
//        }

        if (sensorsToUse[0]) {
            if (mAccelerometer != null) {
                mSensorManager.registerListener(this, mAccelerometer, sensorDelay);
            } else {
                Toast.makeText(context.getApplicationContext(), "No Accelerometer found", Toast.LENGTH_LONG).show();
            }
        }
        if (sensorsToUse[1]) {
            if (mGyro != null) {
                mSensorManager.registerListener(this, mGyro, sensorDelay);
            } else {
                Toast.makeText(context.getApplicationContext(), "No Gyroscope found", Toast.LENGTH_LONG).show();
            }
        }
        if (sensorsToUse[2]) {
            if (mMag != null) {
                mSensorManager.registerListener(this, mMag, sensorDelay);
            } else {
                Toast.makeText(context.getApplicationContext(), "No Magnetometer found", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Clears all arrays containing data from the sensors.
     * This has to be done before sampling starts.
     */
    public void clearAllSensorValues() {
        accXArray.clear();
        accYArray.clear();
        accZArray.clear();
        accTimeStamp.clear();
        accAccuracy.clear();

        gyrXArray.clear();
        gyrYArray.clear();
        gyrZArray.clear();
        gyrTimeStamp.clear();
        gyrAccuracy.clear();

        magXArray.clear();
        magYArray.clear();
        magZArray.clear();
        magTimeStamp.clear();
        magAccuracy.clear();
    }

    /**
     * Returns true if the sensor has finished sampling.
     * @return
     */
    public boolean hasFinished() {
        return finishRecFlag;
    }

    // Getters for the arrays

    public ArrayList<Float> getAccXArray() {
        return accXArray;
    }

    public ArrayList<Float> getAccYArray() {
        return accYArray;
    }

    public ArrayList<Float> getAccZArray() {
        return accZArray;
    }

    public ArrayList<Float> getAccTimeStamp() {
        return accTimeStamp;
    }

    public ArrayList<Float> getGyrXArray() {
        return gyrXArray;
    }

    public ArrayList<Float> getGyrYArray() {
        return gyrYArray;
    }

    public ArrayList<Float> getGyrZArray() {
        return gyrZArray;
    }

    public ArrayList<Float> getGyrTimeStamp() {
        return gyrTimeStamp;
    }

    public ArrayList<Float> getMagXArray() {
        return magXArray;
    }

    public float getStartRecTime() {
        return startRecTime;
    }

    public float getStopRecTime() {
        return stopRecTime;
    }

    public ArrayList<ArrayList<Float>> getAllData() {
        ArrayList<ArrayList<Float>> allData = new ArrayList<ArrayList<Float>>();

        allData.add(accXArray);
        allData.add(accYArray);
        allData.add(accZArray);

        allData.add(gyrXArray);
        allData.add(gyrYArray);
        allData.add(gyrZArray);

        allData.add(magXArray);
        allData.add(magYArray);
        allData.add(magZArray);

        return allData;
    }

    public ArrayList<ArrayList<Float>> getAllTimestamps() {
        ArrayList<ArrayList<Float>> allTS = new ArrayList<ArrayList<Float>>();

        allTS.add(accTimeStamp);
        allTS.add(gyrTimeStamp);
        allTS.add(magTimeStamp);

        return allTS;
    }

    public ArrayList<ArrayList<Integer>> getAllAccuracy() {
        ArrayList<ArrayList<Integer>> allAccuracy = new ArrayList<ArrayList<Integer>>();

        allAccuracy.add(accAccuracy);
        allAccuracy.add(gyrAccuracy);
        allAccuracy.add(magAccuracy);

        return allAccuracy;
    }

}
