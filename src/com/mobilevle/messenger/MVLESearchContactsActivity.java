package com.mobilevle.messenger;

import android.os.*;
import android.widget.*;
import android.view.View;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.app.ProgressDialog;
import android.app.AlertDialog;
import com.mobilevle.core.InvalidSessionException;
import com.mobilevle.core.moodle.User;
import com.mobilevle.messenger.dao.UsersDAO;
import com.mobilevle.messenger.dao.UsersDAOSQLiteImpl;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * <p></p>
 *
 * @author johnhunsley
 *         Date: 01-Feb-2011
 *         Time: 23:47:03
 */
public class MVLESearchContactsActivity extends AbstractMVLEMoodleActivity  {
    private UsersDAO usersDAO;
    private ListView resultsList;
    private TextView resultsLabel;
    private ProgressDialog progressDialog;
    private List<User> result;


    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.search_contacts_layout);
        usersDAO = new UsersDAOSQLiteImpl(this);
        final Spinner spinner = (Spinner) findViewById(R.id.search_field);
        final EditText value = (EditText)findViewById(R.id.search_value);
        final Button searchButton = (Button)findViewById(R.id.search);
        resultsList = (ListView)findViewById(R.id.results_list);
        resultsLabel = (TextView)findViewById(R.id.results_field_text);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.search_field_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        searchButton.setOnClickListener(new View.OnClickListener() {
            /**
             *
             * @param view
             */
            public void onClick(View view) {
                final String searchValue = value.getText().toString();
                final String searchField = spinner.getSelectedItem().toString();
                String actualField = "lastname";

                if(searchField.equals("Last Name")) actualField = "lastname";

                if(searchField.equals("First Name")) actualField = "firstname";

                if(searchField.equals("Username")) actualField = "username";

                //kick off a new Thread to perform the search by the handler
                progressDialog = new ProgressDialog(MVLESearchContactsActivity.this);
                progressDialog.setCancelable(true);
                progressDialog.setTitle("Searching Users");
                progressDialog.setMessage("Searching......");
                progressDialog.show();
                Thread thread = new Thread(
                                    new SearchRunner(searchValue, actualField));
                thread.start();

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     *
     */
    private Handler processHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
            final ListAdapter adapter =
                    new UserListAdapter(MVLESearchContactsActivity.this, R.layout.users_list, result);
            resultsList.setAdapter(adapter);

            resultsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                /**
                 *
                 * @param adapterView
                 * @param view
                 * @param position
                 * @param l
                 */
                public void onItemClick(AdapterView<?> adapterView, View view, final int position, final long l) {
                    final User user = (User)adapter.getItem(position);
                    final CharSequence[] actions = {"Send Message", "Add to my contacts"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(MVLESearchContactsActivity.this);
                    builder.setTitle("Select an action");

                    builder.setItems(actions, new DialogInterface.OnClickListener() {
                        /**
                         * 
                         * @param dialog
                         * @param item
                         */
                        public void onClick(DialogInterface dialog, final int item) {
                            switch(item) {
                                case 0 : sendNewMessage(user); break;
                                case 1 : addContact(user); break;
                            }
                        }
                    });
                    
                    builder.show();
                }
            });

            resultsLabel.setText(R.string.results_field_text);
        }
    };

    /**
     *
     * @param user
     */
    private void addContact(User user) {

        if(!usersDAO.exists(user)) {
            usersDAO.saveUser(user);
            Toast.makeText(
                    getApplicationContext(),
                    "Added user "+user.getFullName()+" to contacts list", Toast.LENGTH_SHORT).show();
        }

        else Toast.makeText(
                getApplicationContext(),
                "User "+user.getFullName()+" already exists in your contacts list", Toast.LENGTH_SHORT).show();
    }

    /**
     * 
     * @param user
     */
    private void sendNewMessage(User user) {
        Intent intent = new Intent(this, MVLEMessageActivity.class);
        intent.putExtra("contactId", user.getId());
        startActivity(intent);
    }

    /**
     * <p>
     *
     * </p>
     */
    private class SearchRunner implements Runnable {
        final String searchValue;
        final String searchField;

        /**
         *
         * @param searchValue
         * @param searchField
         */
        private SearchRunner(String searchValue, String searchField) {
            this.searchValue = searchValue;
            this.searchField = searchField;
        }

        /**
         *
         */
        public void run() {
            try {
                result = handler.searchUsers(searchField, searchValue);
                processHandler.sendEmptyMessage(0);

            } catch (InvalidSessionException e) {
                e.printStackTrace();
            }
        }
    }

}
