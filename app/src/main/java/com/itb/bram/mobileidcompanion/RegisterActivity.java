package com.itb.bram.mobileidcompanion;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class RegisterActivity extends ActionBarActivity implements View.OnClickListener {

    final String TAG = "MobileID Companion";

    Button CaptureSig, ScanQR;
    ImageView signImage;
    TextView QRContent;

    final int SignatureReqCode = 0;
    final int QRReqCode = 1;
    final int RESULT_OK = 1;

    byte[] b;
    String QRContentText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        signImage = (ImageView) findViewById(R.id.imageView1);
        CaptureSig = (Button) findViewById(R.id.CreateSig);
        CaptureSig.setOnClickListener(this);
        ScanQR = (Button) findViewById(R.id.ScanQR);
        ScanQR.setOnClickListener(this);
        QRContent = (TextView) findViewById(R.id.QRContentTv);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.CreateSig:
                //delete previous data
                b = null;
                Intent j = new Intent(RegisterActivity.this, CaptureSignature.class);
                startActivityForResult(j, SignatureReqCode);
                break;

            case R.id.ScanQR:
                //delete previous data
                QRContentText = null;
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                startActivityForResult(intent, QRReqCode);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SignatureReqCode:
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
                QRContentText = data.getStringExtra("SCAN_RESULT");
                Log.i(TAG, "Receive content "+QRContentText);
                QRContent.setText(QRContentText);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
