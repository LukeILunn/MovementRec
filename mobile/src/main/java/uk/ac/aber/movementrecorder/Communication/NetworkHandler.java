package uk.ac.aber.movementrecorder.Communication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import android.app.Activity;
import android.app.ActivityManager;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import uk.ac.aber.movementrecorder.UI.MainActivity;
import uk.ac.aber.movementrecorder.UI.NotificationHandler;
import uk.ac.aber.movementrecorder.UI.TimeSeriesFragment;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.Context.VIBRATOR_SERVICE;
import static uk.ac.aber.movementrecorder.Data.SamplingService.ServiceReceiver.PHONE_SS_ACTION_RESP;
import static uk.ac.aber.movementrecorder.UI.TimeSeriesFragment.ServiceReceiver.TS_RESP;

/**
 * Created by einar on 09/07/2018.
 */

public class NetworkHandler {
    private static final String URL = "https://meredith.dcs.aber.ac.uk/api/participant/submit";

    private Context context;

    public void newPOSTRequest(final String jsonObject, final Context context) {

        this.context = context;

        RequestQueue requestQueue = Volley.newRequestQueue(context);

        Map<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("blob", jsonObject);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                Request.Method.POST, URL, new JSONObject(jsonParams),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response", response.toString());
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
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(context, "Upload failed, retrying in 5 seconds", Toast.LENGTH_LONG).show();
                new Timer().schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                newPOSTRequest(jsonObject, context);
                            }
                        },
                        5000
                );


                if (error.getMessage() != null)
                    Log.d("Volly Error Message", "Failed with error msg:\t" + error.getMessage());
                if (error.getStackTrace() != null)
                    Log.d("Volly Error StackTrace", "Error StackTrace: \t" + error.getStackTrace());
                // edited here
                try {
                    byte[] htmlBodyBytes = error.networkResponse.data;
                    Log.e("Volly Error Response", new String(htmlBodyBytes), error);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
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
        }) {

            /**
             * Passing some request headers
             */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer 2e718172-f828-4793-850a-2346ce305d99");
                return headers;
            }

        };
        requestQueue.add(jsonObjReq);
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
