package com.itb.bram.mobileidcompanion.steps;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.itb.bram.mobileidcompanion.R;

import org.codepond.wizardroid.WizardStep;

/**
 * Form Register Step 1 : Enter User PIN
 */

public class RegisterStep1 extends WizardStep {

    private String PIN1;
    private String PIN2;

    EditText PIN1Et;
    EditText PIN2Et;

    //You must have an empty constructor for every step
    public RegisterStep1() {
    }

    //Set your layout here
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.regstep1, container, false);
        //Get reference to the textboxes
        PIN1Et = (EditText) v.findViewById(R.id.PIN1Field);
        PIN2Et = (EditText) v.findViewById(R.id.PIN2Field);

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
        PIN1 = PIN1Et.getText().toString();
        PIN2 = PIN2Et.getText().toString();
    }
}
