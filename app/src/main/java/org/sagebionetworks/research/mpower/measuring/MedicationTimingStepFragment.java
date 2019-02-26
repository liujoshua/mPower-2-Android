/*
 * BSD 3-Clause License
 *
 * Copyright 2018  Sage Bionetworks. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1.  Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2.  Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * 3.  Neither the name of the copyright holder(s) nor the names of any contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission. No license is granted to the trademarks of
 * the copyright holders even if such marks are included in this software.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.sagebionetworks.research.mpower.measuring;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.sagebionetworks.research.domain.result.AnswerResultType;
import org.sagebionetworks.research.domain.result.implementations.AnswerResultBase;
import org.sagebionetworks.research.mobile_ui.show_step.view.FormUIStepFragment;
import org.sagebionetworks.research.mobile_ui.show_step.view.ShowStepFragmentBase;
import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.presentation.model.form.ChoiceInputFieldViewBase;
import org.sagebionetworks.research.presentation.model.form.ChoiceView;
import org.sagebionetworks.research.presentation.model.form.InputFieldView;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import java.util.List;

public class MedicationTimingStepFragment extends FormUIStepFragment {
    private static final Logger LOGGER = LoggerFactory.getLogger(MedicationTimingStepFragment.class);
    public static final String MEDICATION_TIMING_SELECTION_KEY = "medicationTiming";

    @NonNull
    public static MedicationTimingStepFragment newInstance(@NonNull StepView stepView) {
        MedicationTimingStepFragment fragment = new MedicationTimingStepFragment();
        Bundle arguments = ShowStepFragmentBase.createArguments(stepView);
        fragment.setArguments(arguments);
        return fragment;
    }

    public void writeMedicationTimingResult(String medicationTiming) {
        AnswerResultBase<String> result = new AnswerResultBase<>(
                MEDICATION_TIMING_SELECTION_KEY, Instant.now(), Instant.now(), medicationTiming, AnswerResultType.STRING);
        this.performTaskViewModel.addStepResult(result);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = super.onCreateView(inflater, container, savedInstanceState);

        RecyclerView recyclerView = this.stepViewBinding.getRecyclerView();
        if (recyclerView != null) {
            recyclerView.setHasFixedSize(true);
            LinearLayoutManager manager = new LinearLayoutManager(recyclerView.getContext());
            recyclerView.setLayoutManager(manager);
            DividerItemDecoration decoration = new DividerItemDecoration(recyclerView.getContext(),
                    manager.getOrientation());
            Drawable drawable = getContext().getResources().getDrawable(R.drawable.form_step_divider);
            decoration.setDrawable(drawable);
            recyclerView.addItemDecoration(decoration);
            List<InputFieldView> inputFields = stepView.getInputFields();
            if (inputFields.isEmpty()) {
                LOGGER.warn("Form step with no input fields created.");
                return result;
            } else if (inputFields.size() > 1) {
                LOGGER.warn("Form step with more than 1 input field created, using the first input field.");
            }

            InputFieldView inputField = inputFields.get(0);
            if (!(inputField instanceof ChoiceInputFieldViewBase<?>)) {
                LOGGER.warn("Form step with a non ChoiceInput field created.");
                return result;
            }
            ChoiceInputFieldViewBase<?> choiceInputField = (ChoiceInputFieldViewBase<?>)inputField;

            // The default value is both if the user hasn't selected before.
            SharedPreferences prefs = this.getSharedPreferencesForTask();
            String lastSelection = prefs.getString(MEDICATION_TIMING_SELECTION_KEY, null);
            if (lastSelection == null) {
                ChoiceView first = choiceInputField.getChoices().get(0);
                if (first.getAnswerValue() instanceof String) {
                    lastSelection = (String)first.getAnswerValue();
                }
            }
            writeMedicationTimingResult(lastSelection);

            MedicationTimingAdapter<?> adapter = new MedicationTimingAdapter<>(this, recyclerView,
                    choiceInputField.getChoices(), lastSelection);
            recyclerView.setAdapter(adapter);
        }

        return result;
    }

    public SharedPreferences getSharedPreferencesForTask() {
        String taskId = performTaskViewModel.getTaskView().getIdentifier();
        return getContext().getSharedPreferences(taskId, Context.MODE_PRIVATE);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mpower2_form_step;
    }
}