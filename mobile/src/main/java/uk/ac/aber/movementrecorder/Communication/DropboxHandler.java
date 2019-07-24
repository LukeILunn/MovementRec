package uk.ac.aber.movementrecorder.Communication;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.UploadErrorException;
import com.dropbox.core.v2.files.WriteMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static android.content.Context.VIBRATOR_SERVICE;
import static uk.ac.aber.movementrecorder.Data.SamplingService.ServiceReceiver.PHONE_SS_ACTION_RESP;
import static uk.ac.aber.movementrecorder.UI.TimeSeriesFragment.ServiceReceiver.TS_RESP;

/**
 * Created by einar on 12/03/2018.
 */

public class DropboxHandler {

    private Context context;
    private static final String ACCESS_TOKEN = "pxM42RHxoEMAAAAAAACqXg0r4-tK3uMCxT6zWBjHEnD4Zhe4UZw5EKHhh25m-kxC";//"pxM42RHxoEMAAAAAAACiBKOFc3k-Glimr3eLbn4mhWGI2ph4v5XavUC_pO3vRIVl";
    private static DbxClientV2 dbxClientV2;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * Connects to dropbox using access token for TremorTracker app.<br/>
     * Connects to client and stores the client connection globally
     * @param context
     */
    public DropboxHandler(Context context) {
        this.context = context;
        if (dbxClientV2 == null) {
            DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder("PTDataCollection/1.0").build(); //.withHttpRequestor(new OkHttp3Requestor(OkHttp3Requestor.defaultOkHttpClient()))

            dbxClientV2 = new DbxClientV2(requestConfig, ACCESS_TOKEN);
        }
    }

    /**
     * Returns the Dropbox client
     * @return dbxClientV2
     */
    public DbxClientV2 getClient() {
        if (dbxClientV2 == null) {
            Toast.makeText(context, "No Dropbox client", Toast.LENGTH_LONG);
        }
        return dbxClientV2;
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

    /**
     * Starts the UploadFile AsyncTask
     * @param name
     * @param path
     */
    public void uploadFile(String name, String path) {
        new UploadFile().execute(name, path);
    }

    /**
     * AsyncTask creates a new tread for the uploading to not disturb or crash the UI
     */
    private class UploadFile extends AsyncTask<String, Integer, String >{

        public static final String ACTION_RESP = "uk.ac.aber.movementrecorder.intent.action.ssmessage";

        @Override
        protected String doInBackground(String... strings) {

            File file = new File(context.getFilesDir(), strings[0]);

            if (!file.exists()) {
                // Could not find file!
                Intent broadcastIntent = new Intent();

                broadcastIntent.setAction(TS_RESP);
                broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                broadcastIntent.putExtra("id", "out");
                broadcastIntent.putExtra("out", "Upload Failed");
                context.sendBroadcast(broadcastIntent);

                broadcastIntent.setAction(PHONE_SS_ACTION_RESP);
                broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                broadcastIntent.putExtra("id", "message");
                broadcastIntent.putExtra("message", "sending_done");
                context.sendBroadcast(broadcastIntent);

                return "Fail";
            }

            try (InputStream inputStream = new FileInputStream(file)) {
                FileMetadata metadata = dbxClientV2.files().uploadBuilder("/" + strings[1])
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(inputStream);
                return "Done";
            } catch (UploadErrorException e) {
                e.printStackTrace();
                return "Fail";
            } catch (DbxException e) {
                e.printStackTrace();
                return "Fail";
            } catch (IOException e) {
                e.printStackTrace();
                return "Fail";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            String message = "";
            if(s.equals("Done")) {

                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(TS_RESP);
                broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                broadcastIntent.putExtra("id", "message");
                broadcastIntent.putExtra("message", "upload_complete");
                context.sendBroadcast(broadcastIntent);

                broadcastIntent.setAction(PHONE_SS_ACTION_RESP);
                broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                broadcastIntent.putExtra("id", "message");
                broadcastIntent.putExtra("message", "sending_done");
                context.sendBroadcast(broadcastIntent);

                vibrate(1000);
            }
            else {
//                mainHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show();
//                    }
//                });
                Intent broadcastIntent = new Intent();

                broadcastIntent.setAction(TS_RESP);
                broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                broadcastIntent.putExtra("id", "out");
                broadcastIntent.putExtra("out", "Upload Failed");
                context.sendBroadcast(broadcastIntent);

                broadcastIntent.setAction(PHONE_SS_ACTION_RESP);
                broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                broadcastIntent.putExtra("id", "message");
                broadcastIntent.putExtra("message", "sending_done");
                context.sendBroadcast(broadcastIntent);
            }
        }
    }
}
