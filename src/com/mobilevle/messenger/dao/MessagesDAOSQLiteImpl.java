package com.mobilevle.messenger.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import com.mobilevle.core.moodle.Message;
import com.mobilevle.core.moodle.User;
import com.mobilevle.messenger.ConversationPrimer;
import com.mobilevle.messenger.MVLEMessengerException;

import java.util.*;

/**
 * <p></p>
 *
 * @author johnhunsley
 *         Date: 07-Jan-2011
 *         Time: 17:01:49
 */
public class MessagesDAOSQLiteImpl implements MessagesDAO {
    private static final int FROM = 0;
    private static final int TO = 1;
    private Context context;
    private DatabaseHelper dbHelper;

    /**
     *
     * @param context
     */
    public MessagesDAOSQLiteImpl(Context context) {
        this.context = context;
        open();
    }

    /**
     *
     * @return this
     */
    public MessagesDAOSQLiteImpl open() {

        if(dbHelper == null || !dbHelper.getWritableDatabase().isOpen())
            dbHelper = new DatabaseHelper(this.context);

        return this;
    }

    /**
     *
     * @param userId
     * @return
     */
    public List<Message> getConversationWithUser(final String userId) {
        SortedSet<Message> messages = new TreeSet<Message>();

        final String sql1 = "SELECT "+DatabaseHelper.MSG_DATE+", "+DatabaseHelper.MSG_ID+", "
                +DatabaseHelper.MSG_FROM_USER+", "+DatabaseHelper.MSG_TO_USER+", "
                +DatabaseHelper.MSG_CONTENT+", "+DatabaseHelper.MSG_SUBJECT+", "+DatabaseHelper.MSG_READ+
                " FROM "+DatabaseHelper.MESSAGES_TABLE+" WHERE "+DatabaseHelper.MSG_FROM_USER+" LIKE ?"+
                " OR "+DatabaseHelper.MSG_TO_USER+" LIKE ? ORDER BY "+DatabaseHelper.MSG_DATE+" ASC";
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor1 = database.rawQuery(sql1, new String[] {userId, userId});

        while(cursor1.moveToNext()) {
            Message inbound = fromCursor(cursor1, false);
            inbound.setType(Message.INBOUND);
            messages.add(inbound);
        }

        cursor1.close();

        final String sql2 = "SELECT "+DatabaseHelper.MSG_DATE+", "+DatabaseHelper.MSG_ID+", "
                +DatabaseHelper.MSG_TO_USER+", "
                +DatabaseHelper.MSG_CONTENT+", "+DatabaseHelper.MSG_SUBJECT+
                " FROM "+DatabaseHelper.SENT_MESSAGES_TABLE+" WHERE "+DatabaseHelper.MSG_TO_USER+" LIKE ? "+
                "ORDER BY "+DatabaseHelper.MSG_DATE+" ASC";
        Cursor cursor2 = database.rawQuery(sql2, new String[] {userId});

        while(cursor2.moveToNext()) {
            Message outbound = fromCursor(cursor2, false);
            outbound.setType(Message.OUTBOUND);
            outbound.setRead(Message.READ);
            messages.add(outbound);
        }

        cursor2.close();

        database.close();
        return new ArrayList<Message>(messages);
    }

    /**
     *
     * @param userId
     */
    public void setConversationMessagesRead(final String userId) {
        final String sql = "UPDATE "+DatabaseHelper.MESSAGES_TABLE+" SET "+DatabaseHelper.MSG_READ+" = 1 "+
                "WHERE "+DatabaseHelper.MSG_FROM_USER+" LIKE ?";
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.beginTransaction();
        database.execSQL(sql, new String[]{userId});
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
    }

    /**
     * <p>
     *
     * </p>
     * @param myId
     * @return {@link Set}
     */
    public Set<Message> getLatestMessageForAllConversations(final String myId) {
        SortedSet<Message> sortedLatestConversationMessages = new TreeSet<Message>();
        Set<String> conversationalUserIds = new HashSet<String>();
        conversationalUserIds.addAll(getConversationUsers(FROM));
        conversationalUserIds.addAll(getConversationUsers(TO));
        conversationalUserIds.remove(myId);

        for(String id : conversationalUserIds) sortedLatestConversationMessages.add(getLatestConversationMessage(id));

        return sortedLatestConversationMessages;
    }

    /**
     * <p>
     * Get the latest messages for all conversations and create {@link ConversationPrimer} objects for each one
     *
     * </p>
     * @param myId
     * @return List of {@link ConversationPrimer}
     */
    public List<ConversationPrimer> getConversationPrimers(final String myId) {
        Set<Message> messages =  getLatestMessageForAllConversations(myId);
        List<ConversationPrimer> primers = new ArrayList<ConversationPrimer>();

        for(Message message : messages) {
            ConversationPrimer primer = new ConversationPrimer(message);

            if(message.getFromUser() != null) {

                if(!message.getFromUser().getId().equals(myId)) primer.setCommunicant(message.getFromUser());

            } else if(message.getToUser() != null) {

                if(!message.getToUser().getId().equals(myId)) primer.setCommunicant(message.getToUser());
            }

            //if for some reason there is no communicant then skip it
            if(primer.getCommunicant() != null) {
                primer.setMessageCount(getConversationMessageCount(primer.getCommunicant().getId()));
                primers.add(primer);
            }
        }

        return primers;
    }

    /**
     *
     * @param userId
     * @return
     */
    private int getConversationReceivedMessageCount(final String userId) {
        final String sql = "SELECT "+DatabaseHelper.MSG_ID+" FROM "+
                DatabaseHelper.MESSAGES_TABLE+" WHERE "+DatabaseHelper.MSG_FROM_USER+" LIKE ?"+
                " OR "+DatabaseHelper.MSG_TO_USER+" LIKE ?";
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = database.rawQuery(sql, new String[] {userId, userId});

        final int count = cursor.getCount();
        cursor.close();
        database.close();
        return count;
    }

    /**
     *
     * @param userId
     * @return
     */
    private int getConversationSentMessageCount(final String userId) {
        final String sql = "SELECT "+DatabaseHelper.MSG_ID+" FROM "+
                DatabaseHelper.SENT_MESSAGES_TABLE+" WHERE "+DatabaseHelper.MSG_TO_USER+" LIKE ?";
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = database.rawQuery(sql, new String[] {userId});

        final int count = cursor.getCount();
        cursor.close();
        database.close();
        return count;
    }

    /**
     *
     * @param userId
     * @return
     */
    public int getConversationMessageCount(final String userId) {
        return getConversationReceivedMessageCount(userId)+getConversationSentMessageCount(userId);
    }

    /**
     *
     * @param userId
     * @return
     */
    private Message getLatestConversationReceivedMessage(final String userId) {
        Message message = null;
        final String sql = "SELECT MAX("+DatabaseHelper.MSG_DATE+"), "+DatabaseHelper.MSG_ID+", "
                +DatabaseHelper.MSG_FROM_USER+", "+DatabaseHelper.MSG_TO_USER+", "
                +DatabaseHelper.MSG_CONTENT+", "+DatabaseHelper.MSG_SUBJECT+", "+DatabaseHelper.MSG_READ+
                " FROM "+DatabaseHelper.MESSAGES_TABLE+" WHERE "+DatabaseHelper.MSG_FROM_USER+" LIKE ?"+
                " OR "+DatabaseHelper.MSG_TO_USER+" LIKE ?";
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = database.rawQuery(sql, new String[] {userId, userId});

        if(cursor.moveToFirst()) message = fromCursor(cursor, true);

        cursor.close();
        database.close();
        return message;
    }

    /**
     *
     * @param userId
     * @return
     */
    private Message getLatestConversationSentMessage(final String userId) {
        Message message = null;
        final String sql = "SELECT MAX("+DatabaseHelper.MSG_DATE+"), "+DatabaseHelper.MSG_ID+", "
                +DatabaseHelper.MSG_TO_USER+", "
                +DatabaseHelper.MSG_CONTENT+", "+DatabaseHelper.MSG_SUBJECT+
                " FROM "+DatabaseHelper.SENT_MESSAGES_TABLE+" WHERE "+DatabaseHelper.MSG_TO_USER+" LIKE ?";
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = database.rawQuery(sql, new String[] {userId});

        if(cursor.moveToFirst()) {
            message = fromCursor(cursor, true);

            if(message != null) {
                message.setRead(Message.READ);
                message.setType(Message.OUTBOUND);
            }
        }

        cursor.close();
        database.close();
        return message;
    }

    /**
     *
     * @param userId
     * @return
     */
    public Message getLatestConversationMessage(final String userId) {
        Message receivedMessage =  getLatestConversationReceivedMessage(userId);
        Message sentMessage = getLatestConversationSentMessage(userId);

        if(receivedMessage != null && sentMessage != null) {

            if(receivedMessage.getSendDate().after(sentMessage.getSendDate())) return receivedMessage;
            else return sentMessage;
        }

        if(receivedMessage != null) return receivedMessage;

        if(sentMessage != null) return sentMessage;

        return null;
    }

    /**
     *
     * @param cursor
     * @return
     */
    private Message fromCursor(Cursor cursor, final boolean maxDate) {
        UsersDAO userDAO = new UsersDAOSQLiteImpl(context);
        Message message = new Message();
        final int messageId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.MSG_ID));

        if(messageId == 0) return null;

        message.setId(messageId);

        if(maxDate) {
            String dateStr = cursor.getString(cursor.getColumnIndexOrThrow("MAX("+DatabaseHelper.MSG_DATE+")"));

            if(dateStr != null)  message.setSendDate(new Date(Long.parseLong(dateStr)));

        } else message.setSendDate(new Date(Long.parseLong(
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.MSG_DATE)))));

        message.setContent(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.MSG_CONTENT)));
        message.setSubject(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.MSG_SUBJECT)));

        final int readCol = cursor.getColumnIndex(DatabaseHelper.MSG_READ);
        final int fromCol = cursor.getColumnIndex(DatabaseHelper.MSG_FROM_USER);
        final int toCol =  cursor.getColumnIndex(DatabaseHelper.MSG_TO_USER);

        if(readCol > -1) message.setRead(cursor.getInt(readCol));

        if(fromCol > -1) {
            String fromUserId = cursor.getString(fromCol);

            if(fromUserId != null && !fromUserId.equals("0")) message.setFromUser(userDAO.getUser(fromUserId));
        }

        if(toCol > -1) {
            String toUserId = cursor.getString(toCol);

            if(toUserId != null && !toUserId.equals("0")) message.setToUser(userDAO.getUser(toUserId));

        }

        return message;
    }

    /**
     *
     * @param direction
     * @return
     */
    private List<String> getConversationUsers(final int direction) throws SQLiteException {
        String column;
        String table;
        List<String> userIds = new ArrayList<String>();

        switch(direction) {
            case TO : column = DatabaseHelper.MSG_TO_USER;
                table = DatabaseHelper.SENT_MESSAGES_TABLE;
                break;

            case FROM : column = DatabaseHelper.MSG_FROM_USER;
                table = DatabaseHelper.MESSAGES_TABLE;
                break;

            default : throw new SQLiteException(direction+" is an unrecognised message direction");
        }

        final String sql = "SELECT DISTINCT "+column+" FROM "+table+" WHERE "+column+" IS NOT '0'";
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = database.rawQuery(sql, null);

        while(cursor.moveToNext()) {
            userIds.add(cursor.getString(cursor.getColumnIndexOrThrow(column)));
        }

        cursor.close();
        database.close();
        return userIds;
    }

    /**
     *
     * @param id
     * @return
     */
    public Message getMessage(final int id) {
        return null;
    }

    /**
     *
     * @param message
     * @throws MVLEMessengerException
     */
    public void saveSentMessage(final Message message) throws MVLEMessengerException {
        if(message.getToUser() == null) throw new MVLEMessengerException(
                "Sent message has no recipient user!........... Tard! go find yourself another job!");

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        message.setId(getNextSentMessageId());
        ContentValues contentValues = new ContentValues();
        contentValues.put(dbHelper.MSG_ID, message.getId());
        contentValues.put(dbHelper.MSG_TO_USER, message.getToUser().getId());
        String content = message.getContent().replaceAll("\\<p\\>", "\n").replaceAll("\\<br\\>", "\n");
        contentValues.put(dbHelper.MSG_CONTENT, content.replaceAll("\\<.*?\\>", ""));
        contentValues.put(dbHelper.MSG_SUBJECT, message.getSubject());
        contentValues.put(dbHelper.MSG_DATE, Long.toString(message.getSendDate().getTime()));
        database.beginTransaction();
        database.insert(dbHelper.SENT_MESSAGES_TABLE, null, contentValues);
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
    }


    /**
     *
     * @param message
     */
    public void saveReceivedMessage(final Message message) throws MVLEMessengerException {
        UsersDAO userDAO = new UsersDAOSQLiteImpl(context);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(dbHelper.MSG_ID, message.getId());
        User communicant = null;

        if(message.getFromUser() != null) {
            communicant = message.getFromUser();
            contentValues.put(dbHelper.MSG_FROM_USER, communicant.getId());
        }

        else contentValues.put(dbHelper.MSG_FROM_USER, "0");

        if(message.getToUser() != null) {
            communicant = message.getToUser();
            contentValues.put(dbHelper.MSG_TO_USER, communicant.getId());
        }

        else contentValues.put(dbHelper.MSG_TO_USER, "0");

        if(communicant == null) throw new MVLEMessengerException("No identifiable communicant for message id "+message.getId());

        if(!userDAO.exists(communicant)){
            Log.i("MessageDAO", "No user currently existing for this communicant saving with id "+communicant.getId());
            userDAO.saveUser(communicant);
        }

        String content = message.getContent().replaceAll("\\<p\\>", "\n").replaceAll("\\<br\\>", "\n");
        contentValues.put(dbHelper.MSG_CONTENT, content.replaceAll("\\<.*?\\>", ""));
        contentValues.put(dbHelper.MSG_SUBJECT, message.getSubject());
        contentValues.put(dbHelper.MSG_DATE, Long.toString(message.getSendDate().getTime()));
        contentValues.put(DatabaseHelper.MSG_READ, 0);
        database.beginTransaction();
        database.insert(dbHelper.MESSAGES_TABLE, null, contentValues);
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
    }

    /**
     *
     * @return the max message id value plus 1
     */
    private int getNextSentMessageId() {
        final String sql = "SELECT MAX("+DatabaseHelper.MSG_ID+") AS MAX_ID FROM "+DatabaseHelper.SENT_MESSAGES_TABLE;
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = database.rawQuery(sql, null);

        //return a negative number so not to confuse with message from Moodle which may have same id
        if(cursor.moveToFirst()) return cursor.getInt(cursor.getColumnIndexOrThrow("MAX_ID"))+1;

        return 1;
    }

    /**
     *
     * @param messages
     */
    public void saveReceivedMessages(List<Message> messages) throws MVLEMessengerException {
        for(Message message : messages) saveReceivedMessage(message);
    }

    /**
     *
     * @param id
     * @return
     */
    public boolean exists(final int id) {
        final String sql =
                "select "+dbHelper.MSG_ID+" from "+dbHelper.MESSAGES_TABLE+" where "+dbHelper.MSG_ID+" = ?";
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = database.rawQuery(sql, new String[] {Integer.toString(id)});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        database.close();
        return exists;
    }

    /**
     * <p></p>
     * @param userId
     */
    public void deleteConversation(final String userId) {
        final String deleteReceived = "DELETE FROM "+DatabaseHelper.MESSAGES_TABLE+" WHERE "+DatabaseHelper.MSG_FROM_USER+" = ?";
        final String deleteSent = "DELETE FROM "+DatabaseHelper.SENT_MESSAGES_TABLE+" WHERE "+DatabaseHelper.MSG_TO_USER+" = ?";
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.beginTransaction();
        database.execSQL(deleteReceived, new String[]{userId});
        database.execSQL(deleteSent, new String[]{userId});
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
    }

    /**
     *
     * @param id
     */
    public void deleteMessage(final int id) {
        final String deleteMessage = "DELETE FROM "+DatabaseHelper.MESSAGES_TABLE+" WHERE "+DatabaseHelper.MSG_ID+" = ?";
        final String deleteSentMessage = "DELETE FROM "+DatabaseHelper.SENT_MESSAGES_TABLE+" WHERE "+DatabaseHelper.MSG_ID+" = ?";
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.beginTransaction();
        database.execSQL(deleteMessage, new Integer[]{id});
        database.execSQL(deleteSentMessage, new Integer[]{id});
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
    }


}
