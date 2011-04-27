package com.mobilevle.messenger;

import org.apache.commons.collections.Predicate;

import java.util.Calendar;

import android.util.Log;

/**
 * <p>
 * Filter {@link ConversationPrimer} objects by message date. Is the date within the epoch defined by the
 * start and end dates.
 * </p>
 *
 * @author johnhunsley
 *         Date: 20-Jan-2011
 *         Time: 22:20:18
 */
public class ConversationPrimerEpochPredicate implements Predicate {
    public final static int DEFAULT    = 0;
    public final static int TODAY      = 1;
    public final static int YESTERDAY  = 2;
    public final static int THIS_WEEK  = 3;
    public final static int LAST_WEEK  = 4;
    public final static int THIS_MONTH = 5;
    final long startOfEpoch;
    final long endOfEpoch;

    /**
     * <p>
     * Set the epoch start and end dates in UNIX time stamps
     * </p>
     * @param epoch
     */
    public ConversationPrimerEpochPredicate(final int epoch) throws MVLEMessengerException {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        switch(epoch) {

            case TODAY :
                startOfEpoch = cal.getTimeInMillis();
                cal.add(Calendar.DATE, 1);
                endOfEpoch = cal.getTimeInMillis();
                break;

            case YESTERDAY :
                endOfEpoch = cal.getTimeInMillis();
                cal.add(Calendar.DATE, -1);
                startOfEpoch = cal.getTimeInMillis();
                break;

            case THIS_WEEK :
                cal.add(Calendar.DATE, -1);
                endOfEpoch = cal.getTimeInMillis();
                cal.set(Calendar.DAY_OF_WEEK, 1);
                startOfEpoch = cal.getTimeInMillis();
                break;

            case LAST_WEEK :
                cal.set(Calendar.DAY_OF_WEEK, 1);
                endOfEpoch = cal.getTimeInMillis();
                cal.add(Calendar.DATE, -7);
                startOfEpoch = cal.getTimeInMillis();
                break;

            case THIS_MONTH :
                cal.set(Calendar.DAY_OF_WEEK, 1);
                cal.add(Calendar.DATE, -7);
                endOfEpoch = cal.getTimeInMillis();
                cal.set(Calendar.DAY_OF_MONTH, 1);
                startOfEpoch = cal.getTimeInMillis();
                break;

            case DEFAULT :
                cal.set(Calendar.DAY_OF_MONTH, 1);
                endOfEpoch = cal.getTimeInMillis();
                startOfEpoch = 0;
                break;

            default : throw new MVLEMessengerException(epoch+" is not a valid predicate epoch");
        }
    }



    /**
     * <p>
     *
     * </p>
     * @param o
     * @return true if the {@link ConversationPrimer} object's message date falls between the start and end dates
     */
    public boolean evaluate(Object o) {
        boolean evaluation = false;

        if(o instanceof ConversationPrimer) {
            ConversationPrimer primer = (ConversationPrimer)o;
            final long messageTime = primer.getLatestMessage().getSendDate().getTime();

            if(messageTime > startOfEpoch && messageTime <= endOfEpoch) evaluation = true;
        }

        return evaluation;
    }
}
