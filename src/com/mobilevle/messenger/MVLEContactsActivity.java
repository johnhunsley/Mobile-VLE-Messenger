package com.mobilevle.messenger;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.mobilevle.core.InvalidSessionException;
import com.mobilevle.core.moodle.User;
import com.mobilevle.core.util.SeparatedListAdapter;
import com.mobilevle.messenger.dao.UsersDAO;
import com.mobilevle.messenger.dao.UsersDAOSQLiteImpl;
import com.mobilevle.oktech.session.SessionDAO;
import com.mobilevle.oktech.session.SessionDAOSQLiteImpl;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * <p></p>
 *
 * @author johnhunsley
 *         Date: 05-Jan-2011
 *         Time: 16:27:08
 */
public class MVLEContactsActivity extends AbstractMVLEMoodleActivity {
    private UsersDAO usersDAO;
    private ProgressDialog progressDialog;

    /**
     *
     * @param bundle
     */
    @Override
    protected void onCreate(Bundle bundle) {
        usersDAO = new UsersDAOSQLiteImpl(this);
        super.onCreate(bundle);
    }

    @Override
    protected void onResume() {
        //register a receiver first to if the sync service updates whilst this view is open
        registerReceiver(newMessageReceiver, new IntentFilter("new.contacts.broadcast"));
        Collection<User> users = usersDAO.loadUsers();

        List<User> students =
                new ArrayList<User>(CollectionUtils.select(
                        users, new UserRolePredicate(new int[] {User.STUDENT_ROLE})));
        Collections.sort(students);

        List<User> teachers =
                new ArrayList<User>(CollectionUtils.select(
                        users, new UserRolePredicate(new int[] {User.TEACHER_ROLE, User.NONE_EDIT_TEACHER_ROLE})));
        Collections.sort(teachers);

        List<User> unknown =
                new ArrayList<User>(CollectionUtils.select(
                        users, new UserRolePredicate(new int[] {User.UNKNOWN_ROLE})));
        Collections.sort(unknown);

        final SeparatedListAdapter adapter = new SeparatedListAdapter(this, R.layout.conversation_primer_list_header);

        if(!teachers.isEmpty())
            adapter.addSection("Teachers", new UserListAdapter(this, R.layout.users_list, teachers));

        if(!students.isEmpty())
            adapter.addSection("Students", new UserListAdapter(this, R.layout.users_list, students));

        if(!unknown.isEmpty())
            adapter.addSection("Unidentified Role", new UserListAdapter(this, R.layout.users_list, unknown));

        ListView listView = new ListView(this);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            /**
             *
             * @param adapterView
             * @param view
             * @param position
             * @param l
             */
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, final long l) {
                final User user = (User)adapter.getItem(position);
                final CharSequence[] actions = {"Send "+user.getFirstName()+" a Message",
                        "Remove "+user.getFirstName()+" from my contacts"};

                AlertDialog.Builder builder = new AlertDialog.Builder(MVLEContactsActivity.this);
                builder.setTitle("Select action");

                builder.setItems(actions, new DialogInterface.OnClickListener() {
                    /**
                     *
                     * @param dialog
                     * @param item
                     */
                    public void onClick(DialogInterface dialog, final int item) {
                        switch(item) {
                            case 0 : sendNewMessage(user); break;
                            case 1 : removeContact(user); break;
                        }
                    }
                });

                builder.show();
            }
        });

        setContentView(listView);

        //if no contacts then prompt user to sync now
        if(adapter.getCount() < 1) {
            Toast toast = Toast.makeText(this, R.string.no_contacts, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0 ,0);
            toast.show();
        }

        super.onResume();
    }

    /**
     *
     * @param user
     */
    private void removeContact(User user) {

        if(usersDAO.exists(user)) {
            usersDAO.deleteUser(user);
            Toast.makeText(
                    getApplicationContext(),
                    "Removed user "+user.getFullName()+" from contacts", Toast.LENGTH_SHORT).show();
            onResume();
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem search = menu.add(0, 0, 0, "Search Contacts");
        search.setIcon(R.drawable.ic_menu_search);
        MenuItem sync = menu.add(0, 1, 1, "Update now");
        sync.setIcon(R.drawable.ic_menu_refresh);


        return super.onCreateOptionsMenu(menu);
    }

    /* Handles the menu item selected
      * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
      */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case 0 : startActivity(new Intent(this, MVLESearchContactsActivity.class)); break;
            case 1 : syncContacts(); break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     *
     */
    private void syncContacts() {
        final ContactSync contactSync = new ContactSync(super.handler, usersDAO);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(true);
        progressDialog.setTitle("Synchronizing contacts");
        progressDialog.setMessage("Synchronizing......");
        progressDialog.show();

        new Thread() {

            @Override
            public void run() {
                Looper.prepare();

                try {
                    contactSync.sync();

                } catch (InvalidSessionException e) {
                    refreshSession();

                    try {
                        contactSync.sync();

                    } catch (InvalidSessionException e1) {
                        Toast toast =
                                Toast.makeText(MVLEContactsActivity.this, R.string.invalid_session_message, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0 ,0);
                        toast.show();
                    }
                }

                processHandler.sendEmptyMessage(0);
            }

        }.start();
    }

    /**
     * <p>{@link Handler} implementation which refreshes the view after dismissing the dialog</p>
     */
    private Handler processHandler = new Handler() {

        @Override
        public void handleMessage(android.os.Message msg) {
            progressDialog.dismiss();
            onResume();
        }
    };

    /**
     * <p>{@link BroadcastReceiver} implementation which simply refreshes the view</p>
     */
    protected final BroadcastReceiver newMessageReceiver =  new BroadcastReceiver() {
                /**
                 *
                 * @param context
                 * @param intent
                 */
                public void onReceive(Context context, Intent intent) {
                    MVLEContactsActivity.this.onResume();
                }
            };

    /**
     *
     */
    protected final void refreshSession() {
        //revalidate session
        SessionDAO sessionDAO = new SessionDAOSQLiteImpl(this);
        AsyncSessionRefreshTask refreshTask = new  AsyncSessionRefreshTask(super.handler, this);
        refreshTask.execute(sessionDAO.loadSession());
    }
}
