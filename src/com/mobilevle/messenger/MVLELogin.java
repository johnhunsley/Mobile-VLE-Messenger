package com.mobilevle.messenger;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.mobilevle.core.InvalidSessionException;
import com.mobilevle.core.VLEHandler;

/**
 * <p></p>
 *
 * @author johnhunsley
 *         Date: 08-Nov-2010
 *         Time: 14:26:22
 */
public class MVLELogin extends AbstractMVLEMoodleActivity {
    public static final String ORIGINATING_ACTIVITY_EXTRA = "OA";
    private final static int LOGIN_SUCCESS = 1;
    private final static int LOGIN_FAIL = 2;
    private String originatingActivity;
    private ProgressDialog progressDialog;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.login);
        originatingActivity = getIntent().getStringExtra(ORIGINATING_ACTIVITY_EXTRA);
        final Button loginButton = (Button)findViewById(R.id.buttonSignIn);
        final EditText username = (EditText)findViewById(R.id.txtUserName);
        final EditText password = (EditText)findViewById(R.id.txtPassword);

        loginButton.setOnClickListener(new View.OnClickListener() {

            /**
             *
             */
            public final void onClick(View v) {
                Log.i("MVLEActivity", "login()....");

                String uname = username.getText().toString();
                String pass = password.getText().toString();

                if(uname == null || uname.length() < 1) showInvalidNameAlertDialog();

                else {
                    progressDialog = new ProgressDialog(MVLELogin.this);
                    progressDialog.setCancelable(true);
                    progressDialog.setTitle("Login");
                    progressDialog.setMessage("Authenticating......");
                    progressDialog.show();

                    new Thread(new LoginRunner(uname, pass)).start();
                }
            }
        });

        loginButton.requestFocus();
        super.onCreate(savedInstanceState);
    }


    /**
     *
     * @return {@link Intent}
     */
    public Intent getOriginatingActivityIntent() {
        try {
            return new Intent(this, Class.forName(originatingActivity));

        } catch (ClassNotFoundException e) {
            startActivity(
                    new Intent(this, MainMenuTabWidget.class));
        }

        return null;
    }


    /**
     *
     */
    private void showInvalidNameAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please enter a valid moodle username");
        builder.show();
    }

    /**
     *
     */
    private void showInvalidLoginAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MVLELogin.this);
        builder.setMessage("invalid login");
        builder.show();

    }

    /**
     * <p>{@link android.os.Handler} implementation which starts the original activity after dismissing the dialog</p>
     */
    private Handler processHandler = new Handler() {

        @Override
        public void handleMessage(android.os.Message msg) {
            progressDialog.dismiss();

            switch(msg.what) {

                case LOGIN_SUCCESS :

                    if(originatingActivity != null) {
                        Log.i("MVLELogin", "forwarding post login intent to - "+originatingActivity);
                        startActivity(getOriginatingActivityIntent());

                    } else startActivity(
                            new Intent(MVLELogin.this, MainMenuTabWidget.class));

                    break;

                case LOGIN_FAIL : showInvalidLoginAlertDialog(); break;
            }
        }
    };

    /**
     * <p>
     * Calls the {@link VLEHandler} authenticate method as a independant 
     * Runnable object
     * </p>
     */
    private class LoginRunner implements Runnable {
        final String uname, pass;

        /**
         *
         * @param uname
         * @param pass
         */
        public LoginRunner(final String uname, final String pass) {
            this.uname = uname;
            this.pass = pass;
        }

        /**
         * <p>
         *  Make the call to authenticate and send a corresponding message to the {@link Handler} 
         * </p>
         */
        public void run() {
            Looper.prepare();

            try {

                if(handler.authenticate(MVLELogin.this, uname, pass, true))
                    processHandler.sendEmptyMessage(LOGIN_SUCCESS);

                else processHandler.sendEmptyMessage(LOGIN_FAIL);

            } catch (InvalidSessionException e) {
                processHandler.sendEmptyMessage(LOGIN_FAIL);
            }
        }
    }
}