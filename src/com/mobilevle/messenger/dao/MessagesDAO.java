package com.mobilevle.messenger.dao;

import com.mobilevle.core.moodle.Message;
import com.mobilevle.messenger.ConversationPrimer;
import com.mobilevle.messenger.MVLEMessengerException;

import java.util.List;
import java.util.Set;

/**
 * <p></p>
 *
 * @author johnhunsley
 *         Date: 06-Jan-2011
 *         Time: 10:12:24
 */
public interface MessagesDAO {

    /**
     *
     * @param userId
     * @return
     */
    List<Message> getConversationWithUser(String userId);


    /**
     * <p>
     * Update all conversation messages as read for those messages sent by the user with the given id
     * </p>
     * @param userId
     */
    void setConversationMessagesRead(String userId);

    /**
     * <p>
     * Get a list of the latest {@link Message} objects for each conversation
     * </p>
     * @param myId - the id of the authenticated user 
     * @return List of {@link Message} objects
     */
    Set<Message> getLatestMessageForAllConversations(String myId);

    /**
     *
     * @param myId
     * @return List of {@link ConversationPrimer}
     */
    List<ConversationPrimer> getConversationPrimers(String myId);

    /**
     *
     * @param id
     * @return number of message in the conversation with the user with the given id
     */
    int getConversationMessageCount(String id);

    /**
     * <p>
     *   Get a list of the latest {@link Message} object from a conversation with the given user id
     * </p>
     * @param userId
     * @return {@link Message}
     */
    Message getLatestConversationMessage(String userId);

    /**
     * 
     * @param id
     * @return
     */
    Message getMessage(int id);

    /**
     * 
     * @param message
     * @throws MVLEMessengerException
     */
    void saveSentMessage(Message message) throws MVLEMessengerException;

    /**
     *
     * @param message
     */
    void saveReceivedMessage(Message message) throws MVLEMessengerException;

    /**
     * 
     * @param messages
     */
    void saveReceivedMessages(List<Message> messages) throws MVLEMessengerException;

    /**
     * <p>Does the database hold a {@link Message} object with the given id?</p>
     * @param id
     * @return true if a {@link Message} object with the given id exists
     */
    boolean exists(int id);

    /**
     * <p>delete all messages sent or received to or from the User with the given id</p>
     * @param id
     */
    void deleteConversation(String id);

    /**
     * <p>Delete the message with the given id</p>
     * @param id
     */
    void deleteMessage(int id);
}
