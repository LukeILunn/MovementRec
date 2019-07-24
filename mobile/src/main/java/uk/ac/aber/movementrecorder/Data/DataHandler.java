package uk.ac.aber.movementrecorder.Data;

import android.app.Notification;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.wearable.DataMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import uk.ac.aber.movementrecorder.Communication.DropboxHandler;
import uk.ac.aber.movementrecorder.Communication.NetworkHandler;

/**
 * Created by einar on 02/02/2018.
 */

public class DataHandler {

    // Wear

    private ArrayList<Float> accXList = new ArrayList<Float>();
    private ArrayList<Float> accYList = new ArrayList<Float>();
    private ArrayList<Float> accZList = new ArrayList<Float>();
    private ArrayList<Float> accTimeList = new ArrayList<Float>();

    private ArrayList<Float> gyrXList = new ArrayList<Float>();
    private ArrayList<Float> gyrYList = new ArrayList<Float>();
    private ArrayList<Float> gyrZList = new ArrayList<Float>();
    private ArrayList<Float> gyrTimeList = new ArrayList<Float>();

    private ArrayList<Float> magXList = new ArrayList<Float>();
    private ArrayList<Float> magYList = new ArrayList<Float>();
    private ArrayList<Float> magZList = new ArrayList<Float>();
    private ArrayList<Float> magTimeList = new ArrayList<Float>();

    private ArrayList<Float> hrtList = new ArrayList<Float>();
    private ArrayList<Float> hrtTimeList = new ArrayList<Float>();

    private static final String FILENAME = "tremortracker.json";// txt

    private String watchTime;
    private String watchDate;
    private float watchStartRecTime;
    private float watchStopRecTime;
    private String watchManufacturer;
    private String watchModel;
    private String watchVersion;
    private String watchRelease;


    private boolean jsonFile = true;

    private Context context;

    private NetworkHandler networkHandler;
    private DropboxHandler dropboxHandler;

    private String filename = "tremortracker";// txt

    public DataHandler(Context context) {
        this.context = context;
        networkHandler = new NetworkHandler();
        dropboxHandler = new DropboxHandler(context);
    }

    /**
     * Takes the datamap and sets x,y,z and time.<br/>
     * Makes the length even
     * @param dataMap
     */
//    public void setDataMapAllAxis(DataMap dataMap) {
//        this.dataMap = dataMap;
//
//        accx = dataMap.getFloatArray("accx");
//        accy = dataMap.getFloatArray("accy");
//        accz = dataMap.getFloatArray("accz");
//        acctime = dataMap.getFloatArray("acct");
//        acca = dataMap.getIntegerArrayList("acca");
//
//        gyrx = dataMap.getFloatArray("gyrx");
//        gyry = dataMap.getFloatArray("gyry");
//        gyrz = dataMap.getFloatArray("gyrz");
//        gyrtime = dataMap.getFloatArray("gyrt");
//        gyra = dataMap.getIntegerArrayList("gyra");
//
//        watchStartRecTime = dataMap.getFloat("startRecTime");
//        watchStopRecTime = dataMap.getFloat("stopRecTime");
//    }

    /**
     * Populates variables with info from watch.<br/>
     * This is the first message sent from the watch after sampling.
     * @param dataMap
     */
    public void setDatasetInfo(DataMap dataMap) {
        watchStartRecTime = dataMap.getFloat("startRecTime");
        watchStopRecTime = dataMap.getFloat("stopRecTime");
        watchDate = getDateFromString(dataMap.getString("timendate"));
        watchTime = getTimeFromString(dataMap.getString("timendate"), true);

        watchManufacturer = dataMap.getString("manufacturer");
        watchModel = dataMap.getString("model");
        watchVersion = dataMap.getString("version");
        watchRelease = dataMap.getString("release");
    }

    public void addDataFromWatch(DataMap dataMap) {
        // Add data to the arrays
        if (dataMap.getFloatArray("accx") != null) {
            int accSize = dataMap.getFloatArray("accx").length;
            for (int i = 0; i < accSize; i++) {
                accXList.add(dataMap.getFloatArray("accx")[i]);
                System.out.println(dataMap.getFloatArray("accx")[i]);
                accYList.add(dataMap.getFloatArray("accy")[i]);
                System.out.println(dataMap.getFloatArray("accy")[i]);
                accZList.add(dataMap.getFloatArray("accz")[i]);
                System.out.println(dataMap.getFloatArray("accz")[i]);
                accTimeList.add(dataMap.getFloatArray("acct")[i]);
                System.out.println(dataMap.getFloatArray("acct")[i]);
            }
        }

        if (dataMap.getFloatArray("gyrx") != null) {
            int gyrSize = dataMap.getFloatArray("gyrx").length;
            for (int i = 0; i < gyrSize; i++) {
                gyrXList.add(dataMap.getFloatArray("gyrx")[i]);
                gyrYList.add(dataMap.getFloatArray("gyry")[i]);
                gyrZList.add(dataMap.getFloatArray("gyrz")[i]);
                gyrTimeList.add(dataMap.getFloatArray("gyrt")[i]);
            }
        }

        if (dataMap.getFloatArray("magx") != null) {
            int magSize = dataMap.getFloatArray("magx").length;
            for (int i = 0; i < magSize; i++) {
                magXList.add(dataMap.getFloatArray("magx")[i]);
                magYList.add(dataMap.getFloatArray("magy")[i]);
                magZList.add(dataMap.getFloatArray("magz")[i]);
                magTimeList.add(dataMap.getFloatArray("magt")[i]);
            }
        }

        if (dataMap.getFloatArray("hrt") != null) {
            int hrtSize = dataMap.getFloatArray("hrt").length;
            for (int i = 0; i < hrtSize; i++) {
                hrtList.add(dataMap.getFloatArray("hrt")[i]);
                hrtTimeList.add(dataMap.getFloatArray("hrtt")[i]);
            }
        }
    }

    /**
     * This method is used for partial sending and is <br/>
     * called when all data has been received.
     */
    public void allDataReceived(String participantID, String activity, String watchHand, int trialNo, DataMap dataMap, ArrayList<ArrayList<Float>> phoneData,
                                ArrayList<ArrayList<Float>> phoneTimestamp, ArrayList<ArrayList<Integer>> phoneAccuracy,
                                float phoneStartRecTime, float phoneStopRecTime, boolean jsonFile, String yob, String weight, String height, boolean[] phonebSensors, boolean[] watchbSensors) {

        this.jsonFile = jsonFile;

        String json = writeJSON(participantID, activity, watchHand, trialNo, phoneData, phoneTimestamp, phoneAccuracy, phoneStartRecTime, phoneStopRecTime, yob, weight, height, phonebSensors, watchbSensors);

        String info = participantID + "-" + activity + "-" + trialNo;

        writeToFile(json);

        //dropboxHandler.uploadFile(FILENAME, info);
        networkHandler.newPOSTRequest(json, context);

    }

    private String writeJSON(String participantID, String activity, String watchHand, int trialNo, ArrayList<ArrayList<Float>> phoneData,
                                ArrayList<ArrayList<Float>> phoneTimestamp, ArrayList<ArrayList<Integer>> phoneAccuracy,
                                float phoneStartRecTime, float phoneStopRecTime, String yob, String weight, String height, boolean[] phonebSensors, boolean[] watchbSensors) {
        JSONObject json = new JSONObject();
        JSONObject headers = new JSONObject();
        JSONObject data = new JSONObject();



        // HEADER
        try {
            JSONObject personal = new JSONObject();
            personal.put("PARTICIPANT_ID", participantID);
            personal.put("YOB", yob);
            personal.put("WEIGHT", weight);
            personal.put("HEIGHT", height);

            JSONObject test = new JSONObject();
            test.put("TYPE", activity);
            test.put("HAND", watchHand);
            test.put("TRIAL_NO", trialNo);
            test.put("DURATION", (watchStopRecTime-watchStartRecTime)/1000);

            // MAIN HEADER
            headers.put("NAME", participantID + "-" + activity + "-" + trialNo);
            headers.put("DATETIME", watchDate + " " + watchTime);
            System.out.println(watchDate + " " + watchTime);
            headers.put("TEST", test);
            headers.put("PARTICIPANT", personal);

            // WATCH HEADER
            JSONArray watchSensorsArray = new JSONArray();
            // SENSOR HEADER
            // ACC
            JSONObject watchACCInfo = new JSONObject();
            if (watchbSensors[0]) {
                watchACCInfo.put("SENSOR_TYPE", "ACC");
                watchACCInfo.put("SAMPLES", accXList.size());
                watchACCInfo.put("CHANNELS", 3);
                watchACCInfo.put("RATE", accXList.size() / ((watchStopRecTime - watchStartRecTime) / 1000));
                watchSensorsArray.put(watchACCInfo);
            }
            // GYRO
            JSONObject watchGYROInfo = new JSONObject();
            if (watchbSensors[1]) {
                watchGYROInfo.put("SENSOR_TYPE", "GYR");
                watchGYROInfo.put("SAMPLES", gyrXList.size());
                watchGYROInfo.put("CHANNELS", 3);
                watchGYROInfo.put("RATE", gyrXList.size() / ((watchStopRecTime - watchStartRecTime) / 1000));
                watchSensorsArray.put(watchGYROInfo);
            }
            // MAG
            JSONObject watchMAGInfo = new JSONObject();
            if (watchbSensors[2]) {
                watchMAGInfo.put("SENSOR_TYPE", "MAG");
                watchMAGInfo.put("SAMPLES", magXList.size());
                watchMAGInfo.put("CHANNELS", 3);
                watchMAGInfo.put("RATE", magXList.size() / ((watchStopRecTime - watchStartRecTime) / 1000));
                watchSensorsArray.put(watchMAGInfo);
            }
            // HRT
            JSONObject watchHRTInfo = new JSONObject();
            if (watchbSensors[3]) {
                watchHRTInfo.put("SENSOR_TYPE", "HRT");
                watchHRTInfo.put("SAMPLES", hrtList.size());
                watchHRTInfo.put("CHANNELS", 1);
                watchHRTInfo.put("RATE", hrtList.size() / ((watchStopRecTime - watchStartRecTime) / 1000));
                watchSensorsArray.put(watchHRTInfo);
            }

            JSONObject watchHeader = new JSONObject();

            JSONObject watchInfo = new JSONObject();
            watchInfo.put("MANUFACTURER", watchManufacturer);
            watchInfo.put("MODEL", watchModel);
            watchInfo.put("VERSION", watchVersion);
            watchInfo.put("VERSION_RELEASE", watchRelease);

            watchHeader.put("DEVICE_TYPE", "WATCH");
            watchHeader.put("DEVICE_INFO", watchInfo);
            watchHeader.put("SENSORS", watchSensorsArray);

            // PHONE HEADER
            JSONArray phoneSensorsArray = new JSONArray();
            // SENSOR HEADER
            // ACC
            JSONObject phoneACCInfo = new JSONObject();
            if (phonebSensors[0]) {
                phoneACCInfo.put("SENSOR_TYPE", "ACC");
                phoneACCInfo.put("SAMPLES", phoneTimestamp.get(0).size());
                phoneACCInfo.put("CHANNELS", 3);
                phoneACCInfo.put("RATE", phoneTimestamp.get(0).size() / ((phoneStopRecTime - phoneStartRecTime) / 1000));
                phoneSensorsArray.put(phoneACCInfo);
            }
            // GYRO
            JSONObject phoneGYROInfo = new JSONObject();
            if (phonebSensors[1]) {
                phoneGYROInfo.put("SENSOR_TYPE", "GYR");
                phoneGYROInfo.put("SAMPLES", phoneTimestamp.get(1).size());
                phoneGYROInfo.put("CHANNELS", 3);
                phoneGYROInfo.put("RATE", phoneTimestamp.get(1).size() / ((phoneStopRecTime - phoneStartRecTime) / 1000));
                phoneSensorsArray.put(phoneGYROInfo);
            }
            // MAG
            JSONObject phoneMAGInfo = new JSONObject();
            if (phonebSensors[2]) {
                phoneMAGInfo.put("SENSOR_TYPE", "MAG");
                phoneMAGInfo.put("SAMPLES", phoneTimestamp.get(2).size());
                phoneMAGInfo.put("CHANNELS", 3);
                phoneMAGInfo.put("RATE", phoneTimestamp.get(2).size() / ((phoneStopRecTime - phoneStartRecTime) / 1000));
                phoneSensorsArray.put(phoneMAGInfo);
            }

            JSONObject phoneInfo = new JSONObject();
            phoneInfo.put("MANUFACTURER", Build.MANUFACTURER);
            phoneInfo.put("MODEL", Build.MODEL);
            phoneInfo.put("VERSION", Build.VERSION.SDK_INT);
            phoneInfo.put("VERSION_RELEASE", Build.VERSION.RELEASE);

            JSONObject phoneHeader = new JSONObject();
            phoneHeader.put("DEVICE_TYPE", "PHONE");
            phoneHeader.put("DEVICE_INFO", phoneInfo);
            phoneHeader.put("SENSORS", phoneSensorsArray);

            JSONArray devicesArray = new JSONArray();
            devicesArray.put(watchHeader);
            devicesArray.put(phoneHeader);
            headers.put("DEVICES", devicesArray);

        } catch (JSONException e) {
            Log.e("MYAPP", "unexpected JSON exception", e);
        }

        try {
            // WATCH DATA
            int dataNumber = 0;
            JSONObject watchData = new JSONObject();
            // ACC
            JSONObject watchACCData = new JSONObject();
            if (watchbSensors[0]) {
                watchACCData.put("TIME", new JSONArray(accTimeList));
                watchACCData.put("X", new JSONArray(accXList));
                watchACCData.put("Y", new JSONArray(accYList));
                watchACCData.put("Z", new JSONArray(accZList));
                watchData.put(Integer.toString(dataNumber), watchACCData);
                dataNumber++;
            }

            // GYR
            JSONObject watchGYRData = new JSONObject();
            if (watchbSensors[1]) {
                watchGYRData.put("TIME", new JSONArray(gyrTimeList));
                watchGYRData.put("X", new JSONArray(gyrXList));
                watchGYRData.put("Y", new JSONArray(gyrYList));
                watchGYRData.put("Z", new JSONArray(gyrZList));
                watchData.put(Integer.toString(dataNumber), watchGYRData);
                dataNumber++;
            }
            // MAG
            JSONObject watchMAGData = new JSONObject();
            if (watchbSensors[2]) {
                watchMAGData.put("TIME", new JSONArray(magTimeList));
                watchMAGData.put("X", new JSONArray(magXList));
                watchMAGData.put("Y", new JSONArray(magYList));
                watchMAGData.put("Z", new JSONArray(magZList));
                watchData.put(Integer.toString(dataNumber), watchMAGData);
                dataNumber++;
            }
            // HRT
            JSONObject watchHRTData = new JSONObject();
            if (watchbSensors[3]) {
                watchHRTData.put("TIME", new JSONArray(hrtTimeList));
                watchHRTData.put("HRT", new JSONArray(hrtList));
                watchData.put(Integer.toString(dataNumber), watchHRTData);
                dataNumber++;
            }

            // PHONE DATA
            dataNumber = 0;
            JSONObject phoneDataJSON = new JSONObject();
            // ACC
            JSONObject phoneACCData = new JSONObject();
            if (phonebSensors[0]) {
                phoneACCData.put("TIME", new JSONArray(phoneTimestamp.get(0)));
                phoneACCData.put("X", new JSONArray(phoneData.get(0)));
                phoneACCData.put("Y", new JSONArray(phoneData.get(1)));
                phoneACCData.put("Z", new JSONArray(phoneData.get(2)));
                phoneDataJSON.put(Integer.toString(dataNumber), phoneACCData);
                dataNumber++;
            }

            // GYR
            JSONObject phoneGYRData = new JSONObject();
            if (phonebSensors[1]) {
                phoneGYRData.put("TIME", new JSONArray(phoneTimestamp.get(1)));
                phoneGYRData.put("X", new JSONArray(phoneData.get(3)));
                phoneGYRData.put("Y", new JSONArray(phoneData.get(4)));
                phoneGYRData.put("Z", new JSONArray(phoneData.get(5)));
                phoneDataJSON.put(Integer.toString(dataNumber), phoneGYRData);
                dataNumber++;
            }

            // MAG
            JSONObject phoneMAGData = new JSONObject();
            if (phonebSensors[2]) {
                phoneMAGData.put("TIME", new JSONArray(phoneTimestamp.get(2)));
                phoneMAGData.put("X", new JSONArray(phoneData.get(6)));
                phoneMAGData.put("Y", new JSONArray(phoneData.get(7)));
                phoneMAGData.put("Z", new JSONArray(phoneData.get(8)));
                phoneDataJSON.put(Integer.toString(dataNumber), phoneMAGData);
                dataNumber++;
            }


            data.put("0", watchData);
            data.put("1", phoneDataJSON);

            json.put("HEADERS", headers);
            json.put("DATA", data);

        } catch (JSONException e) {
            Log.e("MYAPP", "unexpected JSON exception", e);
        }

        return json.toString();
    }


//    private String arrayToString(ArrayList<Float> arrayList) {
//        StringBuilder sb = new StringBuilder();
//
//        sb.append("[");
//
//        for (int i = 0; i < arrayList.size(); i++) {
//            sb.append("\"" + arrayList.get(i) + "\"");
//            if (i < arrayList.size() - 1){
//                sb.append(", ");
//            }
//        }
//        return sb.toString();
//    }

    /*private String writeCSV(String participantID, String activity, ArrayList<ArrayList<Float>> phoneData,
                          ArrayList<ArrayList<Float>> phoneTimestamp, ArrayList<ArrayList<Integer>> phoneAccuracy,
                          float phoneStartRecTime, float phoneStopRecTime) {

        StringBuilder csv = new StringBuilder();
        //csv.append(readFile());
        csv.append("<Header>" + "\n");
        csv.append("Name: " + participantID + "-" + activity + "\n");
        csv.append("Date: " + watchDate + "\n");
        csv.append("Time: " + watchTime + "\n");
        csv.append("Recording Time (start): " + watchStartRecTime + "\n");
        csv.append("Recording Time (stop): " + watchStopRecTime + "\n");
        csv.append("Device 1: " + "Smartwatch" + "\n");
        csv.append("Number of sensors: " + 2 + "\n");
        csv.append("Sensors: " + "Accelerometer" + "," + "Gyroscope" + "\n");
        csv.append("Number of samples (accelerometer): " + accXList.size() + "\n");
        csv.append("Number of channels (accelerometer): " + 5 + "\n");
        csv.append("Channels (accelerometer): " + "<time>" + "<accuracy>" + "<x>" + "<y>" + "<z>" + "\n");
        csv.append("Number of samples (gyroscope): " + gyrXList.size() + "\n");
        csv.append("Number of channels (gyroscope): " + 5 + "\n");
        csv.append("Channels (gyroscope): " + "<time>" + "<accuracy>" + "<x>" + "<y>" + "<z>" + "\n");
        csv.append("Device 2: " + "Mobile phone" + "\n");
        csv.append("Recording time (start): " + phoneStartRecTime + "" + "\n");
        csv.append("Recording time (stop): " + phoneStopRecTime + "" + "\n");
        csv.append("Number of sensors: " + 2 + "\n");
        csv.append("Sensors: " + "Accelerometer" + "," + "Gyroscope" + "\n");
        csv.append("Number of samples (accelerometer): " + phoneTimestamp.get(0).size() + "\n");
        csv.append("Number of channels (accelerometer): " + 5 + "\n");
        csv.append("Channels (accelerometer): " + "<time>" + "<accuracy>" + "<x>" + "<y>" + "<z>" + "\n");
        csv.append("Number of samples (gyroscope): " + phoneTimestamp.get(1).size() + "\n");
        csv.append("Number of channels (gyroscope): " + 5 + "\n");
        csv.append("Channels (gyroscope): " + "<time>" + "<accuracy>" + "<x>" + "<y>" + "<z>" + "\n");

        // -----------------------------------------------------------------------------------------
        // Data
        // -----------------------------------------------------------------------------------------

        // Wear
        csv.append("<Data>" + "\n");
        // accelerometer time
        for (int i = 0; i < accTimeList.size(); i++) {
            csv.append(accTimeList.get(i));
            if (i < accTimeList.size() - 1){
                csv.append(",");
            }
            else{
                csv.append("\n");
            }
        }

        // accelerometer accuracy
        for (int i = 0; i < accAList.size(); i++) {
            csv.append(accAList.get(i));
            if(i < accAList.size()-1) {
                csv.append(",");
            }
            else {
                csv.append("\n");
            }
        }

        // accelerometer x
        for (int i = 0; i < accXList.size(); i++) {
            csv.append(accXList.get(i));
            if (i < accXList.size() - 1){
                csv.append(",");
            }
            else {
                csv.append("\n");
            }

        }

        // accelerometer y
        for (int i = 0; i < accYList.size(); i++) {
            csv.append(accYList.get(i));
            if (i < accYList.size() - 1){
                csv.append(",");
            }
            else {
                csv.append("\n");
            }
        }

        // accelerometer z
        for (int i = 0; i < accZList.size(); i++) {
            csv.append(accZList.get(i));
            if (i < accZList.size() - 1){
                csv.append(",");
            }
            else {
                csv.append("\n");
            }
        }

        // gyro time
        for (int i = 0; i < gyrTimeList.size(); i++) {
            csv.append(gyrTimeList.get(i));
            if (i < gyrTimeList.size() - 1){
                csv.append(",");
            }
            else{
                csv.append("\n");
            }
        }

        // gyroscope accuracy
        for (int i = 0; i < gyrAList.size(); i++) {
            csv.append(gyrAList.get(i));
            if(i < gyrAList.size()-1) {
                csv.append(",");
            }
            else {
                csv.append("\n");
            }
        }

        // gyroscope x
        for (int i = 0; i < gyrXList.size(); i++) {
            csv.append(gyrXList.get(i));
            if (i < gyrXList.size() - 1){
                csv.append(",");
            }
            else {
                csv.append("\n");
            }

        }

        // gyroscope y
        for (int i = 0; i < gyrYList.size(); i++) {
            csv.append(gyrYList.get(i));
            if (i < gyrYList.size() - 1){
                csv.append(",");
            }
            else {
                csv.append("\n");
            }
        }

        // gyroscope z
        for (int i = 0; i < gyrZList.size(); i++) {
            csv.append(gyrZList.get(i));
            if (i < gyrZList.size() - 1){
                csv.append(",");
            }
            else {
                csv.append("\n");
            }
        }

        // Mobile phone

        // accelerometer time

        for (int i = 0; i < phoneTimestamp.get(0).size(); i++) {
            csv.append(phoneTimestamp.get(0).get(i));
            if (i < phoneTimestamp.get(0).size()-1){
                csv.append(",");
            }
            else {
                csv.append("\n");
            }
        }

        // accelerometer accuracy

        for (int i = 0; i < phoneAccuracy.get(0).size(); i++) {
            csv.append(phoneAccuracy.get(0).get(i));
            if (i < phoneAccuracy.get(0).size()-1){
                csv.append(",");
            }
            else {
                csv.append("\n");
            }
        }

        // accelerometer x, y and z channels

        for (ArrayList<Float> floatArrayList : phoneData.subList(0, 3)) {
            for (int i = 0; i < floatArrayList.size(); i++) {
                csv.append(floatArrayList.get(i));
                if (i < floatArrayList.size()-1){
                    csv.append(",");
                }
                else {
                    csv.append("\n");
                }
            }
        }

        // gyroscope time
        for (int i = 0; i < phoneTimestamp.get(1).size(); i++) {
            csv.append(phoneTimestamp.get(1).get(i));
            if (i < phoneTimestamp.get(1).size()-1) {
                csv.append(",");
            }
            else {
                csv.append("\n");
            }
        }

        // gyroscope accuracy
        for (int i = 0; i < phoneAccuracy.get(1).size(); i++) {
            csv.append(phoneAccuracy.get(1).get(i));
            if (i < phoneAccuracy.get(1).size()-1) {
                csv.append(",");
            }
            else {
                csv.append("\n");
            }
        }

        // gyroscope x, y, and z channels
        for (ArrayList<Float> floatArrayList : phoneData.subList(3, phoneData.size())) {

            for (int i = 0; i < floatArrayList.size(); i++) {
                csv.append(floatArrayList.get(i));
                if (i < floatArrayList.size()-1){
                    csv.append(",");
                }
                else {
                    csv.append("\n");
                }
            }

        }
        return csv.toString();
    }*/

    private void writeToFile(String data) {
        if (jsonFile)
            filename += ".json";
        else
            filename += ".txt";

        File file = new File(context.getFilesDir(), FILENAME);


        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileOutputStream outputStream;
        try {
            //outputStream = context.openFileOutput(file.getName(), context.MODE_PRIVATE);
            outputStream = new FileOutputStream(file);
            outputStream.write(data.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // yyyy-MM-dd HH:mm:ss

    /**
     * Returns a string containing only the time.
     * If seconds is true, it will include the seconds (HH:mm:ss)
     * otherwise, it will return the time without seconds (HH:mm)
     * @param timendate
     * @param seconds
     * @return
     */
    private String getTimeFromString(String timendate, boolean seconds) {
        StringBuilder s = new StringBuilder(timendate);
        int i = s.indexOf(" ") + 1;
        s.delete(0, i); // HH:mm:ss

        if (!seconds) {
            s.delete(s.lastIndexOf(":"), s.length());
        }

        return s.toString();
    }

    /**
     * Returns the date in the format yyyy-MM-dd
     * @param timendate
     * @return
     */
    public String getDateFromString(String timendate) {
        StringBuilder s = new StringBuilder(timendate);
        int i = s.indexOf(" ");
        s.delete(i, s.length());
        return s.toString();
    }

    public ArrayList<Float> getAccXList() {
        return accXList;
    }

    public ArrayList<Float> getGyrXList() {
        return gyrXList;
    }

    public ArrayList<Float> getMagXList() {
        return magXList;
    }

    public ArrayList<Float> getHrtList() {
        return hrtList;
    }

}
