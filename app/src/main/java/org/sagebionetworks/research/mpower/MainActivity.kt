package org.sagebionetworks.research.mpower

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.google.common.base.Supplier
import com.google.common.collect.ImmutableMap
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.activity_main.navigation
import org.sagebionetworks.research.mpower.authentication.ExternalIdSignInActivity
import org.sagebionetworks.research.mpower.profile.ProfileFragment
import org.sagebionetworks.research.mpower.research.MpIdentifier.SIGN_UP
import org.sagebionetworks.research.mpower.sageresearch.ui.BridgeAccessViewModel
import org.sagebionetworks.research.mpower.sageresearch.ui.WebConsentFragment
import org.sagebionetworks.research.mpower.tracking.TrackingTabFragment
import org.slf4j.LoggerFactory
import javax.inject.Inject

class MainActivity : AppCompatActivity(), HasSupportFragmentInjector {
    private val LOGGER = LoggerFactory.getLogger(MainActivity::class.java)
    private val CONSENT_ACTIVITY_REQUEST_CODE = 1725

    // tag for identifying an instance of a fragment
    private val TAG_FRAGMENT_TRACKING = "tracking"
    private val TAG_FRAGMENT_PROFILE = "profile"
    private val TAG_FRAGMENT_WEB_CONSENT = "webCosent"

    // Mapping of a tag to a creation method for a fragment
    private val FRAGMENT_TAG_TO_CREATOR = ImmutableMap.Builder<String, Supplier<Fragment>>()
            .put(TAG_FRAGMENT_TRACKING, Supplier { TrackingTabFragment() })
            .put(TAG_FRAGMENT_PROFILE, Supplier { ProfileFragment() })
            .put(TAG_FRAGMENT_WEB_CONSENT, Supplier {WebConsentFragment()})
            .build()

    // mapping of navigation IDs to a fragment tag
    private val FRAGMENT_NAV_ID_TO_TAG = ImmutableMap.Builder<Int, String>()
            .put(R.id.navigation_tracking, TAG_FRAGMENT_TRACKING)
            .put(R.id.navigation_profile, TAG_FRAGMENT_PROFILE)
            .build()

    @Inject
    lateinit var bridgeAccessViewModelFactory: BridgeAccessViewModel.Factory

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var mainViewModelProviderFactory: MainViewModelFactory

    @Inject
    lateinit var taskLauncher: TaskLauncher

    private lateinit var mainViewModel: MainViewModel

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        showFragment(FRAGMENT_NAV_ID_TO_TAG[item.itemId])
        return@OnNavigationItemSelectedListener true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
        startActivity(Intent(Intent(this, ExternalIdSignInActivity::class.java)))
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

        val fragmentTransaction = supportFragmentManager.beginTransaction()

        val previousFragment = supportFragmentManager
                .findFragmentById(R.id.fragment_container)
        if (previousFragment != null) {
            LOGGER.debug("detaching fragment with tag: {}", previousFragment.tag)
            fragmentTransaction.detach(previousFragment)
        }

        var nextFragment = supportFragmentManager.findFragmentByTag(fragmentTag)
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