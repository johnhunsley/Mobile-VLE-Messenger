package com.mobilevle.messenger;

import com.mobilevle.core.moodle.User;
import com.mobilevle.core.moodle.Message;

/**
 * <p></p>
 *
 * @author johnhunsley
 *         Date: 18-Jan-2011
 *         Time: 16:59:57
 */
public class ConversationPrimer implements Comparable {
    private int messageCount;
    private User communicant;
    private Message latestMessage;

    public ConversationPrimer() { }

    /**
     * 
     * @param latestMessage
     */
    public ConversationPrimer(Message latestMessage) {
        this.latestMessage = latestMessage;
    }

    /**
     *
     * @return messageCount
     */
    public int getMessageCount() {
        return messageCount;
    }

    /**
     *
     * @param messageCount
     */
    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    /**
     *
     * @return communicant
     */
    public User getCommunicant() {
        return communicant;
    }

    /**
     *
     * @param communicant
     */
    public void setCommunicant(User communicant) {
        this.communicant = communicant;
    }

    /**
     *
     * @return latestMessage
     */
    public Message getLatestMessage() {
        return latestMessage;
    }

    /**
     *
     * @param latestMessage
     */
    public void setLatestMessage(Message latestMessage) {
        this.latestMessage = latestMessage;
    }

    /**
     *
     * @param o
     * @return 
     */
    public int compareTo(Object o) {
        ConversationPrimer primer = (ConversationPrimer)o;
        return latestMessage.compareTo(primer.latestMessage);
    }
}
