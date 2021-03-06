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

package org.sagebionetworks.research.mpower.researchstack.framework.step;

import com.google.gson.annotations.SerializedName;

import org.sagebionetworks.researchstack.backbone.model.survey.FormSurveyItem;

/**
 * Created by TheMDP on 10/24/17.
 */

public class MpFormSurveyItem extends FormSurveyItem {

    /**
     * A string representation of a color resource for the background
     */
    @SerializedName("backgroundColor")
    public String backgroundColorRes;

    /**
     * A string representation of a color resource for the image background
     */
    @SerializedName("imageColor")
    public String imageColorRes;

    /**
     * A string representation of a color resource for the status bar
     */
    @SerializedName("statusBarColor")
    public String statusBarColorRes;

    /**
     * String to show for the "Next" button
     */
    @SerializedName("buttonTitle")
    public String buttonTitle;

    /**
     * Set to true to hide the back button in the bottom submit bar
     */
    @SerializedName("hideBackButton")
    public Boolean hideBackButton;

    /**
     * String representation of a dimen resource for bottom text container padding
     */
    @SerializedName("textContainerBottomPaddingRes")
    public String textContainerBottomPaddingRes;

    /**
     * The identifier of the task to show when bottomLinkText is clicked
     */
    @SerializedName("bottomLinkTaskId")
    public String bottomLinkTaskId;

    /* Default constructor needed for serilization/deserialization of object */
    public MpFormSurveyItem() {
        super();
    }
}
