package com.itb.bram.mobileidcompanion;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * This activity create wizard form to register user
 * Flow : RegisterActivity -> wizard/RegForm -> steps/RegisterStep1,RegisterStep2,RegisterStep3
 */
public class RegisterActivity extends FragmentActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regform);
    }
}
