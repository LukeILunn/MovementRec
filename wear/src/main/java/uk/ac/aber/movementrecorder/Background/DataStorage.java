package uk.ac.aber.movementrecorder.Background;

import android.view.ViewDebug;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by einar on 31/01/2018.
 */

public class DataStorage {

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

    private ArrayList<Float> hrtArray = new ArrayList<Float>();
    private ArrayList<Float> hrtTimeStamp = new ArrayList<Float>();

    public ArrayList<String> sensList = new ArrayList<>();

    private ArrayList<float[]> allData;
    private static final int SENDINGLENGTH = 200;

    private float startRecTime;
    private float stopRecTime;

    int partialCounter;

    public DataStorage() {
    }

    /**
     * Adds raw data to arrays.
     * @param x
     * @param y
     * @param z
     * @param timestamp
     * @param accuracy
     */
    public void addAccelerometerData(float x, float y, float z, float timestamp, int accuracy) {
        accXArray.add(x);
        accYArray.add(y);
        accZArray.add(z);
        accTimeStamp.add(timestamp);
        accAccuracy.add(accuracy);
        partialCounter = -1;
    }

    /**
     * Adds raw data to arrays.
     * @param x
     * @param y
     * @param z
     * @param timestamp
     * @param accuracy
     */
    public void addGyroData(float x, float y, float z, float timestamp, int accuracy) {
        gyrXArray.add(x);
        gyrYArray.add(y);
        gyrZArray.add(z);
        gyrTimeStamp.add(timestamp);
        gyrAccuracy.add(accuracy);
    }

    /**
     * Adds raw data to arrays.
     * @param x
     * @param y
     * @param z
     * @param timestamp
     * @param accuracy
     */
    public void addMagData(float x, float y, float z, float timestamp, int accuracy) {
        magXArray.add(x);
        magYArray.add(y);
        magZArray.add(z);
        magTimeStamp.add(timestamp);
        magAccuracy.add(accuracy);
    }

    /**
     * Adds raw data to arrays.
     * @param hrt
     * @param timestamp
     */
    public void addHrtData(float hrt, float timestamp) {
        hrtArray.add(hrt);
        hrtTimeStamp.add(timestamp);
    }

    /**
     * Clears all arrays used
     */
    public void clear() {
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

        hrtArray.clear();
        hrtTimeStamp.clear();
    }

    /**
     * Returns an array of accelerometer accuracy as long
     * @return Accuracy
     */
    public ArrayList<Integer> getAccAccuracy() {
        return accAccuracy;
    }

    /**
     * Returns an array of accelerometer timestamps as long
     * @return Timestamps
     */
    public float[] getAccTimeStamp() {
        float[] array = new float[accTimeStamp.size()];

        for (int i = 0; i < accTimeStamp.size(); i++) {
            array[i] = accTimeStamp.get(i);
        }
        return array;
    }

    /**
     * Returns an array of gyroscope timestamps as long
     * @return Timestamps
     */
    public float[] getGyroTimeStamp() {
        float[] array = new float[gyrTimeStamp.size()];

        for (int i = 0; i < gyrTimeStamp.size(); i++) {
            array[i] = gyrTimeStamp.get(i);
        }
        return array;
    }

    /**
     * Returns a float array with raw data from accelerometer X axis
     * @return X Axis
     */
    public float[] getAccX() {
        float[] array = new float[accXArray.size()];

        for (int i = 0; i < accXArray.size(); i++) {
            array[i] = accXArray.get(i);
        }
        return array;
    }

    /**
     * Returns a float array with raw data from accelerometer Y axis
     * @return Y Axis
     */
    public float[] getAccY() {
        float[] array = new float[accYArray.size()];

        for (int i = 0; i < accYArray.size(); i++) {
            array[i] = accYArray.get(i);
        }
        return array;
    }

    /**
     * Returns a float array with raw data from accelerometer Z axis
     * @return Z Axis
     */
    public float[] getAccZ() {
        float[] array = new float[accZArray.size()];

        for (int i = 0; i < accZArray.size(); i++) {
            array[i] = accZArray.get(i);
        }
        return array;
    }

    /**
     * Returns an array of gyroscope accuracy as long
     * @return Accuracy
     */
    public ArrayList<Integer> getGyrAccuracy() {
        return gyrAccuracy;
    }

    /**
     * Returns a float array with raw data from gyro X axis
     * @return X Axis
     */
    public float[] getGyroX() {
        float[] array = new float[gyrXArray.size()];

        for (int i = 0; i < gyrXArray.size(); i++) {
            array[i] = gyrXArray.get(i);
        }
        return array;
    }

    /**
     * Returns a float array with raw data from gyro Y axis
     * @return Y Axis
     */
    public float[] getGyroY() {
        float[] array = new float[gyrYArray.size()];

        for (int i = 0; i < gyrYArray.size(); i++) {
            array[i] = gyrYArray.get(i);
        }
        return array;
    }

    /**
     * Returns a float array with raw data from gyro Z axis
     * @return Z Axis
     */
    public float[] getGyroZ() {
        float[] array = new float[gyrZArray.size()];

        for (int i = 0; i < gyrZArray.size(); i++) {
            array[i] = gyrZArray.get(i);
        }
        return array;
    }

    public float getStartRecTime() {
        return startRecTime;
    }

    public void setStartRecTime(float startRecTime) {
        this.startRecTime = startRecTime;
    }

    public float getStopRecTime() {
        return stopRecTime;
    }

    public void setStopRecTime(float stopRecTime) {
        this.stopRecTime = stopRecTime;
    }


    /*
    Send partial methods returns 1500 datapoints for each time they are called.
     */

    public float[] getPartialAccX() {

        if(accXArray.isEmpty())
            return new float[0];

        int size = 0;
        if (accXArray.size() >= SENDINGLENGTH)
            size = SENDINGLENGTH;
        else
            size = accXArray.size();

        float[] partial = new float[size];

        Iterator itr = accXArray.iterator();

        for (int i = 0; i < size; i++) {
            partial[i] = (float)itr.next();
            itr.remove();
        }

        return partial;
    }

    public float[] getPartialAccY() {
        if(accYArray.isEmpty())
            return new float[0];

        int size = 0;
        if (accYArray.size() >= SENDINGLENGTH)
            size = SENDINGLENGTH;
        else
            size = accYArray.size();

        float[] partial = new float[size];

        Iterator itr = accYArray.iterator();

        for (int i = 0; i < size; i++) {
            partial[i] = (float)itr.next();
            itr.remove();
        }

        return partial;
    }

    public float[] getPartialAccZ() {
        if(accZArray.isEmpty())
            return new float[0];

        int size = 0;
        if (accZArray.size() >= SENDINGLENGTH)
            size = SENDINGLENGTH;
        else
            size = accZArray.size();

        float[] partial = new float[size];

        Iterator itr = accZArray.iterator();

        for (int i = 0; i < size; i++) {
            partial[i] = (float)itr.next();
            itr.remove();
        }

        return partial;
    }

    public float[] getPartialAccTimestamp() {
        if(accTimeStamp.isEmpty())
            return new float[0];

        int size = 0;
        if (accTimeStamp.size() >= SENDINGLENGTH)
            size = SENDINGLENGTH;
        else
            size = accTimeStamp.size();

        float[] partial = new float[size];

        Iterator itr = accTimeStamp.iterator();

        for (int i = 0; i < size; i++) {
            partial[i] = (float)itr.next();
            itr.remove();
        }


        return partial;
    }

    public ArrayList<Integer> getPartialAccAccuracy() {
        if(accAccuracy.isEmpty())
            return accAccuracy;

        int size = 0;
        if (accAccuracy.size() >= SENDINGLENGTH)
            size = SENDINGLENGTH;
        else
            size = accAccuracy.size();

        ArrayList<Integer> partial = new ArrayList<Integer>(accAccuracy.subList(0, size));

        Iterator itr = accAccuracy.iterator();

        for (int i = 0; i < size; i++) {
            itr.next();
            itr.remove();
        }

        return partial;
    }

    public float[] getPartialMagX() {
        if(magXArray.isEmpty())
            return new float[0];

        int size = 0;
        if (magXArray.size() >= SENDINGLENGTH)
            size = SENDINGLENGTH;
        else
            size = magXArray.size();

        float[] partial = new float[size];

        Iterator itr = magXArray.iterator();

        for (int i = 0; i < size; i++) {
            partial[i] = (float)itr.next();
            itr.remove();
        }

        return partial;
    }

    public float[] getPartialMagY() {
        if(magYArray.isEmpty())
            return new float[0];

        int size = 0;
        if (magYArray.size() >= SENDINGLENGTH)
            size = SENDINGLENGTH;
        else
            size = magYArray.size();

        float[] partial = new float[size];

        Iterator itr = magYArray.iterator();

        for (int i = 0; i < size; i++) {
            partial[i] = (float)itr.next();
            itr.remove();
        }

        return partial;
    }

    public float[] getPartialMagZ() {
        if(magZArray.isEmpty())
            return new float[0];

        int size = 0;
        if (magZArray.size() >= SENDINGLENGTH)
            size = SENDINGLENGTH;
        else
            size = magZArray.size();

        float[] partial = new float[size];

        Iterator itr = magZArray.iterator();

        for (int i = 0; i < size; i++) {
            partial[i] = (float)itr.next();
            itr.remove();
        }

        return partial;
    }

    public float[] getPartialMagTimestamp() {
        if(magTimeStamp.isEmpty())
            return new float[0];

        int size = 0;
        if (magTimeStamp.size() >= SENDINGLENGTH)
            size = SENDINGLENGTH;
        else
            size = magTimeStamp.size();

        float[] partial = new float[size];

        Iterator itr = magTimeStamp.iterator();

        for (int i = 0; i < size; i++) {
            partial[i] = (float)itr.next();
            itr.remove();
        }

        return partial;
    }

    public ArrayList<Integer> getPartialMagAccuracy() {
        if(magAccuracy.isEmpty())
            return magAccuracy;

        int size = 0;
        if (magAccuracy.size() >= SENDINGLENGTH)
            size = SENDINGLENGTH;
        else
            size = magAccuracy.size();

        ArrayList<Integer> partial = new ArrayList<Integer>(magAccuracy.subList(0, size));

        Iterator itr = magAccuracy.iterator();

        for (int i = 0; i < size; i++) {
            itr.next();
            itr.remove();
        }

        return partial;
    }




    public float[] getPartialHrt() {
        if(hrtArray.isEmpty())
            return new float[0];

        int size = 0;
        if (hrtArray.size() >= SENDINGLENGTH)
            size = SENDINGLENGTH;
        else
            size = hrtArray.size();

        float[] partial = new float[size];

        Iterator itr = hrtArray.iterator();

        for (int i = 0; i < size; i++) {
            partial[i] = (float)itr.next();
            itr.remove();
        }

        return partial;
    }

    public float[] getPartialHrtTimestamp() {
        if(hrtTimeStamp.isEmpty())
            return new float[0];

        int size = 0;
        if (hrtTimeStamp.size() >= SENDINGLENGTH)
            size = SENDINGLENGTH;
        else
            size = hrtTimeStamp.size();

        float[] partial = new float[size];

        Iterator itr = hrtTimeStamp.iterator();

        for (int i = 0; i < size; i++) {
            partial[i] = (float)itr.next();
            itr.remove();
        }

        return partial;
    }

    public float[] getPartialGyrX() {
        if(gyrXArray.isEmpty())
            return new float[0];

        int size = 0;
        if (gyrXArray.size() >= SENDINGLENGTH)
            size = SENDINGLENGTH;
        else
            size = gyrXArray.size();

        float[] partial = new float[size];

        Iterator itr = gyrXArray.iterator();

        for (int i = 0; i < size; i++) {
            partial[i] = (float)itr.next();
            itr.remove();
        }

        return partial;
    }

    public float[] getPartialGyrY() {
        if(gyrYArray.isEmpty())
            return new float[0];

        int size = 0;
        if (gyrYArray.size() >= SENDINGLENGTH)
            size = SENDINGLENGTH;
        else
            size = gyrYArray.size();

        float[] partial = new float[size];

        Iterator itr = gyrYArray.iterator();

        for (int i = 0; i < size; i++) {
            partial[i] = (float)itr.next();
            itr.remove();
        }

        return partial;
    }

    public float[] getPartialGyrZ() {
        if(gyrZArray.isEmpty())
            return new float[0];

        int size = 0;
        if (gyrZArray.size() >= SENDINGLENGTH)
            size = SENDINGLENGTH;
        else
            size = gyrZArray.size();

        float[] partial = new float[size];

        Iterator itr = gyrZArray.iterator();

        for (int i = 0; i < size; i++) {
            partial[i] = (float)itr.next();
            itr.remove();
        }

        return partial;
    }

    public float[] getPartialGyrTimestamp() {
        if(gyrTimeStamp.isEmpty())
            return new float[0];

        int size = 0;
        if (gyrTimeStamp.size() >= SENDINGLENGTH)
            size = SENDINGLENGTH;
        else
            size = gyrTimeStamp.size();

        float[] partial = new float[size];

        Iterator itr = gyrTimeStamp.iterator();

        for (int i = 0; i < size; i++) {
            partial[i] = (float)itr.next();
            itr.remove();
        }

        return partial;
    }

    public ArrayList<Integer> getPartialGyrAccuracy() {
        if(gyrAccuracy.isEmpty())
            return gyrAccuracy;

        int size = 0;
        if (gyrAccuracy.size() >= SENDINGLENGTH)
            size = SENDINGLENGTH;
        else
            size = gyrAccuracy.size();

        ArrayList<Integer> partial = new ArrayList<Integer>(gyrAccuracy.subList(0, size));

        Iterator itr = gyrAccuracy.iterator();

        for (int i = 0; i < size; i++) {
            itr.next();
            itr.remove();
        }

        return partial;
    }

    public ArrayList<float[]> getAllPartial() {
        allData = new ArrayList<>();

        if (!accXArray.isEmpty()) {
            allData.add(getPartialAccX());
            allData.add(getPartialAccY());
            allData.add(getPartialAccZ());
            allData.add(getPartialAccTimestamp());
        }

        if (!gyrXArray.isEmpty()) {
            allData.add(getPartialGyrX());
            allData.add(getPartialGyrY());
            allData.add(getPartialGyrZ());
            allData.add(getPartialGyrTimestamp());
        }

        if (!magXArray.isEmpty()) {
            allData.add(getPartialMagX());
            allData.add(getPartialMagY());
            allData.add(getPartialMagZ());
            allData.add(getPartialMagTimestamp());
        }

        if (!hrtArray.isEmpty()) {
            allData.add(getPartialHrt());
            allData.add(getPartialHrtTimestamp());
        }

        return allData;
    }

    public ArrayList<String> getSensorNames() {
        return sensList;
    }


    public boolean isEmpty() {
        if(accXArray.isEmpty() && accYArray.isEmpty() && accZArray.isEmpty() &&
                accTimeStamp.isEmpty() &&
                gyrXArray.isEmpty() && gyrYArray.isEmpty() && gyrZArray.isEmpty() &&
                gyrTimeStamp.isEmpty() &&
                magXArray.isEmpty() && magYArray.isEmpty() && magZArray.isEmpty() &&
                magTimeStamp.isEmpty() &&
                hrtArray.isEmpty() && hrtTimeStamp.isEmpty())
            return true;
        else
            return false;
    }

    public ArrayList<Float> getGyrTimeStamp() {
        return gyrTimeStamp;
    }

    public ArrayList<Float> getMagTimeStamp() {
        return magTimeStamp;
    }

    public ArrayList<Float> getAccuTimeStamp() {
        return accTimeStamp;
    }

    public void setAccTimeStamp(ArrayList<Float> accTimeStamp) {
        this.accTimeStamp = accTimeStamp;
    }

    public void setGyrTimeStamp(ArrayList<Float> gyrTimeStamp) {
        this.gyrTimeStamp = gyrTimeStamp;
    }

    public void setMagTimeStamp(ArrayList<Float> magTimeStamp) {
        this.magTimeStamp = magTimeStamp;
    }


}
