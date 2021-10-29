package eu.h2020.helios_social.happ.helios.talk.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import javax.inject.Inject;

import eu.h2020.helios_social.happ.helios.talk.navdrawer.NavDrawerActivity;
import eu.h2020.helios_social.modules.groupcommunications.api.CommunicationManager;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.EventBus;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.NetworkConnectionChangedEvent;


public class NetworkChangeReceiver extends BroadcastReceiver {
    Context mContext;
    EventBus eventBus;
    private boolean isConnected=false;
    private boolean isInitial = true;
    private CommunicationManager communicationManager;

    public NetworkChangeReceiver(EventBus eventBus, CommunicationManager communicationManager) {
        this.eventBus = eventBus;
        this.communicationManager = communicationManager;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        int status = NetworkUtil.getConnectionType(context);

        Log.e("Receiver ", "" + status);

        isConnected = status != Constants.NOT_CONNECT;
        Log.d("broadcasting connection changed event","");
        // eventBus.broadcast(new NetworkConnectionChangedEvent(isConnected,status));
        if (isInitial) isInitial = false;
        else communicationManager.setNeedRestart(true);
    }

    public boolean isInitial() {
        return isInitial;
    }

    public void setInitial(boolean initial) {
        isInitial = initial;
    }
}
