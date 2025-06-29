package com.sag.todo.list.task.reminder.activities

import android.app.AlarmManager
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.Animation
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textview.MaterialTextView
import com.sag.todo.list.task.reminder.BuildConfig
import com.sag.todo.list.task.reminder.R
import com.sag.todo.list.task.reminder.adapters.ViewPagerAdapter
import com.sag.todo.list.task.reminder.adsPlugin.bannerAd.BannerAdController
import com.sag.todo.list.task.reminder.base.BaseActivity
import com.sag.todo.list.task.reminder.controllers.PermissionsController
import com.sag.todo.list.task.reminder.databinding.ActivityDashBoardBinding
import com.sag.todo.list.task.reminder.databinding.CustomTabBinding
import com.sag.todo.list.task.reminder.databinding.ExitFromAnAppDialogLayoutBinding
import com.sag.todo.list.task.reminder.databinding.SignOutDialogLayoutBinding
import com.sag.todo.list.task.reminder.enums.Tabs
import com.sag.todo.list.task.reminder.enums.Visibility
import com.sag.todo.list.task.reminder.fragments.AllTasksFragment
import com.sag.todo.list.task.reminder.listeners.SearchViewVisibilityListener
import com.sag.todo.list.task.reminder.listeners.StartAndStopFABAnimationListener
import com.sag.todo.list.task.reminder.utils.CommonFunctions.changeAppMode
import com.sag.todo.list.task.reminder.utils.CommonFunctions.changeVisibility
import com.sag.todo.list.task.reminder.utils.CommonFunctions.isSomethingChanged
import com.sag.todo.list.task.reminder.utils.CommonFunctions.keepActivityOn
import com.sag.todo.list.task.reminder.utils.CommonFunctions.openAppInPlayStore
import com.sag.todo.list.task.reminder.utils.CommonFunctions.openGoogleAppStore
import com.sag.todo.list.task.reminder.utils.CommonFunctions.openPrivacyPolicyActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class DashBoardActivity : BaseActivity(), View.OnClickListener, SearchViewVisibilityListener {

    private val binding by lazy {
        ActivityDashBoardBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var alarmManager: AlarmManager

    @Inject
    @Named(value = "FabRateUsAndApplyAnimation")
    lateinit var animation: Animation

    private lateinit var startAndStopFABAnimationListener: StartAndStopFABAnimationListener
    private val postNotificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            Toast.makeText(activityContext, "Granted...!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(activityContext, "Not-Granted...!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val defaultColor = ContextCompat.getColor(activityContext, R.color.defaultColor)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(defaultColor),
            navigationBarStyle = SystemBarStyle.dark(defaultColor)
        )
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        lifecycle.addObserver(PermissionsController(
            context = activityContext,
            postNotificationPermissionLauncher = postNotificationPermissionLauncher,
            alarmManager = alarmManager
        ))

        with(binding) {
            BannerAdController.loadAndShowBannerAd(
                activity = activityContext,
                containerLayout = adLayout,
                loadingLayout = adLoadingInclude.rootLayout,
                isInternetConnected = internetController.isInternetConnected,
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

            val actionBarDrawerToggle = ActionBarDrawerToggle(activityContext, dashBoardActivityDrawerLayout,
                R.string.navigation_drawer_open_text, R.string.navigation_drawer_close_text)
            dashBoardActivityDrawerLayout.addDrawerListener(actionBarDrawerToggle)
            actionBarDrawerToggle.syncState()

            navigationDrawerInclude.versionNumberTV.text = String.format("%s%s", "v", BuildConfig.VERSION_NAME)

            val viewPagerAdapter = ViewPagerAdapter(activityContext)
            dashBoardViewPager.adapter = viewPagerAdapter
            TabLayoutMediator(tabLayout, dashBoardViewPager) {
                tab: TabLayout.Tab, position: Int ->
                val customTabBinding = CustomTabBinding.inflate(layoutInflater)
                customTabBinding.apply {
                    tabTV.text = if (position == Tabs.TASKS_TAB.ordinal) {
                        getString(R.string.tasks_text)
                    } else {
                        getString(R.string.completed_text)
                    }
                    tabTV.setTextColor(ContextCompat.getColor(activityContext, R.color.tabLayoutUnSelectedTabTextColor))
                    tab.customView = root
                }
            }.attach()

            tabLayout.getTabAt(0)?.customView?.findViewById<TextView>(R.id.tabTV)?.apply {
                setTextColor(ContextCompat.getColor(activityContext, R.color.defaultColor))
                setTypeface(typeface, Typeface.BOLD)
            }

            dashBoardViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    manageTabsScrolling(position)
                }
            })

            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab?.customView?.findViewById<MaterialTextView>(R.id.tabTV)?.apply {
                        setTextColor(ContextCompat.getColor(activityContext, R.color.defaultColor))
                        setTypeface(typeface, Typeface.BOLD)
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    tab?.customView?.findViewById<MaterialTextView>(R.id.tabTV)?.apply {
                        setTextColor(ContextCompat.getColor(activityContext, R.color.tabLayoutUnSelectedTabTextColor))
                        setTypeface(typeface, Typeface.NORMAL)
                    }
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
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
                        internetController.isInternetConnected
                    )
                    dashBoardActivityDrawerLayout.closeDrawer(GravityCompat.START)
                }

                R.id.checkUpdateOuterLayout -> {
                    openAppInPlayStore(activityContext, BuildConfig.APPLICATION_ID)
                    dashBoardActivityDrawerLayout.closeDrawer(GravityCompat.START)
                }

                R.id.searchIV -> {
                    toolbarGroup.changeVisibility(Visibility.GONE.ordinal)
                    searchIV.changeVisibility(Visibility.GONE.ordinal)
                    searchLayout.changeVisibility(Visibility.VISIBLE.ordinal)
                    softKeyboardVisibilityController.showSoftKeyboard()
                    searchET.requestFocus()
                }

                R.id.searchCrossIV -> {
                    searchET.text = null
                    toolbarGroup.changeVisibility(Visibility.VISIBLE.ordinal)
                    searchLayout.changeVisibility(Visibility.GONE.ordinal)
                    val currentlyLoadedFragmentInVP = supportFragmentManager.findFragmentByTag("f${binding.dashBoardViewPager.currentItem}") as AllTasksFragment
                    if (currentlyLoadedFragmentInVP.getTasksListSize() == 0) {
                        searchIV.changeVisibility(Visibility.GONE.ordinal)
                    } else {
                        searchIV.changeVisibility(Visibility.VISIBLE.ordinal)
                    }
                    softKeyboardVisibilityController.hideSoftKeyboard(view)
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
            signOutAlertDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            signOutAlertDialog.window?.setWindowAnimations(R.style.dialogBoxesAnimation)
            signOutAlertDialog.show()
        }

        if (binding.dashBoardViewPager.currentItem == 0) {
            startAndStopFABAnimationListener.startAndStopFABAnimation(0)
        }

        with(signOutDialogLayoutBinding) {
            signOutIV.startAnimation(animation)

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
                startActivity(Intent(activityContext, SignInActivity::class.java))
                finish()
            }
        }
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
            exitFromAnAppAlertDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            exitFromAnAppAlertDialog.window?.setWindowAnimations(R.style.dialogBoxesAnimation)
            exitFromAnAppAlertDialog.show()
        }

        if (binding.dashBoardViewPager.currentItem == 0) {
            startAndStopFABAnimationListener.startAndStopFABAnimation(0)
        }

        with(exitFromAnAppDialogLayoutBinding) {
            exitFromAnAppIV.startAnimation(animation)

            noButton.setOnClickListener { _: View ->
                if (binding.dashBoardViewPager.currentItem == 0) {
                    startAndStopFABAnimationListener.startAndStopFABAnimation(1)
                }
                if (!activityContext.isFinishing && !activityContext.isDestroyed) {
                    exitFromAnAppAlertDialog.dismiss()
                }
            }

            yesButton.setOnClickListener { _: View ->
                startActivity(Intent(activityContext, ThankYouActivity::class.java))
                finish()
                if (!activityContext.isFinishing && !activityContext.isDestroyed) {
                    exitFromAnAppAlertDialog.dismiss()
                }
            }
        }
    }

    override fun isShowSearchViewORNot(isShow: Boolean) {
        with(binding) {
            searchIV.changeVisibility(if (isShow) {
                if (searchLayout.isVisible) {
                    Visibility.GONE.ordinal
                } else {
                    Visibility.VISIBLE.ordinal
                }
            } else {
                Visibility.GONE.ordinal
            })
        }
    }
}