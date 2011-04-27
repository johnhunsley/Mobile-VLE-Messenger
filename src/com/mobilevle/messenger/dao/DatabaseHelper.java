package com.mobilevle.messenger.dao;

import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.content.Context;

/**
 * <p></p>
 *   
 * @author johnhunsley
 *         Date: 07-Jan-2011
 *         Time: 14:36:36
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    public static String DATABASE_NAME = "mvle_messenger_db";
    public static final int DATABASE_VERSION = 1;

    public static final String MESSAGES_TABLE = "received_messages";
    public static final String MSG_ID = "_id";
    public static final String MSG_FROM_USER = "from_id";
    public static final String MSG_TO_USER = "to_id";
    public static final String MSG_CONTENT = "content";
    public static final String MSG_SUBJECT = "subject";
    public static final String MSG_DATE = "date";
    public static final String MSG_READ = "read";

    public static final String SENT_MESSAGES_TABLE = "sent_messages";

    public static final String USERS_TABLE = "users";
    public static final String USR_ID = "_id";
    public static final String USR_ROLE = "role";
    public static final String USR_FNAME = "first_name";
    public static final String USR_LNAME = "last_name";
    public static final String USR_USERNAME = "username";
    public static final String USR_EMAIL = "email";
    public static final String USR_FULL_NAME = "full_name";

    /**
     *
     * @param context
     */
    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /*
    * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
    */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "+MESSAGES_TABLE+" ("
                +MSG_ID+" integer primary key, "
                +MSG_FROM_USER+" text not null, "
                +MSG_TO_USER+" text not null, "
                +MSG_CONTENT+" text, "
                +MSG_SUBJECT+" text, "
                +MSG_READ+" integer default 0, "
                +MSG_DATE+" text not null);");

        db.execSQL("create table "+SENT_MESSAGES_TABLE+" ("
                +MSG_ID+" integer primary key, "
                +MSG_TO_USER+" text not null, "
                +MSG_CONTENT+" text, "
                +MSG_SUBJECT+" text, "
                +MSG_DATE+" text not null);");

        db.execSQL("create table "+USERS_TABLE+" ("
                +USR_ID+" text primary key, "
                +USR_FNAME+" text, "
                +USR_LNAME+" text, "
                +USR_USERNAME+" text, "
                +USR_EMAIL+" text, "
                +USR_ROLE+" integer);");
    }

    /*
    * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
    */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL("drop table if exists "+LOCATIONS_TABLE+";");
//        onCreate(db);
    }
}
