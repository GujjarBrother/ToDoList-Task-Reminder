package com.todo.list.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
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
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.todo.list.BuildConfig
import com.todo.list.R
import com.todo.list.adapters.ViewPagerAdapter
import com.todo.list.adsPlugin.bannerAd.BannerAdController
import com.todo.list.application.Application.Companion.prefs
import com.todo.list.application.Application.Companion.typeface
import com.todo.list.base.BaseActivity
import com.todo.list.databinding.ActivityDashBoardBinding
import com.todo.list.databinding.ExitFromAnAppDialogLayoutBinding
import com.todo.list.databinding.SignOutDialogLayoutBinding
import com.todo.list.enums.Tabs
import com.todo.list.enums.Visibility
import com.todo.list.listeners.StartAndStopFABAnimationListener
import com.todo.list.models.SelectedColors
import com.todo.list.utils.ColorsUtils
import com.todo.list.utils.ColorsUtils.blackColor
import com.todo.list.utils.ColorsUtils.cardsNightModeColor
import com.todo.list.utils.ColorsUtils.darkModeTextColor
import com.todo.list.utils.ColorsUtils.getContextCompatColor
import com.todo.list.utils.ColorsUtils.lightBlueColor
import com.todo.list.utils.ColorsUtils.screensNightModeColor
import com.todo.list.utils.ColorsUtils.snowWhiteColor
import com.todo.list.utils.ColorsUtils.subTitlesTextColor
import com.todo.list.utils.ColorsUtils.switchTrackOffColor
import com.todo.list.utils.ColorsUtils.tabLayoutUnSelectedTabTextColor
import com.todo.list.utils.ColorsUtils.whiteColor
import com.todo.list.utils.CommonFunctions.applyAnimation
import com.todo.list.utils.CommonFunctions.changeStatusBarColor
import com.todo.list.utils.CommonFunctions.changeVisibility
import com.todo.list.utils.CommonFunctions.isSomethingChanged
import com.todo.list.utils.CommonFunctions.keepActivityOn
import com.todo.list.utils.CommonFunctions.openAppInPlayStore
import com.todo.list.utils.CommonFunctions.openGoogleAppStore
import com.todo.list.utils.CommonFunctions.openPrivacyPolicyActivity

class DashBoardActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityDashBoardBinding
    private lateinit var startAndStopFABAnimationListener: StartAndStopFABAnimationListener
    private lateinit var selectedColors: SelectedColors

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedColors = ColorsUtils.getSelectedColor(activityContext, prefs)

        with(binding) {
            BannerAdController.loadAndShowBannerAd(
                activity = activityContext,
                containerLayout = adLayout,
                loadingLayout = adLoadingInclude.rootLayout,
                isInternetConnected = isInternetConnectedORNot((getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager)),
                adID = getString(R.string.dashboardScreenBannerAdId)
            )

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
                prefs.isDarkModeEnable = isChecked
                applyLightAndDarkModeOnDashboardActivity()
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

    override fun onResume() {
        super.onResume()
        applyLightAndDarkModeOnDashboardActivity()
    }

    private fun applyLightAndDarkModeOnDashboardActivity() {
        with(binding) {
            val trackDrawable = navigationDrawerInclude.lightAndDarkModeSwitch.trackDrawable
            if (prefs.isDarkModeEnable) {
                changeStatusBarColor(activityContext, getContextCompatColor(activityContext, screensNightModeColor))
                toolbar.setBackgroundColor(getContextCompatColor(activityContext, screensNightModeColor))
                rootLayout.setBackgroundColor(getContextCompatColor(activityContext, screensNightModeColor))
                tabLayout.setBackgroundColor(getContextCompatColor(activityContext, screensNightModeColor))
                tabLayout.setSelectedTabIndicatorColor(getContextCompatColor(activityContext, cardsNightModeColor))
                tabLayout.setTabTextColors(
                    getContextCompatColor(activityContext, tabLayoutUnSelectedTabTextColor),
                    getContextCompatColor(activityContext, whiteColor)
                )
                searchIV.setColorFilter(getContextCompatColor(activityContext, lightBlueColor))
                searchCrossIV.setColorFilter(getContextCompatColor(activityContext, lightBlueColor))
                searchET.setHintTextColor(getContextCompatColor(activityContext, screensNightModeColor))
                adLoadingInclude.adIsLoadingTextView.setTextColor(getContextCompatColor(activityContext, whiteColor))
                adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(
                    getContextCompatColor(activityContext, whiteColor)
                )

                openAndCloseDrawerIV.setColorFilter(getContextCompatColor(activityContext, lightBlueColor))
                signOutIV.setColorFilter(getContextCompatColor(activityContext, lightBlueColor))
                settingsIV.setColorFilter(getContextCompatColor(activityContext, lightBlueColor))

                navigationDrawerInclude.rootLayout.background.colorFilter = PorterDuffColorFilter(
                    getContextCompatColor(activityContext, screensNightModeColor), PorterDuff.Mode.SRC_IN
                )
                navigationDrawerInclude.appNameTV.setTextColor(getContextCompatColor(activityContext, whiteColor))
                navigationDrawerInclude.featuresTV.setBackgroundColor(getContextCompatColor(activityContext, cardsNightModeColor))
                navigationDrawerInclude.lightAndDarkIV.setImageResource(R.drawable.sun_image)
                navigationDrawerInclude.lightAndDarkIV.colorFilter = PorterDuffColorFilter(getContextCompatColor(activityContext, lightBlueColor), PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.lightAndDarkModeTV.text = getString(R.string.light_mode_text)
                navigationDrawerInclude.lightAndDarkModeTV.setTextColor(getContextCompatColor(activityContext, whiteColor))
                navigationDrawerInclude.switchBetweenLightAndDarkModeTV.setTextColor(getContextCompatColor(activityContext, darkModeTextColor))
                navigationDrawerInclude.lightAndDarkModeSwitch.isChecked = true
                trackDrawable.colorFilter = PorterDuffColorFilter(getContextCompatColor(activityContext, snowWhiteColor), PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.lightAndDarkModeSwitch.thumbDrawable =
                    ContextCompat.getDrawable(activityContext, R.drawable.switch_thumb_night_mode)
                navigationDrawerInclude.generalSettingsTV.setBackgroundColor(getContextCompatColor(activityContext, cardsNightModeColor))
                navigationDrawerInclude.generalSettingsTV.setTextColor(getContextCompatColor(activityContext, whiteColor))
                navigationDrawerInclude.settingsIV.colorFilter = PorterDuffColorFilter(
                    getContextCompatColor(activityContext, lightBlueColor), PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.settingsTV.setTextColor(getContextCompatColor(activityContext, whiteColor))
                navigationDrawerInclude.seeTheRequiredSettingsTV.setTextColor(getContextCompatColor(activityContext, darkModeTextColor))
                navigationDrawerInclude.settingsArrowIV.colorFilter = PorterDuffColorFilter(
                    getContextCompatColor(activityContext, lightBlueColor), PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.visitOurAppStoreIV.colorFilter = PorterDuffColorFilter(
                    getContextCompatColor(activityContext, lightBlueColor), PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.visitOurAppStoreTV.setTextColor(getContextCompatColor(activityContext, whiteColor))
                navigationDrawerInclude.checkOurMoreAppsOnPlayStoreTV.setTextColor(getContextCompatColor(activityContext, darkModeTextColor))
                navigationDrawerInclude.visitOurAppStoreArrowIV.colorFilter = PorterDuffColorFilter(
                    getContextCompatColor(activityContext, lightBlueColor), PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.privacyPolicyIV.colorFilter = PorterDuffColorFilter(
                    getContextCompatColor(activityContext, lightBlueColor), PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.privacyPolicyTV.setTextColor(getContextCompatColor(activityContext, whiteColor))
                navigationDrawerInclude.readOurPrivacyPolicyTV.setTextColor(getContextCompatColor(activityContext, darkModeTextColor))
                navigationDrawerInclude.privacyPolicyArrowIV.colorFilter = PorterDuffColorFilter(
                    getContextCompatColor(activityContext, lightBlueColor), PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.checkUpdateIV.colorFilter = PorterDuffColorFilter(
                    getContextCompatColor(activityContext, lightBlueColor), PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.checkUpdateTV.setTextColor(getContextCompatColor(activityContext, whiteColor))
                navigationDrawerInclude.versionNumberTV.setTextColor(getContextCompatColor(activityContext, darkModeTextColor))
                navigationDrawerInclude.checkUpdateArrowIV.colorFilter = PorterDuffColorFilter(
                    getContextCompatColor(activityContext, lightBlueColor), PorterDuff.Mode.SRC_IN)
            } else {
                changeStatusBarColor(activityContext, selectedColors.originalColor)
                navigationDrawerInclude.lightAndDarkIV.setImageResource(R.drawable.moon_image)
                navigationDrawerInclude.lightAndDarkModeTV.text = getString(R.string.dark_mode_text)
                navigationDrawerInclude.lightAndDarkModeSwitch.isChecked = false
                trackDrawable.colorFilter = PorterDuffColorFilter(getContextCompatColor(activityContext, switchTrackOffColor), PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.lightAndDarkModeSwitch.thumbDrawable = ContextCompat.getDrawable(activityContext, R.drawable.switch_thumb)
                rootLayout.setBackgroundColor(getContextCompatColor(activityContext, snowWhiteColor))
                tabLayout.setSelectedTabIndicatorColor(getContextCompatColor(activityContext, snowWhiteColor))
                tabLayout.setTabTextColors(tabLayoutUnSelectedTabTextColor, getContextCompatColor(activityContext, blackColor))
                openAndCloseDrawerIV.setColorFilter(getContextCompatColor(activityContext, whiteColor))
                signOutIV.setColorFilter(getContextCompatColor(activityContext, whiteColor))
                settingsIV.setColorFilter(getContextCompatColor(activityContext, whiteColor))
                searchIV.setColorFilter(getContextCompatColor(activityContext, whiteColor))

                toolbar.setBackgroundColor(selectedColors.originalColor)
                tabLayout.setBackgroundColor(selectedColors.originalColor)
                searchCrossIV.setColorFilter(selectedColors.originalColor)
                searchET.setHintTextColor(selectedColors.originalColor)
                adLoadingInclude.adIsLoadingTextView.setTextColor(selectedColors.originalColor)
                adLoadingInclude.progressBar.indeterminateTintList = ColorStateList.valueOf(selectedColors.originalColor)
                navigationDrawerInclude.rootLayout.background.colorFilter = PorterDuffColorFilter(
                    getContextCompatColor(activityContext, snowWhiteColor), PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.appNameTV.setTextColor(getContextCompatColor(activityContext, blackColor))
                navigationDrawerInclude.featuresTV.setBackgroundColor(selectedColors.transparentColor)
                navigationDrawerInclude.lightAndDarkIV.colorFilter = PorterDuffColorFilter(selectedColors.originalColor, PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.lightAndDarkModeTV.setTextColor(getContextCompatColor(activityContext, blackColor))
                navigationDrawerInclude.switchBetweenLightAndDarkModeTV.setTextColor(getContextCompatColor(activityContext, subTitlesTextColor))
                navigationDrawerInclude.generalSettingsTV.setBackgroundColor(selectedColors.transparentColor)
                navigationDrawerInclude.settingsIV.colorFilter = PorterDuffColorFilter(selectedColors.originalColor, PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.settingsTV.setTextColor(getContextCompatColor(activityContext, blackColor))
                navigationDrawerInclude.seeTheRequiredSettingsTV.setTextColor(getContextCompatColor(activityContext, subTitlesTextColor))
                navigationDrawerInclude.settingsArrowIV.colorFilter = PorterDuffColorFilter(selectedColors.originalColor, PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.visitOurAppStoreIV.colorFilter = PorterDuffColorFilter(selectedColors.originalColor, PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.visitOurAppStoreTV.setTextColor(getContextCompatColor(activityContext, blackColor))
                navigationDrawerInclude.checkOurMoreAppsOnPlayStoreTV.setTextColor(getContextCompatColor(activityContext, subTitlesTextColor))
                navigationDrawerInclude.visitOurAppStoreArrowIV.colorFilter = PorterDuffColorFilter(selectedColors.originalColor, PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.privacyPolicyIV.colorFilter = PorterDuffColorFilter(selectedColors.originalColor, PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.privacyPolicyTV.setTextColor(getContextCompatColor(activityContext, blackColor))
                navigationDrawerInclude.readOurPrivacyPolicyTV.setTextColor(getContextCompatColor(activityContext, subTitlesTextColor))
                navigationDrawerInclude.privacyPolicyArrowIV.colorFilter = PorterDuffColorFilter(selectedColors.originalColor, PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.checkUpdateIV.colorFilter = PorterDuffColorFilter(selectedColors.originalColor, PorterDuff.Mode.SRC_IN)
                navigationDrawerInclude.checkUpdateTV.setTextColor(getContextCompatColor(activityContext, blackColor))
                navigationDrawerInclude.versionNumberTV.setTextColor(getContextCompatColor(activityContext, subTitlesTextColor))
                navigationDrawerInclude.checkUpdateArrowIV.colorFilter = PorterDuffColorFilter(selectedColors.originalColor, PorterDuff.Mode.SRC_IN)
            }
        }
    }

    private fun applyCustomFont() {
        with(binding) {
            toolbarTV.typeface = typeface
            searchET.typeface = typeface
            adLoadingInclude.adIsLoadingTextView.typeface = typeface
            navigationDrawerInclude.appNameTV.typeface = typeface
            navigationDrawerInclude.featuresTV.typeface = typeface
            navigationDrawerInclude.lightAndDarkModeTV.typeface = typeface
            navigationDrawerInclude.switchBetweenLightAndDarkModeTV.typeface = typeface
            navigationDrawerInclude.generalSettingsTV.typeface = typeface
            navigationDrawerInclude.settingsTV.typeface = typeface
            navigationDrawerInclude.seeTheRequiredSettingsTV.typeface = typeface
            navigationDrawerInclude.visitOurAppStoreTV.typeface = typeface
            navigationDrawerInclude.checkOurMoreAppsOnPlayStoreTV.typeface = typeface
            navigationDrawerInclude.privacyPolicyTV.typeface = typeface
            navigationDrawerInclude.readOurPrivacyPolicyTV.typeface = typeface
            navigationDrawerInclude.checkUpdateTV.typeface = typeface
            navigationDrawerInclude.versionNumberTV.typeface = typeface

//        Here We Applying Our Custom Font On Tab Item's Text In TabLayout In Tabbed Activity...........
            val viewGroup = tabLayout.getChildAt(0) as ViewGroup
            val tabsCount = viewGroup.childCount
            for (j in 0 until tabsCount) {
                val vgTab = viewGroup.getChildAt(j) as ViewGroup
                val tabChildCount = vgTab.childCount
                for (i in 0 until tabChildCount) {
                    val tabViewChild = vgTab.getChildAt(i)
                    if (tabViewChild is TextView) {
                        tabViewChild.typeface = typeface
                    }
                }
            }
        }
    }

    override fun onClick(view: View?) {
        with(binding) {
            when (view?.id) {
                R.id.signOutIV -> {
                    showSignOutDialog()
                }

                R.id.settingsIV -> {
                    openSettingsActivity()
                }

                R.id.openAndCloseDrawerIV -> {
                    dashBoardActivityDrawerLayout.openDrawer(GravityCompat.START)
                }

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
            applyCustomFontOnSignOutDialogViews(this)
            applyLightAndDarkModeOnSignOutDialogViews(this)

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

    //    Here, We Initialize Stop FAB Animation From ToDosFragment Listener...
    fun initializeStopFABAnimationFromToDosFragmentListener(
        startAndStopFABAnimationListener: StartAndStopFABAnimationListener
    ) {
        this.startAndStopFABAnimationListener = startAndStopFABAnimationListener
    }

    private fun openSettingsActivity() = startActivity(Intent(activityContext, SettingsActivity::class.java))

    private fun applyLightAndDarkModeOnSignOutDialogViews(
            signOutDialogLayoutBinding: SignOutDialogLayoutBinding
    ) {
        with(signOutDialogLayoutBinding) {
            if (prefs.isDarkModeEnable) {
                rootLayout.background.colorFilter = PorterDuffColorFilter(getContextCompatColor(activityContext, screensNightModeColor), PorterDuff.Mode.SRC_IN)
                signOutIV.setColorFilter(getContextCompatColor(activityContext, lightBlueColor))
                signOutMessageTV.setTextColor(getContextCompatColor(activityContext, whiteColor))
                noButton.strokeColor = ColorStateList.valueOf(getContextCompatColor(activityContext, lightBlueColor))
                noButton.setTextColor(getContextCompatColor(activityContext, lightBlueColor))
                yesButton.setBackgroundColor(getContextCompatColor(activityContext, lightBlueColor))
                yesButton.setTextColor(getContextCompatColor(activityContext, blackColor))
            } else {
                signOutIV.setColorFilter(selectedColors.originalColor)
                noButton.strokeColor = ColorStateList.valueOf(selectedColors.originalColor)
                noButton.setTextColor(selectedColors.originalColor)
                yesButton.setBackgroundColor(selectedColors.originalColor)
            }
        }
    }

    private fun applyCustomFontOnSignOutDialogViews(signOutDialogLayoutBinding: SignOutDialogLayoutBinding) {
        with(signOutDialogLayoutBinding) {
            signOutMessageTV.typeface = typeface
            yesButton.typeface = typeface
            noButton.typeface = typeface
        }
    }

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
            applyCustomFontOnExitFromAnAppDialogViews(this)
            applyLightAndDarkModeOnExitDialogViews(this)

            noButton.setOnClickListener { _: View ->
                if (!activityContext.isFinishing && !activityContext.isDestroyed) {
                    exitFromAnAppAlertDialog.dismiss()
                }
                if (binding.dashBoardViewPager.currentItem == 0) {
                    startAndStopFABAnimationListener.startAndStopFABAnimation(1)
                }
            }

            yesButton.setOnClickListener { _: View ->
                if (!activityContext.isFinishing && !activityContext.isDestroyed) {
                    exitFromAnAppAlertDialog.dismiss()
                }
                openThankYouActivity()
            }
        }
    }

    private fun openThankYouActivity() {
        startActivity(Intent(activityContext, ThankYouActivity::class.java))
        finish()
    }

    private fun applyLightAndDarkModeOnExitDialogViews(
            exitFromAnAppDialogLayoutBinding: ExitFromAnAppDialogLayoutBinding
    ) {
        with(exitFromAnAppDialogLayoutBinding) {
            if (prefs.isDarkModeEnable) {
                rootLayout.background.colorFilter = PorterDuffColorFilter(getContextCompatColor(activityContext, screensNightModeColor), PorterDuff.Mode.SRC_IN)
                exitFromAnAppIV.setColorFilter(getContextCompatColor(activityContext, whiteColor))
                exitFromAnAppMessageTV.setTextColor(getContextCompatColor(activityContext, whiteColor))
                noButton.strokeColor = ColorStateList.valueOf(getContextCompatColor(activityContext, lightBlueColor))
                noButton.setTextColor(getContextCompatColor(activityContext, lightBlueColor))
                yesButton.setBackgroundColor(getContextCompatColor(activityContext, lightBlueColor))
                yesButton.setTextColor(getContextCompatColor(activityContext, blackColor))
            } else {
                exitFromAnAppIV.setColorFilter(selectedColors.originalColor)
                noButton.strokeColor = ColorStateList.valueOf(selectedColors.originalColor)
                noButton.setTextColor(selectedColors.originalColor)
                yesButton.setBackgroundColor(selectedColors.originalColor)
            }
        }
    }

    private fun applyCustomFontOnExitFromAnAppDialogViews(
            exitFromAnAppDialogLayoutBinding: ExitFromAnAppDialogLayoutBinding
    ) {
        with(exitFromAnAppDialogLayoutBinding) {
            exitFromAnAppMessageTV.typeface = typeface
            yesButton.typeface = typeface
            noButton.typeface = typeface
        }
    }

    //    Override 'onConfigurationChanged' Method, Which Is Used To Prevent An Activity To 'Re-create' When
    //    Changing The Screen Orientation.i.e., Switching Between 'PORTRAIT MODE' TO 'LANDSCAPE MODE' & Vice Versa.
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }
}