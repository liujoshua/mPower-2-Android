package org.sagebionetworks.research.mpower.researchstack;

import android.os.Bundle;

import org.researchstack.backbone.step.Step;
import org.researchstack.backbone.ui.ActiveTaskActivity;

public class MpSignupActivity extends ActiveTaskActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onDataReady(); // TODO: Do this right
    }

    @Override
    public void showStep(Step step, boolean alwaysReplaceView) {
        super.showStep(step, alwaysReplaceView);
    }

    @Override
    protected void requestStorageAccess() {
        // TODO: remove this
    }
}
