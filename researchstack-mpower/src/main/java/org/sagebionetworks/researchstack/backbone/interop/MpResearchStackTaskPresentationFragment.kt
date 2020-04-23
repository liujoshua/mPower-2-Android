/*
 * BSD 3-Clause License
 *
 * Copyright 2020  Sage Bionetworks. All rights reserved.
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
package org.sagebionetworks.researchstack.backbone.interop

import androidx.appcompat.widget.Toolbar
import org.researchstack.foundation.components.presentation.TaskPresentationViewModel
import org.researchstack.foundation.core.models.step.Step
import org.sagebionetworks.research.mpower.researchstack.framework.step.toolbar.MpTaskToolbar
import org.sagebionetworks.researchstack.backbone.interop.presentation.BackwardsCompatibleTaskPresentationFragment

class MpResearchStackTaskPresentationFragment : BackwardsCompatibleTaskPresentationFragment() {
    protected var toolbar: Toolbar? = null

    override fun showStep(taskNavigatorState: TaskPresentationViewModel.TaskNavigatorState<Step>) {
        super.showStep(taskNavigatorState)

        var toolbar: MpTaskToolbar? = getToolbar()
//        toolbar.refreshToolbarIcons(currentStepLayout, getSupportActionBar())
//        toolbar.refreshToolbarBackgroundColor(currentStepLayout, toolbarContainer)
//        toolbar.refreshViewBehindToolbar(root, currentStepLayout)
//        toolbar.refreshViewHideToolbar(currentStepLayout, toolbarContainer)
//
//        val stepProgressText = toolbar.createOrderedTaskStepProgressText(task, currentStep)
//        toolbar.refreshStepProgress(currentStepLayout, stepProgressTextView, stepProgressText)
//
//        toolbar.setStatusBarColor(this, currentStepLayout)
//
//        MpViewTaskActivity.refreshVolumeControl(this, currentStepLayout)
//        MpViewTaskActivity.callTaskResultListener(taskResult, currentStepLayout)
    }

    protected fun getToolbar(): MpTaskToolbar? {
        return if (toolbar != null && toolbar is MpTaskToolbar) {
            toolbar as MpTaskToolbar?
        } else null
    }
}