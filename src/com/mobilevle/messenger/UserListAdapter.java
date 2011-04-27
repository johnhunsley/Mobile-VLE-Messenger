package com.mobilevle.messenger;

import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import com.mobilevle.core.moodle.User;

/**
 * <p></p>
 *
 * @author johnhunsley
 *         Date: 27-Jan-2011
 *         Time: 16:56:59
 */
public class UserListAdapter extends ArrayAdapter {
    private final int resource;

    /**
     *
     * @param context
     * @param textViewResourceId
     * @param users
     */
    public UserListAdapter(Context context, final int textViewResourceId, List<User> users) {
        super(context, textViewResourceId, users);
        resource = textViewResourceId;
    }

    /*
    * @param position
    * @param view
    * @param parent
    * @return view
    */
    public View getView(final int position, View view, ViewGroup parent) {
        if(view == null) view = View.inflate(getContext(), resource, null);

        User user = (User)getItem(position);
        TextView userFullName = (TextView)view.findViewById(R.id.user_full_name);
        userFullName.setText(user.getFullName());
        TextView username = (TextView)view.findViewById(R.id.user_username);
        username.setText(user.getUsername());
        return view;
    }
}
