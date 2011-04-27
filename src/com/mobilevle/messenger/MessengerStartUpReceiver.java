package com.mobilevle.messenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * <p></p>
 *
 * @author johnhunsley
 *         Date: 07-Jan-2011
 *         Time: 17:26:39
 */
public class MessengerStartUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("MessengerStartUpReceiver",
                "####################### MVLE Messenger booting receiver #######################");
        boot(context);
    }

    /**
     *
     * @param context
     */
    public static void boot(Context context) {
        //boot up the message checking service
        Intent serviceIntent1 = new Intent();
        serviceIntent1.setAction("com.mobilevle.messenger.MessageSyncService");
        context.startService(serviceIntent1);

        //boot up the contact syn service
        Intent serviceIntent2 = new Intent();
        serviceIntent2.setAction("com.mobilevle.messenger.ContactSyncService");
        context.startService(serviceIntent2);
    }
}