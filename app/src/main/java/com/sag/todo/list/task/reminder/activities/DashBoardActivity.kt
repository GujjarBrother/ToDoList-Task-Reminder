package com.sag.todo.list.task.reminder.activities

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.Animation
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textview.MaterialTextView
import com.sag.todo.list.task.reminder.BuildConfig
import com.sag.todo.list.task.reminder.R
import com.sag.todo.list.task.reminder.adapters.NavigationDrawerRVAdapter
import com.sag.todo.list.task.reminder.adapters.ViewPagerAdapter
import com.sag.todo.list.task.reminder.base.BaseActivity
import com.sag.todo.list.task.reminder.databinding.ActivityDashBoardBinding
import com.sag.todo.list.task.reminder.databinding.CustomTabBinding
import com.sag.todo.list.task.reminder.databinding.ExitFromAnAppDialogLayoutBinding
import com.sag.todo.list.task.reminder.databinding.SignOutDialogLayoutBinding
import com.sag.todo.list.task.reminder.enums.StartStopFAB
import com.sag.todo.list.task.reminder.enums.Tabs
import com.sag.todo.list.task.reminder.enums.Visibility
import com.sag.todo.list.task.reminder.fragments.AllTasksFragment
import com.sag.todo.list.task.reminder.listeners.AdaptersListener
import com.sag.todo.list.task.reminder.listeners.SearchViewVisibilityListener
import com.sag.todo.list.task.reminder.listeners.StartAndStopFABAnimationListener
import com.sag.todo.list.task.reminder.models.NavigationDrawer
import com.sag.todo.list.task.reminder.utils.AppConstants.changeAppMode
import com.sag.todo.list.task.reminder.utils.AppConstants.changeVisibility
import com.sag.todo.list.task.reminder.utils.AppConstants.getColorResource
import com.sag.todo.list.task.reminder.utils.AppConstants.keepActivityOn
import com.sag.todo.list.task.reminder.utils.AppConstants.openAppInPlayStore
import com.sag.todo.list.task.reminder.utils.AppConstants.openGoogleAppStore
import com.sag.todo.list.task.reminder.utils.AppConstants.openPrivacyPolicyActivity
import com.sag.todo.list.task.reminder.utils.AppConstants.showExplainingWhyNotificationPermissionIsRequiredDialog
import com.sag.todo.list.task.reminder.utils.FabRateUsAndApplyAnimation
import com.sag.todo.list.task.reminder.utils.controllers.PermissionsController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DashBoardActivity : BaseActivity(), View.OnClickListener, SearchViewVisibilityListener {

    private val binding by lazy {
        ActivityDashBoardBinding.inflate(layoutInflater)
    }
    @Inject
    lateinit var alarmManager: AlarmManager
    @Inject
    @FabRateUsAndApplyAnimation
    lateinit var animation: Animation
    private lateinit var startAndStopFABAnimationListener: StartAndStopFABAnimationListener
    override val isApplyEdgeToEdgeForDashBoardActivity = false
    private val postNotificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (!it) {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                showExplainingWhyNotificationPermissionIsRequiredDialog(activityContext, true) {
                    openAppSettings()
                }
            }
        }
    }
    private val navigationDrawerRVAdapter by lazy {
        NavigationDrawerRVAdapter(object : AdaptersListener<NavigationDrawer, Int, Int> {
            override fun itemClicked(
                item: NavigationDrawer?, currentPosition: Int?, previousPosition: Int?, isSwitchChecked: Boolean?
            ) {
                when (currentPosition) {
                    0 -> {
                        val isDarkMode = isSwitchChecked ?: false
                        changeAppMode(isDarkMode)
                        prefs.isDarkModeEnable = isDarkMode
                        recreate()
                    }
                    1 -> openSettingsActivity()
                    2 -> openGoogleAppStore(activityContext)
                    3 -> openPrivacyPolicyActivity(activityContext, internetController.isInternetConnected, toastController)
                    4 -> openAppInPlayStore(activityContext, BuildConfig.APPLICATION_ID)
                }
                binding.dashBoardActivityDrawerLayout.closeDrawer(GravityCompat.START)
            }
        })
    }
    private val navigationDrawerList by lazy {
        mutableListOf(
            NavigationDrawer(
                heading = getString(com.example.core.R.string.features_text),
                image = R.drawable.sun_image,
                title = getString(com.example.core.R.string.light_mode_text),
                subTitle = getString(com.example.core.R.string.switch_between_light_dark_mode_text),
                isSwitch = true
            ), NavigationDrawer(
                heading = getString(com.example.core.R.string.general_settings_text),
                image = R.drawable.settings_icon,
                title = getString(com.example.core.R.string.setting_s_text),
                subTitle = getString(com.example.core.R.string.see_the_required_settings_text)
            ), NavigationDrawer(
                image = R.drawable.visit_our_app_store_image,
                title = getString(com.example.core.R.string.visit_our_app_store_text),
                subTitle = getString(com.example.core.R.string.check_our_more_app_s_on_play_store_text)
            ), NavigationDrawer(
                image = R.drawable.privacy_policy_image,
                title = getString(com.example.core.R.string.privacy_policy_text),
                subTitle = getString(com.example.core.R.string.read_our_privacy_policy_text)
            ), NavigationDrawer(
                image = R.drawable.check_update_image,
                title = getString(com.example.core.R.string.check_update_text),
                subTitle = String.format("%s%s", "v", BuildConfig.VERSION_NAME)
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val defaultColor = this.getColorResource(R.color.defaultColor)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(defaultColor),
            navigationBarStyle = SystemBarStyle.dark(defaultColor)
        )

        lifecycle.addObserver(
            PermissionsController(
                context = activityContext,
                postNotificationPermissionLauncher = postNotificationPermissionLauncher,
                alarmManager = alarmManager
            )
        )

        binding.apply {
            navigationDrawerList.forEach {
                if (it.isSwitch) {
                    it.isSwitchChecked = prefs.isDarkModeEnable
                    it.image =
                        if (it.isSwitchChecked) R.drawable.moon_image else R.drawable.sun_image
                    it.title =
                        if (it.isSwitchChecked) getString(com.example.core.R.string.dark_mode_text) else getString(com.example.core.R.string.light_mode_text)
                }
            }
            navigationDrawerInclude.navigationDrawerRV.layoutManager = LinearLayoutManager(activityContext, VERTICAL, false)
            navigationDrawerRVAdapter.submitList(navigationDrawerList)
            navigationDrawerInclude.navigationDrawerRV.adapter = navigationDrawerRVAdapter

            keepActivityOn(activityContext)

            val actionBarDrawerToggle = ActionBarDrawerToggle(activityContext, dashBoardActivityDrawerLayout,
                com.example.core.R.string.navigation_drawer_open_text, com.example.core.R.string.navigation_drawer_close_text)
            dashBoardActivityDrawerLayout.addDrawerListener(actionBarDrawerToggle)
            actionBarDrawerToggle.syncState()

            val viewPagerAdapter = ViewPagerAdapter(activityContext)
            dashBoardViewPager.adapter = viewPagerAdapter
            TabLayoutMediator(tabLayout, dashBoardViewPager) {
                tab: TabLayout.Tab, position: Int ->
                val customTabBinding = CustomTabBinding.inflate(layoutInflater)
                customTabBinding.apply {
                    tabTV.text = if (position == Tabs.TASKS_TAB.ordinal)
                        getString(com.example.core.R.string.tasks_text)
                    else
                        getString(com.example.core.R.string.completed_text)
                    tabTV.setTextColor(activityContext.getColorResource(R.color.tabLayoutUnSelectedTabTextColor))
                    tab.customView = root
                }
            }.attach()

            tabLayout.getTabAt(0)?.customView?.findViewById<TextView>(R.id.tabTV)?.apply {
                setTextColor(activityContext.getColorResource(R.color.defaultColor))
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
                        setTextColor(activityContext.getColorResource(R.color.defaultColor))
                        setTypeface(typeface, Typeface.BOLD)
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    tab?.customView?.findViewById<MaterialTextView>(R.id.tabTV)?.apply {
                        setTextColor(activityContext.getColorResource(R.color.tabLayoutUnSelectedTabTextColor))
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
            dashBoardActivityDrawerLayout.setScrimColor(activityContext.getColorResource(R.color.navigationDrawerScrimColor))
        }
    }

    private fun ActivityDashBoardBinding.manageTabsScrolling(position: Int) {
        if (position == 0) {
            toolbarTV.text = getString(com.example.core.R.string.tasks_text)
            startAndStopFABAnimationListener.startAndStopFABAnimation(StartStopFAB.START)
        } else if (position == 1)
            toolbarTV.text = getString(com.example.core.R.string.completed_text)
        dashBoardViewPager.setCurrentItem(position, true)
    }

    override fun onClick(view: View?) {
        binding.apply {
            when (view?.id) {
                R.id.signOutIV -> showSignOutDialog()
                R.id.settingsIV -> openSettingsActivity()
                R.id.openAndCloseDrawerIV -> dashBoardActivityDrawerLayout.openDrawer(GravityCompat.START)
                R.id.searchIV -> {
                    toolbarGroup.changeVisibility(Visibility.GONE)
                    searchIV.changeVisibility(Visibility.GONE)
                    searchLayout.changeVisibility(Visibility.VISIBLE)
                    softKeyboardVisibilityController.showSoftKeyboard()
                    searchET.requestFocus()
                }
                R.id.searchCrossIV -> {
                    searchET.text = null
                    toolbarGroup.changeVisibility(Visibility.VISIBLE)
                    searchLayout.changeVisibility(Visibility.GONE)
                    val currentlyLoadedFragmentInVP = supportFragmentManager.findFragmentByTag("f${binding.dashBoardViewPager.currentItem}") as AllTasksFragment
                    searchIV.changeVisibility(
                        if (currentlyLoadedFragmentInVP.getTasksListSize() == 0) Visibility.GONE else Visibility.VISIBLE
                    )
                    softKeyboardVisibilityController.hideSoftKeyboard(view)
                }
                else -> {}
            }
        }
    }

    private fun showSignOutDialog() {
        val signOutDialogLayoutBinding = SignOutDialogLayoutBinding.inflate(layoutInflater)

        val signOutDialogBuilder = AlertDialog.Builder(activityContext)
        signOutDialogBuilder.apply {
            setView(signOutDialogLayoutBinding.root)
            setCancelable(true)
            setOnDismissListener {
                if (binding.dashBoardViewPager.currentItem == 0) {
                    startAndStopFABAnimationListener.startAndStopFABAnimation(StartStopFAB.START)
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
            startAndStopFABAnimationListener.startAndStopFABAnimation(StartStopFAB.STOP)
        }

        signOutDialogLayoutBinding.apply {
            signOutIV.startAnimation(animation)

            noButton.setOnClickListener { _: View ->
                if (!activityContext.isFinishing && !activityContext.isDestroyed) {
                    signOutAlertDialog.dismiss()
                }
                if (binding.dashBoardViewPager.currentItem == 0) {
                    startAndStopFABAnimationListener.startAndStopFABAnimation(StartStopFAB.START)
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

    fun initializeStartAndStopFABAnimationListenerFromToDosFragment(
        startAndStopFABAnimationListener: StartAndStopFABAnimationListener
    ) {
        this.startAndStopFABAnimationListener = startAndStopFABAnimationListener
    }

    private fun openSettingsActivity() = startActivity(Intent(activityContext, SettingsActivity::class.java))

    private fun showExitDialog() {
        val exitFromAnAppDialogLayoutBinding = ExitFromAnAppDialogLayoutBinding.inflate(layoutInflater)

        val exitFromAnAppDialogBuilder = AlertDialog.Builder(activityContext)
        exitFromAnAppDialogBuilder.apply {
            setView(exitFromAnAppDialogLayoutBinding.root)
            setCancelable(true)
            setOnDismissListener {
                if (binding.dashBoardViewPager.currentItem == 0) {
                    startAndStopFABAnimationListener.startAndStopFABAnimation(StartStopFAB.START)
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
            startAndStopFABAnimationListener.startAndStopFABAnimation(StartStopFAB.STOP)
        }

        exitFromAnAppDialogLayoutBinding.apply {
            exitFromAnAppIV.startAnimation(animation)

            noButton.setOnClickListener { _: View ->
                if (binding.dashBoardViewPager.currentItem == 0) {
                    startAndStopFABAnimationListener.startAndStopFABAnimation(StartStopFAB.START)
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
        binding.apply {
            searchIV.changeVisibility(if (isShow) Visibility.VISIBLE else Visibility.GONE)
            if (searchLayout.isVisible) {
                searchLayout.changeVisibility(Visibility.GONE)
                toolbarGroup.changeVisibility(Visibility.VISIBLE)
            }
        }
    }

    private fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun handleActivitiesBackPressed() {
        binding.apply {
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
}