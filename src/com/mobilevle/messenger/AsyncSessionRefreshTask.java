package com.mobilevle.messenger;

import android.os.AsyncTask;
import android.os.Looper;
import android.content.Context;
import android.util.Log;
import com.mobilevle.oktech.session.Session;
import com.mobilevle.core.VLEHandler;
import com.mobilevle.core.InvalidSessionException;

/**
 * <p></p>
 *
 * @author johnhunsley
 *         Date: 26-Jan-2011
 *         Time: 22:28:01
 */
public class AsyncSessionRefreshTask extends AsyncTask<Session, Void, Boolean> {
    final VLEHandler handler;
    final Context context;

    /**
     *
     * @param handler
     */
    public AsyncSessionRefreshTask(final VLEHandler handler, final Context context) {
        this.handler = handler;
        this.context = context;
    }

    /**
     *
     * @param sessions
     * @return
     */
    protected Boolean doInBackground(Session[] sessions) {
        if(sessions.length < 1 || sessions[0] == null) return false;

        try {
            Log.i("AsyncSessionRefreshTask",
                    "####################### refreshing session with stored credentials #######################");
            //always remember the password as its needed for the message checker service
            return handler.authenticate(
                    context, sessions[0].getUsername(), sessions[0].getPassword(), true);

        } catch (InvalidSessionException e) {
            return false;
        }
    }
}
