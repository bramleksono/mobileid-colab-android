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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.LaunchReg:
                Intent i = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(i);
                break;
            case R.id.UserClearBtn:
                RegisterGCM();
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

    public void RegisterGCM(){
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(PROJECT_NUMBER);
                    msg = "Device registered, registration ID=" + regid;
                    Log.i("GCM", msg);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();

                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                GCMTitle.setVisibility(View.VISIBLE);
                GCMTv.setVisibility(View.VISIBLE);
                GCMTv.setText(regid);
            }
        }.execute(null, null, null);
    }
}
