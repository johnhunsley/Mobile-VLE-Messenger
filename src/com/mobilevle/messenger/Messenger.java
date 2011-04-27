package com.mobilevle.messenger;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import com.mobilevle.core.InvalidSessionException;
import com.mobilevle.core.moodle.Message;
import com.mobilevle.core.moodle.MoodleVLEHandler;
import com.mobilevle.messenger.dao.MessagesDAO;
import com.mobilevle.messenger.dao.MessagesDAOSQLiteImpl;
import com.mobilevle.messenger.dao.UsersDAO;
import com.mobilevle.messenger.dao.UsersDAOSQLiteImpl;
import com.mobilevle.oktech.session.Session;
import com.mobilevle.oktech.session.SessionDAO;
import com.mobilevle.oktech.session.SessionDAOSQLiteImpl;

import java.util.Calendar;

/**
 * <p></p>
 *
 * @author johnhunsley
 *         Date: 11-Feb-2011
 *         Time: 14:06:59
 */
public class Messenger implements Runnable {
    public final static int SEND_SUCCESSFUL = 0;
    public final static int SEND_SESSION_FAIL = 1;
    public final static int SEND_FAIL = 2;
    final String recipientId, content;
    final UsersDAO usersDAO;
    final MessagesDAO messagesDAO;
    final SessionDAO sessionDAO;
    final MoodleVLEHandler handler;
    final Handler processHandler;
    final Context context;

    /**
     *
     * @param recipientId
     * @param content
     */
    public Messenger(MoodleVLEHandler handler,
                     Context context,
                     Handler processHandler,
                     final String recipientId,
                     final String content) {
        usersDAO = new UsersDAOSQLiteImpl(context);
        messagesDAO = new MessagesDAOSQLiteImpl(context);
        sessionDAO = new SessionDAOSQLiteImpl(context);
        this.context = context;
        this.handler = handler;
        this.processHandler = processHandler;
        this.recipientId = recipientId;
        this.content = content;
    }

    /**
     *
     */
    public void run() {
        Message message = new Message();
        message.setRead(Message.READ);
        message.setContent(content);
        message.setToUser(usersDAO.getUser(recipientId));
        message.setFromUser(sessionDAO.loadSession().asAuthenticatedUser());
        message.setSendDate(Calendar.getInstance().getTime());
        message.setType(Message.OUTBOUND);

        try {
            send(message);
            processHandler.sendEmptyMessage(SEND_SUCCESSFUL);

        } catch (InvalidSessionException e) {
            Log.i("Messenger", "Session is invalid, revalidating....");

            if(refreshSession(sessionDAO.loadSession())) {

                try {
                    send(message);
                    processHandler.sendEmptyMessage(SEND_SUCCESSFUL);

                } catch (InvalidSessionException e1) {
                    Log.e("Messenger", e1.toString());
                    processHandler.sendEmptyMessage(SEND_SESSION_FAIL);
                    
                } catch (MVLEMessengerException e1) {
                    Log.e("Messenger", e1.toString());
                    processHandler.sendEmptyMessage(SEND_FAIL);
                }
                
            } else processHandler.sendEmptyMessage(SEND_FAIL);

        } catch (MVLEMessengerException e) {
            Log.e("Messenger", e.toString());
            processHandler.sendEmptyMessage(SEND_FAIL);
        }
    }

    /**
     *
     * @param message
     * @throws InvalidSessionException
     */
    private void send(Message message) throws InvalidSessionException, MVLEMessengerException {
        handler.sendMessage(message);
        messagesDAO.saveSentMessage(message);
    }

    /**
     *
     * @param session
     * @return
     */
    private boolean refreshSession(Session session) {
        if(session == null) return false;

        try {
            Log.i("Messenger",
                    "####################### refreshing session with stored credentials #######################");
            //always remember the password as its needed for the message checker service
            return handler.authenticate(
                    context, session.getUsername(), session.getPassword(), true);

        } catch (InvalidSessionException e) {
            return false;
        }
    }
}
