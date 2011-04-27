package com.mobilevle.messenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * <p>
 * Used for Activities which wish to listen for new messages arriving. For example, the conversation and conversations
 * activities will need to display a new message if one arrives whilst the activity is active. The implemented
 * {@link BroadcastReceiver} is registered for the duration the sub classed activity is active.
 * </p>
 *
 * @author johnhunsley
 *         Date: 12-Feb-2011
 *         Time: 23:40:55
 */
public class AbstractMVLEMoodleMessageReceivingActivity extends AbstractMVLEMoodleActivity {

    protected final  BroadcastReceiver newMessageReceiver =
            new BroadcastReceiver() {
                /**
                 *
                 * @param context
                 * @param intent
                 */
                public void onReceive(Context context, Intent intent) {
                    AbstractMVLEMoodleMessageReceivingActivity.this.onResume();
                }
            };

    /**
     * <p>Register the {@link BroadcastReceiver}</p>
     */
    protected void onResume() {
        registerReceiver(newMessageReceiver, new IntentFilter("new.message.broadcast"));
        super.onResume();
    }

    /**
     * <p>unregister the {@link BroadcastReceiver}</p>
     */
    protected void onPause() {
        unregisterReceiver(newMessageReceiver);
        super.onPause();
    }
}
