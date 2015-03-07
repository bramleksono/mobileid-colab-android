package com.itb.bram.mobileidcompanion;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends Activity implements OnClickListener {

    final String TAG = "MobileID Companion";
    Context context;
    String userid,userinfo,userhash;
    JSONObject gcmObj, form;

    //sharedpreference string
    private static final String PROPERTY_GCMID = "GCMID";
    private static final String PROPERTY_IDNUMBER = "UserIdNumber";

    //GCM Property
    GoogleCloudMessaging gcm;
    String regid;
    final String PROJECT_NUMBER = "139518708260";

    TextView GCMTv,UserInfo;
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

        GCMTv = (TextView) findViewById(R.id.GCMTv);
        UserInfo = (TextView) findViewById(R.id.UserInfo);
        UserInfo.setOnClickListener(this);

        //check gcm
        regid = getGCM(context);
        if (regid != null) {
            GCMTv.setText(regid);
        } else {
            RegisterGCM();
        }

        //check if user already registered
        userid = getIDNumber(context);
        if (userid != null) {
            UserInfo.setText("NIK = "+userid);
            //process userinfo from storage
            userinfo = readUserInfo();
            userinfo = userinfo.replace(" ", "");
            userinfo = userinfo.replace("\n","").replace("\r", "");
            userhash = Converter.sha256Hash(userinfo);
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
                deleteIDNumber(context);
                break;
            case R.id.UserInfo:
                new AlertDialog.Builder(this)
                            .setTitle("Reading User Info")
                            .setMessage("text:"+userinfo+" hash:"+userhash)
                            .show();
                /*
                try {
                    form = new JSONObject(forHashCalc);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //construct confirm URL
                String userinfourl = "http://postcatcher.in/catchers/54f6896ec8958803000005f2";
                Log.i(TAG, "Sending user information "+userinfo);
                SendResponse(form, userinfourl);
                */
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Bundle extras = intent.getExtras();

        if (extras != null) {
            parseExtras(extras);
            confirmationDialog();
        }
    }

    private String readUserInfo() {
        String filename = "mobileid-userinfo.json";
        FileOperations fop = new FileOperations();
        String content = fop.read(filename);
        if(content.isEmpty()){
            return null;
        }
        Log.i(TAG,"File content: "+content);
        return content;
    }

    private void confirmationDialog(){
        final EditText input = new EditText(MainActivity.this);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        String messagetype =""
               ,content = "";

        try {
            messagetype = gcmObj.getString("info");
            content = gcmObj.getString("content");
        } catch (JSONException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        Log.i(TAG,"info: "+ messagetype);
        if(messagetype.compareTo("notification") == 0) {
            //only show simple alert
            final AlertDialog d = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Notification")
                    .setMessage(content)
                    .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                    .create();
            d.setOnShowListener(new DialogInterface.OnShowListener() {

                @Override
                public void onShow(DialogInterface dialog) {
                    Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
                    b.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            d.dismiss();
                            Log.i(TAG,"OK Clicked!");
                        }
                    });
                }
            });
            d.show();
        } else {
            //create alert with input box and data sending
            final AlertDialog d = new AlertDialog.Builder(MainActivity.this)
                    .setView(input)
                    .setTitle("Enter PIN")
                    .setMessage(content)
                    .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();

            final String finalMessagetype = messagetype;
            d.setOnShowListener(new DialogInterface.OnShowListener() {

                @Override
                public void onShow(DialogInterface dialog) {
                    Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
                    b.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            String passphrase="",userhmac="",OTP="",SIaddress="",PID="";
                            passphrase = input.getText().toString();

                            JSONObject MessagetoSI = new JSONObject();

                            try {
                                OTP = gcmObj.getString("OTP");
                                SIaddress = gcmObj.getString("SIaddress");
                                PID = gcmObj.getString("PID");
                            } catch (JSONException e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            }

                            switch (finalMessagetype) {
                                case "login":
                                    Log.i(TAG,"Begin Login Procedure");
                                    //generate hmac
                                    try {
                                        userhmac = Converter.sha256Hmac(OTP, userhash);
                                        MessagetoSI.put("HMAC", userhmac);
                                        MessagetoSI.put("PID", PID);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    break;
                            }
                            //sending message
                            Log.i(TAG, "Confirm Registration to "+SIaddress);
                            SendResponse(MessagetoSI, SIaddress);
                            d.dismiss();
                            Log.i(TAG,"OK Clicked!");
                        }
                    });
                }
            });
            d.show();
        }
    }

    private void parseExtras(Bundle extras){
        String gcmMessage = extras.getString("gcmMsg");
        Log.i(TAG,"Receive extras "+gcmMessage);
        try {
            gcmObj = new JSONObject(gcmMessage);
        } catch (JSONException e) {
            e.printStackTrace();
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

    private void SendResponse(final JSONObject Form, final String url){
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String textresponse;
                MediaType JSON
                        = MediaType.parse("application/json; charset=utf-8");

                OkHttpClient client = new OkHttpClient();

                RequestBody body = RequestBody.create(JSON, Form.toString());

                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                Response response;
                try {
                    response = client.newCall(request).execute();
                    textresponse = response.body().string();
                } catch (IOException e) {
                    textresponse = null;
                }
                return textresponse;
            }
            @Override
            protected void onPostExecute(String result) {
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, result, duration);
                toast.show();
            }
        }.execute(null, null, null);
    }
}
