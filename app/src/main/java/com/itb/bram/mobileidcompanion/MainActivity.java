package com.itb.bram.mobileidcompanion;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.view.View.OnClickListener;



public class MainActivity extends ActionBarActivity implements OnClickListener {

    Button LaunchReg;
    Button CaptureSig;
    ImageView signImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        signImage = (ImageView) findViewById(R.id.imageView1);

        //button to launch Register Activity
        LaunchReg = (Button) findViewById(R.id.LaunchReg);
        LaunchReg.setOnClickListener(this);
        CaptureSig = (Button) findViewById(R.id.CreateSig);
        CaptureSig.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.LaunchReg:
                Intent i = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(i);
                break;

            case R.id.CreateSig:
                Intent j = new Intent(MainActivity.this, CaptureSignature.class);
                startActivityForResult(j, 0);
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (resultCode == 1) {
            Bitmap b = BitmapFactory.decodeByteArray(
                    data.getByteArrayExtra("byteArray"), 0,
                    data.getByteArrayExtra("byteArray").length);
            signImage.setImageBitmap(b);
        }
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
