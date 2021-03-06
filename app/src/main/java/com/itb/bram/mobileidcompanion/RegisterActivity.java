package com.itb.bram.mobileidcompanion;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class RegisterActivity extends Activity implements View.OnClickListener {

    final String TAG = "MobileID Companion";
    Context context;

    //sharedpreference string
    private static final String PROPERTY_GCMID = "GCMID";
    private static final String PROPERTY_IDNUMBER = "UserIdNumber";
    private static final String PROPERTY_PIN = "UserPIN";

    //Registration variable
    String RegCheckAddr, RegConfirmAddr, RegCode, userinfo, idnumber, datatosave;
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

                @Override
                protected String doInBackground(String... params) {
                    ReqURL.setLength(0);
                    ReqURL.append(RegCheckAddr);
                    ReqURL.append("?regcode=");
                    ReqURL.append(RegCode);
                    Log.i(TAG, "Ask User Info to "+ReqURL);

                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(ReqURL.toString())
                            .build();
                    Response response;

                    try {
                        response = client.newCall(request).execute();
                        userinfo = response.body().string();
                    } catch (IOException e) {
                        return null;
                    }

                    return userinfo;
                }

                @Override
                protected void onPostExecute(String result) {
                    super.onPostExecute(result);

                    String ktpString;
                    if (result != null) {
                        //process userinfo
                        Log.i(TAG, "Receive HTTP data " + userinfo);
                        //update user interface
                        subSum.setText("Review and Submit Data");

                        JSONObject ktpObj;
                        try {
                            ktpObj = new JSONObject(userinfo);
                            //parse user info to text
                            ktpString = "NIK: " + ktpObj.getString("nik") + "\n";
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
                            SubmitRegBtn.setVisibility(View.VISIBLE);
                        } catch (JSONException e) {
                            ktpString = "Error reading user info, repeat Scan QR Code (Kesalahan membaca user info, ulangi Scan QR Code).";
                        }

                    } else {
                        //cannot connect to server
                        Log.i(TAG, "Cannot connect to server.");
                        ktpString = "Cannot retrieved user info, check your connection (Tidak dapat mengambil user info, periksa koneksi anda).";
                    }

                    ReviewTv.setText(ktpString);
                    ReviewTv.setVisibility(View.VISIBLE);
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
                    RegistrationInfo.put("RegCode", RegCode);
                    RegistrationInfo.put("GCMAddress", getGCM(context));
                    RegistrationInfo.put("PIN", PIN1.getText().toString());
                    RegistrationInfo.put("Signature", encodedImage);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //construct confirm URL
                Log.i(TAG, "Confirm Registration to "+RegConfirmAddr);
                SendRegistrationData(RegistrationInfo, RegConfirmAddr);
                SubmitRegBtn.setEnabled(false);
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
                        RegCode = mainObject.getString("RegCode");
                    } catch (JSONException e) {
                        QRContentText = null;
                        QRContentTv.setText("Try scan again (Ulangi proses scan)");
                    }
                } else {
                    Log.i(TAG, "QR Code Not OK "+resultCode);
                }
                break;
        }
    }

    private void SendRegistrationData(final JSONObject RegistrationInfo, final String url){
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
                    return null;
                }

                return textresponse;
            }
            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    try {
                        JSONObject mainObject = new JSONObject(userinfo);
                        idnumber = mainObject.getString("nik");
                        storeIDNumber(context,idnumber);

                        storePIN(context, PIN1.getText().toString());

                        userinfo = userinfo.replaceAll("[\\p{Cc}\\p{Cf}\\p{Co}\\p{Cn}]", "");
                        saveUserInfo(userinfo);

                        result = "User info saved";
                        finish();

                    } catch (JSONException e) {
                        result = "Problem with saving user info";
                    }

                } else {
                    result = "Problem with sending data, check your connection (Tidak dapat mengirim data, periksa koneksi anda).";
                }
                SubmitRegBtn.setEnabled(true);
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

    private void storeIDNumber(Context context, String idnumber) {
        final SharedPreferences prefs = getAppPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_IDNUMBER, idnumber);
        editor.commit();
        Log.i(TAG, "Storing ID Number");
    }

    private void storePIN(Context context, String PIN) {
        final SharedPreferences prefs = getAppPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_PIN, PIN);
        editor.commit();
        Log.i(TAG, "Storing ID Number");
    }

    private void saveUserInfo(String string) {
        String filename = "mobileid-userinfo.json";
        FileOperations fop = new FileOperations();
        fop.write(filename, string);
        if(fop.write(filename, string)){
            Toast.makeText(getApplicationContext(), filename+" created", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "I/O error", Toast.LENGTH_SHORT).show();
        }
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
