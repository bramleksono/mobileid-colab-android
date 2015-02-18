package com.itb.bram.mobileidcompanion.wizard;

import com.itb.bram.mobileidcompanion.steps.RegisterStep1;
import com.itb.bram.mobileidcompanion.steps.RegisterStep2;

import org.codepond.wizardroid.WizardFlow;
import org.codepond.wizardroid.layouts.BasicWizardLayout;

/**
 *
 */
public class RegForm extends BasicWizardLayout {

    public RegForm() {
        super();
    }

    @Override
    public WizardFlow onSetup() {
        return new WizardFlow.Builder()
                /*
                Add your steps in the order you want them to appear and eventually call create()
                to create the wizard flow.
                 */
                .addStep(RegisterStep1.class)
                /*
                Mark this step as 'required', preventing the user from advancing to the next step
                until a certain action is taken to mark this step as completed by calling WizardStep#notifyCompleted()
                from the step.
                 */
                .addStep(RegisterStep2.class)
                .create();
    }

    @Override
    public void onWizardComplete() {
        super.onWizardComplete();   //Make sure to first call the super method before anything else
        //... Access context variables here before terminating the wizard
        //...
        //String fullname = firstname + lastname;
        //Store the data in the DB or pass it back to the calling activity
        getActivity().finish();     //Terminate the wizard
    }
}
