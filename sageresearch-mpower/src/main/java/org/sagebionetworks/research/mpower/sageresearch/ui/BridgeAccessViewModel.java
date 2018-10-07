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

package org.sagebionetworks.research.mpower.sageresearch.ui;

import static com.google.common.base.Preconditions.checkArgument;

import static hu.akarnokd.rxjava.interop.RxJavaInterop.toV2Single;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.webkit.JavascriptInterface;

import org.sagebionetworks.bridge.android.BridgeConfig;
import org.sagebionetworks.bridge.android.manager.AuthenticationManager;
import org.sagebionetworks.bridge.rest.model.SharingScope;
import org.sagebionetworks.bridge.rest.model.UserSessionInfo;
import org.sagebionetworks.research.presentation.contract.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

public class BridgeAccessViewModel extends ViewModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(BridgeAccessViewModel.class);

    private final AuthenticationManager authenticationManager;

    private final BridgeConfig bridgeConfig;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final MutableLiveData<Resource<BridgeAccessStatus>> bridgeAccessStatusMutableLiveData = new MutableLiveData<>();

    /**
     * Represents authentication and authorization state.
     * <p>
     * App version unsupported Requires authentication. Requires consent. Access Granted.
     */
    public enum BridgeAccessStatus {
        REQUIRES_APP_UPGRADE,
        REQUIRES_AUTHENTICATION,
        REQUIRES_CONSENT,
        GRANTED
    }

    public static class BridgeAccessState {
        private final boolean isWaiting;

        private final BridgeAccessStatus isConsented;

        private final String errorMessage;

        public BridgeAccessState(final boolean isWaiting, final BridgeAccessStatus isConsented, final String errorMessage) {
            this.isWaiting = isWaiting;
            this.isConsented = isConsented;
            this.errorMessage = errorMessage;
        }

        /**
         * @return localized error message if any, or null
         */
        public String getErrorMessage() {
            return errorMessage;
        }

        /**
         * @return true if user is consented
         */
        public BridgeAccessStatus getAccessState() {
            return isConsented;
        }

        /**
         * @return true if app is uploading consent to Bridge
         */
        public boolean isWaiting() {
            return isWaiting;
        }
    }

    static class ConsentsToResearchModel {
        String name;

        String scope;
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final AuthenticationManager authenticationManager;

        private final BridgeConfig bridgeConfig;

        @Inject
        public Factory(final AuthenticationManager authenticationManager,
                final BridgeConfig bridgeConfig) {
            this.authenticationManager = authenticationManager;
            this.bridgeConfig = bridgeConfig;
        }

        @NonNull
        @Override
        @SuppressWarnings(value = "unchecked")
        public <T extends ViewModel> T create(@NonNull final Class<T> modelClass) {
            checkArgument(modelClass.isAssignableFrom(BridgeAccessViewModel.class));
            return (T) new BridgeAccessViewModel(authenticationManager, bridgeConfig);
        }
    }

    public BridgeAccessViewModel(
            final AuthenticationManager authenticationManager,
            final BridgeConfig bridgeConfig) {
        this.authenticationManager = authenticationManager;
        this.bridgeConfig = bridgeConfig;

        checkAuth();
    }

    @VisibleForTesting
    void checkAuth() {
        UserSessionInfo userSessionInfo = authenticationManager.getUserSessionInfo();
        boolean authenticated = userSessionInfo != null && userSessionInfo.isAuthenticated();

        BridgeAccessStatus state;
        if (!authenticated) {
            bridgeAccessStatusMutableLiveData.postValue(
                    Resource.loading(BridgeAccessStatus.REQUIRES_AUTHENTICATION)
            );
        } else {
            checkConsent();
        }
    }

    @VisibleForTesting
    void checkConsent() {
        boolean consented = authenticationManager.isConsented();

        BridgeAccessStatus state;
        if (!consented) {
            bridgeAccessStatusMutableLiveData.postValue(
                    Resource.loading(BridgeAccessStatus.REQUIRES_CONSENT)
            );
        } else {
            bridgeAccessStatusMutableLiveData.postValue(
                    Resource.success(BridgeAccessStatus.GRANTED)
            );
        }
    }

    @JavascriptInterface
    @AnyThread
    public void consentsToResearch(String jsonString) {
        LOGGER.debug("consentsToResearch called");

//        ConsentsToResearchModel model = new Gson().fromJson(jsonString, ConsentsToResearchModel.class)
        ConsentsToResearchModel model = new ConsentsToResearchModel();
        model.name = "HARDCODED NAME";
        model.scope = "all_qualified_researchers";

        bridgeAccessStatusMutableLiveData.postValue(
                Resource.loading(BridgeAccessStatus.REQUIRES_CONSENT)
        );

        compositeDisposable.add(
                toV2Single(authenticationManager.giveConsent(
                        bridgeConfig.getStudyId(),
                        model.name,
                        null, null, null,
                        SharingScope.fromValue(model.scope)
                        )
                ).subscribe(this::onConsentSuccess, this::onConsentFailure)
        );
    }

    public MutableLiveData<Resource<BridgeAccessStatus>> getBridgeAccessStatus() {
        return bridgeAccessStatusMutableLiveData;
    }

    @VisibleForTesting
    void onConsentSuccess(UserSessionInfo userSessionInfo) {
        LOGGER.debug("consent upload success");

        bridgeAccessStatusMutableLiveData.postValue(
                Resource.success(BridgeAccessStatus.GRANTED)
        );
    }

    @VisibleForTesting
    void onConsentFailure(Throwable t) {
        LOGGER.warn("consent upload error", t);

        bridgeAccessStatusMutableLiveData.postValue(
                Resource.error(t.getLocalizedMessage(), BridgeAccessStatus.REQUIRES_CONSENT)
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}