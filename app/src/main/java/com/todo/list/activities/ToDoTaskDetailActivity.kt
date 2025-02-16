package com.todo.list.activities

import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import com.todo.list.R
import com.todo.list.adsPlugin.bannerAd.BannerAdController
import com.todo.list.base.BaseActivity
import com.todo.list.databinding.ActivityToDoTaskDetailBinding
import com.todo.list.db.ToDoTask
import com.todo.list.utils.CommonFunctions.keepActivityOn
import java.util.Locale

class ToDoTaskDetailActivity : BaseActivity(), View.OnClickListener {

    private val binding by lazy {
        ActivityToDoTaskDetailBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        keepActivityOn(activityContext)
        val toDoTask = intent.getSerializableExtra("taskDetail") as ToDoTask?

        with(binding) {
            BannerAdController.loadAndShowBannerAd(
                activity = activityContext,
                containerLayout = adLayout,
                loadingLayout = adLoadingInclude.rootLayout,
                isInternetConnected = isInternetConnectedORNot((getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager)),
                adID = getString(R.string.detailScreenBannerAdId)
            )
            if (toDoTask != null) {
                toolbarTV.text = toDoTask.title
                titleTV.text = toDoTask.title
                descriptionTV.text = toDoTask.description
                dateAndDayTV.text = String.format(Locale.getDefault(), "%s, %s %s, %s",
                    toDoTask.day, toDoTask.month, toDoTask.date, toDoTask.year)
                timeTV.text = toDoTask.time
            }
            titleTV.textSize = prefs.textSizeValue.toFloat()
            descriptionTV.textSize = prefs.textSizeValue.toFloat()
            dateAndDayTV.textSize = prefs.textSizeValue.toFloat()
            timeTV.textSize = prefs.textSizeValue.toFloat()
            backArrowIV.setOnClickListener(this@ToDoTaskDetailActivity)
        }

        val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goBackToDashBoardActivity()
            }
        }
        onBackPressedDispatcher.addCallback(onBackPressedCallback)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.backArrowIV -> goBackToDashBoardActivity()
        }
    }

    private fun goBackToDashBoardActivity() = finish()

    //    Override 'onConfigurationChanged' Method, Which Is Used To Prevent An Activity To 'Re-create' When
    //    Changing The Screen Orientation.i.e., Switching Between 'PORTRAIT MODE' TO 'LANDSCAPE MODE' & Vice Versa.
    /*override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }*/
}