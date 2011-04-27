package com.mobilevle.messenger;

import android.content.Context;
import android.widget.Toast;

/**
 * <p></p>
 *
 * @author johnhunsley
 *         Date: 12-Feb-2011
 *         Time: 15:57:19
 */
public class MessengerToast {

    /**
     *
     * @param context
     * @param length
     * @param messageValue
     * @return
     * @throws MVLEMessengerException
     */
    public static Toast makeMessengerToast(Context context, final int length, final int messageValue) throws MVLEMessengerException{
        String toastMsg;

        switch(messageValue) {
            case Messenger.SEND_SUCCESSFUL: toastMsg = "Message sent successfully"; break;
            case Messenger.SEND_SESSION_FAIL: toastMsg =
                    "Message failed! please check your Moodle username and password are correct in settings"; break;
            case Messenger.SEND_FAIL: toastMsg = "Message failed! Maybe the Moodle server is down.\nPlease resend later"; break;
            default: throw new MVLEMessengerException(messageValue+" was an recognised value");
        }

        return Toast.makeText(context, toastMsg, length);
    }
}
