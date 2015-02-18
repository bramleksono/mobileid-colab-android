package com.itb.bram.mobileidcompanion.steps;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.itb.bram.mobileidcompanion.CaptureSignature;
import com.itb.bram.mobileidcompanion.MainActivity;
import com.itb.bram.mobileidcompanion.R;

import org.codepond.wizardroid.WizardStep;
import org.codepond.wizardroid.persistence.ContextVariable;

/**
 * Form Register Step 2 : Scan QR Code
 */

public class RegisterStep2 extends WizardStep {

    //get User PIN
    @ContextVariable
    private String PIN1;

    Button SigBtn;

    //Registration URL
    private String RegContentText = "No Data Set";

    //You must have an empty constructor for every step
    public RegisterStep2() {
    }

    //Set your layout here
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.regstep2, container, false);

        TextView RegContent = (TextView) v.findViewById(R.id.RegContentTv);

        //inject value
        RegContent.setText(RegContentText);
        return v;
    }

    @Override
    public void onExit(int exitCode) {
        switch (exitCode) {
            case WizardStep.EXIT_NEXT:
                bindDataFields();
                break;
            case WizardStep.EXIT_PREVIOUS:
                //Do nothing...
                break;
        }
    }

    private void bindDataFields() {
        //Do some work
        //...
        //The values of these fields will be automatically stored in the wizard context
        //and will be populated in the next steps only if the same field names are used.
    }
}