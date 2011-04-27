package com.mobilevle.messenger;

import org.apache.commons.collections.Predicate;
import com.mobilevle.core.moodle.User;

/**
 * <p>
 * Evaluates a {@link User} based on role. If the given {@link User} evaluee's role is equal to
 * any of the given array of role values the evaluee will return true
 * </p>
 * @see User
 * @author johnhunsley
 *         Date: 01-Feb-2011
 *         Time: 22:07:51
 */
public class UserRolePredicate implements Predicate {
    final int[] roles;

    /**
     *
     * @param roles
     */
    public UserRolePredicate(final int[] roles) {
        this.roles = roles;
    }

    /**
     * <p>Evaluate on role</p>
     * @param o
     * @return
     */
    public boolean evaluate(Object o) {

        if(o instanceof User) {
            User user = (User)o;

            for(int role : roles) {

                if(role == user.getRole()) return true;
            }
        }

        return false;
    }
}
