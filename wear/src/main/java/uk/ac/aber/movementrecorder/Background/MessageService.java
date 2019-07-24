package uk.ac.aber.movementrecorder.Background;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import androidx.annotation.Nullable;

import static uk.ac.aber.movementrecorder.Background.MessageHandler.MESSAGE_RESP;
import static uk.ac.aber.movementrecorder.Background.SSManager.ActivityReceiver.ACTIVITY_RESP;

/**
 * Created by einar on 23/07/2018.
 */

public class MessageService extends Service implements MessageHandler.IMessageHandler{

    private MessageHandler messageHandler;
    private ServiceReceiver serviceReceiver;
    private boolean receiversRegisterd = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        messageHandler = new MessageHandler(this, this);
        messageHandler.startMessageListener();


        registerReceivers();


        return START_NOT_STICKY;
    }

    private void registerReceivers() {
        if (receiversRegisterd)
            return;
        // Register broadcast receiver for service
        IntentFilter serviceFilter = new IntentFilter(ACTIVITY_RESP);
        serviceFilter.addCategory(Intent.CATEGORY_DEFAULT);
        serviceReceiver = new ServiceReceiver();
        this.registerReceiver(serviceReceiver, serviceFilter);

        receiversRegisterd = true;
    }

    private void unregisterReceivers() {
        if (!receiversRegisterd)
            return;
        unregisterReceiver(serviceReceiver);

        receiversRegisterd = false;
    }

    @Override
    public void onDestroy() {
        messageHandler.stopMessageListener();
        unregisterReceivers();
        super.onDestroy();
    }

    @Override
    public void messageSent(String message) {

    }

    @Override
    public void messageFailed(String error) {

    }

    @Override
    public void onMessageReceived(String message) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MESSAGE_RESP);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra("id", "message");
        broadcastIntent.putExtra("message", message);
        this.sendBroadcast(broadcastIntent);
    }

    public class ServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getStringExtra("id")){
                case "message":
                    if (intent.getStringExtra("message").equals("destroy")) {
                        unregisterReceivers();
                        messageHandler.stopMessageListener();
                        stopSelf();
                    }
                default:
                    break;
            }
        }
    }
}
