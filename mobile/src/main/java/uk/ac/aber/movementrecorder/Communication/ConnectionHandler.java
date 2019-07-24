package uk.ac.aber.movementrecorder.Communication;

import android.content.Context;
import androidx.annotation.NonNull;

import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by einar on 30/01/2018.
 */

public class ConnectionHandler implements DataClient.OnDataChangedListener {

    private IConnectionHandler iConnectionHandler;
    private Context context;
    private static final String PATH = "/tremor_tracker_message";



    // Data
    public interface IConnectionHandler {
        void dataReceived(DataMap data);
    }

    public ConnectionHandler(Context context, IConnectionHandler iConnectionHandler) {
        this.iConnectionHandler = iConnectionHandler;
        this.context = context;
    }

    /**
     * Source:
     * Google, “Sync data items on Wear  |  Android Developers,” developer.android.com, 2018. [Online]. Available: https://developer.android.com/training/wearables/data-layer/data-items. [Accessed: 02-May-2018].
     *
     * Incoming data are captured in this method and sent to datareceived() in communication service
     * @param dataEventBuffer
     */
    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {

        for (DataEvent dataEvent : dataEventBuffer) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = dataEvent.getDataItem();
                if(item.getUri().getPath().compareTo("/tremor-allaxis") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    iConnectionHandler.dataReceived(dataMap);
                }
            }
        }
    }

    /**
     * Starts/resumes the onDataChangedListener
     */
    public void resume() {
        Wearable.getDataClient(context).addListener(this);
    }

    /**
     * Stops/pauses the onDataChangedListener
     */
    public void pause() {
        Wearable.getDataClient(context).removeListener(this);
    }

}
