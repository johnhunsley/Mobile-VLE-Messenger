package com.mobilevle.messenger;

import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.graphics.Color;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import com.mobilevle.core.moodle.Message;
import com.mobilevle.core.moodle.User;

/**
 * <p></p>
 *
 * @author johnhunsley
 *         Date: 24-Jan-2011
 *         Time: 16:29:32
 */
public class MessageListAdapter extends ArrayAdapter {
    private final int resource;
    private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy hh:mma");

    /**
     *
     * @param context
     * @param textViewResourceId
     * @param messages
     */
    public MessageListAdapter(Context context,
                                         final int textViewResourceId,
                                         List<Message> messages) {
        super(context, textViewResourceId, messages);
        resource = textViewResourceId;
    }

    /**
     *
     * @param position
     * @param view
     * @param parent
     * @return view
     */
    public View getView(final int position, View view, ViewGroup parent) {
        if(view == null) view = View.inflate(getContext(), resource, null);

        Message message = (Message)getItem(position);
        int textColour = Color.BLACK;

        if(message.getType() == Message.OUTBOUND) {
            view.setBackgroundColor(Color.LTGRAY);

        } else {
            view.setBackgroundColor(Color.WHITE);
        }

        TextView name = (TextView)view.findViewById(R.id.messages_communicant_name);
        name.setTextColor(textColour);
        User source = message.getFromUser();

        if(source == null) name.setText("Me:");

        else name.setText(message.getFromUser().getFullName()+":");

//        TextView subject = (TextView)view.findViewById(R.id.messages_subject_content);
//        subject.setText(message.getSubject());
        TextView content = (TextView)view.findViewById(R.id.messages_message_content);
        content.setTextColor(textColour);
        content.setText(message.getContent());
        TextView date = (TextView)view.findViewById(R.id.messages_message_date);
        date.setTextColor(Color.GRAY);
        date.setText(dateFormat.format(message.getSendDate()));
        return view;
    }
}
