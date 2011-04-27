package com.mobilevle.messenger.dao;

import android.database.Cursor;
import com.mobilevle.core.moodle.User;
import com.mobilevle.messenger.MVLEMessengerException;

import java.util.List;

/**
 * <p></p>
 *
 * @author johnhunsley
 *         Date: 06-Jan-2011
 *         Time: 09:19:41
 */
public interface UsersDAO {

    /**
     *
     * @return List of {@link User}
     */
    List<User> loadUsers();

    /**
     *
     * @param user
     */
    void saveUser(User user);

    /**
     *
     * @param id
     * @return {@link User}
     */
    User getUser(String id);

    /**
     *
     * @param user
     * @return true if a {@link User} withthe given id already exists in the db
     */
    boolean exists(User user);

    /**
     *
     * @param user
     */
    void deleteUser(User user);


    /**
     *
     * @param criteria
     * @return Array of {@link User} full names based ont he search criteria
     */
    Cursor searchUserFullName(String criteria);

    /**
     * <p>Update the User with the same id as the given User and set the role to that of the given User role</p>
     * @param user
     * @throws MVLEMessengerException if the given user doesnt already exist
     */
    void updateUsernameAndRole(User user) throws MVLEMessengerException;
}
