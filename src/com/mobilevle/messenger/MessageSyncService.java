package com.mobilevle.messenger;

import android.app.Notification;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;
import com.mobilevle.core.InvalidSessionException;
import com.mobilevle.core.moodle.Message;
import com.mobilevle.messenger.dao.MessagesDAO;
import com.mobilevle.messenger.dao.MessagesDAOSQLiteImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

/**
 * <p>
W/dalvikvm( 3678): threadid=11: thread exiting with uncaught exception (group=0x400259f8)
E/AndroidRuntime( 3678): FATAL EXCEPTION: Thread-15
E/AndroidRuntime( 3678): java.lang.IllegalStateException: database /data/data/com.mobilevle.messenger/databases/mvle_messenger_db already closed
E/AndroidRuntime( 3678):        at android.database.sqlite.SQLiteCompiledSql.<init>(SQLiteCompiledSql.java:58)
E/AndroidRuntime( 3678):        at android.database.sqlite.SQLiteProgram.<init>(SQLiteProgram.java:80)
E/AndroidRuntime( 3678):        at android.database.sqlite.SQLiteQuery.<init>(SQLiteQuery.java:46)
E/AndroidRuntime( 3678):        at android.database.sqlite.SQLiteDirectCursorDriver.query(SQLiteDirectCursorDriver.java:53)
E/AndroidRuntime( 3678):        at android.database.sqlite.SQLiteDatabase.rawQueryWithFactory(SQLiteDatabase.java:1412)
E/AndroidRuntime( 3678):        at android.database.sqlite.SQLiteDatabase.rawQuery(SQLiteDatabase.java:1382)
E/AndroidRuntime( 3678):        at com.mobilevle.messenger.dao.MessagesDAOSQLiteImpl.exists(MessagesDAOSQLiteImpl.java:471)
E/AndroidRuntime( 3678):        at com.mobilevle.messenger.MessageSyncService.updateMessages(MessageSyncService.java:122)
E/AndroidRuntime( 3678):        at com.mobilevle.messenger.MessageSyncService.access$000(MessageSyncService.java:31)
E/AndroidRuntime( 3678):        at com.mobilevle.messenger.MessageSyncService$1$1.run(MessageSyncService.java:71)
I/Process ( 3678): Sending signal. PID: 3678 SIG: 9
V/MediaPlayerService(   67): Client(29) destructor pid = 3678
V/MediaPlayerService(   67): disconnect(29) from pid 3678
V/AudioSink(   67): close
I/ActivityManager(   92): Process com.mobilevle.messenger (pid 3678) has died.
W/ActivityManager(   92): Service crashed 2 times, stopping: ServiceRecord{46a1bd48 com.mobilevle.messenger/.ContactSyncService}
W/ActivityManager(   92): Service crashed 2 times, stopping: ServiceRecord{46973750 com.mobilevle.messenger/.MessageSyncService}
 *
 * </p>
 *
 * @author johnhunsley
 *         Date: 05-Feb-2011
 *         Time: 22:28:11
 */
public class MessageSyncService extends AbstractSyncService {
    private MediaPlayer mp;
    private MessagesDAO messagesDAO;

    @Override
    public void onCreate() {
        super.onCreate();
        Uri ringtoneURI = Uri.parse(preferences.getString(
                getText(R.string.alert_tone_key).toString(),
                "content://settings/system/notification_sound"));
        mp = MediaPlayer.create(MessageSyncService.this, ringtoneURI);
        messagesDAO = new MessagesDAOSQLiteImpl(this);
    }

    /**
     *
     */
    protected void startService() {
        Log.i("MessengerService", "####################### Starting MVLE Message sync service #######################");
        final String messageInterval = getText(R.string.message_interval_key).toString();
        final int defaultMessageInterval = Integer.parseInt(getText(R.string.message_interval_default).toString());
        Log.i("MessengerService", "message inbox timer interval is "+defaultMessageInterval+" millis");
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
                        Log.i("MessengerService", "####################### MVLE Messenger service checking inbox #######################");

                        if(updateMessages()) {
                            //initiate alert sequence
                            if(preferences.getBoolean(getText(R.string.message_alert_key).toString(), true))
                                //play the selected tune
                                mp.start();

                            if(preferences.getBoolean(getText(R.string.message_vibrate_key).toString(), true))
                                ((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(300);

                            String text = getText(R.string.new_message).toString();
                            Notification notification = new Notification(
                                    R.drawable.sym_message, text, System.currentTimeMillis());
                            notification.setLatestEventInfo(MessageSyncService.this, getText(R.string.app_name),
                                    text, contentIntent);

//                                notification.defaults |= Notification.DEFAULT_LIGHTS;
                            notification.ledARGB = Color.MAGENTA;
                            notification.ledOnMS = 300;
                            notification.ledOffMS = 1000;
                            notification.flags |= Notification.FLAG_SHOW_LIGHTS;

                            mNM.notify(R.string.new_message, notification);
                            MessageSyncService.this.sendBroadcast(new Intent("new.message.broadcast"));
                        }
                    }

                }.start();
            }

        }, 0, preferences.getInt(messageInterval, defaultMessageInterval));

        running = true;
    }

    /**
     * <p>Sync the messages from the handler with the message dao. If there are new messages from the handler
     * return true</p>
     * @return true if new messages are found
     */
    private synchronized boolean updateMessages() {
        boolean hasNewMessages = false;

        try {
            List<Message> newMessages = vleHandler.getNewMessages();
            List<Message> uncheckedNewMessages = new ArrayList<Message>();

            if(newMessages != null && !newMessages.isEmpty()) {

                //check if each message has been loaded before
                for(Message newMessage : newMessages) {

                    if(!messagesDAO.exists(newMessage.getId())) uncheckedNewMessages.add(newMessage);
                }

                if(!uncheckedNewMessages.isEmpty()) {
                    Log.i("MessengerService",
                            "####################### MVLE Messenger service - found "+
                                    uncheckedNewMessages.size()+" new messages #######################");
                    hasNewMessages = true;

                    try {
                        messagesDAO.saveReceivedMessages(uncheckedNewMessages);

                    } catch (MVLEMessengerException e) {
                        Log.e("MessageControllerImpl", e.toString());
                    }
                }
            }

        } catch (InvalidSessionException e) {
            refreshSession();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return hasNewMessages;
    }
}
