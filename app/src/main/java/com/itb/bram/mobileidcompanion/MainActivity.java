package com.itb.bram.mobileidcompanion;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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

import static android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD;

public class MainActivity extends Activity implements OnClickListener {

    final String TAG = "MobileID Companion";
    Context context;
    String userid, userinfo, parseduserinfo, userhash;
    JSONObject gcmObj;

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
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        //check if user already registered
        userid = getIDNumber(context);
        if (userid != null) {
            //process userinfo from storage
            userinfo = readUserInfo();
            if (userinfo != null) {
                UserInfo.setText("NIK = "+userid);
                //parse json to string
                parseduserinfo = parseIdentity(userinfo);
                //clean text before calculate hash
                userinfo = userinfo.replace(" ", "");
                userinfo = userinfo.replace("\n","").replace("\r", "");
                userhash = Converter.sha256Hash(userinfo);
            } else {
                //must be registration fault. clear id number
                deleteIDNumber(context);
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(context, "Cannot read user info. Please repeat registration process.", duration);
                toast.show();
            }

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
                            .setMessage("Detail:\n"+parseduserinfo+"\n Hash:\n"+userhash)
                            .show();
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
        if(content != null && !content.isEmpty() ){
            Log.i(TAG, "File content: " + content);
            return content;
        } else {
            return null;
        }
    }

    private String parseIdentity(String content) {
        JSONObject ktpObj;
        if(content.isEmpty()){
            return null;
        } else {
            try {
                ktpObj = new JSONObject(content);
                //parse user info to text
                String ktpString = "NIK: " + ktpObj.getString("nik") + "\n";
                ktpString = ktpString.concat("Nama: " + ktpObj.getString("nama") + "\n");
                ktpString = ktpString.concat("Tempat/Tgl Lahir: " + ktpObj.getString("ttl") + "\n");
                ktpString = ktpString.concat("Jenis Kelamin: " + ktpObj.getString("jeniskelamin") + "\n");
                ktpString = ktpString.concat("Gol.Darah: " + ktpObj.getString("goldarah") + "\n");
                ktpString = ktpString.concat("Alamat: " + ktpObj.getString("alamat") + "\n");
                ktpString = ktpString.concat("RT/RW: " + ktpObj.getString("rtrw") + "\n");
                ktpString = ktpString.concat("Kel/Desa: " + ktpObj.getString("keldesa") + "\n");
                ktpString = ktpString.concat("Kecamatan: " + ktpObj.getString("kecamatan") + "\n");
                ktpString = ktpString.concat("Agama: " + ktpObj.getString("agama") + "\n");
                ktpString = ktpString.concat("Status Perkawinan: " + ktpObj.getString("statperkawinan") + "\n");
                ktpString = ktpString.concat("Pekerjaan: " + ktpObj.getString("pekerjaan") + "\n");
                ktpString = ktpString.concat("Kewarganegaraan: " + ktpObj.getString("kewarganegaraan") + "\n");
                ktpString = ktpString.concat("Berlaku Hingga: " + ktpObj.getString("berlaku") + "\n");

                content = ktpString;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return content;
    }

    private void confirmationDialog(){
        final EditText input = new EditText(MainActivity.this);
        input.setInputType(TYPE_NUMBER_VARIATION_PASSWORD);

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
        } else if((messagetype.compareTo("login") == 0) || (messagetype.compareTo("verification") == 0)) {
            messagetype = messagetype.substring(0,1).toUpperCase() + messagetype.substring(1);
            //show message and ok, close button
            final AlertDialog d = new AlertDialog.Builder(MainActivity.this)
                    .setTitle(messagetype)
                    .setMessage(content)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                    .create();

            d.setOnShowListener(new DialogInterface.OnShowListener() {

                @Override
                public void onShow(DialogInterface dialog) {
                    Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
                    b.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            String hmac = "", OTP = "", SIaddress = "", PID = "", hash = "";

                            JSONObject MessagetoSI = new JSONObject();

                            try {
                                OTP = gcmObj.getString("OTP");
                                SIaddress = gcmObj.getString("SIaddress");
                                PID = gcmObj.getString("PID");
                            } catch (JSONException e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            }

                            Log.i(TAG, "Begin Login Procedure");
                            //generate hmac
                            try {
                                hmac = Converter.sha256Hmac(OTP, userhash);
                                MessagetoSI.put("HMAC", hmac);
                                MessagetoSI.put("PID", PID);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            //sending message
                            Log.i(TAG, "Confirm Login to " + SIaddress);
                            Log.i(TAG, "Sending data " + MessagetoSI);
                            SendResponse(MessagetoSI, SIaddress);
                            d.dismiss();
                            Log.i(TAG, "OK Clicked!");
                        }
                    });
                }
            });
            d.show();
        }
        else {
            //create alert with input box and data sending
            final AlertDialog d = new AlertDialog.Builder(MainActivity.this)
                    .setView(input)
                    .setTitle("Enter PIN")
                    .setMessage(content)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                    .create();

            d.setOnShowListener(new DialogInterface.OnShowListener() {

                @Override
                public void onShow(DialogInterface dialog) {
                    Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
                    b.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            String passphrase="",hmac="",OTP="",SIaddress="",PID="", hash="";
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

                            Log.i(TAG,"Begin Signing Procedure");
                            //get hash and generate hmac
                            try {
                                hash = gcmObj.getString("hash");
                                hmac = Converter.sha256Hmac(OTP, hash);
                                MessagetoSI.put("HMAC", hmac);
                                MessagetoSI.put("PID", PID);
                                MessagetoSI.put("Passphrase", passphrase);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            //sending message
                            Log.i(TAG, "Confirm Signing to "+SIaddress);
                            Log.i(TAG, "Sending data " + MessagetoSI);
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
                Log.i(TAG, "Received Response " + result);
                //play sound
                try {
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    r.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(context, result, duration);
                toast.show();
            }
        }.execute(null, null, null);
    }
}
