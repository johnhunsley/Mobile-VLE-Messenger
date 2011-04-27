package com.mobilevle.messenger;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import com.mobilevle.oktech.session.Session;
import com.mobilevle.oktech.session.SessionDAO;
import com.mobilevle.oktech.session.SessionDAOSQLiteImpl;

/**
 * <p>
 * todo save the credentials to the sessionDAO and restart the services on period change
 * </p>
 *
 * @author johnhunsley
 *         Date: 06-Jan-2011
 *         Time: 11:37:10
 */
public class MVLEMessengerPreferences extends PreferenceActivity {
    /**
     * 
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SessionDAO sessionDAO = new SessionDAOSQLiteImpl(this);
        addPreferencesFromResource(R.xml.messenger_preferences);
        EditTextPreference usernamePref = (EditTextPreference)findPreference(getText(R.string.moodle_user_key));
        EditTextPreference passwordPref = (EditTextPreference)findPreference(getText(R.string.moodle_pwd_key));
        ListPreference messageSyncFeq = (ListPreference)findPreference("message_interval_key");
        ListPreference contactSyncFeq = (ListPreference)findPreference("contact_interval_key");
        final Session session = sessionDAO.loadSession();
        usernamePref.setText(session.getUsername());
        passwordPref.setText(session.getPassword());

        usernamePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            /**
             *
             * @param preference
             * @param newValue
             * @return
             */
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                session.setUsername((String)newValue);
                sessionDAO.saveSession(session, true);
                return true;
            }
        });

        passwordPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            /**
             *
             * @param preference
             * @param newValue
             * @return
             */
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                session.setPassword((String)newValue);
                sessionDAO.saveSession(session, true);
                return true;
            }
        });

        messageSyncFeq.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            /**
             *
             * @param preference
             * @param newValue
             * @return
             */
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                MessengerStartUpReceiver.boot(MVLEMessengerPreferences.this);
                return true;
            }
        });

        contactSyncFeq.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            /**
             *
             * @param preference
             * @param newValue
             * @return
             */
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                MessengerStartUpReceiver.boot(MVLEMessengerPreferences.this);
                return true;
            }
        });

        Log.i("MVLEMEssengerPreferences",
                "Setting pref username - "+session.getUsername()+" password - "+session.getPassword());
    }
}
