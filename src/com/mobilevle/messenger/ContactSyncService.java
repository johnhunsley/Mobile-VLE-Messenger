package com.mobilevle.messenger;

import android.content.Intent;
import android.os.Looper;
import android.util.Log;
import com.mobilevle.core.InvalidSessionException;
import com.mobilevle.messenger.dao.UsersDAOSQLiteImpl;

import java.util.TimerTask;

/**
 * <p></p>
 *
 * @author johnhunsley
 *         Date: 04-Feb-2011
 *         Time: 17:13:11
 */
public class ContactSyncService extends AbstractSyncService {
    private ContactSync contactSync;

    @Override
    public void onCreate() {
        super.onCreate();
        contactSync = new ContactSync(vleHandler, new UsersDAOSQLiteImpl(this));
//        startService();
    }



    /**
     * <p></p>
     */
    protected void startService() {
        Log.i("ContactSyncService", "####################### Starting MVLE Contact Sync service #######################");
        final String syncInterval = getText(R.string.contact_interval_key).toString();
        final int defaultSyncInterval = Integer.parseInt(getText(R.string.contact_interval_default).toString());
        Log.i("ContactSyncService", "contact sync timer interval is "+defaultSyncInterval+" millis");
        stopService();

        timer.scheduleAtFixedRate(new TimerTask() {

            /**
             * <p>Call the VLEHandler and get new messages</p>
             */
            public void run() {

                new Thread() {

                    @Override
                    public void run() {
                        Looper.prepare();
                        Log.i("ContactSyncService", "####################### MVLE Contact Sync service checking users #######################");
                        boolean sendBroardcast = false;

                        try {
                            sendBroardcast = contactSync.sync();

                        } catch (InvalidSessionException e) {
                            ContactSyncService.super.refreshSession();

                            try {
                                sendBroardcast = contactSync.sync();

                            } catch (InvalidSessionException e1) {
                                ContactSyncService.super.refreshSession();
                            }
                        }

                        if(sendBroardcast) ContactSyncService.this.sendBroadcast(new Intent("new.contacts.broadcast"));
                    }

                }.start();
            }

        }, 0, preferences.getInt(syncInterval, defaultSyncInterval));

        running = true;
    }
}