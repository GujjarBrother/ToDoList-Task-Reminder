package com.todo.list.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.todo.list.BuildConfig
import com.todo.list.R
import com.todo.list.adapters.ViewPagerAdapter
import com.todo.list.adsPlugin.bannerAd.BannerAdController
import com.todo.list.base.BaseActivity
import com.todo.list.databinding.ActivityDashBoardBinding
import com.todo.list.databinding.ExitFromAnAppDialogLayoutBinding
import com.todo.list.databinding.SignOutDialogLayoutBinding
import com.todo.list.enums.Tabs
import com.todo.list.enums.Visibility
import com.todo.list.listeners.SearchViewVisibilityListener
import com.todo.list.listeners.StartAndStopFABAnimationListener
import com.todo.list.utils.CommonFunctions.applyAnimation
import com.todo.list.utils.CommonFunctions.changeAppMode
import com.todo.list.utils.CommonFunctions.changeVisibility
import com.todo.list.utils.CommonFunctions.isSomethingChanged
import com.todo.list.utils.CommonFunctions.keepActivityOn
import com.todo.list.utils.CommonFunctions.openAppInPlayStore
import com.todo.list.utils.CommonFunctions.openGoogleAppStore
import com.todo.list.utils.CommonFunctions.openPrivacyPolicyActivity

class DashBoardActivity : BaseActivity(), View.OnClickListener, SearchViewVisibilityListener {

    private val binding by lazy {
        ActivityDashBoardBinding.inflate(layoutInflater)
    }
    private lateinit var startAndStopFABAnimationListener: StartAndStopFABAnimationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        with(binding) {
            BannerAdController.loadAndShowBannerAd(
                activity = activityContext,
                containerLayout = adLayout,
                loadingLayout = adLoadingInclude.rootLayout,
                isInternetConnected = isInternetConnectedORNot((getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager)),
                adID = getString(R.string.dashboardScreenBannerAdId)
            )

            navigationDrawerInclude.lightAndDarkModeSwitch.isChecked = when (AppCompatDelegate.getDefaultNightMode()) {
                AppCompatDelegate.MODE_NIGHT_YES -> {
                    navigationDrawerInclude.lightAndDarkModeIV.setImageResource(R.drawable.moon_image)
                    navigationDrawerInclude.lightAndDarkModeTV.text = getString(R.string.dark_mode_text)
                    true
                }

                else -> {
                    navigationDrawerInclude.lightAndDarkModeIV.setImageResource(R.drawable.sun_image)
                    navigationDrawerInclude.lightAndDarkModeTV.text = getString(R.string.light_mode_text)
                    false
                }
            }

            keepActivityOn(activityContext)
            applyCustomFont()

            val viewPagerAdapter = ViewPagerAdapter(activityContext)

            val actionBarDrawerToggle = ActionBarDrawerToggle(activityContext, dashBoardActivityDrawerLayout,
                R.string.navigation_drawer_open_text, R.string.navigation_drawer_close_text)
            dashBoardActivityDrawerLayout.addDrawerListener(actionBarDrawerToggle)
            actionBarDrawerToggle.syncState()

            navigationDrawerInclude.versionNumberTV.text = String.format("%s%s", "v", BuildConfig.VERSION_NAME)

            dashBoardViewPager.adapter = viewPagerAdapter
            TabLayoutMediator(tabLayout, dashBoardViewPager) {
                tab: TabLayout.Tab, position: Int -> tab.setText(
                if (position == Tabs.TASKS_TAB.ordinal) {
                    getString(R.string.tasks_text)
                } else {
                    getString(R.string.completed_text)
                })
            }.attach()

            dashBoardViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    manageTabsScrolling(position)
                }
            })
            signOutIV.setOnClickListener(this@DashBoardActivity)
            settingsIV.setOnClickListener(this@DashBoardActivity)
            searchIV.setOnClickListener(this@DashBoardActivity)
            searchCrossIV.setOnClickListener(this@DashBoardActivity)
            openAndCloseDrawerIV.setOnClickListener(this@DashBoardActivity)
            navigationDrawerInclude.settingsOuterLayout.setOnClickListener(this@DashBoardActivity)
            navigationDrawerInclude.visitOurAppStoreOuterLayout.setOnClickListener(this@DashBoardActivity)
            navigationDrawerInclude.privacyPolicyOuterLayout.setOnClickListener(this@DashBoardActivity)
            navigationDrawerInclude.checkUpdateOuterLayout.setOnClickListener(this@DashBoardActivity)
            navigationDrawerInclude.appNameTV.isSelected = true

            searchET.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (::startAndStopFABAnimationListener.isInitialized) {
                        startAndStopFABAnimationListener.search(query = s.toString().trim())
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                }
            })

            navigationDrawerInclude.lightAndDarkModeSwitch.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                changeAppMode(isChecked)
                prefs.isDarkModeEnable = isChecked
                isSomethingChanged.value = true
                dashBoardActivityDrawerLayout.closeDrawer(GravityCompat.START)
            }

            val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (dashBoardActivityDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                        dashBoardActivityDrawerLayout.closeDrawer(GravityCompat.START)
                    } else {
                        if (dashBoardViewPager.currentItem != 0) {
                            dashBoardViewPager.currentItem = 0
                        } else {
                            showExitDialog()
                        }
                    }
                }
            }
            onBackPressedDispatcher.addCallback(onBackPressedCallback)
            dashBoardActivityDrawerLayout.setScrimColor(ContextCompat.getColor(activityContext, R.color.navigationDrawerScrimColor))
        }
    }

    private fun ActivityDashBoardBinding.manageTabsScrolling(position: Int) {
        if (position == 0) {
            toolbarTV.text = getString(R.string.tasks_text)
            startAndStopFABAnimationListener.startAndStopFABAnimation(1)
        } else if (position == 1) {
            toolbarTV.text = getString(R.string.completed_text)
        }
        dashBoardViewPager.setCurrentItem(position, true)
    }

    private fun ActivityDashBoardBinding.applyCustomFont() {
//        Here We Applying Our Custom Font On Tab Item's Text In TabLayout In Tabbed Activity...........
        val viewGroup = tabLayout.getChildAt(0) as ViewGroup
        val tabsCount = viewGroup.childCount
        for (j in 0 until tabsCount) {
            val vgTab = viewGroup.getChildAt(j) as ViewGroup
            val tabChildCount = vgTab.childCount
            for (i in 0 until tabChildCount) {
                val tabViewChild = vgTab.getChildAt(i)
                if (tabViewChild is TextView) {
//                    tabViewChild.typeface = typeface
                }
            }
        }
    }

    override fun onClick(view: View?) {
        with(binding) {
            when (view?.id) {
                R.id.signOutIV -> showSignOutDialog()
                R.id.settingsIV -> openSettingsActivity()
                R.id.openAndCloseDrawerIV -> dashBoardActivityDrawerLayout.openDrawer(GravityCompat.START)
                R.id.settingsOuterLayout -> {
                    openSettingsActivity()
                    dashBoardActivityDrawerLayout.closeDrawer(GravityCompat.START)
                }

                R.id.visitOurAppStoreOuterLayout -> {
                    openGoogleAppStore(activityContext)
                    dashBoardActivityDrawerLayout.closeDrawer(GravityCompat.START)
                }

                R.id.privacyPolicyOuterLayout -> {
                    openPrivacyPolicyActivity(
                        activityContext,
                        isInternetConnectedORNot((getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager))
                    )
                    dashBoardActivityDrawerLayout.closeDrawer(GravityCompat.START)
                }

                R.id.checkUpdateOuterLayout -> {
                    openAppInPlayStore(activityContext, BuildConfig.APPLICATION_ID)
                    dashBoardActivityDrawerLayout.closeDrawer(GravityCompat.START)
                }

                R.id.searchIV -> {
                    toolbarGroup.changeVisibility(Visibility.GONE.ordinal)
                    searchLayout.changeVisibility(Visibility.VISIBLE.ordinal)
                    showSoftKeyboard()
                    searchET.requestFocus()
                }

                R.id.searchCrossIV -> {
                    searchET.text = null
                    toolbarGroup.changeVisibility(Visibility.VISIBLE.ordinal)
                    searchLayout.changeVisibility(Visibility.GONE.ordinal)
                    hideSoftKeyboard(searchET)
                }

                else -> {}
            }
        }
    }

    private fun showSignOutDialog() {
        val signOutDialogLayoutBinding = SignOutDialogLayoutBinding.inflate(layoutInflater)

        val signOutDialogBuilder = AlertDialog.Builder(activityContext)
        with(signOutDialogBuilder) {
            setView(signOutDialogLayoutBinding.root)
            setCancelable(true)
            setOnDismissListener {
                if (binding.dashBoardViewPager.currentItem == 0) {
                    startAndStopFABAnimationListener.startAndStopFABAnimation(1)
                }
            }
        }
        val signOutAlertDialog = signOutDialogBuilder.create()
        if (!activityContext.isFinishing && !activityContext.isDestroyed && !signOutAlertDialog.isShowing) {
            signOutAlertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            signOutAlertDialog.window?.setWindowAnimations(R.style.dialogBoxesAnimation)
            signOutAlertDialog.show()
        }

        if (binding.dashBoardViewPager.currentItem == 0) {
            startAndStopFABAnimationListener.startAndStopFABAnimation(0)
        }

        with(signOutDialogLayoutBinding) {
            signOutIV.startAnimation(applyAnimation(activityContext))

            noButton.setOnClickListener { _: View ->
                if (!activityContext.isFinishing && !activityContext.isDestroyed) {
                    signOutAlertDialog.dismiss()
                }
                if (binding.dashBoardViewPager.currentItem == 0) {
                    startAndStopFABAnimationListener.startAndStopFABAnimation(1)
                }
            }

            yesButton.setOnClickListener { _: View ->
                prefs.isUserSignIn = false
                if (!activityContext.isFinishing && !activityContext.isDestroyed) {
                    signOutAlertDialog.dismiss()
                }
                openSignInActivity()
            }
        }
    }

    private fun openSignInActivity() {
        startActivity(Intent(activityContext, SignInActivity::class.java))
        finish()
    }

    fun initializeStopFABAnimationFromToDosFragmentListener(
        startAndStopFABAnimationListener: StartAndStopFABAnimationListener
    ) {
        this.startAndStopFABAnimationListener = startAndStopFABAnimationListener
    }

    private fun openSettingsActivity() = startActivity(Intent(activityContext, SettingsActivity::class.java))

    private fun showExitDialog() {
        val exitFromAnAppDialogLayoutBinding = ExitFromAnAppDialogLayoutBinding.inflate(layoutInflater)

        val exitFromAnAppDialogBuilder = AlertDialog.Builder(activityContext)
        with(exitFromAnAppDialogBuilder) {
            setView(exitFromAnAppDialogLayoutBinding.root)
            setCancelable(true)
            setOnDismissListener {
                if (binding.dashBoardViewPager.currentItem == 0) {
                    startAndStopFABAnimationListener.startAndStopFABAnimation(1)
                }
            }
        }
        val exitFromAnAppAlertDialog = exitFromAnAppDialogBuilder.create()
        if (!activityContext.isFinishing && !activityContext.isDestroyed && !exitFromAnAppAlertDialog.isShowing) {
            exitFromAnAppAlertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            exitFromAnAppAlertDialog.window?.setWindowAnimations(R.style.dialogBoxesAnimation)
            exitFromAnAppAlertDialog.show()
        }

        if (binding.dashBoardViewPager.currentItem == 0) {
            startAndStopFABAnimationListener.startAndStopFABAnimation(0)
        }

        with(exitFromAnAppDialogLayoutBinding) {
            exitFromAnAppIV.startAnimation(applyAnimation(activityContext))

            noButton.setOnClickListener { _: View ->
                if (binding.dashBoardViewPager.currentItem == 0) {
                    startAndStopFABAnimationListener.startAndStopFABAnimation(1)
                }
                if (!activityContext.isFinishing && !activityContext.isDestroyed) {
                    exitFromAnAppAlertDialog.dismiss()
                }
            }

            yesButton.setOnClickListener { _: View ->
                openThankYouActivity()
                if (!activityContext.isFinishing && !activityContext.isDestroyed) {
                    exitFromAnAppAlertDialog.dismiss()
                }
            }
        }
    }

    private fun openThankYouActivity() {
        startActivity(Intent(activityContext, ThankYouActivity::class.java))
        finish()
    }

    override fun isShowSearchViewORNot(isShow: Boolean) {
        with(binding) {
            if (isShow) {
                searchIV.changeVisibility(Visibility.VISIBLE.ordinal)
            } else {
                searchIV.changeVisibility(Visibility.GONE.ordinal)
            }
        }
    }
}