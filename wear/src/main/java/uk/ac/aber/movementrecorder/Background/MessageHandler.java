package uk.ac.aber.movementrecorder.Background;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Created by einar on 11/03/2018.
 *
 * Code from: https://developer.android.com/training/wearables/data-layer/messages.html
 */

public class MessageHandler implements MessageClient.OnMessageReceivedListener{

    private Context context;
    private static final String WEAR_CAPABILITY_NAME = "WEARABLE";
    private String transcriptionNodeId = null;
    private static final String PATH = "/tremor_tracker_message";
    public static final String MESSAGE_RESP = "uk.ac.aber.movementrecorder.intent.action.incmessage";
    //private GoogleApiClient googleApiClient;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private IMessageHandler iMessageHandler;

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        if(messageEvent.getPath().equals(PATH)) {
            iMessageHandler.onMessageReceived(new String(messageEvent.getData()));
        }
    }

    /**
     * Starts the listener which listens for messages in the message layer.</b>
     * Path for message is set in constant PATH
     */
    public void startMessageListener() {
        Wearable.getMessageClient(context).addListener(this);
    }

    /**
     * Stops message listener.
     */
    public void stopMessageListener() {
        Wearable.getMessageClient(context).removeListener(this);
    }

    public interface IMessageHandler {
        void messageSent(String message);
        void messageFailed(String error);
        void onMessageReceived(String message);
    }

    public MessageHandler(Context context, IMessageHandler iMessageHandler) {
        this.iMessageHandler = iMessageHandler;
        this.context = context;
    }

    /**
     * Startes the AsyncTask that sends the message
     * @param message
     */
    public void send(String message) {
        // Start thread
        new SendMessage().execute(message);
    }

    /**
     * Source:
     * Google, “Send and receive messages on Wear  |  Android Developers,” developer.android.com, 2018. [Online]. Available: https://developer.android.com/training/wearables/data-layer/messages. [Accessed: 01-May-2018].
     * Sets the capability. This ensures that it is connected to the right node (the smartwatch)
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private void setupNode() throws ExecutionException, InterruptedException {
        CapabilityInfo capabilityInfo = Tasks.await(
                Wearable.getCapabilityClient(context).getCapability(
                        WEAR_CAPABILITY_NAME, CapabilityClient.FILTER_REACHABLE));

        Task<Map<String, CapabilityInfo>> capabilitiesTask =
                Wearable.getCapabilityClient(context)
                        .getAllCapabilities(CapabilityClient.FILTER_REACHABLE);

        updateTranscriptionCapability(capabilityInfo);
    }

    private void updateTranscriptionCapability(CapabilityInfo capabilityInfo) {
        final Set<Node> connectedNodes = capabilityInfo.getNodes();

        if(connectedNodes == null) {
            if (mainHandler != null) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "No compatible devices connected", Toast.LENGTH_LONG).show();
                    }
                });
            }
            else {
                Toast.makeText(context, "No compatible devices connected", Toast.LENGTH_LONG).show();
            }
            iMessageHandler.messageFailed("No compatible devices connected");
            return;
        }

        transcriptionNodeId = pickBestNodeId(connectedNodes);
    }

    /**
     * Source:
     * Google, “Send and receive messages on Wear  |  Android Developers,” developer.android.com, 2018. [Online]. Available: https://developer.android.com/training/wearables/data-layer/messages. [Accessed: 01-May-2018].
     *
     * Goes through all the connected nodes and picks the best one
     * @param nodes
     * @return
     */
    private String pickBestNodeId(Set<Node> nodes) {
        String bestNodeId = null;

        for (Node node : nodes) {
            if (node.isNearby()) {
                return node.getId();
            }
            bestNodeId = node.getId();
        }
        return bestNodeId;
    }

    /**
     * Sends the message to the connected node. Called from asynctask
     * @param message
     */
    private void sendMessage(final String message) {
        byte[] byteMessage = message.getBytes();
        if (transcriptionNodeId != null) {
            Task<Integer> sendTask =
                    Wearable.getMessageClient(context).sendMessage(
                            transcriptionNodeId, PATH, byteMessage);
            sendTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
                @Override
                public void onSuccess(Integer integer) {
                    //Toast.makeText(context, "Starting...", Toast.LENGTH_SHORT);
                    iMessageHandler.messageSent(message);
                }
            });
            sendTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "Could not send start to phone", Toast.LENGTH_LONG);
                    iMessageHandler.messageFailed("Could not send start to phone");
                }
            });
        } else {
            //Toast.makeText(context, "Could not find phone", Toast.LENGTH_LONG);
            iMessageHandler.messageFailed("No compatible devices connected");
            // No connected watches
        }
    }

    /**
     * AsyncTask creates a new tread for sending message to not disturb the UI
     */
    private class SendMessage extends AsyncTask<String, Integer, String>{

        @Override
        protected String doInBackground(String... strings) {
            try {
                setupNode();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            sendMessage(strings[0]);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }
}
