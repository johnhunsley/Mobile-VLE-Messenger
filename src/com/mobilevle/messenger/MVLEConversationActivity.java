package com.mobilevle.messenger;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import com.mobilevle.core.moodle.Message;
import com.mobilevle.core.util.SeparatedListAdapter;
import com.mobilevle.messenger.dao.MessagesDAO;
import com.mobilevle.messenger.dao.MessagesDAOSQLiteImpl;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * <p></p>
 *
 * @author johnhunsley
 *         Date: 24-Jan-2011
 *         Time: 11:50:33
 */
public class MVLEConversationActivity extends AbstractMVLEMoodleMessageReceivingActivity {
    private MessagesDAO messagesDAO;
    private String communicantId;
    private DateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy");
    private ListView listView;
    private ProgressDialog progressDialog;
    /**
     *
     */
    private Handler processHandler = new Handler() {

        @Override
        public void handleMessage(android.os.Message msg) {
            progressDialog.dismiss();

            try {
                Toast toast =
                        MessengerToast.makeMessengerToast(MVLEConversationActivity.this, Toast.LENGTH_SHORT, msg.what);
                toast.setGravity(Gravity.CENTER, 0 ,0);
                toast.show();
                onResume();

            } catch (MVLEMessengerException e) {
                Log.e("MVLEConversationActivity", e.toString());
            }
        }
    };

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        messagesDAO = new MessagesDAOSQLiteImpl(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String[] parcels = getIntent().getStringArrayExtra(MVLEConversationsActivity.COMMUNICANT_ID);
        communicantId = parcels[0];
        messagesDAO.setConversationMessagesRead(communicantId);
        setContentView(R.layout.conversation_layout);
        listView = (ListView)findViewById(R.id.messages_list);
        Button send = (Button)findViewById(R.id.reply);
        final EditText content = (EditText)findViewById(R.id.reply_content);

        send.setOnClickListener(new View.OnClickListener() {

            /**
             *
             * @param view
             */
            public void onClick(View view) {
                progressDialog = new ProgressDialog(MVLEConversationActivity.this);
                progressDialog.setCancelable(true);
                progressDialog.setTitle("Sending Message");
                progressDialog.setMessage("Sending......");
                progressDialog.show();

                Thread thread = new Thread(
                        new Messenger(handler,
                                MVLEConversationActivity.this, processHandler, communicantId,
                                content.getText().toString()));
                thread.start();
            }
        });


        populateListLayout();
    }




    /**
     *
     */
    private void populateListLayout() {
        List<Message> conversation = messagesDAO.getConversationWithUser(communicantId);
        MultiMap messagesByDayDate = new MultiValueMap();

        for(Message message : conversation) messagesByDayDate.put(message.getSendDayDate(), message);

        List<Date> sortedKeys = new ArrayList<Date>(
                new TreeSet<Date>(
                        messagesByDayDate.keySet()));
        Collections.reverse(sortedKeys);
        SeparatedListAdapter adapter = new SeparatedListAdapter(this, R.layout.conversation_primer_list_header);

        for(Date date : sortedKeys) {
            List<Message> messagesList = new ArrayList<Message>(
                    new TreeSet<Message>(
                            (Collection<Message>)messagesByDayDate.get(date)));

            adapter.addSection(dateFormat.format(date),
                    new MessageListAdapter(
                            this, R.layout.conversation_message_list, messagesList));

        }

        //long press options to delete or view
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            /**
             *
             * @param adapterView
             * @param view
             * @param i
             * @param l
             * @return
             */
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                //dialog - are you sure?
                AlertDialog deleteDialog = new AlertDialog.Builder(MVLEConversationActivity.this).create();
                deleteDialog.setTitle("Are you sure you want to delete this message?");
                deleteDialog.setButton("OK", new DialogInterface.OnClickListener() {

                    /**
                     * <p>delete conversation via DAO then refresh view</p>
                     * @param dialog
                     * @param which
                     */
                    public void onClick(DialogInterface dialog, int which) {
                        final Message message = (Message)listView.getAdapter().getItem(i);
                        messagesDAO.deleteMessage(message.getId());
                        MVLEConversationActivity.this.onResume();
                    }
                });

                deleteDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {

                    /**
                     * <p>Do nothing just cancel</p>
                     * @param dialog
                     * @param which
                     */
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });

                deleteDialog.show();
                return true;
            }
        });

        listView.setAdapter(adapter);
    }



}
