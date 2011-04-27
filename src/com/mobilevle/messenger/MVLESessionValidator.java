package com.mobilevle.messenger;

import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import com.mobilevle.oktech.session.SessionDAO;
import com.mobilevle.oktech.session.SessionDAOSQLiteImpl;
import com.mobilevle.oktech.session.Session;

/**
 * <p></p>
 *
 * @author johnhunsley
 *         Date: 26-Jan-2011
 *         Time: 20:49:46
 */
public class MVLESessionValidator extends AbstractMVLEMoodleActivity {

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SessionDAO sessionDAO = new SessionDAOSQLiteImpl(this);
        Session session = sessionDAO.loadSession();
        Intent intent;

        if(session != null) {
            Log.i("MVLESEssionValidator", "################### session is valid");
            intent = new Intent(this, MainMenuTabWidget.class);
        }
        else intent = new Intent(this, MVLELogin.class);

        startActivity(intent);
    }
}
