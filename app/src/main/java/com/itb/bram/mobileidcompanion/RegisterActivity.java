package com.itb.bram.mobileidcompanion;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class RegisterActivity extends ActionBarActivity implements View.OnClickListener {

    final String TAG = "MobileID Companion";

    EditText PIN1;
    EditText PIN2;
    Button CaptureSig, ScanQR, SubmitRegBtn;
    ImageView signImage;
    TextView QRContentTv, subSum, ReviewTv;

    final int SignatureReqCode = 0;
    final int QRReqCode = 1;
    final int RESULT_OK = 1;

    byte[] b;
    String QRContentText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //execute when this activity started
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
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


                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(QRContentText)
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
                b = null; //delete previous data
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
                    byte[] b = data.getByteArrayExtra("byteArray");
                    signImage.setImageBitmap(BitmapFactory.decodeByteArray(b, 0, b.length));
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
                } else {
                    Log.i(TAG, "QR Code Not OK "+resultCode);
                }
                break;
        }
    }
}
