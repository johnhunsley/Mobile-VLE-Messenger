package com.mobilevle.messenger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import com.mobilevle.core.moodle.User;
import com.mobilevle.messenger.dao.DatabaseHelper;
import com.mobilevle.messenger.dao.UsersDAO;
import com.mobilevle.messenger.dao.UsersDAOSQLiteImpl;

/**
 * <p></p>
 *
 * @author johnhunsley
 *         Date: 05-Jan-2011
 *         Time: 16:30:49
 */
public class MVLEMessageActivity extends AbstractMVLEMoodleActivity {
    private UsersDAO usersDAO;
    private String selectedUserId = null;
    private String selectedUserFullName = null;
    private ProgressDialog progressDialog;
    private AutoCompleteTextView recipients;

    @Override
    protected void onCreate(Bundle bundle) {
        usersDAO = new UsersDAOSQLiteImpl(this);
        super.onCreate(bundle);
    }

    @Override
    protected void onResume() {
        selectedUserId = null;
        setContentView(R.layout.compose_message_layout);
        recipients = (AutoCompleteTextView)findViewById(R.id.auto_complete_recipients);
        UserSearchCursorAdapter adapter = new UserSearchCursorAdapter();
        recipients.setAdapter(adapter);
        recipients.setOnItemClickListener(adapter);

        final EditText content = (EditText)findViewById(R.id.message_content);
        final Button send = (Button)findViewById(R.id.message_send);

        send.setOnClickListener(new View.OnClickListener() {

            /**
             *
             * @param view
             */
            public void onClick(View view) {
                if(selectedUserId == null) showInvalidRecpientError();

                if(selectedUserFullName == null ||
                        !selectedUserFullName.equals(recipients.getText().toString().trim()))
                    showInvalidRecpientError();                    

                else {
                    progressDialog = new ProgressDialog(MVLEMessageActivity.this);
                    progressDialog.setCancelable(true);
                    progressDialog.setTitle("Sending Message");
                    progressDialog.setMessage("Sending......");
                    progressDialog.show();
                    Thread thread = new Thread(
                            new Messenger(handler,
                                    MVLEMessageActivity.this, processHandler, selectedUserId,
                                        content.getText().toString()));
                    thread.start();
                }
            }

            /**
             *
             */
            private void showInvalidRecpientError() {
                Toast toast = Toast.makeText(MVLEMessageActivity.this,
                        "Please select a valid recipient before sending", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0 ,0);
                toast.show();
            }
        });

        //set the recipient if the intent came from the contacts list
        String contactIdStr = getIntent().getStringExtra("contactId");

        if(contactIdStr != null && contactIdStr.length() > 0) {
            User recipient = usersDAO.getUser(contactIdStr);

            if(recipient != null) {
                selectedUserId = contactIdStr;
                selectedUserFullName = recipient.getFullName();
                recipients.setText(recipient.getFullName());
            }
        }

        super.onResume();
    }

    /**
     *
     */
    private Handler processHandler = new Handler() {

        @Override
        public void handleMessage(android.os.Message msg) {
            progressDialog.dismiss();

            try {
                Toast toast =
                        MessengerToast.makeMessengerToast(MVLEMessageActivity.this, Toast.LENGTH_SHORT, msg.what);
                toast.setGravity(Gravity.CENTER, 0 ,0);
                toast.show();
                Intent intent = new Intent(MVLEMessageActivity.this, MVLEConversationActivity.class);
                intent.putExtra(MVLEConversationsActivity.COMMUNICANT_ID, new String[]{selectedUserId});
                MVLEMessageActivity.this.startActivity(intent);

            } catch (MVLEMessengerException e) {
                Log.e("MVLEConversationActivity", e.toString());
            }

        }
    };

    /**
     *
     */
    class UserSearchCursorAdapter extends SimpleCursorAdapter implements AdapterView.OnItemClickListener {

        /**
         *
         */
        UserSearchCursorAdapter() {
            super(MVLEMessageActivity.this,
                    android.R.layout.simple_dropdown_item_1line,
                    null, new String[] {DatabaseHelper.USR_FULL_NAME}, new int[] {android.R.id.text1});
        }

        @Override
        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
            if (getFilterQueryProvider() != null) {
                return getFilterQueryProvider().runQuery(constraint);
            }

            Cursor cursor = usersDAO.searchUserFullName(
                    (constraint != null ? constraint.toString() : null));

            return cursor;
        }

        @Override
        public String convertToString(Cursor cursor) {
            final int columnIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.USR_FULL_NAME);
            final String str = cursor.getString(columnIndex);
            return str;
        }

        /**
         *
         * @param adapterView
         * @param view
         * @param position
         * @param l
         */
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
            selectedUserId = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
            selectedUserFullName = recipients.getText().toString().trim();
            cursor.close();
        }
    }
}
