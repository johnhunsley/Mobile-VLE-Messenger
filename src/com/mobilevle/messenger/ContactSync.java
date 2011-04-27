package com.mobilevle.messenger;

import android.util.Log;
import com.mobilevle.core.InvalidSessionException;
import com.mobilevle.core.VLEHandler;
import com.mobilevle.core.moodle.Course;
import com.mobilevle.core.moodle.MoodleVLEHandler;
import com.mobilevle.core.moodle.User;
import com.mobilevle.messenger.dao.UsersDAO;

import java.util.List;

/**
 * <p>Wrapper for getting {@link User} contacts from the {@link VLEHandler} for each course and 
 * persisting them with the {@link UsersDAO} if they don't already exist</p>
 *
 * @author johnhunsley
 *         Date: 14-Feb-2011
 *         Time: 20:32:01
 */
public class ContactSync {
    private final MoodleVLEHandler vleHandler;
    private final UsersDAO usersDAO;

    /**
     *
     * @param vleHandler
     * @param usersDAO
     */
    public ContactSync(MoodleVLEHandler vleHandler, UsersDAO usersDAO) {
        this.vleHandler = vleHandler;
        this.usersDAO = usersDAO;
    }

    /**
     *
     * @throws InvalidSessionException
     */
    public synchronized boolean sync() throws InvalidSessionException {
        boolean newContacts = false;
        List<Course> courses = vleHandler.getCourses();

        for(Course course : courses) {
            List<User> syncdUsers = vleHandler.getCourseUsers(course.getId());
            Log.i("ContactSyncService", "Course "+course.getName()+" has "+syncdUsers.size()+" participants");

            for(User user : syncdUsers) {
                User existing = usersDAO.getUser(user.getId());

                if(existing != null) {

                    if(existing.getRole() != user.getRole()) {

                        try {
                            usersDAO.updateUsernameAndRole(user);
                            
                        } catch (MVLEMessengerException e) {
                            e.printStackTrace();
                        }
                    }

                } else {
                    Log.i("ContactSyncService", "User "+user.getFullName()
                            +" is not currently listed, saving to my contacts");
                    usersDAO.saveUser(user);
                    newContacts = true;
                }
            }
        }

        return newContacts;
    }
}
