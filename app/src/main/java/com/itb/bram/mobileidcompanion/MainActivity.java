package com.itb.bram.mobileidcompanion;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

public class MainActivity extends Activity implements OnClickListener {

    final String TAG = "MobileID Companion";
    Context context;
    String userid;

    //sharedpreference string
    private static final String PROPERTY_GCMID = "GCMID";
    private static final String PROPERTY_IDNUMBER = "UserIdNumber";

    //GCM Property
    GoogleCloudMessaging gcm;
    String regid;
    final String PROJECT_NUMBER = "139518708260";

    TextView GCMTitle, GCMTv;
    Button LaunchReg, UserClearBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        LaunchReg = (Button) findViewById(R.id.LaunchReg);
        LaunchReg.setOnClickListener(this);
        UserClearBtn = (Button) findViewById(R.id.UserClearBtn);
        UserClearBtn.setOnClickListener(this);

        GCMTitle = (TextView) findViewById(R.id.GCMTitle);
        GCMTv = (TextView) findViewById(R.id.GCMTv);

        //check gcm
        regid = getGCM(context);
        if (regid != null) {
            GCMTv.setText(regid);
        } else {
            RegisterGCM();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.LaunchReg:
                Intent i = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(i);
                break;
            case R.id.UserClearBtn:
                deleteGCM(context);
                break;
        }
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getAppPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences,
        // but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    private void deleteGCM(Context context) {
        final SharedPreferences prefs = getAppPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(PROPERTY_GCMID);
        editor.commit();
        Log.i(TAG, "GCM ID deleted");
    }

    private void deleteIDNumber(Context context) {
        final SharedPreferences prefs = getAppPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(PROPERTY_IDNUMBER);
        editor.commit();
        Log.i(TAG, "ID number deleted");
    }

    private void storeGCM(Context context, String gcmid) {
        final SharedPreferences prefs = getAppPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_GCMID, gcmid);
        editor.commit();
        Log.i(TAG, "Storing GCM ID");
    }

    private void storeIDNumber(Context context, String idnumber) {
        final SharedPreferences prefs = getAppPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_IDNUMBER, idnumber);
        editor.commit();
        Log.i(TAG, "Storing ID Number");
    }

    private String getGCM(Context context) {
        final SharedPreferences prefs = getAppPreferences(context);
        String gcmid = prefs.getString(PROPERTY_GCMID, "");
        if (gcmid.isEmpty()) {
            Log.i(TAG, "There's no GCM ID in preferences");
            return null;
        }
        return gcmid;
    }

    private String getIDNumber(Context context) {
        final SharedPreferences prefs = getAppPreferences(context);
        String Content = prefs.getString(PROPERTY_IDNUMBER, "");
        if (Content.isEmpty()) {
            Log.i(TAG, "There's no ID Number in preferences");
            return null;
        }
        return Content;
    }

    public void RegisterGCM(){
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg;
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    msg = gcm.register(PROJECT_NUMBER);
                } catch (IOException ex) {
                    msg = null;

                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                CharSequence text;
                int duration = Toast.LENGTH_SHORT;
                if (msg != null) {
                    text = "GCM registration success";
                    storeGCM(context, msg);
                    GCMTv.setText(msg);
                } else {
                    text = "Cannot register GCM";
                    GCMTv.setText("Cannot register GCM");
                }
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        }.execute(null, null, null);
    }
}
