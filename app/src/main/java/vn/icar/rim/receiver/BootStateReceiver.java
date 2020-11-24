package vn.icar.rim.receiver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import vn.icar.rim.service.SerialListenerService;

public class BootStateReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent)
    {

//        SerialListenerService.intent(context).start();
        context.startService(new Intent(context, SerialListenerService.class));

    }

}
