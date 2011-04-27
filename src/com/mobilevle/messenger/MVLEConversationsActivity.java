package com.mobilevle.messenger;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.mobilevle.core.util.SeparatedListAdapter;
import com.mobilevle.messenger.dao.MessagesDAO;
import com.mobilevle.messenger.dao.MessagesDAOSQLiteImpl;
import com.mobilevle.oktech.session.Session;
import com.mobilevle.oktech.session.SessionDAO;
import com.mobilevle.oktech.session.SessionDAOSQLiteImpl;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

/**
 * <p></p>
 *
 * @author johnhunsley
 *         Date: 05-Jan-2011
 *         Time: 16:29:31
 */
public class MVLEConversationsActivity extends AbstractMVLEMoodleMessageReceivingActivity {
    public static final String COMMUNICANT_ID = "communicant_id";
    private MessagesDAO messagesDAO;
    private SessionDAO sessionDAO;
    private static final String TODAY_HEADER = "Today";
    private static final String YESTERDAY_HEADER = "Yesterday";
    private static final String THIS_WEEK_HEADER = "This Week";
    private static final String LAST_WEEK_HEADER = "Last Week";
    private static final String THIS_MONTH_HEADER = "This Month";
    private static final String PREVIOUS_HEADER = "Before this month.....";

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        messagesDAO = new MessagesDAOSQLiteImpl(this);
        sessionDAO = new SessionDAOSQLiteImpl(this);

    }

    @Override
    protected void onResume() {

        try {
            populateListLayout();
            //now remove message icon from the notification bar
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.cancel(R.string.new_message);

        } catch (MVLEMessengerException e) {
            Log.e("MVLEConversationsActivity", e.toString());
        }
        super.onResume();
    }

    private void populateListLayout() throws MVLEMessengerException {
        Session session = sessionDAO.loadSession();
        List<ConversationPrimer> primers = messagesDAO.getConversationPrimers(session.getUserId());
        //filter conversation primers by date
        Collection<ConversationPrimer> todaysPrimers =
                CollectionUtils.select(primers, new ConversationPrimerEpochPredicate(
                        ConversationPrimerEpochPredicate.TODAY));
        primers.removeAll(todaysPrimers);

        Collection<ConversationPrimer> yesterdaysPrimers =
                CollectionUtils.select(primers, new ConversationPrimerEpochPredicate(
                        ConversationPrimerEpochPredicate.YESTERDAY));
        primers.removeAll(yesterdaysPrimers);

        Collection<ConversationPrimer> thisWeeksPrimers =
                CollectionUtils.select(primers, new ConversationPrimerEpochPredicate(
                        ConversationPrimerEpochPredicate.THIS_WEEK));
        primers.removeAll(thisWeeksPrimers);

        Collection<ConversationPrimer> lastWeeksPrimers =
                CollectionUtils.select(primers, new ConversationPrimerEpochPredicate(
                        ConversationPrimerEpochPredicate.LAST_WEEK));
        primers.removeAll(lastWeeksPrimers);

        Collection<ConversationPrimer> thisMonthsPrimers =
                CollectionUtils.select(primers, new ConversationPrimerEpochPredicate(
                        ConversationPrimerEpochPredicate.THIS_MONTH));
        primers.removeAll(thisMonthsPrimers);

        Collection<ConversationPrimer> oldestPrimers =
                CollectionUtils.select(primers, new ConversationPrimerEpochPredicate(
                        ConversationPrimerEpochPredicate.DEFAULT));
        primers.removeAll(oldestPrimers);

        final SeparatedListAdapter adapter = new SeparatedListAdapter(this, R.layout.conversation_primer_list_header);

        if(!todaysPrimers.isEmpty()) {
            adapter.addSection(TODAY_HEADER,
                    new ConversationPrimerListAdapter(
                            this, R.layout.conversation_primer_list,
                            new ArrayList<ConversationPrimer>(
                                    new TreeSet<ConversationPrimer>(todaysPrimers))));
        }

        if(!yesterdaysPrimers.isEmpty()) {
            adapter.addSection(YESTERDAY_HEADER,
                    new ConversationPrimerListAdapter(
                            this, R.layout.conversation_primer_list,
                            new ArrayList<ConversationPrimer>(
                                    new TreeSet<ConversationPrimer>(yesterdaysPrimers))));
        }

        if(!thisWeeksPrimers.isEmpty()) {
            adapter.addSection(THIS_WEEK_HEADER,
                    new ConversationPrimerListAdapter(
                            this, R.layout.conversation_primer_list,
                            new ArrayList<ConversationPrimer>(
                                    new TreeSet<ConversationPrimer>(thisWeeksPrimers))));
        }

        if(!lastWeeksPrimers.isEmpty()) {
            adapter.addSection(LAST_WEEK_HEADER,
                    new ConversationPrimerListAdapter(
                            this, R.layout.conversation_primer_list,
                            new ArrayList<ConversationPrimer>(
                                    new TreeSet<ConversationPrimer>(lastWeeksPrimers))));
        }

        if(!thisMonthsPrimers.isEmpty()) {
            adapter.addSection(THIS_MONTH_HEADER,
                    new ConversationPrimerListAdapter(
                            this, R.layout.conversation_primer_list,
                            new ArrayList<ConversationPrimer>(
                                    new TreeSet<ConversationPrimer>(thisMonthsPrimers))));
        }

        if(!oldestPrimers.isEmpty()) {
            adapter.addSection(PREVIOUS_HEADER,
                    new ConversationPrimerListAdapter(
                            this, R.layout.conversation_primer_list,
                            new ArrayList<ConversationPrimer>(
                                    new TreeSet<ConversationPrimer>(oldestPrimers))));
        }

        ListView listView = new ListView(this);
        listView.setAdapter(adapter);

        //short press views the conversation in full
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            /**
             * <p>
             *  Start a new MVLEConversationActivity for the selected {@link ConversationPrimer}
             *
             * </p>
             * @param adapterView
             * @param view
             * @param i
             * @param l
             */
            public void onItemClick(AdapterView<?> adapterView, final View view, final int i, final long l) {
                showFullconversation((ConversationPrimer)adapter.getItem(i));
            }
        });

        //long press options to delete or view
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            /**
             *
             * @param adapterView
             * @param view
             * @param i
             * @param l
             * @return true
             */
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                final CharSequence[] actions = {"View Conversation", "Delete Conversation"};

                AlertDialog.Builder builder = new AlertDialog.Builder(MVLEConversationsActivity.this);

                builder.setItems(actions, new DialogInterface.OnClickListener() {
                    /**
                     *
                     * @param dialog
                     * @param item
                     */
                    public void onClick(DialogInterface dialog, final int item) {
                        ConversationPrimer primer = (ConversationPrimer)adapter.getItem(i);

                        switch(item) {
                            case 0 : showFullconversation(primer); break;
                            case 1 : deleteConversation(primer); break;
                        }
                    }
                });

                builder.setTitle("Select action");
                builder.show();
                return true;
            }
        });

        //alert if no messages
        if(adapter.getCount() < 1) {
            Toast toast = Toast.makeText(this, R.string.no_messages, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0 ,0);
            toast.show();
        }

        setContentView(listView);
    }

    /**
     *
     * @param primer
     */
    private void deleteConversation(final ConversationPrimer primer) {
        //dialog - are you sure?
        AlertDialog deleteDialog = new AlertDialog.Builder(this).create();
        deleteDialog.setTitle("Are you sure you want to delete this conversation?");
        deleteDialog.setButton("OK", new DialogInterface.OnClickListener() {

            /**
             * <p>delete conversation via DAO then refresh view</p>
             * @param dialog
             * @param which
             */
            public void onClick(DialogInterface dialog, int which) {
                messagesDAO.deleteConversation(primer.getCommunicant().getId());
                MVLEConversationsActivity.this.onResume();
            }
        });

        deleteDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {

            /**
             *
             * @param dialog
             * @param which
             */
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });

        deleteDialog.show();
    }

    /**
     *
     * @param selectedPrimer
     */
    private void showFullconversation(final ConversationPrimer selectedPrimer) {
        final String communicantId = selectedPrimer.getCommunicant().getId();
        Intent intent = new Intent(MVLEConversationsActivity.this, MVLEConversationActivity.class);
        intent.putExtra(COMMUNICANT_ID, new String[]{communicantId});
        MVLEConversationsActivity.this.startActivity(intent);
    }
}
