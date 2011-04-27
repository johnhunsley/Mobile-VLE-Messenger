package com.mobilevle.messenger.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.mobilevle.core.moodle.User;
import com.mobilevle.messenger.MVLEMessengerException;

import java.util.ArrayList;
import java.util.List;

/**
 * <p></p>
 *
 * @author johnhunsley
 *         Date: 07-Jan-2011
 *         Time: 16:51:06
 */
public class UsersDAOSQLiteImpl implements UsersDAO {
    private Context context;
    private DatabaseHelper dbHelper;

    /**
     * 
     * @param context
     */
    public UsersDAOSQLiteImpl(Context context) {
        this.context = context;
        open();
    }

    /**
     *
     * @return this
     */
    public UsersDAOSQLiteImpl open() {

        if(dbHelper == null || !dbHelper.getWritableDatabase().isOpen())
            dbHelper = new DatabaseHelper(this.context);

        return this;
    }

    /**
     *
     * @return
     */
    public List<User> loadUsers() {
        List<User> users = new ArrayList<User>();
        final String sql = "SELECT "+DatabaseHelper.USR_ID+", "+DatabaseHelper.USR_USERNAME+", "+
                DatabaseHelper.USR_FNAME+", "+DatabaseHelper.USR_LNAME+", "+DatabaseHelper.USR_ROLE+
                ", "+DatabaseHelper.USR_EMAIL+" FROM "+DatabaseHelper.USERS_TABLE;
        SQLiteDatabase database = dbHelper.getWritableDatabase();
		Cursor cursor = database.rawQuery(sql, null);

        while(cursor.moveToNext()) users.add(fromCursor(cursor));

        cursor.close();
        database.close();
        return users;
    }

    /**
     *
     * @param user
     */
    public void saveUser(final User user) {
        Log.i("UserDAO", "Saving user with id "+user.getId());
        SQLiteDatabase database = dbHelper.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.USR_ID, user.getId());
        contentValues.put(DatabaseHelper.USR_USERNAME, user.getUsername());
        contentValues.put(DatabaseHelper.USR_FNAME, user.getFirstName());
        contentValues.put(DatabaseHelper.USR_LNAME, user.getLastName());
        contentValues.put(DatabaseHelper.USR_ROLE, user.getRole());
        contentValues.put(DatabaseHelper.USR_EMAIL, user.getEmail());
        database.beginTransaction();
        database.insert(DatabaseHelper.USERS_TABLE, null, contentValues);
		database.setTransactionSuccessful();
		database.endTransaction();
        database.close();
    }

    /**
     *
     * @param id
     * @return  {@link User} with given id
     */
    public User getUser(final String id) {
        Log.i("UsersDAO", "getting user with id "+id);
        User user = null;
        final String sql = "SELECT "+DatabaseHelper.USR_ID+", "+DatabaseHelper.USR_USERNAME+", "+
                DatabaseHelper.USR_FNAME+", "+DatabaseHelper.USR_LNAME+", "+DatabaseHelper.USR_ROLE+
                ", "+DatabaseHelper.USR_EMAIL+" FROM "+DatabaseHelper.USERS_TABLE+" WHERE "+
                DatabaseHelper.USR_ID+" LIKE ?";
        SQLiteDatabase database = dbHelper.getWritableDatabase();
		Cursor cursor = database.rawQuery(sql, new String[] {id});   

        if(cursor.moveToFirst()) {
            user = fromCursor(cursor);
            Log.i("UserDAO", "got User with id "+user.getId());
        }

        cursor.close();
        database.close();
        return user;
    }

    /**
     *
     * @param cursor
     * @return {@link User} constructed from the given {@link Cursor}
     */
    private User fromCursor(final Cursor cursor) {
        User user = new User();
        user.setId(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USR_ID)));
        user.setFirstName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USR_FNAME)));
        user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USR_USERNAME)));
        user.setLastName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USR_LNAME)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USR_EMAIL)));
        user.setRole(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.USR_ROLE)));
        return user;
    }

    /**
     *
     * @param user
     * @return
     */
    public boolean exists(final User user) {
        final String sql = "SELECT "+DatabaseHelper.USR_ID+" FROM "+DatabaseHelper.USERS_TABLE+" WHERE "+
                DatabaseHelper.USR_ID+" LIKE ?";
        SQLiteDatabase database = dbHelper.getWritableDatabase();
		Cursor cursor = database.rawQuery(sql, new String[] {user.getId()});
        boolean b =  cursor.getCount() > 0;
        cursor.close();
        database.close();
        return b;
    }

    /**
     *
     * @param user
     */
    public void deleteUser(User user) {
        final String delete = "DELETE FROM "+DatabaseHelper.USERS_TABLE+" WHERE "+DatabaseHelper.USR_ID+" = ?";
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.beginTransaction();
        database.execSQL(delete, new String[]{user.getId()});
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
    }


    /**
     * 
     * @param criteria
     * @return {@link Cursor}
     */
    public Cursor searchUserFullName(String criteria) {
        if(criteria == null) criteria = "";

        final String sql = "SELECT "+DatabaseHelper.USR_FNAME+" || ' ' || "+DatabaseHelper.USR_LNAME+
                " AS "+DatabaseHelper.USR_FULL_NAME+", "+
                DatabaseHelper.USR_ID+" AS _id FROM "+DatabaseHelper.USERS_TABLE+
                " WHERE "+DatabaseHelper.USR_FNAME+
                " LIKE ? OR "+DatabaseHelper.USR_LNAME+" LIKE ?";

        SQLiteDatabase database = dbHelper.getWritableDatabase();
		Cursor cursor = database.rawQuery(sql, new String[] {criteria.trim()+"%"});
        return cursor;
    }

    /**
     * <p></p>
     * @param user
     * @throws MVLEMessengerException
     */
    public void updateUsernameAndRole(final User user) throws MVLEMessengerException {

        if(exists(user)) {
            final String update = "UPDATE "+DatabaseHelper.USERS_TABLE
                    +" SET "+DatabaseHelper.USR_ROLE+" = ?, "+DatabaseHelper.USR_USERNAME+" = ? WHERE "+DatabaseHelper.USR_ID+" = ?";
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            database.beginTransaction();
            database.execSQL(update, new String[]{Integer.toString(user.getRole()), user.getUsername(), user.getId()});
            database.setTransactionSuccessful();
            database.endTransaction();
            database.close();
        } else throw new MVLEMessengerException("User with id "+user.getId()+" Doesn't exist");
    }
}
