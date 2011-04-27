package com.mobilevle.messenger;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

/**
 * <p></p>
 *
 * @author johnhunsley
 *         Date: 05-Jan-2011
 *         Time: 22:09:02
 */
public class MainMenuTabWidget extends TabActivity {

    /**
     *
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_tab_screen);

        Resources res = getResources(); // Resource object to get Drawables
        TabHost tabHost = getTabHost();  // The activity TabHost
        TabHost.TabSpec spec;  // Resusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab

        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, MVLEContactsActivity.class);

        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost.newTabSpec("contacts").setIndicator("Contacts",
                res.getDrawable(R.drawable.ic_contacts_state))
                .setContent(intent);
        tabHost.addTab(spec);

        // Do the same for the other tabs
        intent = new Intent().setClass(this, MVLEConversationsActivity.class);
        spec = tabHost.newTabSpec("conversations").setIndicator("Conversations",
                res.getDrawable(R.drawable.ic_conversation_state))
                .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, MVLEMessageActivity.class);
        spec = tabHost.newTabSpec("new message").setIndicator("New Message",
                res.getDrawable(R.drawable.ic_menu_message))
                .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, MVLEMessengerPreferences.class);
        spec = tabHost.newTabSpec("settings").setIndicator("Settings",
                res.getDrawable(R.drawable.ic_settings_state))
                .setContent(intent);
        tabHost.addTab(spec);

        tabHost.setCurrentTab(2);
        //boot up the services
        MessengerStartUpReceiver.boot(this);
    }
}
