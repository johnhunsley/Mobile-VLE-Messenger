package com.mobilevle.messenger;

import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import android.graphics.drawable.Drawable;
import com.mobilevle.core.moodle.Activity;
import com.mobilevle.core.moodle.Message;
import com.mobilevle.core.MobileVLECoreException;

import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.commons.collections.ListUtils;

/**
 * <p></p>
 *
 * @author johnhunsley
 *         Date: 18-Jan-2011
 *         Time: 16:54:36
 */
public class ConversationPrimerListAdapter extends ArrayAdapter {
    private final int resource;
    private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy hh:mma");

    /**
     *
     * @param context
     * @param textViewResourceId
     * @param primers
     */
    public ConversationPrimerListAdapter(Context context,
                                         final int textViewResourceId,
                                         List<ConversationPrimer> primers) {        
        super(context, textViewResourceId, primers);
        resource = textViewResourceId;
    }

    /**
     *
     * @param position
     * @param view
     * @param parent
     * @return
     */
    public View getView(final int position, View view, ViewGroup parent) {
        if(view == null) view = View.inflate(getContext(), resource, null);

        ConversationPrimer primer = (ConversationPrimer)getItem(position);
        TextView name = (TextView)view.findViewById(R.id.communicant_name);
        name.setText(primer.getCommunicant().getFullName()+" ("+primer.getMessageCount()+")");

        if(!primer.getLatestMessage().isRead()) {
            Drawable img = getContext().getResources().getDrawable(R.drawable.new_message);
            img.setBounds(0,0,30,32);
            name.setCompoundDrawables(null, null, img, null);
        }

        TextView subject = (TextView)view.findViewById(R.id.subject_summary);
        TextView content = (TextView)view.findViewById(R.id.message_summary);
        TextView date = (TextView)view.findViewById(R.id.message_date);
        date.setText(dateFormat.format(primer.getLatestMessage().getSendDate()));

        try {
            subject.setText(primer.getLatestMessage().getMessageElementSummary(Message.SUBJECT));
            content.setText(primer.getLatestMessage().getMessageElementSummary(Message.CONTENT));

        } catch (MobileVLECoreException e) {
            Log.e("ConversationPrimerListAdapter", e.toString());
        }

        return view;
    }
}
