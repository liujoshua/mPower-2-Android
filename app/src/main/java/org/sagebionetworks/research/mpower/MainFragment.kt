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

package org.sagebionetworks.research.mpower

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.common.base.Supplier
import com.google.common.collect.ImmutableMap
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.activity_main.navigation
import org.sagebionetworks.research.mpower.authentication.ExternalIdSignInActivity
import org.sagebionetworks.research.mpower.profile.ProfileFragment
import org.sagebionetworks.research.mpower.sageresearch.ui.WebConsentFragment
import org.sagebionetworks.research.mpower.tracking.TrackingTabFragment
import org.slf4j.LoggerFactory
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 *
 */
class MainFragment : Fragment(), HasSupportFragmentInjector {

    private val LOGGER = LoggerFactory.getLogger(MainFragment::class.java)
    private val CONSENT_ACTIVITY_REQUEST_CODE = 1725

    // tag for identifying an instance of a fragment
    private val TAG_FRAGMENT_TRACKING = "tracking"
    private val TAG_FRAGMENT_PROFILE = "profile"
    private val TAG_FRAGMENT_WEB_CONSENT = "webCosent"

    // Mapping of a tag to a creation method for a fragment
    private val FRAGMENT_TAG_TO_CREATOR = ImmutableMap.Builder<String, Supplier<Fragment>>()
            .put(TAG_FRAGMENT_TRACKING, Supplier { TrackingTabFragment() })
            .put(TAG_FRAGMENT_PROFILE, Supplier { ProfileFragment() })
            .put(TAG_FRAGMENT_WEB_CONSENT, Supplier { WebConsentFragment() })
            .build()

    // mapping of navigation IDs to a fragment tag
    private val FRAGMENT_NAV_ID_TO_TAG = ImmutableMap.Builder<Int, String>()
            .put(R.id.navigation_tracking, TAG_FRAGMENT_TRACKING)
            .put(R.id.navigation_profile, TAG_FRAGMENT_PROFILE)
            .build()

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var taskLauncher: TaskLauncher

    private lateinit var mainViewModel: MainViewModel

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        showFragment(FRAGMENT_NAV_ID_TO_TAG[item.itemId])
        return@OnNavigationItemSelectedListener true
    }

    @Inject
    lateinit var mainViewModelProviderFactory: MainViewModelFactory

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_main, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mainViewModel = ViewModelProviders.of(this, mainViewModelProviderFactory.create())
                .get(MainViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()
        // TODO: make this async for data refresh from network @liujoshua 2018/09/27
        if (!mainViewModel.isAuthenticated) {
            showSignUpActivity()
        } else if (!mainViewModel.isConsented) {
            // FIXME check for local consent pending upload @liujoshua 2018/08/06
            showConsent()
        } else {
            showMainActivityLayout()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CONSENT_ACTIVITY_REQUEST_CODE) {
            // TODO: get rid of blocking network call 2018/09/27 @liujoshua
            mainViewModel.refreshSession()
        }
    }

    fun showSignUpActivity() {
        LOGGER.debug("Showing sign up activity")

//        taskLauncher.launchTask(this, SIGN_UP, null)
        startActivity(Intent(context, ExternalIdSignInActivity::class.java))
    }

    fun showConsent() {
        LOGGER.debug("Showing consent activity")

        showFragment(TAG_FRAGMENT_WEB_CONSENT)
    }

    fun showMainActivityLayout() {
        LOGGER.debug("Showing main activity")

        showFragment(TAG_FRAGMENT_TRACKING)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    /**
     * Show the fragment specified by a certain tag. The fragment currently displayed in fragment_container is
     * detached from the UI. If this is the first time we are showing a fragment, an instance is created and added to
     * the fragment_container. If we have previously displayed a fragment, we retrieve it from the fragment manager
     * and re-attached to the UI.
     */
    fun showFragment(fragmentTag: String?) {
        if (fragmentTag == null) {
            LOGGER.warn("could not show fragment with null tag")
        }

        val fragmentTransaction = childFragmentManager.beginTransaction()

        val previousFragment = childFragmentManager
                .findFragmentById(R.id.fragment_container)
        if (previousFragment != null) {
            LOGGER.debug("detaching fragment with tag: {}", previousFragment.tag)
            fragmentTransaction.detach(previousFragment)
        }

        var nextFragment = childFragmentManager.findFragmentByTag(fragmentTag)
        if (nextFragment == null) {
            LOGGER.debug("no fragment found for tag: {}, creating a new one ", fragmentTag)
            val fragmentSupplier: Supplier<Fragment>? = FRAGMENT_TAG_TO_CREATOR[fragmentTag]
                    ?: FRAGMENT_TAG_TO_CREATOR[TAG_FRAGMENT_TRACKING]

            if (fragmentSupplier == null) {
                LOGGER.warn("no supplier found for fragment with tag: {}", fragmentTag)
                return
            }
            nextFragment = fragmentSupplier.get()

            fragmentTransaction
                    .add(R.id.fragment_container, nextFragment, fragmentTag)
        } else {
            LOGGER.debug("reattaching fragment with tag: {}", nextFragment.tag)
            fragmentTransaction.attach(nextFragment)
        }
        fragmentTransaction.commit()
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentInjector
    }
}
