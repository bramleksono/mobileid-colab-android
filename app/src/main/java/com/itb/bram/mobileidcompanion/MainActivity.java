package com.itb.bram.mobileidcompanion;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.view.View.OnClickListener;
import android.widget.TextView;



import java.io.ByteArrayOutputStream;

public class MainActivity extends Activity implements OnClickListener {

    final String TAG = "MobileID Companion";

    //sharedpreference string
    Context context;
    private static final String PROPERTY_QRCODE = "QRCode";
    private static final String PROPERTY_SIGNATURE = "Signature";

    Button LaunchReg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //button to launch Register Activity
        LaunchReg = (Button) findViewById(R.id.LaunchReg);
        LaunchReg.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.LaunchReg:
                Intent i = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(i);
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

    private void deleteSignature(Context context) {
        final SharedPreferences prefs = getAppPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(PROPERTY_SIGNATURE);
        editor.commit();
        Log.i(TAG, "Signature deleted");
    }

    private void deleteQRContent(Context context) {
        final SharedPreferences prefs = getAppPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(PROPERTY_QRCODE);
        editor.commit();
        Log.i(TAG, "QR Content deleted");
    }

    private void storeSignature(Context context, String signature) {
        final SharedPreferences prefs = getAppPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_SIGNATURE, signature);
        editor.commit();
        Log.i(TAG, "Storing Signature");
    }

    private void storeQRContent(Context context, String content) {
        final SharedPreferences prefs = getAppPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_QRCODE, content);
        editor.commit();
        Log.i(TAG, "Storing QR Content");
    }

    private String getSignature(Context context) {
        final SharedPreferences prefs = getAppPreferences(context);
        String Signature = prefs.getString(PROPERTY_SIGNATURE, "");
        if (Signature.isEmpty()) {
            Log.i(TAG, "There's no Signature in preferences");
            return null;
        }
        return Signature;
    }

    private String getQRContent(Context context) {
        final SharedPreferences prefs = getAppPreferences(context);
        String Content = prefs.getString(PROPERTY_QRCODE, "");
        if (Content.isEmpty()) {
            Log.i(TAG, "There's no QRCode content in preferences");
            return null;
        }
        return Content;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
