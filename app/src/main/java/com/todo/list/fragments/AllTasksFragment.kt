package com.todo.list.fragments

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.todo.list.R
import com.todo.list.activities.DashBoardActivity
import com.todo.list.activities.ToDoTaskDetailActivity
import com.todo.list.adapters.CategoryAdapter
import com.todo.list.adapters.TasksRecyclerViewAdapter
import com.todo.list.application.Application.Companion.prefs
import com.todo.list.application.Application.Companion.toDosDatabase
import com.todo.list.application.Application.Companion.typeface
import com.todo.list.base.BaseFragment
import com.todo.list.databinding.AddAndUpdateTasksDialogLayoutBinding
import com.todo.list.databinding.CustomPopupMenuLayoutBinding
import com.todo.list.databinding.DeleteTaskDialogLayoutBinding
import com.todo.list.databinding.FragmentAllTasksBinding
import com.todo.list.databinding.SortingDialogLayoutBinding
import com.todo.list.db.ToDoTask
import com.todo.list.listeners.CategorySelectionListener
import com.todo.list.listeners.StartAndStopFABAnimationAndSwitchBetweenLightAndDarkModeListener
import com.todo.list.listeners.TaskUpdateAndDeleteListener
import com.todo.list.listeners.ToDoTaskDetailListener
import com.todo.list.livedata.AllTasksListLiveData
import com.todo.list.livedata.CompletedTasksListLiveData
import com.todo.list.utils.CommonFunctions.DEFAULT_CATEGORY
import com.todo.list.utils.CommonFunctions.PERSONAL_CATEGORY
import com.todo.list.utils.CommonFunctions.WORK_CATEGORY
import com.todo.list.utils.CommonFunctions.applyAnimation
import com.todo.list.utils.CommonFunctions.isSomethingChanged
import es.dmoral.toasty.Toasty
import java.util.Calendar
import java.util.Collections

class AllTasksFragment : BaseFragment(), View.OnClickListener, ToDoTaskDetailListener,
    TaskUpdateAndDeleteListener, CategorySelectionListener {

    private lateinit var binding: FragmentAllTasksBinding
    private lateinit var completedTasksListLiveData: CompletedTasksListLiveData
    private lateinit var allTasksListLiveData: AllTasksListLiveData
    private var category = 0
    private lateinit var errorColorStateList: ColorStateList
    private var aboveTempValue = 1
    private var belowTempValue = 7
    private var aboveSortedValue = 1
    private var belowSortedValue = 7
    private lateinit var allTasksArrayList: ArrayList<ToDoTask>
    private var isAboveSortingValueSelected = false
    private var isBelowSortingValueSelected = false
    private lateinit var adapter: TasksRecyclerViewAdapter
    private var dateOfMonth: String = ""
    private var month: String = ""
    private var year: String = ""
    private lateinit var popupWindow: PopupWindow
    private lateinit var addAndUpdateTasksDialogLayoutBinding: AddAndUpdateTasksDialogLayoutBinding
    private lateinit var toDoTask: ToDoTask
    private var toUpdateAndDeleteItemPosition = 0
    private lateinit var updatedToDoTask: ToDoTask

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllTasksBinding.inflate(inflater, container, false)

//        Here, We Stop FAB Animation By Clicking SignOut ImageView From DashBoardActivity...
        (requireActivity() as DashBoardActivity).initializeStopFABAnimationFromToDosFragmentListener(
            object : StartAndStopFABAnimationAndSwitchBetweenLightAndDarkModeListener {
                override fun goAhead(startAndStopFABAnimation: Int, isLightAndDarkMode: Boolean,
                                     isFromNavigationDrawer: Boolean) {
                    if (startAndStopFABAnimation == 0) {
                        stopFABAnimation()
                    } else {
                        startFABAnimation()
                    }

                    if (isFromNavigationDrawer) {
                        if (isLightAndDarkMode) {
                            applyColorSchemeOrLightAndDarkModeOnAllTasksFragment()
                            if (::adapter.isInitialized) {
                                adapter.notifyDataSetChanged()
                            }
                        } else {
                            applyColorSchemeOrLightAndDarkModeOnAllTasksFragment()
                            if (::adapter.isInitialized) {
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }
        )

        startFABAnimation()

        with(binding) {
            if (prefs.allTasksStyleValue) {
                listAndGridViewStylesImageView.setImageDrawable(listViewStyleImage)
                listAndGridViewStylesTextView.text = getString(R.string.listview_text)
            } else {
                listAndGridViewStylesImageView.setImageDrawable(gridViewStyleImage)
                listAndGridViewStylesTextView.text = getString(R.string.gridview_text)
            }
            applyCustomFont()
            nothingInHereTextView.typeface = typeface
            addNewTasksFloatingActionButton.setOnClickListener(this@AllTasksFragment)
            sortingCardView.setOnClickListener(this@AllTasksFragment)
            stylesCardView.setOnClickListener(this@AllTasksFragment)
        }

        completedTasksListLiveData =
            ViewModelProvider(requireActivity())[CompletedTasksListLiveData::class.java]
        allTasksListLiveData =
            ViewModelProvider(requireActivity())[AllTasksListLiveData::class.java]
        allTasksArrayList = ArrayList()
        readAllTasks()

        return binding.root
    }

    private fun startFABAnimation() =
        binding.addNewTasksFloatingActionButton.startAnimation(applyAnimation(fragmentContext))

    private fun stopFABAnimation() =
        binding.addNewTasksFloatingActionButton.clearAnimation()

    override fun onResume() {
        super.onResume()

        applyColorSchemeOrLightAndDarkModeOnAllTasksFragment()

        if (isSomethingChanged) {
            if (::adapter.isInitialized) {
                adapter.isTextSizeChanged = true
                adapter.notifyDataSetChanged()
            }
        }

        allTasksListLiveData.mutableLiveData.observe(viewLifecycleOwner) { aBoolean: Boolean ->
            if (aBoolean) {
                readAllTasks()
            }
        }
    }

    private fun applyCustomFont() {
        with(binding) {
            listAndGridViewStylesTextView.typeface = typeface
            sortingTextView.typeface = typeface
        }
    }

    private fun applyColorSchemeOrLightAndDarkModeOnAllTasksFragment() {
        with(binding) {
            if (prefs.dayAndNightModeSwitchValue) {
                allTasksFragmentCardView.setCardBackgroundColor(screensNightModeColor)
                nothingInHereTextView.setTextColor(whiteColor)
                addNewTasksFloatingActionButton.backgroundTintList =
                    ColorStateList.valueOf(whiteColor)
                addNewTasksFloatingActionButton.setColorFilter(blackColor)
                sortingCardView.setCardBackgroundColor(cardsNightModeColor)
                sortingImageView.setColorFilter(whiteColor)
                sortingTextView.setTextColor(whiteColor)
                stylesCardView.setCardBackgroundColor(cardsNightModeColor)
                listAndGridViewStylesImageView.setColorFilter(whiteColor)
                listAndGridViewStylesTextView.setTextColor(whiteColor)
            } else {
                allTasksFragmentCardView.setCardBackgroundColor(fragmentsCardViewsColor)
                addNewTasksFloatingActionButton.setColorFilter(whiteColor)
                sortingCardView.setCardBackgroundColor(whiteColor)
                sortingTextView.setTextColor(blackColor)
                stylesCardView.setCardBackgroundColor(whiteColor)
                listAndGridViewStylesTextView.setTextColor(blackColor)
                when (prefs.colorSchemeValue) {
                    0 -> {
                        val defaultColorStateList = ColorStateList.valueOf(defaultColor)
                        errorColorStateList = defaultColorStateList
                        listAndGridViewStylesImageView.setColorFilter(defaultColor)
                        sortingImageView.setColorFilter(defaultColor)
                        nothingInHereTextView.setTextColor(defaultColor)
                        addNewTasksFloatingActionButton.backgroundTintList = defaultColorStateList
                    }

                    1 -> {
                        val darkYellowColorStateList = ColorStateList.valueOf(darkYellowColor)
                        errorColorStateList = darkYellowColorStateList
                        listAndGridViewStylesImageView.setColorFilter(darkYellowColor)
                        sortingImageView.setColorFilter(darkYellowColor)
                        nothingInHereTextView.setTextColor(darkYellowColor)
                        addNewTasksFloatingActionButton.backgroundTintList =
                            darkYellowColorStateList
                    }

                    2 -> {
                        val orangeColorStateList = ColorStateList.valueOf(orangeColor)
                        errorColorStateList = orangeColorStateList
                        listAndGridViewStylesImageView.setColorFilter(orangeColor)
                        sortingImageView.setColorFilter(orangeColor)
                        nothingInHereTextView.setTextColor(orangeColor)
                        addNewTasksFloatingActionButton.backgroundTintList = orangeColorStateList
                    }

                    3 -> {
                        val lightGreenColorStateList = ColorStateList.valueOf(lightGreenColor)
                        errorColorStateList = lightGreenColorStateList
                        listAndGridViewStylesImageView.setColorFilter(lightGreenColor)
                        sortingImageView.setColorFilter(lightGreenColor)
                        nothingInHereTextView.setTextColor(lightGreenColor)
                        addNewTasksFloatingActionButton.backgroundTintList =
                            lightGreenColorStateList
                    }

                    4 -> {
                        val blueColorStateList = ColorStateList.valueOf(blueColor)
                        errorColorStateList = blueColorStateList
                        listAndGridViewStylesImageView.setColorFilter(blueColor)
                        sortingImageView.setColorFilter(blueColor)
                        nothingInHereTextView.setTextColor(blueColor)
                        addNewTasksFloatingActionButton.backgroundTintList = blueColorStateList
                    }

                    5 -> {
                        val cyanColorStateList = ColorStateList.valueOf(cyanColor)
                        errorColorStateList = cyanColorStateList
                        listAndGridViewStylesImageView.setColorFilter(cyanColor)
                        sortingImageView.setColorFilter(cyanColor)
                        nothingInHereTextView.setTextColor(cyanColor)
                        addNewTasksFloatingActionButton.backgroundTintList = cyanColorStateList
                    }

                    6 -> {
                        val pinkColorStateList = ColorStateList.valueOf(pinkColor)
                        errorColorStateList = pinkColorStateList
                        listAndGridViewStylesImageView.setColorFilter(pinkColor)
                        sortingImageView.setColorFilter(pinkColor)
                        nothingInHereTextView.setTextColor(pinkColor)
                        addNewTasksFloatingActionButton.backgroundTintList = pinkColorStateList
                    }

                    7 -> {
                        val darkBlueColorStateList = ColorStateList.valueOf(darkBlueColor)
                        errorColorStateList = darkBlueColorStateList
                        listAndGridViewStylesImageView.setColorFilter(darkBlueColor)
                        sortingImageView.setColorFilter(darkBlueColor)
                        nothingInHereTextView.setTextColor(darkBlueColor)
                        addNewTasksFloatingActionButton.backgroundTintList = darkBlueColorStateList
                    }

                    8 -> {
                        val redColorStateList = ColorStateList.valueOf(redColor)
                        errorColorStateList = redColorStateList
                        listAndGridViewStylesImageView.setColorFilter(redColor)
                        sortingImageView.setColorFilter(redColor)
                        nothingInHereTextView.setTextColor(redColor)
                        addNewTasksFloatingActionButton.backgroundTintList = redColorStateList
                    }

                    9 -> {
                        val lightPurpleColorStateList = ColorStateList.valueOf(lightPurpleColor)
                        errorColorStateList = lightPurpleColorStateList
                        listAndGridViewStylesImageView.setColorFilter(lightPurpleColor)
                        sortingImageView.setColorFilter(lightPurpleColor)
                        nothingInHereTextView.setTextColor(lightPurpleColor)
                        addNewTasksFloatingActionButton.backgroundTintList =
                            lightPurpleColorStateList
                    }
                }
            }
        }
    }

    private fun readAllTasks() {
        with(binding) {
            allTasksArrayList.clear()
            allTasksArrayList = toDosDatabase.dao().getAllTasks() as ArrayList<ToDoTask>
            if (allTasksArrayList.size > 0) {
                group1.visibility = GONE
                group2.visibility = VISIBLE
                displayAllTasksOnRecyclerView()
            } else {
                group1.visibility = VISIBLE
                group2.visibility = GONE
            }
        }
    }

    private fun sortAnArrayList() {
        val toDosSortingArray = prefs.allTasksSortingValues
        aboveSortedValue = toDosSortingArray[0]
        belowSortedValue = toDosSortingArray[1]
        if (aboveSortedValue == 1) {
            if (belowSortedValue == 7) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    allTasksArrayList.sortWith(Comparator.comparing(ToDoTask::title))
                }
            } else if (belowSortedValue == 8) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    allTasksArrayList.sortWith(Collections.reverseOrder(Comparator.comparing(ToDoTask::title)))
                }
            }
        } else if (aboveSortedValue == 2) {
            if (belowSortedValue == 7) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    allTasksArrayList.sortWith(Comparator.comparing(ToDoTask::day))
                }
            } else if (belowSortedValue == 8) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    allTasksArrayList.sortWith(Collections.reverseOrder(Comparator.comparing(ToDoTask::day)))
                }
            }
        } else if (aboveSortedValue == 3) {
            if (belowSortedValue == 7) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    allTasksArrayList.sortWith(Comparator.comparing(ToDoTask::date))
                }
            } else if (belowSortedValue == 8) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    allTasksArrayList.sortWith(Collections.reverseOrder(Comparator.comparing(ToDoTask::date)))
                }
            }
        } else if (aboveSortedValue == 4) {
            if (belowSortedValue == 7) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    allTasksArrayList.sortWith(Comparator.comparing(ToDoTask::month))
                }
            } else if (belowSortedValue == 8) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    allTasksArrayList.sortWith(Collections.reverseOrder(Comparator.comparing(ToDoTask::month)))
                }
            }
        } else if (aboveSortedValue == 5) {
            if (belowSortedValue == 7) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    allTasksArrayList.sortWith(Comparator.comparing(ToDoTask::year))
                }
            } else if (belowSortedValue == 8) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    allTasksArrayList.sortWith(Collections.reverseOrder(Comparator.comparing(ToDoTask::year)))
                }
            }
        } else if (aboveSortedValue == 6) {
            if (belowSortedValue == 7) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    allTasksArrayList.sortWith(Comparator.comparing(ToDoTask::time))
                }
            } else if (belowSortedValue == 8) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    allTasksArrayList.sortWith(Collections.reverseOrder(Comparator.comparing(ToDoTask::time)))
                }
            }
        }
    }

    private fun displayAllTasksOnRecyclerView() {
        sortAnArrayList()

        val colorsSchemeArray = intArrayOf(
            defaultColor, darkYellowColor, orangeColor, lightGreenColor,
            blueColor, cyanColor, pinkColor, darkBlueColor, redColor, lightPurpleColor
        )

        if (!::adapter.isInitialized) {
            adapter = TasksRecyclerViewAdapter(
                this, this,
                colorsSchemeArray, true
            )
        }
        val layoutManager: RecyclerView.LayoutManager = if (prefs.allTasksStyleValue) {
            GridLayoutManager(fragmentContext, 2, GridLayoutManager.VERTICAL, false)
        } else {
            LinearLayoutManager(fragmentContext, LinearLayoutManager.VERTICAL, false)
        }

        with(binding) {
            allTasksRecyclerView.layoutManager = layoutManager
            if (::adapter.isInitialized) {
                allTasksRecyclerView.adapter = adapter
                adapter.submitList(allTasksArrayList)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.addNewTasksFloatingActionButton -> {
                showAddNewAndUpdateTaskDialog(1)
            }

            R.id.sortingCardView -> {
                showSortingDialog()
            }

            R.id.stylesCardView -> {
                with(binding) {
                    if (prefs.allTasksStyleValue) {
                        listAndGridViewStylesImageView.setImageDrawable(gridViewStyleImage)
                        listAndGridViewStylesTextView.setText(R.string.gridview_text)
                        prefs.allTasksStyleValue = false
                        displayAllTasksOnRecyclerView()
                    } else {
                        listAndGridViewStylesImageView.setImageDrawable(listViewStyleImage)
                        listAndGridViewStylesTextView.setText(R.string.listview_text)
                        prefs.allTasksStyleValue = true
                        displayAllTasksOnRecyclerView()
                    }
                }
            }
        }
    }

    private fun showAddNewAndUpdateTaskDialog(fromWhereInvoked: Int) {
        addAndUpdateTasksDialogLayoutBinding =
            AddAndUpdateTasksDialogLayoutBinding.inflate(layoutInflater)

        val addTasksDialogBuilder = AlertDialog.Builder(fragmentContext)
        addTasksDialogBuilder.setView(addAndUpdateTasksDialogLayoutBinding.root)
        addTasksDialogBuilder.setCancelable(false)
        val addTasksAlertDialog = addTasksDialogBuilder.create()

        if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed && !addTasksAlertDialog.isShowing) {
            addTasksAlertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            addTasksAlertDialog.window?.setWindowAnimations(R.style.dialogBoxesAnimation)
            addTasksAlertDialog.show()
        }

        with(addAndUpdateTasksDialogLayoutBinding) {
            stopFABAnimation()
            applyCustomFontOnAddAndUpdateTasksDialogViews(this)
            applyColorSchemeORLightAndDarkModeOnAddAndUpdateTasksDialogViews(this)
            if (fromWhereInvoked == 2) {
                addAndEditImageView.setImageResource(R.drawable.update_image)
                addAndUpdateToDoTaskTextView.text = getString(R.string.update_todo_task_text)
                infoTextView.text = getString(R.string.update_info_message_text)
                titleTextInputLayout.editText?.setText(toDoTask.title)
                descriptionTextInputLayout.editText?.setText(toDoTask.description)
                dayOfWeekTextInputLayout.editText?.setText(toDoTask.day)
                dateTextInputLayout.editText?.setText(
                    String.format("%s %s, %s", toDoTask.month, toDoTask.date, toDoTask.year)
                )
                timeTextInputLayout.editText?.setText(toDoTask.time)
                timeTextInputLayout.editText!!.setText(toDoTask.time)
                if (toDoTask.category == DEFAULT_CATEGORY || toDoTask.category == PERSONAL_CATEGORY) {
                    selectCategoryTextView.text = getString(R.string.personal_text)
                } else if (toDoTask.category == WORK_CATEGORY) {
                    selectCategoryTextView.text = getString(R.string.work_text)
                }
                selectCategoryTextView.setTextColor(blackColor)
                saveAndUpdateButton.text = getString(R.string.update_text)
            }

            selectCategoryLayout.setOnClickListener { view: View ->
                showCustomPopupForCategorySelection(view, fromWhereInvoked)
            }

            dismissDialogImageView.setOnClickListener { _: View? ->
                if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed) {
                    addTasksAlertDialog.dismiss()
                }
                startFABAnimation()
            }

            dateTextInputEditText.setOnClickListener { _: View? ->
                showMaterialDatePicker(addAndUpdateTasksDialogLayoutBinding)
            }

            timeTextInputEditText.setOnClickListener { _: View? ->
                showMaterialTimePicker(addAndUpdateTasksDialogLayoutBinding)
            }

            saveAndUpdateButton.setOnClickListener { _: View? ->
                val title = titleTextInputLayout.editText?.text.toString().trim()
                val description = descriptionTextInputLayout.editText?.text.toString().trim()
                val dayOfWeek: String = dayOfWeekTextInputLayout.editText?.text.toString().trim()
                val date = dateTextInputLayout.editText?.text.toString().trim()
                val time = timeTextInputLayout.editText?.text.toString().trim()
                val whiteColorStateList = ColorStateList.valueOf(whiteColor)
                if (TextUtils.isEmpty(title)) {
                    if (prefs.dayAndNightModeSwitchValue) {
                        titleTextInputLayout.boxStrokeErrorColor = whiteColorStateList
                        titleTextInputLayout.setErrorIconTintList(whiteColorStateList)
                        titleTextInputLayout.setErrorTextColor(whiteColorStateList)
                        titleTextInputLayout.error = getString(R.string.title_error_text)
                    } else {
                        titleTextInputLayout.boxStrokeErrorColor = errorColorStateList
                        titleTextInputLayout.setErrorIconTintList(errorColorStateList)
                        titleTextInputLayout.setErrorTextColor(errorColorStateList)
                        titleTextInputLayout.error = getString(R.string.title_error_text)
                    }
                } else if (TextUtils.isEmpty(description)) {
                    if (prefs.dayAndNightModeSwitchValue) {
                        titleTextInputLayout.error = null
                        descriptionTextInputLayout.boxStrokeErrorColor = whiteColorStateList
                        descriptionTextInputLayout.setErrorIconTintList(whiteColorStateList)
                        descriptionTextInputLayout.setErrorTextColor(whiteColorStateList)
                        descriptionTextInputLayout.error =
                            getString(R.string.description_error_text)
                    } else {
                        titleTextInputLayout.error = null
                        descriptionTextInputLayout.boxStrokeErrorColor = errorColorStateList
                        descriptionTextInputLayout.setErrorIconTintList(errorColorStateList)
                        descriptionTextInputLayout.setErrorTextColor(errorColorStateList)
                        descriptionTextInputLayout.error =
                            getString(R.string.description_error_text)
                    }
                } else if (TextUtils.isEmpty(dayOfWeek)) {
                    if (prefs.dayAndNightModeSwitchValue) {
                        descriptionTextInputLayout.error = null
                        dayOfWeekTextInputLayout.boxStrokeErrorColor = whiteColorStateList
                        dayOfWeekTextInputLayout.setErrorIconTintList(whiteColorStateList)
                        dayOfWeekTextInputLayout.setErrorTextColor(whiteColorStateList)
                        dayOfWeekTextInputLayout.error = getString(R.string.day_of_week_error_text)
                    } else {
                        descriptionTextInputLayout.error = null
                        dayOfWeekTextInputLayout.boxStrokeErrorColor = errorColorStateList
                        dayOfWeekTextInputLayout.setErrorIconTintList(errorColorStateList)
                        dayOfWeekTextInputLayout.setErrorTextColor(errorColorStateList)
                        dayOfWeekTextInputLayout.error = getString(R.string.day_of_week_error_text)
                    }
                } else if (TextUtils.isEmpty(date)) {
                    if (prefs.dayAndNightModeSwitchValue) {
                        dayOfWeekTextInputLayout.error = null
                        dateTextInputLayout.boxStrokeErrorColor = whiteColorStateList
                        dateTextInputLayout.setErrorIconTintList(whiteColorStateList)
                        dateTextInputLayout.setErrorTextColor(whiteColorStateList)
                        dateTextInputLayout.error = getString(R.string.select_date_error_text)
                    } else {
                        dayOfWeekTextInputLayout.error = null
                        dateTextInputLayout.boxStrokeErrorColor = errorColorStateList
                        dateTextInputLayout.setErrorIconTintList(errorColorStateList)
                        dateTextInputLayout.setErrorTextColor(errorColorStateList)
                        dateTextInputLayout.error = getString(R.string.select_date_error_text)
                    }
                } else if (TextUtils.isEmpty(time)) {
                    if (prefs.dayAndNightModeSwitchValue) {
                        dateTextInputLayout.error = null
                        timeTextInputLayout.boxStrokeErrorColor = whiteColorStateList
                        timeTextInputLayout.setErrorIconTintList(whiteColorStateList)
                        timeTextInputLayout.setErrorTextColor(whiteColorStateList)
                        timeTextInputLayout.error = getString(R.string.select_time_error_text)
                    } else {
                        dateTextInputLayout.error = null
                        timeTextInputLayout.boxStrokeErrorColor = errorColorStateList
                        timeTextInputLayout.setErrorIconTintList(errorColorStateList)
                        timeTextInputLayout.setErrorTextColor(errorColorStateList)
                        timeTextInputLayout.error = getString(R.string.select_time_error_text)
                    }
                } else {
                    titleTextInputLayout.error = null
                    descriptionTextInputLayout.error = null
                    dayOfWeekTextInputLayout.error = null
                    dateTextInputLayout.error = null
                    timeTextInputLayout.error = null
                    if (fromWhereInvoked == 1) {
                        val toDoTask: ToDoTask
                        if (dateOfMonth.isNotEmpty() && month.isNotEmpty() && year.isNotEmpty()) {
                            toDoTask = ToDoTask(0, dayOfWeek, dateOfMonth, month, year, title, description,
                                time, category)
                            val isTaskAlreadySaved = toDosDatabase.dao().isTaskAlreadySaved(
                                toDoTask.day,
                                toDoTask.date,
                                toDoTask.month,
                                toDoTask.year,
                                toDoTask.title,
                                toDoTask.description,
                                toDoTask.time,
                                toDoTask.category
                            )
                            if (isTaskAlreadySaved >= 1) {
                                Toasty.info(fragmentContext,
                                    getString(R.string.this_task_is_already_saved_toast_text), Toasty.LENGTH_LONG)
                                    .show()
                            } else {
                                val newlyAddedTaskID = toDosDatabase.dao().saveTask(toDoTask)
                                if (newlyAddedTaskID >= 1) {
                                    prefs.category = category

                                    val searchingIndex = allTasksArrayList.binarySearch(
                                        toDoTask, compareBy(
                                            { it.title },
                                            { it.day },
                                            { it.date },
                                            { it.month },
                                            { it.year },
                                            { it.time })
                                    )
                                    val insertionIndex = if (searchingIndex < 0) {
                                        -(searchingIndex + 1)
                                    } else {
                                        searchingIndex
                                    }

                                    toDoTask.id = newlyAddedTaskID.toInt()
                                    allTasksArrayList.add(insertionIndex, toDoTask)
                                    adapter.notifyItemInserted(insertionIndex)
                                    binding.allTasksRecyclerView.smoothScrollToPosition(insertionIndex)

                                    dateOfMonth = ""
                                    month = ""
                                    year = ""
                                    Toasty.success(requireContext(), R.string.task_is_saved_successfully_toast_text,
                                        Toasty.LENGTH_LONG).show()
                                    titleTextInputLayout.editText?.text = null
                                    descriptionTextInputLayout.editText?.text = null
                                    dayOfWeekTextInputLayout.editText?.text = null
                                    dateTextInputLayout.editText?.text = null
                                    timeTextInputLayout.editText?.text = null
                                    titleTextInputLayout.editText?.requestFocus()
                                    category = 0
                                    if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed) {
                                        addTasksAlertDialog.dismiss()
                                    }
                                    startFABAnimation()
                                } else {
                                    Toasty.error(requireContext(), R.string.task_is_not_saved_successfully_toast_text,
                                        Toasty.LENGTH_LONG).show()
                                }
                            }
                        }
                    } else if (fromWhereInvoked == 2) {
                        if (!((title.equals(toDoTask.title, ignoreCase = true)
                                    && description.equals(toDoTask.description, ignoreCase = true)
                                    && dayOfWeek.equals(toDoTask.day, ignoreCase = true)
                                    && month.equals(toDoTask.month, ignoreCase = true)
                                    && dateOfMonth.equals(toDoTask.date, ignoreCase = true)
                                    && year.equals(toDoTask.year, ignoreCase = true)
                                    && time.equals(toDoTask.time, ignoreCase = true)
                                    && (category == toDoTask.category)))
                            ) {
                            if ((dateOfMonth.equals("", ignoreCase = true)
                                        && month.equals("", ignoreCase = true)
                                        && year.equals("", ignoreCase = true))
                            ) {
                                dateOfMonth = toDoTask.date
                                month = toDoTask.month
                                year = toDoTask.year
                            }
                            updatedToDoTask = ToDoTask(
                                toDoTask.id, dayOfWeek, dateOfMonth, month, year,
                                title, description, time, category
                            )
                            val isUpdated = toDosDatabase.dao().updateTask(updatedToDoTask)
                            if (isUpdated == 1) {
                                dateOfMonth = ""
                                month = ""
                                year = ""
                                Toasty.success(fragmentContext, R.string.updated_successfully_toast_text,
                                    Toasty.LENGTH_LONG).show()
                                prefs.category = category

                                allTasksArrayList[toUpdateAndDeleteItemPosition] = updatedToDoTask
                                adapter.notifyItemChanged(toUpdateAndDeleteItemPosition)
                                sortAnArrayList()

                                if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed) {
                                    addTasksAlertDialog.dismiss()
                                }
                                startFABAnimation()
                                category = 0
                            } else {
                                Toasty.success(fragmentContext, R.string.not_updated_successfully_toast_text,
                                    Toasty.LENGTH_LONG).show()
                            }
                        } else if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed) {
                            addTasksAlertDialog.dismiss()
                        }
                    }
                }
            }
        }
    }

    private fun showCustomPopupForCategorySelection(view: View, fromWhereInvoked: Int) {
        val customPopupMenuLayoutBinding = CustomPopupMenuLayoutBinding.inflate(layoutInflater)

        if (prefs.dayAndNightModeSwitchValue) {
            customPopupMenuLayoutBinding.root.setCardBackgroundColor(screensNightModeColor)
        }

        popupWindow = PopupWindow(
            customPopupMenuLayoutBinding.root,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.isOutsideTouchable = true
        popupWindow.elevation = 5f
        val categoryArrayList = ArrayList<Int>()
        with(categoryArrayList) {
            add(DEFAULT_CATEGORY)
            add(PERSONAL_CATEGORY)
            add(WORK_CATEGORY)
            if (fromWhereInvoked == 2) {
                removeAt(0)
            }
        }

        val categoryAdapter = CategoryAdapter(this, "Category")
        customPopupMenuLayoutBinding.customPopUpMenuRecyclerView.adapter = categoryAdapter
        categoryAdapter.submitList(categoryArrayList)
        popupWindow.showAsDropDown(view)
    }

    private fun showSortingDialog() {
        val sortingDialogLayoutBinding = SortingDialogLayoutBinding.inflate(layoutInflater)

        val sortingDialogBuilder = AlertDialog.Builder(fragmentContext)
        sortingDialogBuilder.setView(sortingDialogLayoutBinding.root)
        sortingDialogBuilder.setCancelable(false)
        val sortingAlertDialog = sortingDialogBuilder.create()
        if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed && !sortingAlertDialog.isShowing) {
            sortingAlertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            sortingAlertDialog.window?.setWindowAnimations(R.style.dialogBoxesAnimation)
            sortingAlertDialog.show()
        }

        with(sortingDialogLayoutBinding) {
            stopFABAnimation()
            applyCustomFontOnSortingDialogViews(this)
            applyColorSchemeORLightAndDarkModeOnSortingDialogViews(this)

            val allTasksSortingArray = prefs.allTasksSortingValues
            aboveSortedValue = allTasksSortingArray[0]
            belowSortedValue = allTasksSortingArray[1]

            if (aboveSortedValue == 1) {
                titleRadioButton.isChecked = true
                if (belowSortedValue == 7) {
                    ascendingAToZRadioButton.isChecked = true
                } else if (belowSortedValue == 8) {
                    descendingZToARadioButton.isChecked = true
                }
            } else if (aboveSortedValue == 2) {
                dayOfWeekRadioButton.isChecked = true
                if (belowSortedValue == 7) {
                    ascendingAToZRadioButton.isChecked = true
                } else if (belowSortedValue == 8) {
                    descendingZToARadioButton.isChecked = true
                }
            } else if (aboveSortedValue == 3) {
                dateRadioButton.isChecked = true
                if (belowSortedValue == 7) {
                    ascendingAToZRadioButton.isChecked = true
                } else if (belowSortedValue == 8) {
                    descendingZToARadioButton.isChecked = true
                }
            } else if (aboveSortedValue == 4) {
                monthRadioButton.isChecked = true
                if (belowSortedValue == 7) {
                    ascendingAToZRadioButton.isChecked = true
                } else if (belowSortedValue == 8) {
                    descendingZToARadioButton.isChecked = true
                }
            } else if (aboveSortedValue == 5) {
                yearRadioButton.isChecked = true
                if (belowSortedValue == 7) {
                    ascendingAToZRadioButton.isChecked = true
                } else if (belowSortedValue == 8) {
                    descendingZToARadioButton.isChecked = true
                }
            } else if (aboveSortedValue == 6) {
                timeRadioButton.isChecked = true
                if (belowSortedValue == 7) {
                    ascendingAToZRadioButton.isChecked = true
                } else if (belowSortedValue == 8) {
                    descendingZToARadioButton.isChecked = true
                }
            }

            cancelButton.setOnClickListener { _: View? ->
                isAboveSortingValueSelected = false
                isBelowSortingValueSelected = false
                if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed) {
                    sortingAlertDialog.dismiss()
                }
                startFABAnimation()
            }

            sortRadioGroup.setOnCheckedChangeListener { _: RadioGroup?, checkedId: Int ->
                isAboveSortingValueSelected = true
                when (checkedId) {
                    R.id.title_radio_button -> {
                        aboveTempValue = 1
                    }

                    R.id.day_of_week_radio_button -> {
                        aboveTempValue = 2
                    }

                    R.id.date_radio_button -> {
                        aboveTempValue = 3
                    }

                    R.id.month_radio_button -> {
                        aboveTempValue = 4
                    }

                    R.id.year_radio_button -> {
                        aboveTempValue = 5
                    }

                    R.id.time_radio_button -> {
                        aboveTempValue = 6
                    }
                }

                if (aboveTempValue == aboveSortedValue) {
                    if (belowSortedValue == 7) {
                        ascendingAToZRadioButton.isChecked = true
                    } else if (belowSortedValue == 8) {
                        descendingZToARadioButton.isChecked = true
                    }
                } else {
                    ascendingDescendingRadioGroup.clearCheck()
                }
            }

            ascendingDescendingRadioGroup.setOnCheckedChangeListener { _: RadioGroup?, checkedId: Int ->
                isBelowSortingValueSelected = true
                if (aboveTempValue == 1) {
                    if (checkedId == R.id.ascending_a_to_z_radio_button) {
                        belowTempValue = 7
                    } else if (checkedId == R.id.descending_z_to_a_radio_button) {
                        belowTempValue = 8
                    }
                } else if (aboveTempValue == 2) {
                    if (checkedId == R.id.ascending_a_to_z_radio_button) {
                        belowTempValue = 7
                    } else if (checkedId == R.id.descending_z_to_a_radio_button) {
                        belowTempValue = 8
                    }
                } else if (aboveTempValue == 3) {
                    if (checkedId == R.id.ascending_a_to_z_radio_button) {
                        belowTempValue = 7
                    } else if (checkedId == R.id.descending_z_to_a_radio_button) {
                        belowTempValue = 8
                    }
                } else if (aboveTempValue == 4) {
                    if (checkedId == R.id.ascending_a_to_z_radio_button) {
                        belowTempValue = 7
                    } else if (checkedId == R.id.descending_z_to_a_radio_button) {
                        belowTempValue = 8
                    }
                } else if (aboveTempValue == 5) {
                    if (checkedId == R.id.ascending_a_to_z_radio_button) {
                        belowTempValue = 7
                    } else if (checkedId == R.id.descending_z_to_a_radio_button) {
                        belowTempValue = 8
                    }
                } else if (aboveTempValue == 6) {
                    if (checkedId == R.id.ascending_a_to_z_radio_button) {
                        belowTempValue = 7
                    } else if (checkedId == R.id.descending_z_to_a_radio_button) {
                        belowTempValue = 8
                    }
                }
            }

            sortButton.setOnClickListener { _: View? ->
                sortRecyclerViewAdapterList()
                if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed) {
                    sortingAlertDialog.dismiss()
                }
            }
        }
    }

    private fun sortRecyclerViewAdapterList() {
        if (isAboveSortingValueSelected) {
            aboveSortedValue = aboveTempValue
        }

        if (isBelowSortingValueSelected) {
            belowSortedValue = belowTempValue
        }

        prefs.saveAllTasksSortingValues(aboveSortedValue, belowSortedValue)
        displayAllTasksOnRecyclerView()
        startFABAnimation()
    }

    private fun applyColorSchemeORLightAndDarkModeOnSortingDialogViews(
        sortingDialogLayoutBinding: SortingDialogLayoutBinding
    ) {
        with(sortingDialogLayoutBinding) {
            if (prefs.dayAndNightModeSwitchValue) {
                sortingDialogRootLayout.setBackgroundResource(dialogBoxesDarkModeBackground)
                sortByTextView.setTextColor(whiteColor)
                titleRadioButton.buttonTintList = ColorStateList.valueOf(whiteColor)
                titleRadioButton.setTextColor(whiteColor)
                dayOfWeekRadioButton.buttonTintList = ColorStateList.valueOf(whiteColor)
                dayOfWeekRadioButton.setTextColor(whiteColor)
                dateRadioButton.buttonTintList = ColorStateList.valueOf(whiteColor)
                dateRadioButton.setTextColor(whiteColor)
                monthRadioButton.buttonTintList = ColorStateList.valueOf(whiteColor)
                monthRadioButton.setTextColor(whiteColor)
                yearRadioButton.buttonTintList = ColorStateList.valueOf(whiteColor)
                yearRadioButton.setTextColor(whiteColor)
                timeRadioButton.buttonTintList = ColorStateList.valueOf(whiteColor)
                timeRadioButton.setTextColor(whiteColor)
                ascendingAToZRadioButton.buttonTintList = ColorStateList.valueOf(whiteColor)
                ascendingAToZRadioButton.setTextColor(whiteColor)
                descendingZToARadioButton.buttonTintList = ColorStateList.valueOf(whiteColor)
                descendingZToARadioButton.setTextColor(whiteColor)
                cancelButton.background.colorFilter =
                    PorterDuffColorFilter(whiteColor, PorterDuff.Mode.SRC_IN)
                cancelButton.setTextColor(whiteColor)
                sortButton.background.colorFilter =
                    PorterDuffColorFilter(whiteColor, PorterDuff.Mode.SRC_IN)
                sortButton.setTextColor(whiteColor)
            } else {
                when (prefs.colorSchemeValue) {
                    0 -> {
                        sortByTextView.setTextColor(defaultColor)
                        titleRadioButton.buttonTintList = ColorStateList.valueOf(defaultColor)
                        dayOfWeekRadioButton.buttonTintList = ColorStateList.valueOf(defaultColor)
                        dateRadioButton.buttonTintList =
                            ColorStateList.valueOf(defaultColor)
                        monthRadioButton.buttonTintList =
                            ColorStateList.valueOf(defaultColor)
                        yearRadioButton.buttonTintList =
                            ColorStateList.valueOf(defaultColor)
                        timeRadioButton.buttonTintList =
                            ColorStateList.valueOf(defaultColor)
                        ascendingAToZRadioButton.buttonTintList =
                            ColorStateList.valueOf(defaultColor)
                        descendingZToARadioButton.buttonTintList =
                            ColorStateList.valueOf(defaultColor)
                        cancelButton.background.colorFilter =
                            PorterDuffColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
                        sortButton.background.colorFilter =
                            PorterDuffColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
                    }

                    1 -> {
                        sortByTextView.setTextColor(darkYellowColor)
                        titleRadioButton.buttonTintList =
                            ColorStateList.valueOf(darkYellowColor)
                        dayOfWeekRadioButton.buttonTintList =
                            ColorStateList.valueOf(darkYellowColor)
                        dateRadioButton.buttonTintList =
                            ColorStateList.valueOf(darkYellowColor)
                        monthRadioButton.buttonTintList =
                            ColorStateList.valueOf(darkYellowColor)
                        yearRadioButton.buttonTintList =
                            ColorStateList.valueOf(darkYellowColor)
                        timeRadioButton.buttonTintList =
                            ColorStateList.valueOf(darkYellowColor)
                        ascendingAToZRadioButton.buttonTintList =
                            ColorStateList.valueOf(darkYellowColor)
                        descendingZToARadioButton.buttonTintList =
                            ColorStateList.valueOf(darkYellowColor)
                        cancelButton.background.colorFilter =
                            PorterDuffColorFilter(darkYellowColor, PorterDuff.Mode.SRC_IN)
                        sortButton.background.colorFilter =
                            PorterDuffColorFilter(darkYellowColor, PorterDuff.Mode.SRC_IN)
                    }

                    2 -> {
                        sortByTextView.setTextColor(orangeColor)
                        titleRadioButton.buttonTintList =
                            ColorStateList.valueOf(orangeColor)
                        dayOfWeekRadioButton.buttonTintList =
                            ColorStateList.valueOf(orangeColor)
                        dateRadioButton.buttonTintList =
                            ColorStateList.valueOf(orangeColor)
                        monthRadioButton.buttonTintList =
                            ColorStateList.valueOf(orangeColor)
                        yearRadioButton.buttonTintList =
                            ColorStateList.valueOf(orangeColor)
                        timeRadioButton.buttonTintList =
                            ColorStateList.valueOf(orangeColor)
                        ascendingAToZRadioButton.buttonTintList =
                            ColorStateList.valueOf(orangeColor)
                        descendingZToARadioButton.buttonTintList =
                            ColorStateList.valueOf(orangeColor)
                        cancelButton.background.colorFilter =
                            PorterDuffColorFilter(orangeColor, PorterDuff.Mode.SRC_IN)
                        sortButton.background.colorFilter =
                            PorterDuffColorFilter(orangeColor, PorterDuff.Mode.SRC_IN)
                    }

                    3 -> {
                        sortByTextView.setTextColor(lightGreenColor)
                        titleRadioButton.buttonTintList =
                            ColorStateList.valueOf(lightGreenColor)
                        dayOfWeekRadioButton.buttonTintList =
                            ColorStateList.valueOf(lightGreenColor)
                        dateRadioButton.buttonTintList =
                            ColorStateList.valueOf(lightGreenColor)
                        monthRadioButton.buttonTintList =
                            ColorStateList.valueOf(lightGreenColor)
                        yearRadioButton.buttonTintList =
                            ColorStateList.valueOf(lightGreenColor)
                        timeRadioButton.buttonTintList =
                            ColorStateList.valueOf(lightGreenColor)
                        ascendingAToZRadioButton.buttonTintList =
                            ColorStateList.valueOf(lightGreenColor)
                        descendingZToARadioButton.buttonTintList =
                            ColorStateList.valueOf(lightGreenColor)
                        cancelButton.background.colorFilter =
                            PorterDuffColorFilter(lightGreenColor, PorterDuff.Mode.SRC_IN)
                        sortButton.background.colorFilter =
                            PorterDuffColorFilter(lightGreenColor, PorterDuff.Mode.SRC_IN)
                    }

                    4 -> {
                        sortByTextView.setTextColor(blueColor)
                        titleRadioButton.buttonTintList =
                            ColorStateList.valueOf(blueColor)
                        dayOfWeekRadioButton.buttonTintList =
                            ColorStateList.valueOf(blueColor)
                        dateRadioButton.buttonTintList =
                            ColorStateList.valueOf(blueColor)
                        monthRadioButton.buttonTintList =
                            ColorStateList.valueOf(blueColor)
                        yearRadioButton.buttonTintList =
                            ColorStateList.valueOf(blueColor)
                        timeRadioButton.buttonTintList =
                            ColorStateList.valueOf(blueColor)
                        ascendingAToZRadioButton.buttonTintList =
                            ColorStateList.valueOf(blueColor)
                        descendingZToARadioButton.buttonTintList =
                            ColorStateList.valueOf(blueColor)
                        cancelButton.background.colorFilter =
                            PorterDuffColorFilter(blueColor, PorterDuff.Mode.SRC_IN)
                        sortButton.background.colorFilter =
                            PorterDuffColorFilter(blueColor, PorterDuff.Mode.SRC_IN)
                    }

                    5 -> {
                        sortByTextView.setTextColor(cyanColor)
                        titleRadioButton.buttonTintList =
                            ColorStateList.valueOf(cyanColor)
                        dayOfWeekRadioButton.buttonTintList =
                            ColorStateList.valueOf(cyanColor)
                        dateRadioButton.buttonTintList =
                            ColorStateList.valueOf(cyanColor)
                        monthRadioButton.buttonTintList =
                            ColorStateList.valueOf(cyanColor)
                        yearRadioButton.buttonTintList =
                            ColorStateList.valueOf(cyanColor)
                        timeRadioButton.buttonTintList =
                            ColorStateList.valueOf(cyanColor)
                        ascendingAToZRadioButton.buttonTintList =
                            ColorStateList.valueOf(cyanColor)
                        descendingZToARadioButton.buttonTintList =
                            ColorStateList.valueOf(cyanColor)
                        cancelButton.background.colorFilter =
                            PorterDuffColorFilter(cyanColor, PorterDuff.Mode.SRC_IN)
                        sortButton.background.colorFilter =
                            PorterDuffColorFilter(cyanColor, PorterDuff.Mode.SRC_IN)
                    }

                    6 -> {
                        sortByTextView.setTextColor(pinkColor)
                        titleRadioButton.buttonTintList =
                            ColorStateList.valueOf(pinkColor)
                        dayOfWeekRadioButton.buttonTintList =
                            ColorStateList.valueOf(pinkColor)
                        dateRadioButton.buttonTintList =
                            ColorStateList.valueOf(pinkColor)
                        monthRadioButton.buttonTintList =
                            ColorStateList.valueOf(pinkColor)
                        yearRadioButton.buttonTintList =
                            ColorStateList.valueOf(pinkColor)
                        timeRadioButton.buttonTintList =
                            ColorStateList.valueOf(pinkColor)
                        ascendingAToZRadioButton.buttonTintList =
                            ColorStateList.valueOf(pinkColor)
                        descendingZToARadioButton.buttonTintList =
                            ColorStateList.valueOf(pinkColor)
                        cancelButton.background.colorFilter =
                            PorterDuffColorFilter(pinkColor, PorterDuff.Mode.SRC_IN)
                        sortButton.background.colorFilter =
                            PorterDuffColorFilter(pinkColor, PorterDuff.Mode.SRC_IN)
                    }

                    7 -> {
                        sortByTextView.setTextColor(darkBlueColor)
                        titleRadioButton.buttonTintList =
                            ColorStateList.valueOf(darkBlueColor)
                        dayOfWeekRadioButton.buttonTintList =
                            ColorStateList.valueOf(darkBlueColor)
                        dateRadioButton.buttonTintList =
                            ColorStateList.valueOf(darkBlueColor)
                        monthRadioButton.buttonTintList =
                            ColorStateList.valueOf(darkBlueColor)
                        yearRadioButton.buttonTintList =
                            ColorStateList.valueOf(darkBlueColor)
                        timeRadioButton.buttonTintList =
                            ColorStateList.valueOf(darkBlueColor)
                        ascendingAToZRadioButton.buttonTintList =
                            ColorStateList.valueOf(darkBlueColor)
                        descendingZToARadioButton.buttonTintList =
                            ColorStateList.valueOf(darkBlueColor)
                        cancelButton.background.colorFilter =
                            PorterDuffColorFilter(darkBlueColor, PorterDuff.Mode.SRC_IN)
                        sortButton.background.colorFilter =
                            PorterDuffColorFilter(darkBlueColor, PorterDuff.Mode.SRC_IN)
                    }

                    8 -> {
                        sortByTextView.setTextColor(redColor)
                        titleRadioButton.buttonTintList =
                            ColorStateList.valueOf(redColor)
                        dayOfWeekRadioButton.buttonTintList =
                            ColorStateList.valueOf(redColor)
                        dateRadioButton.buttonTintList =
                            ColorStateList.valueOf(redColor)
                        monthRadioButton.buttonTintList =
                            ColorStateList.valueOf(redColor)
                        yearRadioButton.buttonTintList =
                            ColorStateList.valueOf(redColor)
                        timeRadioButton.buttonTintList =
                            ColorStateList.valueOf(redColor)
                        ascendingAToZRadioButton.buttonTintList =
                            ColorStateList.valueOf(redColor)
                        descendingZToARadioButton.buttonTintList =
                            ColorStateList.valueOf(redColor)
                        cancelButton.background.colorFilter =
                            PorterDuffColorFilter(redColor, PorterDuff.Mode.SRC_IN)
                        sortButton.background.colorFilter =
                            PorterDuffColorFilter(redColor, PorterDuff.Mode.SRC_IN)
                    }

                    9 -> {
                        sortByTextView.setTextColor(lightPurpleColor)
                        titleRadioButton.buttonTintList =
                            ColorStateList.valueOf(lightPurpleColor)
                        dayOfWeekRadioButton.buttonTintList =
                            ColorStateList.valueOf(lightPurpleColor)
                        dateRadioButton.buttonTintList =
                            ColorStateList.valueOf(lightPurpleColor)
                        monthRadioButton.buttonTintList =
                            ColorStateList.valueOf(lightPurpleColor)
                        yearRadioButton.buttonTintList =
                            ColorStateList.valueOf(lightPurpleColor)
                        timeRadioButton.buttonTintList =
                            ColorStateList.valueOf(lightPurpleColor)
                        ascendingAToZRadioButton.buttonTintList =
                            ColorStateList.valueOf(lightPurpleColor)
                        descendingZToARadioButton.buttonTintList =
                            ColorStateList.valueOf(lightPurpleColor)
                        cancelButton.background.colorFilter =
                            PorterDuffColorFilter(lightPurpleColor, PorterDuff.Mode.SRC_IN)
                        sortButton.background.colorFilter =
                            PorterDuffColorFilter(lightPurpleColor, PorterDuff.Mode.SRC_IN)
                    }
                }
            }
        }
    }

    private fun applyCustomFontOnSortingDialogViews(sortingDialogLayoutBinding: SortingDialogLayoutBinding) {
        with(sortingDialogLayoutBinding) {
            sortByTextView.typeface = typeface
            titleRadioButton.typeface = typeface
            dayOfWeekRadioButton.typeface = typeface
            dateRadioButton.typeface = typeface
            monthRadioButton.typeface = typeface
            yearRadioButton.typeface = typeface
            timeRadioButton.typeface = typeface
            ascendingAToZRadioButton.typeface = typeface
            descendingZToARadioButton.typeface = typeface
            cancelButton.typeface = typeface
            sortButton.typeface = typeface
        }
    }

    private fun applyColorSchemeORLightAndDarkModeOnAddAndUpdateTasksDialogViews(
        addAndUpdateTasksDialogLayoutBinding: AddAndUpdateTasksDialogLayoutBinding
    ) {
        with(addAndUpdateTasksDialogLayoutBinding) {
            if (prefs.dayAndNightModeSwitchValue) {
                addToDoTaskDialogRootLayout.setBackgroundResource(dialogBoxesDarkModeBackground)

                dismissDialogImageView.setColorFilter(whiteColor)
                addAndEditImageView.setColorFilter(whiteColor)
                addAndUpdateToDoTaskTextView.setTextColor(whiteColor)
                infoTextView.setTextColor(whiteColor)

//            Here, We Change The Box Stroke Color Of TextInputLayout When That is Un-Focused...
                titleTextInputLayout.setBoxStrokeColorStateList(
                    textInputLayoutBoxStrokeDarkModeColor
                )
                descriptionTextInputLayout.setBoxStrokeColorStateList(
                    textInputLayoutBoxStrokeDarkModeColor
                )
                dayOfWeekTextInputLayout.setBoxStrokeColorStateList(
                    textInputLayoutBoxStrokeDarkModeColor
                )
                dateTextInputLayout.setBoxStrokeColorStateList(
                    textInputLayoutBoxStrokeDarkModeColor
                )
                timeTextInputLayout.setBoxStrokeColorStateList(
                    textInputLayoutBoxStrokeDarkModeColor
                )
                titleTextInputLayout.boxStrokeColor = whiteColor
                titleTextInputLayout.setStartIconTintList(
                    whiteColorStateList
                )
                titleTextInputLayout.hintTextColor =
                    whiteColorStateList
                titleTextInputLayout.boxStrokeErrorColor =
                    whiteColorStateList
                titleTextInputEditText.setTextColor(whiteColor)
                descriptionTextInputLayout.boxStrokeColor =
                    whiteColor
                descriptionTextInputLayout.setStartIconTintList(
                    whiteColorStateList
                )
                descriptionTextInputLayout.hintTextColor =
                    whiteColorStateList
                descriptionTextInputLayout.boxStrokeErrorColor =
                    whiteColorStateList
                descriptionTextInputEditText.setTextColor(
                    whiteColor
                )
                dayOfWeekTextInputLayout.boxStrokeColor =
                    whiteColor
                dayOfWeekTextInputLayout.setStartIconTintList(
                    whiteColorStateList
                )
                dayOfWeekTextInputLayout.hintTextColor =
                    whiteColorStateList
                dayOfWeekTextInputLayout.boxStrokeErrorColor =
                    whiteColorStateList
                dayOfWeekTextInputEditText.setTextColor(
                    whiteColor
                )
                dateTextInputLayout.boxStrokeColor = whiteColor
                dateTextInputLayout.setStartIconTintList(
                    whiteColorStateList
                )
                dateTextInputLayout.hintTextColor =
                    whiteColorStateList
                dateTextInputLayout.boxStrokeErrorColor =
                    whiteColorStateList
                dateTextInputEditText.setTextColor(whiteColor)
                timeTextInputLayout.boxStrokeColor = whiteColor
                timeTextInputLayout.setStartIconTintList(
                    whiteColorStateList
                )
                timeTextInputLayout.hintTextColor =
                    whiteColorStateList
                timeTextInputLayout.boxStrokeErrorColor =
                    whiteColorStateList
                timeTextInputEditText.setTextColor(whiteColor)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    titleTextInputEditText.textCursorDrawable =
                        editTextsCursorDarkModeColor
                    descriptionTextInputEditText.textCursorDrawable =
                        editTextsCursorDarkModeColor
                    dayOfWeekTextInputEditText.textCursorDrawable =
                        editTextsCursorDarkModeColor
                    dateTextInputEditText.textCursorDrawable =
                        editTextsCursorDarkModeColor
                    timeTextInputEditText.textCursorDrawable =
                        editTextsCursorDarkModeColor
                }
                selectCategoryLayout.setBackgroundResource(
                    spinnerLayoutNightModeBackground
                )
                selectCategoryTextView.setTextColor(whiteColor)
                dropDownImageView.setColorFilter(whiteColor)
                saveAndUpdateButton.background.colorFilter =
                    PorterDuffColorFilter(
                        whiteColor, PorterDuff.Mode.SRC_IN
                    )
                saveAndUpdateButton.setTextColor(blackColor)
            } else {
                when (prefs.colorSchemeValue) {
                    0 -> {
                        val defaultColorStateList = ColorStateList.valueOf(defaultColor)
                        dismissDialogImageView.setColorFilter(
                            defaultColor
                        )
                        addAndEditImageView.setColorFilter(
                            defaultColor
                        )
                        addAndUpdateToDoTaskTextView.setTextColor(
                            defaultColor
                        )
                        titleTextInputLayout.setStartIconTintList(
                            defaultColorStateList
                        )
                        descriptionTextInputLayout.setStartIconTintList(
                            defaultColorStateList
                        )
                        dayOfWeekTextInputLayout.setStartIconTintList(
                            defaultColorStateList
                        )
                        dateTextInputLayout.setStartIconTintList(
                            defaultColorStateList
                        )
                        timeTextInputLayout.setStartIconTintList(
                            defaultColorStateList
                        )
                        dropDownImageView.setColorFilter(
                            defaultColor
                        )
                        saveAndUpdateButton.background.colorFilter =
                            PorterDuffColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
                    }

                    1 -> {
                        val darkYellowColorStateList = ColorStateList.valueOf(darkYellowColor)
                        dismissDialogImageView.setColorFilter(
                            darkYellowColor
                        )
                        addAndEditImageView.setColorFilter(
                            darkYellowColor
                        )
                        addAndUpdateToDoTaskTextView.setTextColor(
                            darkYellowColor
                        )
                        titleTextInputLayout.setStartIconTintList(
                            darkYellowColorStateList
                        )
                        descriptionTextInputLayout.setStartIconTintList(
                            darkYellowColorStateList
                        )
                        dayOfWeekTextInputLayout.setStartIconTintList(
                            darkYellowColorStateList
                        )
                        dateTextInputLayout.setStartIconTintList(
                            darkYellowColorStateList
                        )
                        timeTextInputLayout.setStartIconTintList(
                            darkYellowColorStateList
                        )
                        dropDownImageView.setColorFilter(
                            darkYellowColor
                        )
                        saveAndUpdateButton.background.colorFilter =
                            PorterDuffColorFilter(darkYellowColor, PorterDuff.Mode.SRC_IN)
                    }

                    2 -> {
                        val orangeColorStateList = ColorStateList.valueOf(orangeColor)
                        dismissDialogImageView.setColorFilter(
                            orangeColor
                        )
                        addAndEditImageView.setColorFilter(
                            orangeColor
                        )
                        addAndUpdateToDoTaskTextView.setTextColor(
                            orangeColor
                        )
                        titleTextInputLayout.setStartIconTintList(
                            orangeColorStateList
                        )
                        descriptionTextInputLayout.setStartIconTintList(
                            orangeColorStateList
                        )
                        dayOfWeekTextInputLayout.setStartIconTintList(
                            orangeColorStateList
                        )
                        dateTextInputLayout.setStartIconTintList(
                            orangeColorStateList
                        )
                        timeTextInputLayout.setStartIconTintList(
                            orangeColorStateList
                        )
                        dropDownImageView.setColorFilter(orangeColor)
                        saveAndUpdateButton.background.colorFilter =
                            PorterDuffColorFilter(orangeColor, PorterDuff.Mode.SRC_IN)
                    }

                    3 -> {
                        val lightGreenColorStateList = ColorStateList.valueOf(lightGreenColor)
                        dismissDialogImageView.setColorFilter(
                            lightGreenColor
                        )
                        addAndEditImageView.setColorFilter(
                            lightGreenColor
                        )
                        addAndUpdateToDoTaskTextView.setTextColor(
                            lightGreenColor
                        )
                        titleTextInputLayout.setStartIconTintList(
                            lightGreenColorStateList
                        )
                        descriptionTextInputLayout.setStartIconTintList(
                            lightGreenColorStateList
                        )
                        dayOfWeekTextInputLayout.setStartIconTintList(
                            lightGreenColorStateList
                        )
                        dateTextInputLayout.setStartIconTintList(
                            lightGreenColorStateList
                        )
                        timeTextInputLayout.setStartIconTintList(
                            lightGreenColorStateList
                        )
                        dropDownImageView.setColorFilter(
                            lightGreenColor
                        )
                        saveAndUpdateButton.background.colorFilter =
                            PorterDuffColorFilter(lightGreenColor, PorterDuff.Mode.SRC_IN)
                    }

                    4 -> {
                        val blueColorStateList = ColorStateList.valueOf(blueColor)
                        dismissDialogImageView.setColorFilter(
                            blueColor
                        )
                        addAndEditImageView.setColorFilter(blueColor)
                        addAndUpdateToDoTaskTextView.setTextColor(
                            blueColor
                        )
                        titleTextInputLayout.setStartIconTintList(
                            blueColorStateList
                        )
                        descriptionTextInputLayout.setStartIconTintList(
                            blueColorStateList
                        )
                        dayOfWeekTextInputLayout.setStartIconTintList(
                            blueColorStateList
                        )
                        dateTextInputLayout.setStartIconTintList(
                            blueColorStateList
                        )
                        timeTextInputLayout.setStartIconTintList(
                            blueColorStateList
                        )
                        dropDownImageView.setColorFilter(blueColor)
                        saveAndUpdateButton.background.colorFilter =
                            PorterDuffColorFilter(blueColor, PorterDuff.Mode.SRC_IN)
                    }

                    5 -> {
                        val cyanColorStateList = ColorStateList.valueOf(cyanColor)
                        dismissDialogImageView.setColorFilter(
                            cyanColor
                        )
                        addAndEditImageView.setColorFilter(cyanColor)
                        addAndUpdateToDoTaskTextView.setTextColor(
                            cyanColor
                        )
                        titleTextInputLayout.setStartIconTintList(
                            cyanColorStateList
                        )
                        descriptionTextInputLayout.setStartIconTintList(
                            cyanColorStateList
                        )
                        dayOfWeekTextInputLayout.setStartIconTintList(
                            cyanColorStateList
                        )
                        dateTextInputLayout.setStartIconTintList(
                            cyanColorStateList
                        )
                        timeTextInputLayout.setStartIconTintList(
                            cyanColorStateList
                        )
                        dropDownImageView.setColorFilter(cyanColor)
                        saveAndUpdateButton.background.colorFilter =
                            PorterDuffColorFilter(cyanColor, PorterDuff.Mode.SRC_IN)
                    }

                    6 -> {
                        val pinkColorStateList = ColorStateList.valueOf(pinkColor)
                        dismissDialogImageView.setColorFilter(
                            pinkColor
                        )
                        addAndEditImageView.setColorFilter(pinkColor)
                        addAndUpdateToDoTaskTextView.setTextColor(
                            pinkColor
                        )
                        titleTextInputLayout.setStartIconTintList(
                            pinkColorStateList
                        )
                        descriptionTextInputLayout.setStartIconTintList(
                            pinkColorStateList
                        )
                        dayOfWeekTextInputLayout.setStartIconTintList(
                            pinkColorStateList
                        )
                        dateTextInputLayout.setStartIconTintList(
                            pinkColorStateList
                        )
                        timeTextInputLayout.setStartIconTintList(
                            pinkColorStateList
                        )
                        dropDownImageView.setColorFilter(pinkColor)
                        saveAndUpdateButton.background.colorFilter =
                            PorterDuffColorFilter(pinkColor, PorterDuff.Mode.SRC_IN)
                    }

                    7 -> {
                        val darkBlueColorStateList = ColorStateList.valueOf(darkBlueColor)
                        dismissDialogImageView.setColorFilter(
                            darkBlueColor
                        )
                        addAndEditImageView.setColorFilter(
                            darkBlueColor
                        )
                        addAndUpdateToDoTaskTextView.setTextColor(
                            darkBlueColor
                        )
                        titleTextInputLayout.setStartIconTintList(
                            darkBlueColorStateList
                        )
                        descriptionTextInputLayout.setStartIconTintList(
                            darkBlueColorStateList
                        )
                        dayOfWeekTextInputLayout.setStartIconTintList(
                            darkBlueColorStateList
                        )
                        dateTextInputLayout.setStartIconTintList(
                            darkBlueColorStateList
                        )
                        timeTextInputLayout.setStartIconTintList(
                            darkBlueColorStateList
                        )
                        dropDownImageView.setColorFilter(
                            darkBlueColor
                        )
                        saveAndUpdateButton.background.colorFilter =
                            PorterDuffColorFilter(darkBlueColor, PorterDuff.Mode.SRC_IN)
                    }

                    8 -> {
                        val redColorStateList = ColorStateList.valueOf(redColor)
                        dismissDialogImageView.setColorFilter(
                            redColor
                        )
                        addAndEditImageView.setColorFilter(redColor)
                        addAndUpdateToDoTaskTextView.setTextColor(
                            redColor
                        )
                        titleTextInputLayout.setStartIconTintList(
                            redColorStateList
                        )
                        descriptionTextInputLayout.setStartIconTintList(
                            redColorStateList
                        )
                        dayOfWeekTextInputLayout.setStartIconTintList(
                            redColorStateList
                        )
                        dateTextInputLayout.setStartIconTintList(
                            redColorStateList
                        )
                        timeTextInputLayout.setStartIconTintList(
                            redColorStateList
                        )
                        dropDownImageView.setColorFilter(redColor)
                        saveAndUpdateButton.background.colorFilter =
                            PorterDuffColorFilter(redColor, PorterDuff.Mode.SRC_IN)
                    }

                    9 -> {
                        val lightPurpleColorStateList = ColorStateList.valueOf(lightPurpleColor)
                        dismissDialogImageView.setColorFilter(
                            lightPurpleColor
                        )
                        addAndEditImageView.setColorFilter(
                            lightPurpleColor
                        )
                        addAndUpdateToDoTaskTextView.setTextColor(
                            lightPurpleColor
                        )
                        titleTextInputLayout.setStartIconTintList(
                            lightPurpleColorStateList
                        )
                        descriptionTextInputLayout.setStartIconTintList(
                            lightPurpleColorStateList
                        )
                        dayOfWeekTextInputLayout.setStartIconTintList(
                            lightPurpleColorStateList
                        )
                        dateTextInputLayout.setStartIconTintList(
                            lightPurpleColorStateList
                        )
                        timeTextInputLayout.setStartIconTintList(
                            lightPurpleColorStateList
                        )
                        dropDownImageView.setColorFilter(
                            lightPurpleColor
                        )
                        saveAndUpdateButton.background.colorFilter =
                            PorterDuffColorFilter(lightPurpleColor, PorterDuff.Mode.SRC_IN)
                    }
                }
            }
        }
    }

    private fun showMaterialDatePicker(addAndUpdateTasksDialogLayoutBinding: AddAndUpdateTasksDialogLayoutBinding) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
        datePicker.setTitleText(R.string.select_date_text)
        datePicker.setSelection(MaterialDatePicker.todayInUtcMilliseconds())
        datePicker.setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
        val picker = datePicker.build()
        picker.show(requireActivity().supportFragmentManager, "MATERIAL_DATE_PICKER")
        picker.addOnPositiveButtonClickListener { selection: Long? ->
            dateOfMonth = dateOfMonthSimpleDateFormat.format(selection)
            month = monthSimpleDateFormat.format(selection)
            year = yearSimpleDateFormat.format(selection)
            val date: String = simpleDateFormat.format(selection)
            addAndUpdateTasksDialogLayoutBinding.dateTextInputLayout.editText?.setText(date)
        }
    }

    private fun showMaterialTimePicker(addAndUpdateTasksDialogLayoutBinding: AddAndUpdateTasksDialogLayoutBinding) {
        val builder = MaterialTimePicker.Builder()
        builder.setTitleText(R.string.select_time_text)
        builder.setTimeFormat(TimeFormat.CLOCK_12H)
        builder.setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
        builder.setHour(calendar[Calendar.HOUR])
        builder.setMinute(calendar[Calendar.MINUTE])
        val materialTimePicker = builder.build()
        materialTimePicker.show(requireActivity().supportFragmentManager, "MATERIAL_TIME_PICKER")
        materialTimePicker.addOnPositiveButtonClickListener { _: View? ->
            calendar.set(Calendar.HOUR_OF_DAY, materialTimePicker.hour)
            calendar.set(Calendar.MINUTE, materialTimePicker.minute)
            val time: String = simpleTimeFormat.format(calendar.time)
            addAndUpdateTasksDialogLayoutBinding.timeTextInputLayout.editText?.setText(time)
        }
    }

    private fun applyCustomFontOnAddAndUpdateTasksDialogViews(
        addAndUpdateTasksDialogLayoutBinding: AddAndUpdateTasksDialogLayoutBinding
    ) {
        with(addAndUpdateTasksDialogLayoutBinding) {
            addAndUpdateToDoTaskTextView.typeface = typeface
            infoTextView.typeface = typeface
            titleTextInputLayout.typeface = typeface
            titleTextInputEditText.typeface = typeface
            descriptionTextInputLayout.typeface = typeface
            descriptionTextInputEditText.typeface = typeface
            dayOfWeekTextInputLayout.typeface = typeface
            dayOfWeekTextInputEditText.typeface = typeface
            dateTextInputLayout.typeface = typeface
            dateTextInputEditText.typeface = typeface
            timeTextInputLayout.typeface = typeface
            timeTextInputEditText.typeface = typeface
            selectCategoryTextView.typeface = typeface
            saveAndUpdateButton.typeface = typeface
        }
    }

    override fun taskDetail(toDoTask: ToDoTask) = openTaskDetailActivity(toDoTask)

    private fun openTaskDetailActivity(toDoTask: ToDoTask) {
        val toDoTaskDetailIntent = Intent(fragmentContext, ToDoTaskDetailActivity::class.java)
        with(toDoTaskDetailIntent) {
            putExtra("taskDetail", toDoTask)
            startActivity(this)
        }
    }

    override fun taskUpdateAndDelete(toDoTask: ToDoTask, view: View, color: Int, position: Int) {
        var popupMenu: PopupMenu? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            popupMenu = PopupMenu(
                fragmentContext, view, Gravity.CENTER, 0,
                if (prefs.dayAndNightModeSwitchValue) {
                    R.style.popUpMenuDarkModeCustomization
                } else {
                    R.style.popUpMenuDayModeCustomization
                }
            )
        }
        popupMenu?.inflate(R.menu.update_and_delete_popup_menu)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenu?.setForceShowIcon(true)
        }
        popupMenu?.setOnMenuItemClickListener { item: MenuItem ->
            val itemId = item.itemId
            if (itemId == R.id.update_item) {
                this.toDoTask = toDoTask
                toUpdateAndDeleteItemPosition = position
                showAddNewAndUpdateTaskDialog(2)
            } else if (itemId == R.id.delete_item) {
                toUpdateAndDeleteItemPosition = position
                showDeleteTaskDialog(toDoTask)
            }
            false
        }
        val menu = popupMenu?.menu as Menu
        for (i in 0 until menu.size()) {
            applyFontToPopupMenuItem(menu.getItem(i))

//            Here, We Change The Color Of PopUpMenu Items Icons...
            val menuItem = menu.getItem(i)
            val popUpMenuIconDrawable = menuItem.icon
            if (popUpMenuIconDrawable != null) {
                if (prefs.dayAndNightModeSwitchValue) {
                    DrawableCompat.setTint(popUpMenuIconDrawable, whiteColor)
                    val itemTitle = menuItem.title.toString().trim()
                    val spannableString = SpannableString(itemTitle)
                    spannableString.setSpan(
                        ForegroundColorSpan(whiteColor), 0, itemTitle.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    menuItem.setTitle(spannableString)
                } else {
                    DrawableCompat.setTint(popUpMenuIconDrawable, color)
                }
            }
            menuItem.setIcon(popUpMenuIconDrawable)
        }
        popupMenu.show()
    }

    private fun showDeleteTaskDialog(toDoTask: ToDoTask) {
        val deleteTaskDialogLayoutBinding = DeleteTaskDialogLayoutBinding.inflate(layoutInflater)

        val deleteTaskDialogBuilder = AlertDialog.Builder(fragmentContext)
        deleteTaskDialogBuilder.setView(deleteTaskDialogLayoutBinding.root)
        deleteTaskDialogBuilder.setCancelable(false)
        val deleteTaskAlertDialog = deleteTaskDialogBuilder.create()

        if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed && !deleteTaskAlertDialog.isShowing) {
            deleteTaskAlertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            deleteTaskAlertDialog.window?.setWindowAnimations(R.style.dialogBoxesAnimation)
            deleteTaskAlertDialog.show()
        }

        Toast.makeText(fragmentContext, "${toDoTask.id}, Pos: $toUpdateAndDeleteItemPosition", Toast.LENGTH_LONG).show()

        with(deleteTaskDialogLayoutBinding) {
            deleteImageView.startAnimation(applyAnimation(fragmentContext))
            stopFABAnimation()
            applyCustomFontOnDeleteTaskDialogViews(this)
            appColorSchemeOrLightAndDarkModeOnDeleteTaskDialogViews(this)

            noButton.setOnClickListener { _: View? ->
                if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed) {
                    deleteTaskAlertDialog.dismiss()
                }
                startFABAnimation()
            }

            yesButton.setOnClickListener { _: View? ->
                val isDeleted = toDosDatabase.dao().deleteTask(toDoTask)
                if (isDeleted == 1) {
                    Toasty.success(fragmentContext, R.string.deleted_successfully_toast_text, Toasty.LENGTH_LONG)
                        .show()
//                    readAllTasks()
                    if (allTasksArrayList.contains(toDoTask)) {
                        allTasksArrayList.removeAt(toUpdateAndDeleteItemPosition)
                        adapter.notifyItemRemoved(toUpdateAndDeleteItemPosition)
                    }
                    if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed) {
                        deleteTaskAlertDialog.dismiss()
                    }
                    startFABAnimation()
                } else {
                    Toasty.success(fragmentContext, R.string.deleted_unsuccessfully_toast_text, Toasty.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    private fun appColorSchemeOrLightAndDarkModeOnDeleteTaskDialogViews(
        deleteTaskDialogLayoutBinding: DeleteTaskDialogLayoutBinding
    ) {
        with(deleteTaskDialogLayoutBinding) {
            if (prefs.dayAndNightModeSwitchValue) {
                deleteTaskDialogRootLayout.setBackgroundResource(dialogBoxesDarkModeBackground)
                deleteImageView.setColorFilter(whiteColor)
                deleteMessageTextView.setTextColor(whiteColor)
                noButton.background.colorFilter = PorterDuffColorFilter(whiteColor, PorterDuff.Mode.SRC_IN)
                noButton.setTextColor(whiteColor)
                yesButton.background.colorFilter = PorterDuffColorFilter(whiteColor, PorterDuff.Mode.SRC_IN)
                yesButton.setTextColor(whiteColor)
            } else {
                when (prefs.colorSchemeValue) {
                    0 -> {
                        deleteImageView.setColorFilter(defaultColor)
                        noButton.background.colorFilter = PorterDuffColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
                        yesButton.background.colorFilter = PorterDuffColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
                    }

                    1 -> {
                        deleteImageView.setColorFilter(darkYellowColor)
                        noButton.background.colorFilter = PorterDuffColorFilter(darkYellowColor, PorterDuff.Mode.SRC_IN)
                        yesButton.background.colorFilter = PorterDuffColorFilter(darkYellowColor, PorterDuff.Mode.SRC_IN)
                    }

                    2 -> {
                        deleteImageView.setColorFilter(orangeColor)
                        noButton.background.colorFilter = PorterDuffColorFilter(orangeColor, PorterDuff.Mode.SRC_IN)
                        yesButton.background.colorFilter = PorterDuffColorFilter(orangeColor, PorterDuff.Mode.SRC_IN)
                    }

                    3 -> {
                        deleteImageView.setColorFilter(lightGreenColor)
                        noButton.background.colorFilter = PorterDuffColorFilter(lightGreenColor, PorterDuff.Mode.SRC_IN)
                        yesButton.background.colorFilter = PorterDuffColorFilter(lightGreenColor, PorterDuff.Mode.SRC_IN)
                    }

                    4 -> {
                        deleteImageView.setColorFilter(blueColor)
                        noButton.background.colorFilter = PorterDuffColorFilter(blueColor, PorterDuff.Mode.SRC_IN)
                        yesButton.background.colorFilter = PorterDuffColorFilter(blueColor, PorterDuff.Mode.SRC_IN)
                    }

                    5 -> {
                        deleteImageView.setColorFilter(cyanColor)
                        noButton.background.colorFilter = PorterDuffColorFilter(cyanColor, PorterDuff.Mode.SRC_IN)
                        yesButton.background.colorFilter = PorterDuffColorFilter(cyanColor, PorterDuff.Mode.SRC_IN)
                    }

                    6 -> {
                        deleteImageView.setColorFilter(pinkColor)
                        noButton.background.colorFilter = PorterDuffColorFilter(pinkColor, PorterDuff.Mode.SRC_IN)
                        yesButton.background.colorFilter = PorterDuffColorFilter(pinkColor, PorterDuff.Mode.SRC_IN)
                    }

                    7 -> {
                        deleteImageView.setColorFilter(darkBlueColor)
                        noButton.background.colorFilter = PorterDuffColorFilter(darkBlueColor, PorterDuff.Mode.SRC_IN)
                        yesButton.background.colorFilter = PorterDuffColorFilter(darkBlueColor, PorterDuff.Mode.SRC_IN)
                    }

                    8 -> {
                        deleteImageView.setColorFilter(redColor)
                        noButton.background.colorFilter = PorterDuffColorFilter(redColor, PorterDuff.Mode.SRC_IN)
                        yesButton.background.colorFilter = PorterDuffColorFilter(redColor, PorterDuff.Mode.SRC_IN)
                    }

                    9 -> {
                        deleteImageView.setColorFilter(lightPurpleColor)
                        noButton.background.colorFilter = PorterDuffColorFilter(lightPurpleColor, PorterDuff.Mode.SRC_IN)
                        yesButton.background.colorFilter = PorterDuffColorFilter(lightPurpleColor, PorterDuff.Mode.SRC_IN)
                    }
                }
            }
        }
    }

    private fun applyCustomFontOnDeleteTaskDialogViews(deleteTaskDialogLayoutBinding: DeleteTaskDialogLayoutBinding) {
        with(deleteTaskDialogLayoutBinding) {
            deleteMessageTextView.typeface = typeface
            yesButton.typeface = typeface
            noButton.typeface = typeface
        }
    }

    private fun applyFontToPopupMenuItem(menuItem: MenuItem) {
        val spannableString = SpannableString(menuItem.title)
        spannableString.setSpan(
            CustomTypeFaceSpan("", typeface, Color.BLACK), 0, spannableString.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        menuItem.setTitle(spannableString)
    }

    override fun selectCategory(category: Int) {
        if (category == DEFAULT_CATEGORY || category == PERSONAL_CATEGORY) {
            this.category = PERSONAL_CATEGORY
        } else if (category == WORK_CATEGORY) {
            this.category = WORK_CATEGORY
        }

        with(addAndUpdateTasksDialogLayoutBinding) {
            if ((category == DEFAULT_CATEGORY)) {
                selectCategoryTextView.text =
                    fragmentContext.getString(R.string.select_category_text)
                selectCategoryTextView.setTextColor(Color.parseColor("#9E9E9E"))
            } else if ((category == PERSONAL_CATEGORY)) {
                selectCategoryTextView.text = fragmentContext.getString(R.string.personal_text)
                selectCategoryTextView.setTextColor(blackColor)
            } else if ((category == WORK_CATEGORY)) {
                selectCategoryTextView.text = fragmentContext.getString(R.string.work_text)
                selectCategoryTextView.setTextColor(blackColor)
            }
        }

        if (popupWindow.isShowing) {
            popupWindow.dismiss()
        }
    }

    private class CustomTypeFaceSpan(
        family: String?,
        private val typeface: Typeface,
        private val black: Int
    ) : TypefaceSpan(family) {
        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.color = black
            applyCustomTypeFace(ds, typeface)
        }

        override fun updateMeasureState(paint: TextPaint) {
            super.updateMeasureState(paint)
            applyCustomTypeFace(paint, typeface)
        }

        private fun applyCustomTypeFace(paint: Paint, tf: Typeface) {
            val oldStyle: Int
            val old = paint.typeface
            oldStyle = old?.style ?: 0
            val fake = oldStyle and tf.style.inv()
            if ((fake and Typeface.BOLD) != 0) {
                paint.isFakeBoldText = true
            }
            if ((fake and Typeface.ITALIC) != 0) {
                paint.textSkewX = -0.25f
            }
            paint.setTypeface(tf)
        }
    }
}