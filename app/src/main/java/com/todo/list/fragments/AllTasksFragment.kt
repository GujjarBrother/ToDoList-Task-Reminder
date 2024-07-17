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
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
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
import com.todo.list.application.Application.Companion.typeface
import com.todo.list.base.BaseFragment
import com.todo.list.databinding.AddAndUpdateTasksDialogLayoutBinding
import com.todo.list.databinding.CustomPopupMenuLayoutBinding
import com.todo.list.databinding.DeleteTaskDialogLayoutBinding
import com.todo.list.databinding.FragmentAllTasksBinding
import com.todo.list.databinding.SortingDialogLayoutBinding
import com.todo.list.db.ToDoTask
import com.todo.list.db.ToDosDatabase
import com.todo.list.enums.TabsEnum
import com.todo.list.enums.TasksCategoriesEnum
import com.todo.list.listeners.StartAndStopFABAnimationAndSwitchBetweenLightAndDarkModeListener
import com.todo.list.repositories.TasksRepo
import com.todo.list.utils.CommonFunctions.applyAnimation
import com.todo.list.utils.CommonFunctions.isSomethingChanged
import com.todo.list.viewModels.TasksViewModel
import com.todo.list.viewModels.TasksViewModelFactory
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Collections
import java.util.Date
import java.util.Locale

class AllTasksFragment : BaseFragment(), View.OnClickListener {

    private lateinit var binding: FragmentAllTasksBinding
    private var category = 0
    private lateinit var errorColorStateList: ColorStateList
    private var aboveTempValue = 1
    private var belowTempValue = 7
    private var aboveSortedValue = 1
    private var belowSortedValue = 7
    private lateinit var allToDosTasksArrayList: ArrayList<ToDoTask>
    private var isAboveSortingValueSelected = false
    private var isBelowSortingValueSelected = false
    private var isForSorting = true
    private lateinit var adapter: TasksRecyclerViewAdapter
    private lateinit var popupWindow: PopupWindow
    private lateinit var addAndUpdateTasksDialogLayoutBinding: AddAndUpdateTasksDialogLayoutBinding
    private lateinit var toDoTask: ToDoTask
    private lateinit var tasksViewModel: TasksViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                            applyLightAndDarkMode()
                            if (::adapter.isInitialized) {
                                adapter.notifyDataSetChanged()
                            }
                        } else {
                            applyLightAndDarkMode()
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
                listAndGridViewStylesIV.setImageDrawable(listViewStyleImage)
                listAndGridViewStylesTV.text = getString(R.string.listview_text)
            } else {
                listAndGridViewStylesIV.setImageDrawable(gridViewStyleImage)
                listAndGridViewStylesTV.text = getString(R.string.gridview_text)
            }
            applyCustomFont()
            nothingInHereTV.typeface = typeface
            addNewTasksFAB.setOnClickListener(this@AllTasksFragment)
            sortingCV.setOnClickListener(this@AllTasksFragment)
            stylesCV.setOnClickListener(this@AllTasksFragment)
        }

        tasksViewModel = ViewModelProvider(this, TasksViewModelFactory(TasksRepo(ToDosDatabase.getDatabase(fragmentContext).dao())))[TasksViewModel::class.java]
        lifecycleScope.launch(Dispatchers.IO) {
            tasksViewModel.updateCompletedAndTimeUpTasks(true, Date(System.currentTimeMillis()))
        }
        tasksViewModel.getAllTasks(false).observe(viewLifecycleOwner) {
            allToDosTasksArrayList = it as ArrayList<ToDoTask>
            readAllTasks()
        }
    }

    private fun startFABAnimation() =
        binding.addNewTasksFAB.startAnimation(applyAnimation(fragmentContext))

    private fun stopFABAnimation() =
        binding.addNewTasksFAB.clearAnimation()

    override fun onResume() {
        super.onResume()

        applyLightAndDarkMode()

        if (isSomethingChanged) {
            if (::adapter.isInitialized) {
                adapter.isTextSizeChanged = true
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun applyCustomFont() {
        with(binding) {
            listAndGridViewStylesTV.typeface = typeface
            sortingTV.typeface = typeface
        }
    }

    private fun applyLightAndDarkMode() {
        with(binding) {
            if (prefs.isDarkModeEnable) {
                allTasksFragmentCV.setCardBackgroundColor(screensNightModeColor)
                nothingInHereTV.setTextColor(darkModeTextColor)
                addNewTasksFAB.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                addNewTasksFAB.setColorFilter(blackColor)
                sortingCV.setCardBackgroundColor(cardsNightModeColor)
                sortingIV.setColorFilter(lightBlueColor)
                sortingTV.setTextColor(darkModeTextColor)
                stylesCV.setCardBackgroundColor(cardsNightModeColor)
                listAndGridViewStylesIV.setColorFilter(lightBlueColor)
                listAndGridViewStylesTV.setTextColor(darkModeTextColor)
            } else {
                allTasksFragmentCV.setCardBackgroundColor(fragmentsCardViewsColor)
                addNewTasksFAB.setColorFilter(whiteColor)
                sortingCV.setCardBackgroundColor(whiteColor)
                sortingTV.setTextColor(blackColor)
                stylesCV.setCardBackgroundColor(whiteColor)
                listAndGridViewStylesTV.setTextColor(blackColor)
                when (prefs.colorSchemeValue) {
                    0 -> {
                        val defaultColorStateList = ColorStateList.valueOf(defaultColor)
                        errorColorStateList = defaultColorStateList
                        listAndGridViewStylesIV.setColorFilter(defaultColor)
                        sortingIV.setColorFilter(defaultColor)
                        nothingInHereTV.setTextColor(defaultColor)
                        addNewTasksFAB.backgroundTintList = defaultColorStateList
                    }

                    1 -> {
                        val darkYellowColorStateList = ColorStateList.valueOf(darkYellowColor)
                        errorColorStateList = darkYellowColorStateList
                        listAndGridViewStylesIV.setColorFilter(darkYellowColor)
                        sortingIV.setColorFilter(darkYellowColor)
                        nothingInHereTV.setTextColor(darkYellowColor)
                        addNewTasksFAB.backgroundTintList = darkYellowColorStateList
                    }

                    2 -> {
                        val orangeColorStateList = ColorStateList.valueOf(orangeColor)
                        errorColorStateList = orangeColorStateList
                        listAndGridViewStylesIV.setColorFilter(orangeColor)
                        sortingIV.setColorFilter(orangeColor)
                        nothingInHereTV.setTextColor(orangeColor)
                        addNewTasksFAB.backgroundTintList = orangeColorStateList
                    }

                    3 -> {
                        val lightGreenColorStateList = ColorStateList.valueOf(lightGreenColor)
                        errorColorStateList = lightGreenColorStateList
                        listAndGridViewStylesIV.setColorFilter(lightGreenColor)
                        sortingIV.setColorFilter(lightGreenColor)
                        nothingInHereTV.setTextColor(lightGreenColor)
                        addNewTasksFAB.backgroundTintList = lightGreenColorStateList
                    }

                    4 -> {
                        val blueColorStateList = ColorStateList.valueOf(blueColor)
                        errorColorStateList = blueColorStateList
                        listAndGridViewStylesIV.setColorFilter(blueColor)
                        sortingIV.setColorFilter(blueColor)
                        nothingInHereTV.setTextColor(blueColor)
                        addNewTasksFAB.backgroundTintList = blueColorStateList
                    }

                    5 -> {
                        val cyanColorStateList = ColorStateList.valueOf(cyanColor)
                        errorColorStateList = cyanColorStateList
                        listAndGridViewStylesIV.setColorFilter(cyanColor)
                        sortingIV.setColorFilter(cyanColor)
                        nothingInHereTV.setTextColor(cyanColor)
                        addNewTasksFAB.backgroundTintList = cyanColorStateList
                    }

                    6 -> {
                        val pinkColorStateList = ColorStateList.valueOf(pinkColor)
                        errorColorStateList = pinkColorStateList
                        listAndGridViewStylesIV.setColorFilter(pinkColor)
                        sortingIV.setColorFilter(pinkColor)
                        nothingInHereTV.setTextColor(pinkColor)
                        addNewTasksFAB.backgroundTintList = pinkColorStateList
                    }

                    7 -> {
                        val darkBlueColorStateList = ColorStateList.valueOf(darkBlueColor)
                        errorColorStateList = darkBlueColorStateList
                        listAndGridViewStylesIV.setColorFilter(darkBlueColor)
                        sortingIV.setColorFilter(darkBlueColor)
                        nothingInHereTV.setTextColor(darkBlueColor)
                        addNewTasksFAB.backgroundTintList = darkBlueColorStateList
                    }

                    8 -> {
                        val redColorStateList = ColorStateList.valueOf(redColor)
                        errorColorStateList = redColorStateList
                        listAndGridViewStylesIV.setColorFilter(redColor)
                        sortingIV.setColorFilter(redColor)
                        nothingInHereTV.setTextColor(redColor)
                        addNewTasksFAB.backgroundTintList = redColorStateList
                    }

                    9 -> {
                        val lightPurpleColorStateList = ColorStateList.valueOf(lightPurpleColor)
                        errorColorStateList = lightPurpleColorStateList
                        listAndGridViewStylesIV.setColorFilter(lightPurpleColor)
                        sortingIV.setColorFilter(lightPurpleColor)
                        nothingInHereTV.setTextColor(lightPurpleColor)
                        addNewTasksFAB.backgroundTintList = lightPurpleColorStateList
                    }
                }
            }
        }
    }

    private fun readAllTasks() {
        with(binding) {
            if (allToDosTasksArrayList.size > 0) {
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
                    allToDosTasksArrayList.sortWith(Comparator.comparing(ToDoTask::title))
                }
            } else if (belowSortedValue == 8) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    allToDosTasksArrayList.sortWith(Collections.reverseOrder(Comparator.comparing(ToDoTask::title)))
                }
            }
        } else if (aboveSortedValue == 2) {
            if (belowSortedValue == 7) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    allToDosTasksArrayList.sortWith(Comparator.comparing(ToDoTask::day))
                }
            } else if (belowSortedValue == 8) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    allToDosTasksArrayList.sortWith(Collections.reverseOrder(Comparator.comparing(ToDoTask::day)))
                }
            }
        } else if (aboveSortedValue == 3) {
            if (belowSortedValue == 7) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    allToDosTasksArrayList.sortWith(Comparator.comparing(ToDoTask::date))
                }
            } else if (belowSortedValue == 8) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    allToDosTasksArrayList.sortWith(Collections.reverseOrder(Comparator.comparing(ToDoTask::date)))
                }
            }
        } else if (aboveSortedValue == 4) {
            if (belowSortedValue == 7) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    allToDosTasksArrayList.sortWith(Comparator.comparing(ToDoTask::month))
                }
            } else if (belowSortedValue == 8) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    allToDosTasksArrayList.sortWith(Collections.reverseOrder(Comparator.comparing(ToDoTask::month)))
                }
            }
        } else if (aboveSortedValue == 5) {
            if (belowSortedValue == 7) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    allToDosTasksArrayList.sortWith(Comparator.comparing(ToDoTask::year))
                }
            } else if (belowSortedValue == 8) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    allToDosTasksArrayList.sortWith(Collections.reverseOrder(Comparator.comparing(ToDoTask::year)))
                }
            }
        } else if (aboveSortedValue == 6) {
            if (belowSortedValue == 7) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    allToDosTasksArrayList.sortWith(Comparator.comparing(ToDoTask::time))
                }
            } else if (belowSortedValue == 8) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    allToDosTasksArrayList.sortWith(Collections.reverseOrder(Comparator.comparing(ToDoTask::time)))
                }
            }
        }
    }

    private fun displayAllTasksOnRecyclerView() {
        if (isForSorting) {
            sortAnArrayList()
        }
        val colorsSchemeArray = intArrayOf(
            defaultColor, darkYellowColor, orangeColor, lightGreenColor,
            blueColor, cyanColor, pinkColor, darkBlueColor, redColor, lightPurpleColor
        )

        if (!::adapter.isInitialized) {
            adapter = TasksRecyclerViewAdapter(
                colorsSchemeArray, true, TabsEnum.TASKS_TAB.ordinal, { toDoTask ->
                    openTaskDetailActivity(toDoTask)
                }, { toDoTask, view, color ->
                    var popupMenu: PopupMenu? = null
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        popupMenu = PopupMenu(
                            fragmentContext, view, Gravity.CENTER, 0,
                            if (prefs.isDarkModeEnable) {
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
                            showAddNewAndUpdateTaskDialog(2)
                        } else if (itemId == R.id.delete_item) {
                            showDeleteTaskDialog(toDoTask)
                        }
                        false
                    }
                    val menu = popupMenu?.menu as Menu
                    for (i in 0 until menu.size()) {
                        applyFontToPopupMenuItem(menu.getItem(i))

//                        Here, We Change The Color Of PopUpMenu Items Icons...
                        val menuItem = menu.getItem(i)
                        val popUpMenuIconDrawable = menuItem.icon
                        if (popUpMenuIconDrawable != null) {
                            if (prefs.isDarkModeEnable) {
                                DrawableCompat.setTint(popUpMenuIconDrawable, lightBlueColor)
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
                adapter.submitList(allToDosTasksArrayList)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.addNewTasksFAB -> {
                showAddNewAndUpdateTaskDialog(1)
            }

            R.id.sortingCV -> {
                showSortingDialog()
            }

            R.id.stylesCV -> {
                with(binding) {
                    isForSorting = false
                    if (prefs.allTasksStyleValue) {
                        listAndGridViewStylesIV.setImageDrawable(gridViewStyleImage)
                        listAndGridViewStylesTV.setText(R.string.gridview_text)
                        prefs.allTasksStyleValue = false
                        displayAllTasksOnRecyclerView()
                    } else {
                        listAndGridViewStylesIV.setImageDrawable(listViewStyleImage)
                        listAndGridViewStylesTV.setText(R.string.listview_text)
                        prefs.allTasksStyleValue = true
                        displayAllTasksOnRecyclerView()
                    }
                }
            }
        }
    }

    private fun showAddNewAndUpdateTaskDialog(fromWhereInvoked: Int) {
        addAndUpdateTasksDialogLayoutBinding = AddAndUpdateTasksDialogLayoutBinding.inflate(layoutInflater)

        val addTasksDialogBuilder = AlertDialog.Builder(fragmentContext)
        with(addTasksDialogBuilder) {
            setView(addAndUpdateTasksDialogLayoutBinding.root)
            setCancelable(true)
            setOnDismissListener {
                startFABAnimation()
            }
        }
        val addTasksAlertDialog = addTasksDialogBuilder.create()

        if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed && !addTasksAlertDialog.isShowing) {
            addTasksAlertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            addTasksAlertDialog.window?.setWindowAnimations(R.style.dialogBoxesAnimation)
            addTasksAlertDialog.show()
        }

        with(addAndUpdateTasksDialogLayoutBinding) {
            infoTV.isSelected = true
            stopFABAnimation()
            applyCustomFontOnAddAndUpdateTasksDialogViews(this)
            applyLightAndDarkModeOnAddAndUpdateTasksDialogViews(this)

            if (fromWhereInvoked == 2) {
                addAndEditIV.setImageResource(R.drawable.update_image)
                addAndUpdateToDoTaskTV.text = getString(R.string.update_todo_task_text)
                infoTV.text = getString(R.string.update_info_message_text)
                titleTIL.editText?.setText(toDoTask.title)
                titleTIL.editText?.setSelection(toDoTask.title.length)
                descriptionTIL.editText?.setText(toDoTask.description)
                descriptionTIL.editText?.setSelection(toDoTask.description.length)
                dayOfWeekTIL.editText?.setText(toDoTask.day)
                dayOfWeekTIL.editText?.setSelection(toDoTask.day.length)
                dateTIL.editText?.setText(
                    String.format("%s %s, %s", toDoTask.month, toDoTask.date, toDoTask.year)
                )
                timeTIL.editText?.setText(toDoTask.time)
                if (toDoTask.category == TasksCategoriesEnum.DEFAULT_CATEGORY.ordinal || toDoTask.category == TasksCategoriesEnum.PERSONAL_CATEGORY.ordinal) {
                    selectCategoryTV.text = getString(R.string.personal_text)
                } else if (toDoTask.category == TasksCategoriesEnum.WORK_CATEGORY.ordinal) {
                    selectCategoryTV.text = getString(R.string.work_text)
                }
                when(prefs.isDarkModeEnable) {
                    true -> selectCategoryTV.setTextColor(whiteColor)
                    false -> selectCategoryTV.setTextColor(blackColor)
                }
                saveAndUpdateButton.text = getString(R.string.update_text)
            }

            selectCategoryLayout.setOnClickListener { view: View ->
                showCustomPopupForCategorySelection(view, fromWhereInvoked)
            }

            crossIV.setOnClickListener { _: View? ->
                if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed) {
                    addTasksAlertDialog.dismiss()
                }
                startFABAnimation()
            }

            dateTIET.setOnClickListener { _: View? ->
                showMaterialDatePicker(addAndUpdateTasksDialogLayoutBinding)
            }

            timeTIET.setOnClickListener { _: View? ->
                showMaterialTimePicker(addAndUpdateTasksDialogLayoutBinding)
            }

            saveAndUpdateButton.setOnClickListener { _: View? ->
                val title = titleTIL.editText?.text.toString().trim()
                val description = descriptionTIL.editText?.text.toString().trim()
                val dayOfWeek: String = dayOfWeekTIL.editText?.text.toString().trim()
                val date = dateTIL.editText?.text.toString().trim()
                val time = timeTIL.editText?.text.toString().trim()
                if (TextUtils.isEmpty(title)) {
                    titleTIL.error = getString(R.string.title_error_text)
                } else if (TextUtils.isEmpty(description)) {
                    titleTIL.error = null
                    descriptionTIL.error = getString(R.string.description_error_text)
                } else if (TextUtils.isEmpty(dayOfWeek)) {
                    descriptionTIL.error = null
                    dayOfWeekTIL.error = getString(R.string.day_of_week_error_text)
                } else if (TextUtils.isEmpty(date)) {
                    dayOfWeekTIL.error = null
                    dateTIL.error = getString(R.string.select_date_error_text)
                } else if (TextUtils.isEmpty(time)) {
                    dateTIL.error = null
                    timeTIL.error = getString(R.string.select_time_error_text)
                } else {
                    titleTIL.error = null
                    descriptionTIL.error = null
                    dayOfWeekTIL.error = null
                    dateTIL.error = null
                    timeTIL.error = null

                    val parse = simpleDateFormat.parse(date) as Date
                    val dateSDF = SimpleDateFormat("dd", Locale.getDefault())
                    val monthSDF = SimpleDateFormat("MMM", Locale.getDefault())
                    val yearSDF = SimpleDateFormat("yyyy", Locale.getDefault())
                    val completeDateAndTime = "${monthSDF.format(parse)} ${dateSDF.format(parse)}, ${yearSDF.format(parse)} $time"
                    val completeDateAndTimeDate = simpleDateAndTimeFormat.parse(completeDateAndTime) as Date

                    if (fromWhereInvoked == 1) {
                        val toDoTask: ToDoTask
                        if (dateSDF.format(parse).isNotEmpty() && monthSDF.format(parse).isNotEmpty()
                            && yearSDF.format(parse).isNotEmpty()) {
                            toDoTask = ToDoTask(0, dayOfWeek, dateSDF.format(parse), monthSDF.format(parse),
                                yearSDF.format(parse), title, description, time, category, completeDateAndTimeDate, false
                            )
                            lifecycleScope.launch(Dispatchers.IO) {
                                val isTaskAlreadySaved = tasksViewModel.isTaskAlreadySaved(toDoTask.day, toDoTask.date,
                                    toDoTask.month, toDoTask.year, toDoTask.title, toDoTask.description, toDoTask.time,
                                    toDoTask.category).await()
                                if (isTaskAlreadySaved >= 1) {
                                    withContext(Dispatchers.Main) {
                                        Toasty.info(fragmentContext, getString(R.string.this_task_is_already_saved_toast_text),
                                            Toasty.LENGTH_LONG).show()
                                    }
                                } else if (parse.time <= System.currentTimeMillis()) {
                                    withContext(Dispatchers.Main) {
                                        Toasty.error(fragmentContext, getString(R.string.please_select_future_date_time_toast_text),
                                            Toasty.LENGTH_LONG).show()
                                    }
                                } else {
                                    val newlyAddedTaskID = tasksViewModel.saveTask(toDoTask).await()
                                    if (newlyAddedTaskID >= 1) {
                                        prefs.category = category
                                        withContext(Dispatchers.Main) {
                                            Toasty.success(fragmentContext, getString(R.string.task_is_saved_successfully_toast_text),
                                                Toasty.LENGTH_LONG).show()
                                            titleTIL.editText?.text = null
                                            descriptionTIL.editText?.text = null
                                            dayOfWeekTIL.editText?.text = null
                                            dateTIL.editText?.text = null
                                            timeTIL.editText?.text = null
                                            titleTIL.editText?.requestFocus()
                                            category = 0
                                            if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed) {
                                                addTasksAlertDialog.dismiss()
                                            }
                                            startFABAnimation()
                                        }
                                    } else {
                                        withContext(Dispatchers.Main) {
                                            Toasty.error(fragmentContext, getString(R.string.task_is_not_saved_successfully_toast_text),
                                                Toasty.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            }
                        }
                    } else if (fromWhereInvoked == 2) {
                        val updatedToDoTask = ToDoTask(
                            toDoTask.id, dayOfWeek, dateSDF.format(parse), monthSDF.format(parse),
                            yearSDF.format(parse), title, description, time, category,
                            completeDateAndTimeDate, false
                        )
                        lifecycleScope.launch(Dispatchers.IO) {
                            if (updatedToDoTask != toDoTask) {
                                val isUpdated = tasksViewModel.updateTask(updatedToDoTask).await()
                                if (isUpdated == 1) {
                                    withContext(Dispatchers.Main) {
                                        Toasty.success(fragmentContext, getString(R.string.updated_successfully_toast_text),
                                            Toasty.LENGTH_LONG).show()
                                        prefs.category = category
                                        if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed) {
                                            addTasksAlertDialog.dismiss()
                                        }
                                        startFABAnimation()
                                        category = 0
                                    }
                                } else {
                                    withContext(Dispatchers.Main) {
                                        Toasty.success(fragmentContext, getString(R.string.not_updated_successfully_toast_text),
                                            Toasty.LENGTH_LONG).show()
                                    }
                                }
                            } else if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed) {
                                addTasksAlertDialog.dismiss()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showCustomPopupForCategorySelection(view: View, fromWhereInvoked: Int) {
        val customPopupMenuLayoutBinding = CustomPopupMenuLayoutBinding.inflate(layoutInflater)

        if (prefs.isDarkModeEnable) {
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
            add(TasksCategoriesEnum.DEFAULT_CATEGORY.ordinal)
            add(TasksCategoriesEnum.PERSONAL_CATEGORY.ordinal)
            add(TasksCategoriesEnum.WORK_CATEGORY.ordinal)
            if (fromWhereInvoked == 2) {
                removeAt(0)
            }
        }

        val categoryAdapter = CategoryAdapter("Category") { category, _ ->
            if (category == TasksCategoriesEnum.DEFAULT_CATEGORY.ordinal || category == TasksCategoriesEnum.PERSONAL_CATEGORY.ordinal) {
                this.category = TasksCategoriesEnum.PERSONAL_CATEGORY.ordinal
            } else if (category == TasksCategoriesEnum.WORK_CATEGORY.ordinal) {
                this.category = TasksCategoriesEnum.WORK_CATEGORY.ordinal
            }

            with(addAndUpdateTasksDialogLayoutBinding) {
                if ((category == TasksCategoriesEnum.DEFAULT_CATEGORY.ordinal)) {
                    selectCategoryTV.text = fragmentContext.getString(R.string.select_category_text)
                    selectCategoryTV.setTextColor(Color.parseColor("#9E9E9E"))
                } else if ((category == TasksCategoriesEnum.PERSONAL_CATEGORY.ordinal)) {
                    selectCategoryTV.text = fragmentContext.getString(R.string.personal_text)
                    when(prefs.isDarkModeEnable) {
                        true -> selectCategoryTV.setTextColor(whiteColor)
                        false -> selectCategoryTV.setTextColor(blackColor)
                    }
                } else if ((category == TasksCategoriesEnum.WORK_CATEGORY.ordinal)) {
                    selectCategoryTV.text = fragmentContext.getString(R.string.work_text)
                    when(prefs.isDarkModeEnable) {
                        true -> selectCategoryTV.setTextColor(whiteColor)
                        false -> selectCategoryTV.setTextColor(blackColor)
                    }
                }
            }

            if (popupWindow.isShowing) {
                popupWindow.dismiss()
            }
        }
        customPopupMenuLayoutBinding.customPopUpMenuRV.adapter = categoryAdapter
        categoryAdapter.submitList(categoryArrayList)
        popupWindow.showAsDropDown(view)
    }

    private fun showSortingDialog() {
        val sortingDialogLayoutBinding = SortingDialogLayoutBinding.inflate(layoutInflater)

        val sortingDialogBuilder = AlertDialog.Builder(fragmentContext)
        with(sortingDialogBuilder) {
            setView(sortingDialogLayoutBinding.root)
            setCancelable(true)
            setOnDismissListener {
                startFABAnimation()
            }
        }
        val sortingAlertDialog = sortingDialogBuilder.create()
        if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed && !sortingAlertDialog.isShowing) {
            sortingAlertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            sortingAlertDialog.window?.setWindowAnimations(R.style.dialogBoxesAnimation)
            sortingAlertDialog.show()
        }

        with(sortingDialogLayoutBinding) {
            stopFABAnimation()
            applyCustomFontOnSortingDialogViews(this)
            applyLightAndDarkModeOnSortingDialogViews(this)

            val allTasksSortingArray = prefs.allTasksSortingValues
            aboveSortedValue = allTasksSortingArray[0]
            belowSortedValue = allTasksSortingArray[1]

            if (aboveSortedValue == 1) {
                titleRB.isChecked = true
                if (belowSortedValue == 7) {
                    ascendingAToZRB.isChecked = true
                } else if (belowSortedValue == 8) {
                    descendingZToARB.isChecked = true
                }
            } else if (aboveSortedValue == 2) {
                dayOfWeekRB.isChecked = true
                if (belowSortedValue == 7) {
                    ascendingAToZRB.isChecked = true
                } else if (belowSortedValue == 8) {
                    descendingZToARB.isChecked = true
                }
            } else if (aboveSortedValue == 3) {
                dateRB.isChecked = true
                if (belowSortedValue == 7) {
                    ascendingAToZRB.isChecked = true
                } else if (belowSortedValue == 8) {
                    descendingZToARB.isChecked = true
                }
            } else if (aboveSortedValue == 4) {
                monthRB.isChecked = true
                if (belowSortedValue == 7) {
                    ascendingAToZRB.isChecked = true
                } else if (belowSortedValue == 8) {
                    descendingZToARB.isChecked = true
                }
            } else if (aboveSortedValue == 5) {
                yearRB.isChecked = true
                if (belowSortedValue == 7) {
                    ascendingAToZRB.isChecked = true
                } else if (belowSortedValue == 8) {
                    descendingZToARB.isChecked = true
                }
            } else if (aboveSortedValue == 6) {
                timeRB.isChecked = true
                if (belowSortedValue == 7) {
                    ascendingAToZRB.isChecked = true
                } else if (belowSortedValue == 8) {
                    descendingZToARB.isChecked = true
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

            sortRG.setOnCheckedChangeListener { _: RadioGroup?, checkedId: Int ->
                isAboveSortingValueSelected = true
                when (checkedId) {
                    R.id.titleRB -> {
                        aboveTempValue = 1
                    }

                    R.id.dayOfWeekRB -> {
                        aboveTempValue = 2
                    }

                    R.id.dateRB -> {
                        aboveTempValue = 3
                    }

                    R.id.monthRB -> {
                        aboveTempValue = 4
                    }

                    R.id.yearRB -> {
                        aboveTempValue = 5
                    }

                    R.id.timeRB -> {
                        aboveTempValue = 6
                    }
                }

                if (aboveTempValue == aboveSortedValue) {
                    if (belowSortedValue == 7) {
                        ascendingAToZRB.isChecked = true
                    } else if (belowSortedValue == 8) {
                        descendingZToARB.isChecked = true
                    }
                } else {
                    ascendingDescendingRG.clearCheck()
                }
            }

            ascendingDescendingRG.setOnCheckedChangeListener { _: RadioGroup?, checkedId: Int ->
                isBelowSortingValueSelected = true
                if (aboveTempValue == 1) {
                    if (checkedId == R.id.ascendingAToZRB) {
                        belowTempValue = 7
                    } else if (checkedId == R.id.descendingZToARB) {
                        belowTempValue = 8
                    }
                } else if (aboveTempValue == 2) {
                    if (checkedId == R.id.ascendingAToZRB) {
                        belowTempValue = 7
                    } else if (checkedId == R.id.descendingZToARB) {
                        belowTempValue = 8
                    }
                } else if (aboveTempValue == 3) {
                    if (checkedId == R.id.ascendingAToZRB) {
                        belowTempValue = 7
                    } else if (checkedId == R.id.descendingZToARB) {
                        belowTempValue = 8
                    }
                } else if (aboveTempValue == 4) {
                    if (checkedId == R.id.ascendingAToZRB) {
                        belowTempValue = 7
                    } else if (checkedId == R.id.descendingZToARB) {
                        belowTempValue = 8
                    }
                } else if (aboveTempValue == 5) {
                    if (checkedId == R.id.ascendingAToZRB) {
                        belowTempValue = 7
                    } else if (checkedId == R.id.descendingZToARB) {
                        belowTempValue = 8
                    }
                } else if (aboveTempValue == 6) {
                    if (checkedId == R.id.ascendingAToZRB) {
                        belowTempValue = 7
                    } else if (checkedId == R.id.descendingZToARB) {
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
        isForSorting = true
        displayAllTasksOnRecyclerView()
        startFABAnimation()
    }

    private fun applyLightAndDarkModeOnSortingDialogViews(
        sortingDialogLayoutBinding: SortingDialogLayoutBinding
    ) {
        with(sortingDialogLayoutBinding) {
            if (prefs.isDarkModeEnable) {
                rootLayout.background.colorFilter = PorterDuffColorFilter(screensNightModeColor, PorterDuff.Mode.SRC_IN)
                sortByTV.setTextColor(lightBlueColor)
                titleRB.buttonTintList = ColorStateList.valueOf(lightBlueColor)
                titleRB.setTextColor(whiteColor)
                dayOfWeekRB.buttonTintList = ColorStateList.valueOf(lightBlueColor)
                dayOfWeekRB.setTextColor(whiteColor)
                dateRB.buttonTintList = ColorStateList.valueOf(lightBlueColor)
                dateRB.setTextColor(whiteColor)
                monthRB.buttonTintList = ColorStateList.valueOf(lightBlueColor)
                monthRB.setTextColor(whiteColor)
                yearRB.buttonTintList = ColorStateList.valueOf(lightBlueColor)
                yearRB.setTextColor(whiteColor)
                timeRB.buttonTintList = ColorStateList.valueOf(lightBlueColor)
                timeRB.setTextColor(whiteColor)
                ascendingAToZRB.buttonTintList = ColorStateList.valueOf(lightBlueColor)
                ascendingAToZRB.setTextColor(whiteColor)
                descendingZToARB.buttonTintList = ColorStateList.valueOf(lightBlueColor)
                descendingZToARB.setTextColor(whiteColor)
                cancelButton.strokeColor = ColorStateList.valueOf(lightBlueColor)
                cancelButton.setTextColor(lightBlueColor)
                sortButton.setBackgroundColor(lightBlueColor)
                sortButton.setTextColor(blackColor)
            } else {
                when (prefs.colorSchemeValue) {
                    0 -> {
                        sortByTV.setTextColor(defaultColor)
                        titleRB.buttonTintList = ColorStateList.valueOf(defaultColor)
                        dayOfWeekRB.buttonTintList = ColorStateList.valueOf(defaultColor)
                        dateRB.buttonTintList = ColorStateList.valueOf(defaultColor)
                        monthRB.buttonTintList = ColorStateList.valueOf(defaultColor)
                        yearRB.buttonTintList = ColorStateList.valueOf(defaultColor)
                        timeRB.buttonTintList = ColorStateList.valueOf(defaultColor)
                        ascendingAToZRB.buttonTintList = ColorStateList.valueOf(defaultColor)
                        descendingZToARB.buttonTintList = ColorStateList.valueOf(defaultColor)
                        cancelButton.strokeColor = ColorStateList.valueOf(defaultColor)
                        cancelButton.setTextColor(defaultColor)
                        sortButton.setBackgroundColor(defaultColor)
                        sortButton.setTextColor(whiteColor)
                    }

                    1 -> {
                        sortByTV.setTextColor(darkYellowColor)
                        titleRB.buttonTintList = ColorStateList.valueOf(darkYellowColor)
                        dayOfWeekRB.buttonTintList = ColorStateList.valueOf(darkYellowColor)
                        dateRB.buttonTintList = ColorStateList.valueOf(darkYellowColor)
                        monthRB.buttonTintList = ColorStateList.valueOf(darkYellowColor)
                        yearRB.buttonTintList = ColorStateList.valueOf(darkYellowColor)
                        timeRB.buttonTintList = ColorStateList.valueOf(darkYellowColor)
                        ascendingAToZRB.buttonTintList = ColorStateList.valueOf(darkYellowColor)
                        descendingZToARB.buttonTintList = ColorStateList.valueOf(darkYellowColor)
                        cancelButton.strokeColor = ColorStateList.valueOf(darkYellowColor)
                        cancelButton.setTextColor(darkYellowColor)
                        sortButton.setBackgroundColor(darkYellowColor)
                        sortButton.setTextColor(whiteColor)
                    }

                    2 -> {
                        sortByTV.setTextColor(orangeColor)
                        titleRB.buttonTintList = ColorStateList.valueOf(orangeColor)
                        dayOfWeekRB.buttonTintList = ColorStateList.valueOf(orangeColor)
                        dateRB.buttonTintList = ColorStateList.valueOf(orangeColor)
                        monthRB.buttonTintList = ColorStateList.valueOf(orangeColor)
                        yearRB.buttonTintList = ColorStateList.valueOf(orangeColor)
                        timeRB.buttonTintList = ColorStateList.valueOf(orangeColor)
                        ascendingAToZRB.buttonTintList = ColorStateList.valueOf(orangeColor)
                        descendingZToARB.buttonTintList = ColorStateList.valueOf(orangeColor)
                        cancelButton.strokeColor = ColorStateList.valueOf(orangeColor)
                        cancelButton.setTextColor(orangeColor)
                        sortButton.setBackgroundColor(orangeColor)
                        sortButton.setTextColor(whiteColor)
                    }

                    3 -> {
                        sortByTV.setTextColor(lightGreenColor)
                        titleRB.buttonTintList = ColorStateList.valueOf(lightGreenColor)
                        dayOfWeekRB.buttonTintList = ColorStateList.valueOf(lightGreenColor)
                        dateRB.buttonTintList = ColorStateList.valueOf(lightGreenColor)
                        monthRB.buttonTintList = ColorStateList.valueOf(lightGreenColor)
                        yearRB.buttonTintList = ColorStateList.valueOf(lightGreenColor)
                        timeRB.buttonTintList = ColorStateList.valueOf(lightGreenColor)
                        ascendingAToZRB.buttonTintList = ColorStateList.valueOf(lightGreenColor)
                        descendingZToARB.buttonTintList = ColorStateList.valueOf(lightGreenColor)
                        cancelButton.strokeColor = ColorStateList.valueOf(lightGreenColor)
                        cancelButton.setTextColor(lightGreenColor)
                        sortButton.setBackgroundColor(lightGreenColor)
                        sortButton.setTextColor(whiteColor)
                    }

                    4 -> {
                        sortByTV.setTextColor(blueColor)
                        titleRB.buttonTintList = ColorStateList.valueOf(blueColor)
                        dayOfWeekRB.buttonTintList = ColorStateList.valueOf(blueColor)
                        dateRB.buttonTintList = ColorStateList.valueOf(blueColor)
                        monthRB.buttonTintList = ColorStateList.valueOf(blueColor)
                        yearRB.buttonTintList = ColorStateList.valueOf(blueColor)
                        timeRB.buttonTintList = ColorStateList.valueOf(blueColor)
                        ascendingAToZRB.buttonTintList = ColorStateList.valueOf(blueColor)
                        descendingZToARB.buttonTintList = ColorStateList.valueOf(blueColor)
                        cancelButton.strokeColor = ColorStateList.valueOf(blueColor)
                        cancelButton.setTextColor(blueColor)
                        sortButton.setBackgroundColor(blueColor)
                        sortButton.setTextColor(whiteColor)
                    }

                    5 -> {
                        sortByTV.setTextColor(cyanColor)
                        titleRB.buttonTintList = ColorStateList.valueOf(cyanColor)
                        dayOfWeekRB.buttonTintList = ColorStateList.valueOf(cyanColor)
                        dateRB.buttonTintList = ColorStateList.valueOf(cyanColor)
                        monthRB.buttonTintList = ColorStateList.valueOf(cyanColor)
                        yearRB.buttonTintList = ColorStateList.valueOf(cyanColor)
                        timeRB.buttonTintList = ColorStateList.valueOf(cyanColor)
                        ascendingAToZRB.buttonTintList = ColorStateList.valueOf(cyanColor)
                        descendingZToARB.buttonTintList = ColorStateList.valueOf(cyanColor)
                        cancelButton.strokeColor = ColorStateList.valueOf(cyanColor)
                        cancelButton.setTextColor(cyanColor)
                        sortButton.setBackgroundColor(cyanColor)
                        sortButton.setTextColor(whiteColor)
                    }

                    6 -> {
                        sortByTV.setTextColor(pinkColor)
                        titleRB.buttonTintList = ColorStateList.valueOf(pinkColor)
                        dayOfWeekRB.buttonTintList = ColorStateList.valueOf(pinkColor)
                        dateRB.buttonTintList = ColorStateList.valueOf(pinkColor)
                        monthRB.buttonTintList = ColorStateList.valueOf(pinkColor)
                        yearRB.buttonTintList = ColorStateList.valueOf(pinkColor)
                        timeRB.buttonTintList = ColorStateList.valueOf(pinkColor)
                        ascendingAToZRB.buttonTintList = ColorStateList.valueOf(pinkColor)
                        descendingZToARB.buttonTintList = ColorStateList.valueOf(pinkColor)
                        cancelButton.strokeColor = ColorStateList.valueOf(pinkColor)
                        cancelButton.setTextColor(pinkColor)
                        sortButton.setBackgroundColor(pinkColor)
                        sortButton.setTextColor(whiteColor)
                    }

                    7 -> {
                        sortByTV.setTextColor(darkBlueColor)
                        titleRB.buttonTintList = ColorStateList.valueOf(darkBlueColor)
                        dayOfWeekRB.buttonTintList = ColorStateList.valueOf(darkBlueColor)
                        dateRB.buttonTintList = ColorStateList.valueOf(darkBlueColor)
                        monthRB.buttonTintList = ColorStateList.valueOf(darkBlueColor)
                        yearRB.buttonTintList = ColorStateList.valueOf(darkBlueColor)
                        timeRB.buttonTintList = ColorStateList.valueOf(darkBlueColor)
                        ascendingAToZRB.buttonTintList = ColorStateList.valueOf(darkBlueColor)
                        descendingZToARB.buttonTintList = ColorStateList.valueOf(darkBlueColor)
                        cancelButton.strokeColor = ColorStateList.valueOf(darkBlueColor)
                        cancelButton.setTextColor(darkBlueColor)
                        sortButton.setBackgroundColor(darkBlueColor)
                        sortButton.setTextColor(whiteColor)
                    }

                    8 -> {
                        sortByTV.setTextColor(redColor)
                        titleRB.buttonTintList = ColorStateList.valueOf(redColor)
                        dayOfWeekRB.buttonTintList = ColorStateList.valueOf(redColor)
                        dateRB.buttonTintList = ColorStateList.valueOf(redColor)
                        monthRB.buttonTintList = ColorStateList.valueOf(redColor)
                        yearRB.buttonTintList = ColorStateList.valueOf(redColor)
                        timeRB.buttonTintList = ColorStateList.valueOf(redColor)
                        ascendingAToZRB.buttonTintList = ColorStateList.valueOf(redColor)
                        descendingZToARB.buttonTintList = ColorStateList.valueOf(redColor)
                        cancelButton.strokeColor = ColorStateList.valueOf(redColor)
                        cancelButton.setTextColor(redColor)
                        sortButton.setBackgroundColor(redColor)
                        sortButton.setTextColor(whiteColor)
                    }

                    9 -> {
                        sortByTV.setTextColor(lightPurpleColor)
                        titleRB.buttonTintList = ColorStateList.valueOf(lightPurpleColor)
                        dayOfWeekRB.buttonTintList = ColorStateList.valueOf(lightPurpleColor)
                        dateRB.buttonTintList = ColorStateList.valueOf(lightPurpleColor)
                        monthRB.buttonTintList = ColorStateList.valueOf(lightPurpleColor)
                        yearRB.buttonTintList = ColorStateList.valueOf(lightPurpleColor)
                        timeRB.buttonTintList = ColorStateList.valueOf(lightPurpleColor)
                        ascendingAToZRB.buttonTintList = ColorStateList.valueOf(lightPurpleColor)
                        descendingZToARB.buttonTintList = ColorStateList.valueOf(lightPurpleColor)
                        cancelButton.strokeColor = ColorStateList.valueOf(lightPurpleColor)
                        cancelButton.setTextColor(lightPurpleColor)
                        sortButton.setBackgroundColor(lightPurpleColor)
                        sortButton.setTextColor(whiteColor)
                    }
                }
            }
        }
    }

    private fun applyCustomFontOnSortingDialogViews(sortingDialogLayoutBinding: SortingDialogLayoutBinding) {
        with(sortingDialogLayoutBinding) {
            sortByTV.typeface = typeface
            titleRB.typeface = typeface
            dayOfWeekRB.typeface = typeface
            dateRB.typeface = typeface
            monthRB.typeface = typeface
            yearRB.typeface = typeface
            timeRB.typeface = typeface
            ascendingAToZRB.typeface = typeface
            descendingZToARB.typeface = typeface
            cancelButton.typeface = typeface
            sortButton.typeface = typeface
        }
    }

    private fun applyLightAndDarkModeOnAddAndUpdateTasksDialogViews(
        addAndUpdateTasksDialogLayoutBinding: AddAndUpdateTasksDialogLayoutBinding
    ) {
        with(addAndUpdateTasksDialogLayoutBinding) {
            if (prefs.isDarkModeEnable) {
                rootLayout.background.colorFilter = PorterDuffColorFilter(screensNightModeColor, PorterDuff.Mode.SRC_IN)
                crossIV.setColorFilter(lightBlueColor)
                addAndEditIV.setColorFilter(lightBlueColor)
                addAndUpdateToDoTaskTV.setTextColor(lightBlueColor)
                infoTV.setTextColor(darkModeTextColor)

//            Here, We Change The Box Stroke Color Of TextInputLayout When That is Un-Focused...
                titleTIL.setBoxStrokeColorStateList(textInputLayoutBoxStrokeDarkModeColor)
                descriptionTIL.setBoxStrokeColorStateList(textInputLayoutBoxStrokeDarkModeColor)
                dayOfWeekTIL.setBoxStrokeColorStateList(textInputLayoutBoxStrokeDarkModeColor)
                dateTIL.setBoxStrokeColorStateList(textInputLayoutBoxStrokeDarkModeColor)
                timeTIL.setBoxStrokeColorStateList(textInputLayoutBoxStrokeDarkModeColor)

                titleTIL.boxStrokeColor = whiteColor
                titleTIL.boxStrokeErrorColor = whiteColorStateList
                titleTIL.setStartIconTintList(ColorStateList.valueOf(lightBlueColor))
                titleTIL.setErrorIconTintList(ColorStateList.valueOf(lightBlueColor))
                titleTIL.setErrorTextColor(whiteColorStateList)
                titleTIL.hintTextColor = whiteColorStateList
                titleTIL.boxStrokeErrorColor = whiteColorStateList
                titleTIET.setTextColor(whiteColor)

                descriptionTIL.boxStrokeColor = whiteColor
                descriptionTIL.boxStrokeErrorColor = whiteColorStateList
                descriptionTIL.setStartIconTintList(ColorStateList.valueOf(lightBlueColor))
                descriptionTIL.setErrorIconTintList(ColorStateList.valueOf(lightBlueColor))
                descriptionTIL.setErrorTextColor(whiteColorStateList)
                descriptionTIL.hintTextColor = whiteColorStateList
                descriptionTIL.boxStrokeErrorColor = whiteColorStateList
                descriptionTIET.setTextColor(whiteColor)
                
                dayOfWeekTIL.boxStrokeColor = whiteColor
                dayOfWeekTIL.boxStrokeErrorColor = whiteColorStateList
                dayOfWeekTIL.setStartIconTintList(ColorStateList.valueOf(lightBlueColor))
                dayOfWeekTIL.setErrorIconTintList(ColorStateList.valueOf(lightBlueColor))
                dayOfWeekTIL.setErrorTextColor(whiteColorStateList)
                dayOfWeekTIL.hintTextColor = whiteColorStateList
                dayOfWeekTIL.boxStrokeErrorColor = whiteColorStateList
                dayOfWeekTIET.setTextColor(whiteColor)

                dateTIL.boxStrokeColor = whiteColor
                dateTIL.boxStrokeErrorColor = whiteColorStateList
                dateTIL.setStartIconTintList(ColorStateList.valueOf(lightBlueColor))
                dateTIL.setErrorIconTintList(ColorStateList.valueOf(lightBlueColor))
                dateTIL.setErrorTextColor(whiteColorStateList)
                dateTIL.hintTextColor = whiteColorStateList
                dateTIL.boxStrokeErrorColor = whiteColorStateList
                dateTIET.setTextColor(whiteColor)

                timeTIL.boxStrokeColor = whiteColor
                timeTIL.boxStrokeErrorColor = whiteColorStateList
                timeTIL.setStartIconTintList(ColorStateList.valueOf(lightBlueColor))
                timeTIL.setErrorIconTintList(ColorStateList.valueOf(lightBlueColor))
                timeTIL.setErrorTextColor(whiteColorStateList)
                timeTIL.hintTextColor = whiteColorStateList
                timeTIL.boxStrokeErrorColor = whiteColorStateList
                timeTIET.setTextColor(whiteColor)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    titleTIL.cursorColor = whiteColorStateList
                    descriptionTIL.cursorColor = whiteColorStateList
                    dayOfWeekTIL.cursorColor = whiteColorStateList
                    dateTIL.cursorColor = whiteColorStateList
                    timeTIL.cursorColor = whiteColorStateList
                }

                selectCategoryLayout.background.colorFilter = PorterDuffColorFilter(darkModeTextColor, PorterDuff.Mode.SRC_IN)
                dropDownImageView.setColorFilter(lightBlueColor)
                saveAndUpdateButton.setBackgroundColor(lightBlueColor)
                saveAndUpdateButton.setTextColor(blackColor)
            } else {
                when (prefs.colorSchemeValue) {
                    0 -> {
                        crossIV.setColorFilter(defaultColor)
                        addAndEditIV.setColorFilter(defaultColor)
                        dropDownImageView.setColorFilter(defaultColor)
                        saveAndUpdateButton.setBackgroundColor(defaultColor)
                        addAndUpdateToDoTaskTV.setTextColor(defaultColor)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            titleTIL.cursorColor = ColorStateList.valueOf(defaultColor)
                            descriptionTIL.cursorColor = ColorStateList.valueOf(defaultColor)
                            dayOfWeekTIL.cursorColor = ColorStateList.valueOf(defaultColor)
                            dateTIL.cursorColor = ColorStateList.valueOf(defaultColor)
                            timeTIL.cursorColor = ColorStateList.valueOf(defaultColor)
                        }

                        titleTIL.setStartIconTintList(ColorStateList.valueOf(defaultColor))
                        titleTIL.boxStrokeErrorColor = ColorStateList.valueOf(defaultColor)
                        titleTIL.setErrorIconTintList(ColorStateList.valueOf(defaultColor))
                        titleTIL.setErrorTextColor(ColorStateList.valueOf(defaultColor))

                        descriptionTIL.setStartIconTintList(ColorStateList.valueOf(defaultColor))
                        descriptionTIL.boxStrokeErrorColor = ColorStateList.valueOf(defaultColor)
                        descriptionTIL.setErrorIconTintList(ColorStateList.valueOf(defaultColor))
                        descriptionTIL.setErrorTextColor(ColorStateList.valueOf(defaultColor))

                        dayOfWeekTIL.setStartIconTintList(ColorStateList.valueOf(defaultColor))
                        dayOfWeekTIL.boxStrokeErrorColor = ColorStateList.valueOf(defaultColor)
                        dayOfWeekTIL.setErrorIconTintList(ColorStateList.valueOf(defaultColor))
                        dayOfWeekTIL.setErrorTextColor(ColorStateList.valueOf(defaultColor))

                        dateTIL.setStartIconTintList(ColorStateList.valueOf(defaultColor))
                        dateTIL.boxStrokeErrorColor = ColorStateList.valueOf(defaultColor)
                        dateTIL.setErrorIconTintList(ColorStateList.valueOf(defaultColor))
                        dateTIL.setErrorTextColor(ColorStateList.valueOf(defaultColor))

                        timeTIL.setStartIconTintList(ColorStateList.valueOf(defaultColor))
                        timeTIL.boxStrokeErrorColor = ColorStateList.valueOf(defaultColor)
                        timeTIL.setErrorIconTintList(ColorStateList.valueOf(defaultColor))
                        timeTIL.setErrorTextColor(ColorStateList.valueOf(defaultColor))
                    }

                    1 -> {
                        crossIV.setColorFilter(darkYellowColor)
                        addAndEditIV.setColorFilter(darkYellowColor)
                        addAndUpdateToDoTaskTV.setTextColor(darkYellowColor)
                        dropDownImageView.setColorFilter(darkYellowColor)
                        saveAndUpdateButton.setBackgroundColor(darkYellowColor)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            titleTIL.cursorColor = ColorStateList.valueOf(darkYellowColor)
                            descriptionTIL.cursorColor = ColorStateList.valueOf(darkYellowColor)
                            dayOfWeekTIL.cursorColor = ColorStateList.valueOf(darkYellowColor)
                            dateTIL.cursorColor = ColorStateList.valueOf(darkYellowColor)
                            timeTIL.cursorColor = ColorStateList.valueOf(darkYellowColor)
                        }

                        titleTIL.setStartIconTintList(ColorStateList.valueOf(darkYellowColor))
                        titleTIL.boxStrokeErrorColor = ColorStateList.valueOf(darkYellowColor)
                        titleTIL.setErrorIconTintList(ColorStateList.valueOf(darkYellowColor))
                        titleTIL.setErrorTextColor(ColorStateList.valueOf(darkYellowColor))

                        descriptionTIL.setStartIconTintList(ColorStateList.valueOf(darkYellowColor))
                        descriptionTIL.boxStrokeErrorColor = ColorStateList.valueOf(darkYellowColor)
                        descriptionTIL.setErrorIconTintList(ColorStateList.valueOf(darkYellowColor))
                        descriptionTIL.setErrorTextColor(ColorStateList.valueOf(darkYellowColor))

                        dayOfWeekTIL.setStartIconTintList(ColorStateList.valueOf(darkYellowColor))
                        dayOfWeekTIL.boxStrokeErrorColor = ColorStateList.valueOf(darkYellowColor)
                        dayOfWeekTIL.setErrorIconTintList(ColorStateList.valueOf(darkYellowColor))
                        dayOfWeekTIL.setErrorTextColor(ColorStateList.valueOf(darkYellowColor))

                        dateTIL.setStartIconTintList(ColorStateList.valueOf(darkYellowColor))
                        dateTIL.boxStrokeErrorColor = ColorStateList.valueOf(darkYellowColor)
                        dateTIL.setErrorIconTintList(ColorStateList.valueOf(darkYellowColor))
                        dateTIL.setErrorTextColor(ColorStateList.valueOf(darkYellowColor))

                        timeTIL.setStartIconTintList(ColorStateList.valueOf(darkYellowColor))
                        timeTIL.boxStrokeErrorColor = ColorStateList.valueOf(darkYellowColor)
                        timeTIL.setErrorIconTintList(ColorStateList.valueOf(darkYellowColor))
                        timeTIL.setErrorTextColor(ColorStateList.valueOf(darkYellowColor))
                    }

                    2 -> {
                        crossIV.setColorFilter(orangeColor)
                        addAndEditIV.setColorFilter(orangeColor)
                        addAndUpdateToDoTaskTV.setTextColor(orangeColor)
                        dropDownImageView.setColorFilter(orangeColor)
                        saveAndUpdateButton.setBackgroundColor(orangeColor)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            titleTIL.cursorColor = ColorStateList.valueOf(orangeColor)
                            descriptionTIL.cursorColor = ColorStateList.valueOf(orangeColor)
                            dayOfWeekTIL.cursorColor = ColorStateList.valueOf(orangeColor)
                            dateTIL.cursorColor = ColorStateList.valueOf(orangeColor)
                            timeTIL.cursorColor = ColorStateList.valueOf(orangeColor)
                        }

                        titleTIL.setStartIconTintList(ColorStateList.valueOf(orangeColor))
                        titleTIL.boxStrokeErrorColor = ColorStateList.valueOf(orangeColor)
                        titleTIL.setErrorIconTintList(ColorStateList.valueOf(orangeColor))
                        titleTIL.setErrorTextColor(ColorStateList.valueOf(orangeColor))

                        descriptionTIL.setStartIconTintList(ColorStateList.valueOf(orangeColor))
                        descriptionTIL.boxStrokeErrorColor = ColorStateList.valueOf(orangeColor)
                        descriptionTIL.setErrorIconTintList(ColorStateList.valueOf(orangeColor))
                        descriptionTIL.setErrorTextColor(ColorStateList.valueOf(orangeColor))

                        dayOfWeekTIL.setStartIconTintList(ColorStateList.valueOf(orangeColor))
                        dayOfWeekTIL.boxStrokeErrorColor = ColorStateList.valueOf(orangeColor)
                        dayOfWeekTIL.setErrorIconTintList(ColorStateList.valueOf(orangeColor))
                        dayOfWeekTIL.setErrorTextColor(ColorStateList.valueOf(orangeColor))

                        dateTIL.setStartIconTintList(ColorStateList.valueOf(orangeColor))
                        dateTIL.boxStrokeErrorColor = ColorStateList.valueOf(orangeColor)
                        dateTIL.setErrorIconTintList(ColorStateList.valueOf(orangeColor))
                        dateTIL.setErrorTextColor(ColorStateList.valueOf(orangeColor))

                        timeTIL.setStartIconTintList(ColorStateList.valueOf(orangeColor))
                        timeTIL.boxStrokeErrorColor = ColorStateList.valueOf(orangeColor)
                        timeTIL.setErrorIconTintList(ColorStateList.valueOf(orangeColor))
                        timeTIL.setErrorTextColor(ColorStateList.valueOf(orangeColor))
                    }

                    3 -> {
                        crossIV.setColorFilter(lightGreenColor)
                        addAndEditIV.setColorFilter(lightGreenColor)
                        addAndUpdateToDoTaskTV.setTextColor(lightGreenColor)
                        dropDownImageView.setColorFilter(lightGreenColor)
                        saveAndUpdateButton.setBackgroundColor(lightGreenColor)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            titleTIL.cursorColor = ColorStateList.valueOf(lightGreenColor)
                            descriptionTIL.cursorColor = ColorStateList.valueOf(lightGreenColor)
                            dayOfWeekTIL.cursorColor = ColorStateList.valueOf(lightGreenColor)
                            dateTIL.cursorColor = ColorStateList.valueOf(lightGreenColor)
                            timeTIL.cursorColor = ColorStateList.valueOf(lightGreenColor)
                        }

                        titleTIL.setStartIconTintList(ColorStateList.valueOf(lightGreenColor))
                        titleTIL.boxStrokeErrorColor = ColorStateList.valueOf(lightGreenColor)
                        titleTIL.setErrorIconTintList(ColorStateList.valueOf(lightGreenColor))
                        titleTIL.setErrorTextColor(ColorStateList.valueOf(lightGreenColor))

                        descriptionTIL.setStartIconTintList(ColorStateList.valueOf(lightGreenColor))
                        descriptionTIL.boxStrokeErrorColor = ColorStateList.valueOf(lightGreenColor)
                        descriptionTIL.setErrorIconTintList(ColorStateList.valueOf(lightGreenColor))
                        descriptionTIL.setErrorTextColor(ColorStateList.valueOf(lightGreenColor))

                        dayOfWeekTIL.setStartIconTintList(ColorStateList.valueOf(lightGreenColor))
                        dayOfWeekTIL.boxStrokeErrorColor = ColorStateList.valueOf(lightGreenColor)
                        dayOfWeekTIL.setErrorIconTintList(ColorStateList.valueOf(lightGreenColor))
                        dayOfWeekTIL.setErrorTextColor(ColorStateList.valueOf(lightGreenColor))

                        dateTIL.setStartIconTintList(ColorStateList.valueOf(lightGreenColor))
                        dateTIL.boxStrokeErrorColor = ColorStateList.valueOf(lightGreenColor)
                        dateTIL.setErrorIconTintList(ColorStateList.valueOf(lightGreenColor))
                        dateTIL.setErrorTextColor(ColorStateList.valueOf(lightGreenColor))

                        timeTIL.setStartIconTintList(ColorStateList.valueOf(lightGreenColor))
                        timeTIL.boxStrokeErrorColor = ColorStateList.valueOf(lightGreenColor)
                        timeTIL.setErrorIconTintList(ColorStateList.valueOf(lightGreenColor))
                        timeTIL.setErrorTextColor(ColorStateList.valueOf(lightGreenColor))
                    }

                    4 -> {
                        crossIV.setColorFilter(blueColor)
                        addAndEditIV.setColorFilter(blueColor)
                        addAndUpdateToDoTaskTV.setTextColor(blueColor)
                        dropDownImageView.setColorFilter(blueColor)
                        saveAndUpdateButton.setBackgroundColor(blueColor)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            titleTIL.cursorColor = ColorStateList.valueOf(blueColor)
                            descriptionTIL.cursorColor = ColorStateList.valueOf(blueColor)
                            dayOfWeekTIL.cursorColor = ColorStateList.valueOf(blueColor)
                            dateTIL.cursorColor = ColorStateList.valueOf(blueColor)
                            timeTIL.cursorColor = ColorStateList.valueOf(blueColor)
                        }

                        titleTIL.setStartIconTintList(ColorStateList.valueOf(blueColor))
                        titleTIL.boxStrokeErrorColor = ColorStateList.valueOf(blueColor)
                        titleTIL.setErrorIconTintList(ColorStateList.valueOf(blueColor))
                        titleTIL.setErrorTextColor(ColorStateList.valueOf(blueColor))

                        descriptionTIL.setStartIconTintList(ColorStateList.valueOf(blueColor))
                        descriptionTIL.boxStrokeErrorColor = ColorStateList.valueOf(blueColor)
                        descriptionTIL.setErrorIconTintList(ColorStateList.valueOf(blueColor))
                        descriptionTIL.setErrorTextColor(ColorStateList.valueOf(blueColor))

                        dayOfWeekTIL.setStartIconTintList(ColorStateList.valueOf(blueColor))
                        dayOfWeekTIL.boxStrokeErrorColor = ColorStateList.valueOf(blueColor)
                        dayOfWeekTIL.setErrorIconTintList(ColorStateList.valueOf(blueColor))
                        dayOfWeekTIL.setErrorTextColor(ColorStateList.valueOf(blueColor))

                        dateTIL.setStartIconTintList(ColorStateList.valueOf(blueColor))
                        dateTIL.boxStrokeErrorColor = ColorStateList.valueOf(blueColor)
                        dateTIL.setErrorIconTintList(ColorStateList.valueOf(blueColor))
                        dateTIL.setErrorTextColor(ColorStateList.valueOf(blueColor))

                        timeTIL.setStartIconTintList(ColorStateList.valueOf(blueColor))
                        timeTIL.boxStrokeErrorColor = ColorStateList.valueOf(blueColor)
                        timeTIL.setErrorIconTintList(ColorStateList.valueOf(blueColor))
                        timeTIL.setErrorTextColor(ColorStateList.valueOf(blueColor))
                    }

                    5 -> {
                        crossIV.setColorFilter(cyanColor)
                        addAndEditIV.setColorFilter(cyanColor)
                        addAndUpdateToDoTaskTV.setTextColor(cyanColor)
                        dropDownImageView.setColorFilter(cyanColor)
                        saveAndUpdateButton.setBackgroundColor(cyanColor)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            titleTIL.cursorColor = ColorStateList.valueOf(cyanColor)
                            descriptionTIL.cursorColor = ColorStateList.valueOf(cyanColor)
                            dayOfWeekTIL.cursorColor = ColorStateList.valueOf(cyanColor)
                            dateTIL.cursorColor = ColorStateList.valueOf(cyanColor)
                            timeTIL.cursorColor = ColorStateList.valueOf(cyanColor)
                        }

                        titleTIL.setStartIconTintList(ColorStateList.valueOf(cyanColor))
                        titleTIL.boxStrokeErrorColor = ColorStateList.valueOf(cyanColor)
                        titleTIL.setErrorIconTintList(ColorStateList.valueOf(cyanColor))
                        titleTIL.setErrorTextColor(ColorStateList.valueOf(cyanColor))

                        descriptionTIL.setStartIconTintList(ColorStateList.valueOf(cyanColor))
                        descriptionTIL.boxStrokeErrorColor = ColorStateList.valueOf(cyanColor)
                        descriptionTIL.setErrorIconTintList(ColorStateList.valueOf(cyanColor))
                        descriptionTIL.setErrorTextColor(ColorStateList.valueOf(cyanColor))

                        dayOfWeekTIL.setStartIconTintList(ColorStateList.valueOf(cyanColor))
                        dayOfWeekTIL.boxStrokeErrorColor = ColorStateList.valueOf(cyanColor)
                        dayOfWeekTIL.setErrorIconTintList(ColorStateList.valueOf(cyanColor))
                        dayOfWeekTIL.setErrorTextColor(ColorStateList.valueOf(cyanColor))

                        dateTIL.setStartIconTintList(ColorStateList.valueOf(cyanColor))
                        dateTIL.boxStrokeErrorColor = ColorStateList.valueOf(cyanColor)
                        dateTIL.setErrorIconTintList(ColorStateList.valueOf(cyanColor))
                        dateTIL.setErrorTextColor(ColorStateList.valueOf(cyanColor))

                        timeTIL.setStartIconTintList(ColorStateList.valueOf(cyanColor))
                        timeTIL.boxStrokeErrorColor = ColorStateList.valueOf(cyanColor)
                        timeTIL.setErrorIconTintList(ColorStateList.valueOf(cyanColor))
                        timeTIL.setErrorTextColor(ColorStateList.valueOf(cyanColor))
                    }

                    6 -> {
                        crossIV.setColorFilter(pinkColor)
                        addAndEditIV.setColorFilter(pinkColor)
                        addAndUpdateToDoTaskTV.setTextColor(pinkColor)
                        dropDownImageView.setColorFilter(pinkColor)
                        saveAndUpdateButton.setBackgroundColor(pinkColor)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            titleTIL.cursorColor = ColorStateList.valueOf(pinkColor)
                            descriptionTIL.cursorColor = ColorStateList.valueOf(pinkColor)
                            dayOfWeekTIL.cursorColor = ColorStateList.valueOf(pinkColor)
                            dateTIL.cursorColor = ColorStateList.valueOf(pinkColor)
                            timeTIL.cursorColor = ColorStateList.valueOf(pinkColor)
                        }

                        titleTIL.setStartIconTintList(ColorStateList.valueOf(pinkColor))
                        titleTIL.boxStrokeErrorColor = ColorStateList.valueOf(pinkColor)
                        titleTIL.setErrorIconTintList(ColorStateList.valueOf(pinkColor))
                        titleTIL.setErrorTextColor(ColorStateList.valueOf(pinkColor))

                        descriptionTIL.setStartIconTintList(ColorStateList.valueOf(pinkColor))
                        descriptionTIL.boxStrokeErrorColor = ColorStateList.valueOf(pinkColor)
                        descriptionTIL.setErrorIconTintList(ColorStateList.valueOf(pinkColor))
                        descriptionTIL.setErrorTextColor(ColorStateList.valueOf(pinkColor))

                        dayOfWeekTIL.setStartIconTintList(ColorStateList.valueOf(pinkColor))
                        dayOfWeekTIL.boxStrokeErrorColor = ColorStateList.valueOf(pinkColor)
                        dayOfWeekTIL.setErrorIconTintList(ColorStateList.valueOf(pinkColor))
                        dayOfWeekTIL.setErrorTextColor(ColorStateList.valueOf(pinkColor))

                        dateTIL.setStartIconTintList(ColorStateList.valueOf(pinkColor))
                        dateTIL.boxStrokeErrorColor = ColorStateList.valueOf(pinkColor)
                        dateTIL.setErrorIconTintList(ColorStateList.valueOf(pinkColor))
                        dateTIL.setErrorTextColor(ColorStateList.valueOf(pinkColor))

                        timeTIL.setStartIconTintList(ColorStateList.valueOf(pinkColor))
                        timeTIL.boxStrokeErrorColor = ColorStateList.valueOf(pinkColor)
                        timeTIL.setErrorIconTintList(ColorStateList.valueOf(pinkColor))
                        timeTIL.setErrorTextColor(ColorStateList.valueOf(pinkColor))
                    }

                    7 -> {
                        crossIV.setColorFilter(darkBlueColor)
                        addAndEditIV.setColorFilter(darkBlueColor)
                        addAndUpdateToDoTaskTV.setTextColor(darkBlueColor)
                        dropDownImageView.setColorFilter(darkBlueColor)
                        saveAndUpdateButton.setBackgroundColor(darkBlueColor)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            titleTIL.cursorColor = ColorStateList.valueOf(darkBlueColor)
                            descriptionTIL.cursorColor = ColorStateList.valueOf(darkBlueColor)
                            dayOfWeekTIL.cursorColor = ColorStateList.valueOf(darkBlueColor)
                            dateTIL.cursorColor = ColorStateList.valueOf(darkBlueColor)
                            timeTIL.cursorColor = ColorStateList.valueOf(darkBlueColor)
                        }

                        titleTIL.setStartIconTintList(ColorStateList.valueOf(darkBlueColor))
                        titleTIL.boxStrokeErrorColor = ColorStateList.valueOf(darkBlueColor)
                        titleTIL.setErrorIconTintList(ColorStateList.valueOf(darkBlueColor))
                        titleTIL.setErrorTextColor(ColorStateList.valueOf(darkBlueColor))

                        descriptionTIL.setStartIconTintList(ColorStateList.valueOf(darkBlueColor))
                        descriptionTIL.boxStrokeErrorColor = ColorStateList.valueOf(darkBlueColor)
                        descriptionTIL.setErrorIconTintList(ColorStateList.valueOf(darkBlueColor))
                        descriptionTIL.setErrorTextColor(ColorStateList.valueOf(darkBlueColor))

                        dayOfWeekTIL.setStartIconTintList(ColorStateList.valueOf(darkBlueColor))
                        dayOfWeekTIL.boxStrokeErrorColor = ColorStateList.valueOf(darkBlueColor)
                        dayOfWeekTIL.setErrorIconTintList(ColorStateList.valueOf(darkBlueColor))
                        dayOfWeekTIL.setErrorTextColor(ColorStateList.valueOf(darkBlueColor))

                        dateTIL.setStartIconTintList(ColorStateList.valueOf(darkBlueColor))
                        dateTIL.boxStrokeErrorColor = ColorStateList.valueOf(darkBlueColor)
                        dateTIL.setErrorIconTintList(ColorStateList.valueOf(darkBlueColor))
                        dateTIL.setErrorTextColor(ColorStateList.valueOf(darkBlueColor))

                        timeTIL.setStartIconTintList(ColorStateList.valueOf(darkBlueColor))
                        timeTIL.boxStrokeErrorColor = ColorStateList.valueOf(darkBlueColor)
                        timeTIL.setErrorIconTintList(ColorStateList.valueOf(darkBlueColor))
                        timeTIL.setErrorTextColor(ColorStateList.valueOf(darkBlueColor))
                    }

                    8 -> {
                        crossIV.setColorFilter(redColor)
                        addAndEditIV.setColorFilter(redColor)
                        addAndUpdateToDoTaskTV.setTextColor(redColor)
                        dropDownImageView.setColorFilter(redColor)
                        saveAndUpdateButton.setBackgroundColor(redColor)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            titleTIL.cursorColor = ColorStateList.valueOf(redColor)
                            descriptionTIL.cursorColor = ColorStateList.valueOf(redColor)
                            dayOfWeekTIL.cursorColor = ColorStateList.valueOf(redColor)
                            dateTIL.cursorColor = ColorStateList.valueOf(redColor)
                            timeTIL.cursorColor = ColorStateList.valueOf(redColor)
                        }

                        titleTIL.setStartIconTintList(ColorStateList.valueOf(redColor))
                        titleTIL.boxStrokeErrorColor = ColorStateList.valueOf(redColor)
                        titleTIL.setErrorIconTintList(ColorStateList.valueOf(redColor))
                        titleTIL.setErrorTextColor(ColorStateList.valueOf(redColor))

                        descriptionTIL.setStartIconTintList(ColorStateList.valueOf(redColor))
                        descriptionTIL.boxStrokeErrorColor = ColorStateList.valueOf(redColor)
                        descriptionTIL.setErrorIconTintList(ColorStateList.valueOf(redColor))
                        descriptionTIL.setErrorTextColor(ColorStateList.valueOf(redColor))

                        dayOfWeekTIL.setStartIconTintList(ColorStateList.valueOf(redColor))
                        dayOfWeekTIL.boxStrokeErrorColor = ColorStateList.valueOf(redColor)
                        dayOfWeekTIL.setErrorIconTintList(ColorStateList.valueOf(redColor))
                        dayOfWeekTIL.setErrorTextColor(ColorStateList.valueOf(redColor))

                        dateTIL.setStartIconTintList(ColorStateList.valueOf(redColor))
                        dateTIL.boxStrokeErrorColor = ColorStateList.valueOf(redColor)
                        dateTIL.setErrorIconTintList(ColorStateList.valueOf(redColor))
                        dateTIL.setErrorTextColor(ColorStateList.valueOf(redColor))

                        timeTIL.setStartIconTintList(ColorStateList.valueOf(redColor))
                        timeTIL.boxStrokeErrorColor = ColorStateList.valueOf(redColor)
                        timeTIL.setErrorIconTintList(ColorStateList.valueOf(redColor))
                        timeTIL.setErrorTextColor(ColorStateList.valueOf(redColor))
                    }

                    9 -> {
                        crossIV.setColorFilter(lightPurpleColor)
                        addAndEditIV.setColorFilter(lightPurpleColor)
                        addAndUpdateToDoTaskTV.setTextColor(lightPurpleColor)
                        dropDownImageView.setColorFilter(lightPurpleColor)
                        saveAndUpdateButton.setBackgroundColor(lightPurpleColor)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            titleTIL.cursorColor = ColorStateList.valueOf(lightPurpleColor)
                            descriptionTIL.cursorColor = ColorStateList.valueOf(lightPurpleColor)
                            dayOfWeekTIL.cursorColor = ColorStateList.valueOf(lightPurpleColor)
                            dateTIL.cursorColor = ColorStateList.valueOf(lightPurpleColor)
                            timeTIL.cursorColor = ColorStateList.valueOf(lightPurpleColor)
                        }

                        titleTIL.setStartIconTintList(ColorStateList.valueOf(lightPurpleColor))
                        titleTIL.boxStrokeErrorColor = ColorStateList.valueOf(lightPurpleColor)
                        titleTIL.setErrorIconTintList(ColorStateList.valueOf(lightPurpleColor))
                        titleTIL.setErrorTextColor(ColorStateList.valueOf(lightPurpleColor))

                        descriptionTIL.setStartIconTintList(ColorStateList.valueOf(lightPurpleColor))
                        descriptionTIL.boxStrokeErrorColor = ColorStateList.valueOf(lightPurpleColor)
                        descriptionTIL.setErrorIconTintList(ColorStateList.valueOf(lightPurpleColor))
                        descriptionTIL.setErrorTextColor(ColorStateList.valueOf(lightPurpleColor))

                        dayOfWeekTIL.setStartIconTintList(ColorStateList.valueOf(lightPurpleColor))
                        dayOfWeekTIL.boxStrokeErrorColor = ColorStateList.valueOf(lightPurpleColor)
                        dayOfWeekTIL.setErrorIconTintList(ColorStateList.valueOf(lightPurpleColor))
                        dayOfWeekTIL.setErrorTextColor(ColorStateList.valueOf(lightPurpleColor))

                        dateTIL.setStartIconTintList(ColorStateList.valueOf(lightPurpleColor))
                        dateTIL.boxStrokeErrorColor = ColorStateList.valueOf(lightPurpleColor)
                        dateTIL.setErrorIconTintList(ColorStateList.valueOf(lightPurpleColor))
                        dateTIL.setErrorTextColor(ColorStateList.valueOf(lightPurpleColor))

                        timeTIL.setStartIconTintList(ColorStateList.valueOf(lightPurpleColor))
                        timeTIL.boxStrokeErrorColor = ColorStateList.valueOf(lightPurpleColor)
                        timeTIL.setErrorIconTintList(ColorStateList.valueOf(lightPurpleColor))
                        timeTIL.setErrorTextColor(ColorStateList.valueOf(lightPurpleColor))
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
            val date: String = simpleDateFormat.format(selection)
            addAndUpdateTasksDialogLayoutBinding.dateTIL.editText?.setText(date)
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
            addAndUpdateTasksDialogLayoutBinding.timeTIL.editText?.setText(time)
        }
    }

    private fun applyCustomFontOnAddAndUpdateTasksDialogViews(
        addAndUpdateTasksDialogLayoutBinding: AddAndUpdateTasksDialogLayoutBinding
    ) {
        with(addAndUpdateTasksDialogLayoutBinding) {
            addAndUpdateToDoTaskTV.typeface = typeface
            infoTV.typeface = typeface
            titleTIL.typeface = typeface
            titleTIET.typeface = typeface
            descriptionTIL.typeface = typeface
            descriptionTIET.typeface = typeface
            dayOfWeekTIL.typeface = typeface
            dayOfWeekTIET.typeface = typeface
            dateTIL.typeface = typeface
            dateTIET.typeface = typeface
            timeTIL.typeface = typeface
            timeTIET.typeface = typeface
            selectCategoryTV.typeface = typeface
            saveAndUpdateButton.typeface = typeface
        }
    }

    private fun openTaskDetailActivity(toDoTask: ToDoTask) {
        val toDoTaskDetailIntent = Intent(fragmentContext, ToDoTaskDetailActivity::class.java)
        with(toDoTaskDetailIntent) {
            putExtra("taskDetail", toDoTask)
            startActivity(this)
        }
    }

    private fun showDeleteTaskDialog(toDoTask: ToDoTask) {
        val deleteTaskDialogLayoutBinding = DeleteTaskDialogLayoutBinding.inflate(layoutInflater)

        val deleteTaskDialogBuilder = AlertDialog.Builder(fragmentContext)
        with(deleteTaskDialogBuilder) {
            setView(deleteTaskDialogLayoutBinding.root)
            setCancelable(true)
            setOnDismissListener {
                startFABAnimation()
            }
        }
        val deleteTaskAlertDialog = deleteTaskDialogBuilder.create()
        if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed && !deleteTaskAlertDialog.isShowing) {
            deleteTaskAlertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            deleteTaskAlertDialog.window?.setWindowAnimations(R.style.dialogBoxesAnimation)
            deleteTaskAlertDialog.show()
        }

        with(deleteTaskDialogLayoutBinding) {
            deleteIV.startAnimation(applyAnimation(fragmentContext))
            stopFABAnimation()
            applyCustomFontOnDeleteTaskDialogViews(this)
            appLightAndDarkModeOnDeleteTaskDialogViews(this)

            noButton.setOnClickListener { _: View? ->
                if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed) {
                    deleteTaskAlertDialog.dismiss()
                }
                startFABAnimation()
            }

            yesButton.setOnClickListener { _: View? ->
                lifecycleScope.launch(Dispatchers.IO) {
                    val isDeleted = tasksViewModel.deleteTask(toDoTask).await()
                    withContext(Dispatchers.Main) {
                        if (isDeleted == 1) {
                            Toasty.success(fragmentContext, R.string.deleted_successfully_toast_text, Toasty.LENGTH_LONG).show()
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
        }
    }

    private fun appLightAndDarkModeOnDeleteTaskDialogViews(
        deleteTaskDialogLayoutBinding: DeleteTaskDialogLayoutBinding
    ) {
        with(deleteTaskDialogLayoutBinding) {
            if (prefs.isDarkModeEnable) {
                rootLayout.background.colorFilter = PorterDuffColorFilter(screensNightModeColor, PorterDuff.Mode.SRC_IN)
                deleteIV.setColorFilter(lightBlueColor)
                deleteMessageTV.setTextColor(whiteColor)
                noButton.strokeColor = ColorStateList.valueOf(lightBlueColor)
                noButton.setTextColor(lightBlueColor)
                yesButton.setBackgroundColor(lightBlueColor)
                yesButton.setTextColor(blackColor)
            } else {
                when (prefs.colorSchemeValue) {
                    0 -> {
                        deleteIV.setColorFilter(defaultColor)
                        noButton.strokeColor = ColorStateList.valueOf(defaultColor)
                        noButton.setTextColor(defaultColor)
                        yesButton.setBackgroundColor(defaultColor)
                        yesButton.setTextColor(whiteColor)
                    }

                    1 -> {
                        deleteIV.setColorFilter(darkYellowColor)
                        noButton.strokeColor = ColorStateList.valueOf(darkYellowColor)
                        noButton.setTextColor(darkYellowColor)
                        yesButton.setBackgroundColor(darkYellowColor)
                        yesButton.setTextColor(whiteColor)
                    }

                    2 -> {
                        deleteIV.setColorFilter(orangeColor)
                        noButton.strokeColor = ColorStateList.valueOf(orangeColor)
                        noButton.setTextColor(orangeColor)
                        yesButton.setBackgroundColor(orangeColor)
                        yesButton.setTextColor(whiteColor)
                    }

                    3 -> {
                        deleteIV.setColorFilter(lightGreenColor)
                        noButton.strokeColor = ColorStateList.valueOf(lightGreenColor)
                        noButton.setTextColor(lightGreenColor)
                        yesButton.setBackgroundColor(lightGreenColor)
                        yesButton.setTextColor(whiteColor)
                    }

                    4 -> {
                        deleteIV.setColorFilter(blueColor)
                        noButton.strokeColor = ColorStateList.valueOf(blueColor)
                        noButton.setTextColor(blueColor)
                        yesButton.setBackgroundColor(blueColor)
                        yesButton.setTextColor(whiteColor)
                    }

                    5 -> {
                        deleteIV.setColorFilter(cyanColor)
                        noButton.strokeColor = ColorStateList.valueOf(cyanColor)
                        noButton.setTextColor(cyanColor)
                        yesButton.setBackgroundColor(cyanColor)
                        yesButton.setTextColor(whiteColor)
                    }

                    6 -> {
                        deleteIV.setColorFilter(pinkColor)
                        noButton.strokeColor = ColorStateList.valueOf(pinkColor)
                        noButton.setTextColor(pinkColor)
                        yesButton.setBackgroundColor(pinkColor)
                        yesButton.setTextColor(whiteColor)
                    }

                    7 -> {
                        deleteIV.setColorFilter(darkBlueColor)
                        noButton.strokeColor = ColorStateList.valueOf(darkBlueColor)
                        noButton.setTextColor(darkBlueColor)
                        yesButton.setBackgroundColor(darkBlueColor)
                        yesButton.setTextColor(whiteColor)
                    }

                    8 -> {
                        deleteIV.setColorFilter(redColor)
                        noButton.strokeColor = ColorStateList.valueOf(redColor)
                        noButton.setTextColor(redColor)
                        yesButton.setBackgroundColor(redColor)
                        yesButton.setTextColor(whiteColor)
                    }

                    9 -> {
                        deleteIV.setColorFilter(lightPurpleColor)
                        noButton.strokeColor = ColorStateList.valueOf(lightPurpleColor)
                        noButton.setTextColor(lightPurpleColor)
                        yesButton.setBackgroundColor(lightPurpleColor)
                        yesButton.setTextColor(whiteColor)
                    }
                }
            }
        }
    }

    private fun applyCustomFontOnDeleteTaskDialogViews(deleteTaskDialogLayoutBinding: DeleteTaskDialogLayoutBinding) {
        with(deleteTaskDialogLayoutBinding) {
            deleteMessageTV.typeface = typeface
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