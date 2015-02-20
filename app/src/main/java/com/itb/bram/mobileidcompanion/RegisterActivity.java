package com.itb.bram.mobileidcompanion;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

public class RegisterActivity extends ActionBarActivity implements View.OnClickListener {

    final String TAG = "MobileID Companion";
    Context context;

    //sharedpreference string
    private static final String PROPERTY_GCMID = "GCMID";

    //Registration variable
    String RegCheckAddr, RegConfirmAddr, regcode;
    StringBuilder ReqURL = new StringBuilder();

    EditText PIN1;
    EditText PIN2;
    Button CaptureSig, ScanQR, SubmitRegBtn;
    ImageView signImage;
    TextView QRContentTv, subSum, ReviewTv;

    final int SignatureReqCode = 0;
    final int QRReqCode = 1;
    final int RESULT_OK = 1;

    byte[] Sigb;
    String QRContentText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //execute when this activity started
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        context = getApplicationContext();

        //step 1
        PIN1 = (EditText) findViewById(R.id.PIN1Field);
        PIN2 = (EditText) findViewById(R.id.PIN2Field);
        //step 2
        signImage = (ImageView) findViewById(R.id.imageView);
        CaptureSig = (Button) findViewById(R.id.CreateSigBtn);
        CaptureSig.setOnClickListener(this);
        //step 3
        ScanQR = (Button) findViewById(R.id.ScanQRBtn);
        ScanQR.setOnClickListener(this);
        QRContentTv = (TextView) findViewById(R.id.QRContentTv);
        //step 4
        SubmitRegBtn = (Button) findViewById(R.id.SubmitRegBtn);
        SubmitRegBtn.setOnClickListener(this);
        subSum = (TextView) findViewById(R.id.subSum);
        ReviewTv = (TextView) findViewById(R.id.ReviewTv);
    }

    @Override
    public void onResume(){
        super.onResume();
        if (QRContentText != null) {
            new AsyncTask<String, Integer, String>(){
                String data;
                @Override
                protected String doInBackground(String... params) {
                    ReqURL.setLength(0);
                    ReqURL.append(RegCheckAddr);
                    ReqURL.append("?regcode=");
                    ReqURL.append(regcode);
                    Log.i(TAG, "Ask User Info to "+ReqURL);

                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(ReqURL.toString())
                            .build();
                    Response response;
                    try {
                        response = client.newCall(request).execute();
                        data = response.body().string();
                    } catch (IOException e) {
                        data = null;
                    }
                    return data;
                }

                @Override
                protected void onPostExecute(String result) {
                    super.onPostExecute(result);
                    Log.i(TAG, "Receive HTTP data " + data);
                    //update user interface
                    subSum.setText("Review and Submit Data");

                    ReviewTv.setText(data);
                    ReviewTv.setVisibility(View.VISIBLE);
                    SubmitRegBtn.setVisibility(View.VISIBLE);
                }
            }.execute();
        }
    }

    @Override
    public void onClick(View v) {
        //handle user click
        switch (v.getId()) {
            case R.id.CreateSigBtn:
                Sigb = null; //delete previous data
                Intent j = new Intent(RegisterActivity.this, CaptureSignature.class);
                startActivityForResult(j, SignatureReqCode);
                break;

            case R.id.ScanQRBtn:
                QRContentText = null; //delete previous data
                ReviewTv.setText("Empty Content");
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                startActivityForResult(intent, QRReqCode);
                break;

            case R.id.SubmitRegBtn:
                //submit {GCM Address, PIN, Signature} to CA, and Save User Info to Disk
                //image to base64 conversion
                String encodedImage = Base64.encodeToString(Sigb, Base64.NO_WRAP);
                //construct as json
                JSONObject RegistrationInfo = new JSONObject();
                try {
                    RegistrationInfo.put("GCMAddress", getGCM(context));
                    RegistrationInfo.put("PIN", PIN1.getText().toString());
                    RegistrationInfo.put("Signature", encodedImage);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //construct confirm URL
                RegConfirmAddr = "http://postcatcher.in/catchers/54e69183c4f07b030000061b";
                ReqURL.setLength(0);
                ReqURL.append(RegConfirmAddr);
                //ReqURL.append("?regcode=");
                //ReqURL.append(regcode);
                Log.i(TAG, "Confirm Registration to "+ReqURL);
                SendRegistrationData(RegistrationInfo, ReqURL.toString());
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //handle other activity result
        switch (requestCode) {
            case SignatureReqCode:
                //from draw signature activity
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "Receive Signature");
                    Sigb = data.getByteArrayExtra("byteArray");
                    signImage.setImageBitmap(BitmapFactory.decodeByteArray(Sigb, 0, Sigb.length));
                    //show imageview
                    signImage.setVisibility(View.VISIBLE);
                } else {
                    Log.i(TAG, "Signature Not OK");
                }
                break;
            case QRReqCode:
                if (resultCode == -1) {
                    //from qr code scanner activity
                    QRContentText = data.getStringExtra("SCAN_RESULT");
                    Log.i(TAG, "Receive content " + QRContentText);
                    QRContentTv.setText(QRContentText);
                    //assign Registration Variable
                    try {
                        JSONObject mainObject = new JSONObject(QRContentText);
                        RegCheckAddr = mainObject.getString("RegCheckAddr");
                        RegConfirmAddr = mainObject.getString("RegConfirmAddr");
                        regcode = mainObject.getString("regcode");
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                } else {
                    Log.i(TAG, "QR Code Not OK "+resultCode);
                }
                break;
        }
    }

    public void SendRegistrationData(final JSONObject RegistrationInfo, final String url){
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String textresponse;
                MediaType JSON
                        = MediaType.parse("application/json; charset=utf-8");

                OkHttpClient client = new OkHttpClient();

                RequestBody body = RequestBody.create(JSON, RegistrationInfo.toString());

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

    private SharedPreferences getAppPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences,
        // but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
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
}
