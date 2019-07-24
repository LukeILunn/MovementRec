package uk.ac.aber.movementrecorder.Background;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;
import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by einar on 08/06/2018.
 */

public class SensorHandler implements SensorEventListener {

    private DataStorage dataStorage;
    private final SensorManager mSensorManager;
    private final Sensor mAccelerometer;
    private final Sensor mGyro;
    private final Sensor mMag;
    private final Sensor mHrt;
    private int samplingTime;
    private boolean firstRecFlag;
    private float startRecTime;
    private float stopRecTime;
    private long initTimeStamp;
    private float timeJump;
    private float newTimeStamp;
    private float prevTimeStamp;
    private float timeIntoRec;
    private float currEventTime;
    private Context context;
    private PowerManager.WakeLock wakeLock;


    private static final int MILL = 1000000;
    private int line = 0;

    private SensorHandler.ISensorHandler iSensorHandler;


    public interface ISensorHandler {
        public void processData();
        void newData(float timeStamp);
    }

    /**
     * SensorHandler constructor.
     * @param context
     * @param storage
     * @param iSensorHandler
     */
    public SensorHandler(Context context, DataStorage storage, SensorHandler.ISensorHandler iSensorHandler, PowerManager.WakeLock wLock) {
        this.iSensorHandler = iSensorHandler;
        wakeLock = wLock;
        dataStorage = storage;
        mSensorManager = (SensorManager)context.getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mMag = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mHrt = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        firstRecFlag=true;
        this.context = context;
    }

    private double round(float value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Float.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Called by SensorEventListener every time data has been sampled.
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {

//        if (wakeLock != null){
//            if(!wakeLock.isHeld()){
//                Log.d("wakeLock", "wakeLock not active");
//            }
//        }
        if (firstRecFlag) {
            startRecTime = (float)(System.nanoTime() / MILL);
            initTimeStamp = event.timestamp;
            firstRecFlag=false;
            prevTimeStamp = event.timestamp;
        }


        currEventTime = event.timestamp;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            dataStorage.addAccelerometerData((float) round(event.values[0], 3), (float) round(event.values[1], 3), (float) round(event.values[2], 3), (float)((currEventTime-initTimeStamp)/ MILL), event.accuracy);
//            System.out.println("Acc data stored");
//            System.out.println(Float.toString(event.values[0]));
        }
        else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            dataStorage.addGyroData(event.values[0], event.values[1], event.values[2], (float)((currEventTime-initTimeStamp)/ MILL), event.accuracy);
//            System.out.println("Gyr data stored");
//            System.out.println(Float.toString(event.values[0]));
        }
        else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            dataStorage.addMagData(event.values[0], event.values[1], event.values[2], (float)((currEventTime-initTimeStamp)/ MILL), event.accuracy);
//            System.out.println("Mag data stored");
//            System.out.println(Float.toString(event.values[0]));


//            newTimeStamp = (currEventTime-initTimeStamp)/ MILL;
//            if(line<15){
//                System.out.print((int)((currEventTime-initTimeStamp)/ MILL) + " , ");
//                line++;
//            } else {
//                System.out.println((int)((currEventTime-initTimeStamp)/ MILL));
//                line = 0;
//            }
//
//            timeJump = newTimeStamp - prevTimeStamp;
//            if(timeJump > 50){
//                System.out.println(" ");
//                System.out.println(" ");
//                System.out.println("SKIP EVENT");
//                System.out.println(" ");```````````````````
//                System.out.println(" ");
//            }
//            prevTimeStamp = newTimeStamp;



        }
//        else if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
//            currEventTime = event.timestamp;
//            dataStorage.addHrtData(event.values[0], (float)((currEventTime-initTimeStamp)/1000000));
//            System.out.println("Hrt data stored");
//            System.out.println(Float.toString(event.values[0]));;
//        }


        iSensorHandler.newData((int)((event.timestamp-initTimeStamp)/ MILL));



        stopRecTime = (int)(System.nanoTime() / MILL);
        if((stopRecTime - startRecTime) >= samplingTime)
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
        dataStorage.setStartRecTime(startRecTime);
        dataStorage.setStopRecTime(stopRecTime);
        if (processData)
            iSensorHandler.processData();
    }

    /**
     * Starts sampling data from accelerometer
     * @param samplingDuration
     */
    public void startSensor(int samplingDuration, ArrayList<String> sensors) {

        samplingTime = samplingDuration * 1000;         // convert sampling duration into milli-seconds

        dataStorage.sensList = sensors;

        int sensorDelay = SensorManager.SENSOR_DELAY_FASTEST;
//        if (samplingDuration > 45) {
//            sensorDelay = SensorManager.SENSOR_DELAY_UI;
//        }

        if (sensors.contains("acc")) {
            if (mAccelerometer != null) {
                mSensorManager.registerListener(this, mAccelerometer, sensorDelay, 20000);
                //mAccelerometer.getFifoMaxEventCount();
            }
            else {
                Log.w(TAG, "Missing accelerometer");
                Toast.makeText(context.getApplicationContext(), "Missing sensor (accelerometer and/or gyro)", Toast.LENGTH_LONG).show();
            }
        }
        if (sensors.contains("gyr")) {
            if (mGyro != null) {
                mSensorManager.registerListener(this, mGyro, sensorDelay, 20000);
            }
            else {
                Log.w(TAG, "Missing gyroscope");
                Toast.makeText(context.getApplicationContext(), "Missing gyroscope", Toast.LENGTH_LONG).show();
            }
        }
        if (sensors.contains("mag")) {
            if (mMag != null) {
                mSensorManager.registerListener(this, mMag, sensorDelay, 20000);
            }
            else {
                Log.w(TAG, "Missing magnetometer");
                Toast.makeText(context.getApplicationContext(), "Missing magnetometer", Toast.LENGTH_LONG).show();
            }
        }
        if (sensors.contains("hrt")) {
            if (mHrt != null) {
                mSensorManager.registerListener(this, mHrt, sensorDelay);
            }
            else {
                Log.w(TAG, "Missing heart rate");
                Toast.makeText(context.getApplicationContext(), "Missing heart rate", Toast.LENGTH_LONG).show();
            }
        }

    }

}
