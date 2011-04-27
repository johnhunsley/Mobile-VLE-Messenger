package com.mobilevle.messenger;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import com.mobilevle.core.MobileVLECoreException;
import com.mobilevle.core.moodle.MoodleVLEHandler;
import com.mobilevle.oktech.session.SessionDAO;
import com.mobilevle.oktech.session.SessionDAOSQLiteImpl;

import java.util.Timer;

/**
 * <p></p>
 *
 * @author johnhunsley
 *         Date: 04-Feb-2011
 *         Time: 17:15:48
 */
public abstract class AbstractSyncService extends Service {
    protected MoodleVLEHandler vleHandler;
    protected Timer timer = new Timer();
    protected SharedPreferences preferences;
    protected boolean running = false;
    protected NotificationManager mNM;
    protected PendingIntent contentIntent;

    @Override
    public void onCreate() {

        try{
            preferences = PreferenceManager.getDefaultSharedPreferences(this);
            mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            contentIntent = PendingIntent.getActivity(
                    AbstractSyncService.this, 0,
                    new Intent(AbstractSyncService.this, MVLEConversationsActivity.class), 0);
            vleHandler = (MoodleVLEHandler)VLEHandlerProvider.provideVLEHandler(this);

        } catch (MobileVLECoreException e) {
            Log.i(getClass().getName(), e.toString());
        }

        super.onCreate();
    }

    /**
     *
     * @return true if the timer is running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     *
     */
    public void stopService() {

        if(running) {
            timer.cancel();
            timer = new Timer();
            running = false;
        }
    }

    /**
     *
     * @param intent
     * @return
     */
    public IBinder onBind(Intent intent) {
        return null;
    }



    /**
     *
     */
    protected final void refreshSession() {
        //revalidate session
        SessionDAO sessionDAO = new SessionDAOSQLiteImpl(this);
        AsyncSessionRefreshTask refreshTask = new  AsyncSessionRefreshTask(vleHandler, this);
        refreshTask.execute(sessionDAO.loadSession());
    }

    @Override
    public void onStart(Intent intent, int i) {
        startService();
    }

    public int onStartCommand(Intent intent, int i, int i1) {
        startService();
        //START_STICKY
        return 1;
    }

    /**
     * <p></p>
     */
    protected abstract void startService();

}
