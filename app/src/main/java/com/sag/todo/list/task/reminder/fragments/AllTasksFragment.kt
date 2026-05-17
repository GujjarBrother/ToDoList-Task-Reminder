package com.sag.todo.list.task.reminder.fragments

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.core.view.get
import androidx.core.view.size
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.sag.todo.list.task.reminder.R
import com.sag.todo.list.task.reminder.activities.DashBoardActivity
import com.sag.todo.list.task.reminder.activities.ToDoTaskDetailActivity
import com.sag.todo.list.task.reminder.adapters.CategoryAdapter
import com.sag.todo.list.task.reminder.adapters.SortAdapter
import com.sag.todo.list.task.reminder.adapters.TasksRecyclerViewAdapter
import com.sag.todo.list.task.reminder.base.BaseFragment
import com.sag.todo.list.task.reminder.databinding.AddAndUpdateTasksDialogLayoutBinding
import com.sag.todo.list.task.reminder.databinding.CustomPopupMenuLayoutBinding
import com.sag.todo.list.task.reminder.databinding.DeleteTaskDialogLayoutBinding
import com.sag.todo.list.task.reminder.databinding.FragmentAllTasksBinding
import com.sag.todo.list.task.reminder.databinding.SortingDialogLayoutBinding
import com.sag.todo.list.task.reminder.enums.StartStopFAB
import com.sag.todo.list.task.reminder.enums.Tabs
import com.sag.todo.list.task.reminder.enums.TasksCategories
import com.sag.todo.list.task.reminder.enums.Visibility
import com.sag.todo.list.task.reminder.listeners.SearchViewVisibilityListener
import com.sag.todo.list.task.reminder.listeners.StartAndStopFABAnimationListener
import com.sag.todo.list.task.reminder.models.Sort
import com.sag.todo.list.task.reminder.models.ToDoTask
import com.sag.todo.list.task.reminder.receivers.ReminderReceiver
import com.sag.todo.list.task.reminder.utils.AppConstants.applyCustomFontAndColorToPopupMenuItemsText
import com.sag.todo.list.task.reminder.utils.AppConstants.changeVisibility
import com.sag.todo.list.task.reminder.utils.AppConstants.getColorResource
import com.sag.todo.list.task.reminder.utils.AppConstants.getDrawableResource
import com.sag.todo.list.task.reminder.utils.FabRateUsAndApplyAnimation
import com.sag.todo.list.task.reminder.viewModels.TasksViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Collections
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class AllTasksFragment : BaseFragment(), View.OnClickListener {

    private var _binding: FragmentAllTasksBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var alarmManager: AlarmManager

    @Inject
    @FabRateUsAndApplyAnimation
    lateinit var animation: Animation
    private val tasksViewModel: TasksViewModel by viewModels()
    private var category = 0
    private lateinit var allToDosTasksArrayList: ArrayList<ToDoTask>
    private lateinit var adapter: TasksRecyclerViewAdapter
    private lateinit var popupWindow: PopupWindow
    private lateinit var addAndUpdateTasksDialogLayoutBinding: AddAndUpdateTasksDialogLayoutBinding
    private lateinit var toDoTask: ToDoTask
    private val whichTabOpened = "whichTab"
    private var selectedTab: Int? = null
    private var searchViewVisibilityListener: SearchViewVisibilityListener? = null
    private var sortBy: Sort? = null
    private var sortOrder: Sort? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            searchViewVisibilityListener = context as SearchViewVisibilityListener
        } catch (_: Exception) {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (fragmentContext as DashBoardActivity).initializeStartAndStopFABAnimationListenerFromToDosFragment(
            object : StartAndStopFABAnimationListener {
                override fun startAndStopFABAnimation(startStopFAB: StartStopFAB) {
                    if (startStopFAB == StartStopFAB.START) startFABAnimation()
                    else stopFABAnimation()
                }

                override fun search(query: String) {
                    if (::adapter.isInitialized)
                        adapter.filter.filter(query)
                }
            }
        )

        selectedTab = arguments?.run {
            getInt(whichTabOpened)
        }

        binding.apply {
            addNewTasksFAB.setOnClickListener(this@AllTasksFragment)
            sortingCV.setOnClickListener(this@AllTasksFragment)
            stylesCV.setOnClickListener(this@AllTasksFragment)
            tasksViewModel.getAllTasks(selectedTab != 0).observe(viewLifecycleOwner) {
                allToDosTasksArrayList = it as ArrayList<ToDoTask>
                readAllTasks()
            }
        }
    }

    fun getTasksListSize() = allToDosTasksArrayList.size

    companion object {
        fun newInstance(whichTab: Int) = AllTasksFragment().apply {
            arguments = Bundle().apply {
                putInt(whichTabOpened, whichTab)
            }
        }
    }

    private fun startFABAnimation() =
        binding.addNewTasksFAB.startAnimation(animation)

    private fun stopFABAnimation() =
        binding.addNewTasksFAB.clearAnimation()

    override fun onResume() {
        super.onResume()
        binding.apply {
            when (selectedTab) {
                Tabs.TASKS_TAB.ordinal -> {
                    addNewTasksFAB.changeVisibility(Visibility.VISIBLE)
                    startFABAnimation()
                    listAndGridViewStylesIV.setImageDrawable(fragmentContext.getDrawableResource(if (prefs.allTasksStyleValue) R.drawable.list_view_style_image else R.drawable.grid_view_style_image))
                    listAndGridViewStylesTV.text = getString(if (prefs.allTasksStyleValue) com.example.core.R.string.listview_text else com.example.core.R.string.gridview_text)
                    if (::allToDosTasksArrayList.isInitialized) {
                        searchViewVisibilityListener?.isShowSearchViewORNot(allToDosTasksArrayList.isNotEmpty())
                    }
                }

                Tabs.COMPLETED_TAB.ordinal -> {
                    addNewTasksFAB.changeVisibility(Visibility.GONE)
                    listAndGridViewStylesIV.setImageDrawable(fragmentContext.getDrawableResource(if (prefs.completedTasksStyleValue) R.drawable.list_view_style_image else R.drawable.grid_view_style_image))
                    listAndGridViewStylesTV.text = getString(if (prefs.completedTasksStyleValue) com.example.core.R.string.listview_text else com.example.core.R.string.gridview_text)
                    if (::allToDosTasksArrayList.isInitialized) {
                        searchViewVisibilityListener?.isShowSearchViewORNot(allToDosTasksArrayList.isNotEmpty())
                    }
                }
            }
        }
    }

    private fun FragmentAllTasksBinding.readAllTasks() {
        if (allToDosTasksArrayList.isNotEmpty()) {
            nothingInHereGroup.changeVisibility(Visibility.GONE)
            dataAvailableGroup.changeVisibility(Visibility.VISIBLE)
            searchViewVisibilityListener?.isShowSearchViewORNot(true)
            displayAllTasksOnRecyclerView()
        } else {
            nothingInHereGroup.changeVisibility(Visibility.VISIBLE)
            dataAvailableGroup.changeVisibility(Visibility.GONE)
            searchViewVisibilityListener?.isShowSearchViewORNot(false)
        }
    }

    private fun sortAnArrayList() {
        val toDosSortingValues = when (selectedTab) {
            Tabs.TASKS_TAB.ordinal -> prefs.getAllTasksSortingValues
            else -> prefs.getCompletedTasksSortingValues
        }
        if (toDosSortingValues[0] == getString(com.example.core.R.string.title_text)) {
            if (toDosSortingValues[1] == getString(com.example.core.R.string.ascending_a_z_text)) {
                allToDosTasksArrayList.sortWith(Comparator.comparing(ToDoTask::title))
            } else if (toDosSortingValues[1] == getString(com.example.core.R.string.descending_z_a_text)) {
                allToDosTasksArrayList.sortWith(
                    Collections.reverseOrder(
                        Comparator.comparing(
                            ToDoTask::title
                        )
                    )
                )
            }
        } else if (toDosSortingValues[0] == getString(com.example.core.R.string.day_of_week_text)) {
            if (toDosSortingValues[1] == getString(com.example.core.R.string.ascending_a_z_text)) {
                allToDosTasksArrayList.sortWith(Comparator.comparing(ToDoTask::day))
            } else if (toDosSortingValues[1] == getString(com.example.core.R.string.descending_z_a_text)) {
                allToDosTasksArrayList.sortWith(
                    Collections.reverseOrder(
                        Comparator.comparing(
                            ToDoTask::day
                        )
                    )
                )
            }
        } else if (toDosSortingValues[0] == getString(com.example.core.R.string.date_text)) {
            if (toDosSortingValues[1] == getString(com.example.core.R.string.ascending_a_z_text)) {
                allToDosTasksArrayList.sortWith(Comparator.comparing(ToDoTask::date))
            } else if (toDosSortingValues[1] == getString(com.example.core.R.string.descending_z_a_text)) {
                allToDosTasksArrayList.sortWith(
                    Collections.reverseOrder(
                        Comparator.comparing(
                            ToDoTask::date
                        )
                    )
                )
            }
        } else if (toDosSortingValues[0] == getString(com.example.core.R.string.month_text)) {
            if (toDosSortingValues[1] == getString(com.example.core.R.string.ascending_a_z_text)) {
                allToDosTasksArrayList.sortWith(Comparator.comparing(ToDoTask::month))
            } else if (toDosSortingValues[1] == getString(com.example.core.R.string.descending_z_a_text)) {
                allToDosTasksArrayList.sortWith(
                    Collections.reverseOrder(
                        Comparator.comparing(
                            ToDoTask::month
                        )
                    )
                )
            }
        } else if (toDosSortingValues[0] == getString(com.example.core.R.string.year_text)) {
            if (toDosSortingValues[1] == getString(com.example.core.R.string.ascending_a_z_text)) {
                allToDosTasksArrayList.sortWith(Comparator.comparing(ToDoTask::year))
            } else if (toDosSortingValues[1] == getString(com.example.core.R.string.descending_z_a_text)) {
                allToDosTasksArrayList.sortWith(
                    Collections.reverseOrder(
                        Comparator.comparing(
                            ToDoTask::year
                        )
                    )
                )
            }
        } else if (toDosSortingValues[0] == getString(com.example.core.R.string.time_hint)) {
            if (toDosSortingValues[1] == getString(com.example.core.R.string.ascending_a_z_text)) {
                allToDosTasksArrayList.sortWith(Comparator.comparing(ToDoTask::time))
            } else if (toDosSortingValues[1] == getString(com.example.core.R.string.descending_z_a_text)) {
                allToDosTasksArrayList.sortWith(
                    Collections.reverseOrder(
                        Comparator.comparing(
                            ToDoTask::time
                        )
                    )
                )
            }
        }
    }

    private fun displayAllTasksOnRecyclerView() {
        sortAnArrayList()

        if (!::adapter.isInitialized) {
            adapter = TasksRecyclerViewAdapter(viewLifecycleOwner,
                if (selectedTab == 0) Tabs.TASKS_TAB.ordinal else Tabs.COMPLETED_TAB.ordinal, prefs, { toDoTask ->
                    if (selectedTab == 0) {
                        openTaskDetailActivity(toDoTask)
                    } else {
                        showDeleteTaskDialog(toDoTask)
                    }
                }, { toDoTask, view ->
                    val popupMenu = PopupMenu(fragmentContext, view, Gravity.CENTER, 0, R.style.popUpMenuStyle)
                    popupMenu.inflate(R.menu.update_and_delete_popup_menu)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        popupMenu.setForceShowIcon(true)
                    }
                    popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                        val itemId = item.itemId
                        if (itemId == R.id.update_item) {
                            this.toDoTask = toDoTask
                            showAddNewAndUpdateTaskDialog(true)
                        } else if (itemId == R.id.delete_item) {
                            showDeleteTaskDialog(toDoTask)
                        }
                        false
                    }
                    val menu = popupMenu.menu as Menu
                    for (i in 0 until menu.size) {
                        applyCustomFontAndColorToPopupMenuItemsText(
                            context = fragmentContext,
                            menuItem = menu[i],
                            customColor = fragmentContext.getColorResource(R.color.blackAndWhiteViewsColor)
                        )
                    }
                    popupMenu.show()
                }
            )
        }

        binding.apply {
            changeStyle()
            if (::adapter.isInitialized) {
                allTasksRecyclerView.adapter = adapter
                adapter.setFullList(list = allToDosTasksArrayList)
            }
        }
    }

    private fun changeStyle() {
        binding.apply {
            var layoutManager: RecyclerView.LayoutManager? = null
            when (selectedTab) {
                Tabs.TASKS_TAB.ordinal -> {
                    layoutManager = if (prefs.allTasksStyleValue)
                        GridLayoutManager(fragmentContext, 2, GridLayoutManager.VERTICAL, false)
                    else
                        LinearLayoutManager(fragmentContext, LinearLayoutManager.VERTICAL, false)
                }

                Tabs.COMPLETED_TAB.ordinal -> {
                    layoutManager = if (prefs.completedTasksStyleValue)
                        GridLayoutManager(fragmentContext, 2, GridLayoutManager.VERTICAL, false)
                    else
                        LinearLayoutManager(fragmentContext, LinearLayoutManager.VERTICAL, false)
                }
            }
            allTasksRecyclerView.layoutManager = layoutManager
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.addNewTasksFAB -> showAddNewAndUpdateTaskDialog(false)
            R.id.sortingCV -> showSortingDialog()
            R.id.stylesCV -> {
                binding.apply {
                    when (selectedTab) {
                        Tabs.TASKS_TAB.ordinal -> {
                            listAndGridViewStylesIV.setImageDrawable(
                                fragmentContext.getDrawableResource(
                                    if (prefs.allTasksStyleValue) R.drawable.grid_view_style_image else R.drawable.list_view_style_image
                                )
                            )
                            listAndGridViewStylesTV.setText(if (prefs.allTasksStyleValue) com.example.core.R.string.gridview_text else com.example.core.R.string.listview_text)
                            prefs.allTasksStyleValue = !prefs.allTasksStyleValue
                            changeStyle()
                        }

                        Tabs.COMPLETED_TAB.ordinal -> {
                            listAndGridViewStylesIV.setImageDrawable(
                                fragmentContext.getDrawableResource(
                                    if (prefs.completedTasksStyleValue) R.drawable.grid_view_style_image else R.drawable.list_view_style_image
                                )
                            )
                            listAndGridViewStylesTV.setText(if (prefs.completedTasksStyleValue) com.example.core.R.string.gridview_text else com.example.core.R.string.listview_text)
                            prefs.completedTasksStyleValue = !prefs.completedTasksStyleValue
                            changeStyle()
                        }
                    }
                }
            }
        }
    }

    private fun showAddNewAndUpdateTaskDialog(isForUpdateTask: Boolean) {
        addAndUpdateTasksDialogLayoutBinding = AddAndUpdateTasksDialogLayoutBinding.inflate(layoutInflater)

        val addTasksDialogBuilder = AlertDialog.Builder(fragmentContext)
        addTasksDialogBuilder.apply {
            setView(addAndUpdateTasksDialogLayoutBinding.root)
            setCancelable(true)
            setOnDismissListener {
                startFABAnimation()
            }
        }
        val addTasksAlertDialog = addTasksDialogBuilder.create()
        if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed && !addTasksAlertDialog.isShowing) {
            addTasksAlertDialog.apply {
                window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                window?.setWindowAnimations(R.style.dialogBoxesAnimation)
                show()
            }
        }

        addAndUpdateTasksDialogLayoutBinding.apply {
            infoTV.isSelected = true
            stopFABAnimation()

            if (prefs.isDarkModeEnable) {
                titleTIL.setBoxStrokeColorStateList(textInputLayoutDarkModeStrokeColor)
                descriptionTIL.setBoxStrokeColorStateList(textInputLayoutDarkModeStrokeColor)
                dayOfWeekTIL.setBoxStrokeColorStateList(textInputLayoutDarkModeStrokeColor)
                dateTIL.setBoxStrokeColorStateList(textInputLayoutDarkModeStrokeColor)
                timeTIL.setBoxStrokeColorStateList(textInputLayoutDarkModeStrokeColor)
            }

            if (isForUpdateTask) {
                addAndEditIV.setImageResource(R.drawable.update_image)
                addAndUpdateToDoTaskTV.text = getString(com.example.core.R.string.update_todo_task_text)
                infoTV.text = getString(com.example.core.R.string.update_info_message_text)
                titleTIL.editText?.setText(toDoTask.title)
                titleTIL.editText?.setSelection(toDoTask.title.length)
                descriptionTIL.editText?.setText(toDoTask.description)
                descriptionTIL.editText?.setSelection(toDoTask.description.length)
                dayOfWeekTIL.editText?.setText(toDoTask.day)
                dayOfWeekTIL.editText?.setSelection(toDoTask.day.length)
                dateTIL.editText?.setText(String.format("%s %s, %s", toDoTask.month, toDoTask.date, toDoTask.year))
                timeTIL.editText?.setText(toDoTask.time)
                if (toDoTask.category == TasksCategories.DEFAULT_CATEGORY.ordinal || toDoTask.category == TasksCategories.PERSONAL_CATEGORY.ordinal) {
                    selectCategoryBtn.text = getString(com.example.core.R.string.personal_text)
                } else if (toDoTask.category == TasksCategories.WORK_CATEGORY.ordinal) {
                    selectCategoryBtn.text = getString(com.example.core.R.string.work_text)
                }
                selectCategoryBtn.setTextColor(fragmentContext.getColorResource(R.color.blackColor))
                saveAndUpdateBtn.text = getString(com.example.core.R.string.update_text)
            }

            titleTIET.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) selectCategoryBtn.strokeColor =
                    ColorStateList.valueOf(fragmentContext.getColorResource(R.color.subColor))
            }

            descriptionTIET.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) selectCategoryBtn.strokeColor =
                    ColorStateList.valueOf(fragmentContext.getColorResource(R.color.subColor))
            }

            dayOfWeekTIET.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) selectCategoryBtn.strokeColor =
                    ColorStateList.valueOf(fragmentContext.getColorResource(R.color.subColor))
            }

            selectCategoryBtn.setOnClickListener { view: View ->
                titleTIET.clearFocus()
                descriptionTIET.clearFocus()
                dayOfWeekTIET.clearFocus()
                dateTIET.clearFocus()
                timeTIET.clearFocus()
                selectCategoryBtn.strokeColor =
                    ColorStateList.valueOf(fragmentContext.getColorResource(R.color.defaultColor))
                showCustomPopupForCategorySelection(view, isForUpdateTask)
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

            saveAndUpdateBtn.setOnClickListener { _: View? ->
                val title = titleTIL.editText?.text.toString().trim()
                val description = descriptionTIL.editText?.text.toString().trim()
                val dayOfWeek: String = dayOfWeekTIL.editText?.text.toString().trim()
                val date = dateTIL.editText?.text.toString().trim()
                val time = timeTIL.editText?.text.toString().trim()
                if (title.isEmpty()) {
                    titleTIL.error = getString(com.example.core.R.string.title_error_text)
                } else if (description.isEmpty()) {
                    titleTIL.error = null
                    descriptionTIL.error = getString(com.example.core.R.string.description_error_text)
                } else if (dayOfWeek.isEmpty()) {
                    descriptionTIL.error = null
                    dayOfWeekTIL.error = getString(com.example.core.R.string.day_of_week_error_text)
                } else if (date.isEmpty()) {
                    dayOfWeekTIL.error = null
                    dateTIL.error = getString(com.example.core.R.string.select_date_error_text)
                } else if (time.isEmpty()) {
                    dateTIL.error = null
                    timeTIL.error = getString(com.example.core.R.string.select_time_error_text)
                } else {
                    titleTIL.error = null
                    descriptionTIL.error = null
                    dayOfWeekTIL.error = null
                    dateTIL.error = null
                    timeTIL.error = null

                    val parseDate = simpleDateFormat.parse(date) as Date
                    val dateSDF = SimpleDateFormat("dd", Locale.getDefault())
                    val monthSDF = SimpleDateFormat("MMM", Locale.getDefault())
                    val yearSDF = SimpleDateFormat("yyyy", Locale.getDefault())
                    val completeDateAndTime = "${monthSDF.format(parseDate)} ${dateSDF.format(parseDate)}, ${yearSDF.format(parseDate)} $time"
                    val completeDateAndTimeDate = simpleDateAndTimeFormat.parse(completeDateAndTime) as Date

                    if (!isForUpdateTask) {
                        if (dateSDF.format(parseDate).isNotEmpty() && monthSDF.format(parseDate).isNotEmpty()
                            && yearSDF.format(parseDate).isNotEmpty()) {
                            val toDoTask = ToDoTask(
                                0, dayOfWeek, dateSDF.format(parseDate), monthSDF.format(parseDate),
                                yearSDF.format(parseDate), title, description, time, category,
                                completeDateAndTimeDate, false
                            )
                            lifecycleScope.launch(Dispatchers.IO) {
                                tasksViewModel.saveTask(
                                    toDoTask = toDoTask,
                                    isPastTimeCallback = {
                                        toastController.showToast(fragmentContext, getString(com.example.core.R.string.past_date_and_time_is_not_acceptable_toast), false)
                                    },
                                    isAlreadySavedCallback = {
                                        toastController.showToast(fragmentContext, getString(com.example.core.R.string.this_task_is_already_saved_toast_text), false)
                                    },
                                    isSavedSuccessfully = { isSavedSuccessfully, savedTaskID ->
                                        if (isSavedSuccessfully) {
                                            prefs.category = category
                                            toastController.showToast(fragmentContext, getString(com.example.core.R.string.task_is_saved_successfully_toast_text), true)
                                            toDoTask.id = savedTaskID.toInt()
                                            checkScheduleExactAlarmPermission(completeDateAndTimeDate, toDoTask)
                                            lifecycleScope.launch(Dispatchers.Main) {
                                                titleTIL.editText?.text = null
                                                descriptionTIL.editText?.text = null
                                                dayOfWeekTIL.editText?.text = null
                                                dateTIL.editText?.text = null
                                                timeTIL.editText?.text = null
                                                titleTIL.editText?.requestFocus()
                                            }
                                            category = 0
                                            if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed)
                                                addTasksAlertDialog.dismiss()
                                            startFABAnimation()
                                        } else
                                            toastController.showToast(fragmentContext, getString(com.example.core.R.string.task_is_not_saved_successfully_toast_text), false)
                                    }
                                )
                            }
                        }
                    } else {
                        val updatedToDoTask = ToDoTask(
                            toDoTask.id, dayOfWeek, dateSDF.format(parseDate), monthSDF.format(parseDate),
                            yearSDF.format(parseDate), title, description, time, category,
                            completeDateAndTimeDate, false
                        )
                        lifecycleScope.launch(Dispatchers.IO) {
                            tasksViewModel.updateTask(
                                toDoTask = updatedToDoTask,
                                isPastTimeCallback = {
                                    toastController.showToast(fragmentContext, getString(com.example.core.R.string.past_date_and_time_is_not_acceptable_toast), false)
                                },
                                isAlreadySavedCallback = {
                                    toastController.showToast(fragmentContext, getString(com.example.core.R.string.this_task_is_already_saved_toast_text), false)
                                },
                                isUpdatedSuccessfullyCallback = {
                                    if (it) {
                                        checkScheduleExactAlarmPermission(
                                            completeDateAndTimeDate, updatedToDoTask, true
                                        )
                                        toastController.showToast(fragmentContext, getString(com.example.core.R.string.updated_successfully_toast_text), true)
                                        prefs.category = category
                                        if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed) {
                                            addTasksAlertDialog.dismiss()
                                        }
                                        startFABAnimation()
                                        category = 0
                                    } else {
                                        toastController.showToast(fragmentContext, getString(com.example.core.R.string.not_updated_successfully_toast_text), false)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun checkScheduleExactAlarmPermission(
        completeDateAndTimeDate: Date,
        toDoTask: ToDoTask,
        isForUpdate: Boolean = false
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms())
                scheduleReminder(completeDateAndTimeDate, toDoTask, isForUpdate)
            else
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = "package:${fragmentContext.packageName}".toUri()
                    startActivity(this)
                }
        } else {
            scheduleReminder(completeDateAndTimeDate, toDoTask, isForUpdate)
        }
    }

    private fun scheduleReminder(
        dateAndTimeInMillis: Date,
        toDoTask: ToDoTask,
        isForUpdate: Boolean
    ) {
        val broadcastPI = PendingIntent.getBroadcast(
            fragmentContext, toDoTask.id,
            Intent(fragmentContext, ReminderReceiver::class.java).apply {
                putExtra("TASK", toDoTask)
            }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (isForUpdate)
            alarmManager.cancel(broadcastPI)

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            dateAndTimeInMillis.time,
            broadcastPI
        )
    }

    private fun showCustomPopupForCategorySelection(view: View, isForUpdateTask: Boolean) {

        val customPopupMenuLayoutBinding = CustomPopupMenuLayoutBinding.inflate(layoutInflater)
        popupWindow = PopupWindow(
            customPopupMenuLayoutBinding.root,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.isOutsideTouchable = true
        popupWindow.elevation = 5F
        val categoryArrayList = ArrayList<Int>()
        categoryArrayList.apply {
            add(TasksCategories.DEFAULT_CATEGORY.ordinal)
            add(TasksCategories.PERSONAL_CATEGORY.ordinal)
            add(TasksCategories.WORK_CATEGORY.ordinal)
            if (isForUpdateTask) removeAt(0)
        }

        val categoryAdapter = CategoryAdapter("Category", prefs) { category, _ ->
            if (category == TasksCategories.DEFAULT_CATEGORY.ordinal || category == TasksCategories.PERSONAL_CATEGORY.ordinal) {
                this.category = TasksCategories.PERSONAL_CATEGORY.ordinal
            } else if (category == TasksCategories.WORK_CATEGORY.ordinal) {
                this.category = TasksCategories.WORK_CATEGORY.ordinal
            }

            addAndUpdateTasksDialogLayoutBinding.apply {
                if ((category == TasksCategories.DEFAULT_CATEGORY.ordinal)) {
                    selectCategoryBtn.text =
                        fragmentContext.getString(com.example.core.R.string.select_category_text)
                    selectCategoryBtn.setTextColor(fragmentContext.getColorResource(R.color.subColor))
                } else if ((category == TasksCategories.PERSONAL_CATEGORY.ordinal)) {
                    selectCategoryBtn.text = fragmentContext.getString(com.example.core.R.string.personal_text)
                    selectCategoryBtn.setTextColor(fragmentContext.getColorResource(R.color.blackAndWhiteViewsColor))
                } else if ((category == TasksCategories.WORK_CATEGORY.ordinal)) {
                    selectCategoryBtn.text = fragmentContext.getString(com.example.core.R.string.work_text)
                    selectCategoryBtn.setTextColor(fragmentContext.getColorResource(R.color.blackAndWhiteViewsColor))
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
        sortingDialogBuilder.apply {
            setView(sortingDialogLayoutBinding.root)
            setCancelable(true)
            setOnDismissListener {
                if (selectedTab == Tabs.TASKS_TAB.ordinal) {
                    startFABAnimation()
                }
            }
        }
        val sortingAlertDialog = sortingDialogBuilder.create()
        if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed && !sortingAlertDialog.isShowing) {
            sortingAlertDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            sortingAlertDialog.window?.setWindowAnimations(R.style.dialogBoxesAnimation)
            sortingAlertDialog.show()
        }

        val sortByList = listOf(
            Sort(getString(com.example.core.R.string.title_text)),
            Sort(getString(com.example.core.R.string.day_of_week_text)),
            Sort(getString(com.example.core.R.string.date_text)),
            Sort(getString(com.example.core.R.string.month_text)),
            Sort(getString(com.example.core.R.string.year_text)),
            Sort(getString(com.example.core.R.string.time_hint))
        )

        val sortOrderList = listOf(
            Sort(getString(com.example.core.R.string.ascending_a_z_text)),
            Sort(getString(com.example.core.R.string.descending_z_a_text))
        )

        var sortByAdapter: SortAdapter? = null
        var sortOrderAdapter: SortAdapter? = null

        sortingDialogLayoutBinding.apply {
            stopFABAnimation()

            sortByRV.layoutManager =
                LinearLayoutManager(fragmentContext, RecyclerView.VERTICAL, false)
            sortByAdapter = SortAdapter { sort, position ->
                when (selectedTab) {
                    Tabs.TASKS_TAB.ordinal -> {
                        sortBy = sort
                        val updatedTasksSortByList =
                            sortByAdapter?.currentList?.mapIndexed { index, sort ->
                                sort.copy(isSelected = position == index)
                            }
                        sortByAdapter?.submitList(updatedTasksSortByList)
                    }

                    Tabs.COMPLETED_TAB.ordinal -> {
                        sortBy = sort
                        val updatedCompletedSortByList =
                            sortByAdapter?.currentList?.mapIndexed { index, sort ->
                                sort.copy(isSelected = position == index)
                        }
                        sortByAdapter?.submitList(updatedCompletedSortByList)
                    }
                }
            }
            sortByRV.adapter = sortByAdapter

            sortOrderRV.layoutManager =
                LinearLayoutManager(fragmentContext, RecyclerView.VERTICAL, false)
            sortOrderAdapter = SortAdapter { sort, position ->
                when (selectedTab) {
                    Tabs.TASKS_TAB.ordinal -> {
                        sortOrder = sort
                        val updatedTasksSortOrderList =
                            sortOrderAdapter?.currentList?.mapIndexed { index, sort ->
                                sort.copy(isSelected = position == index)
                            }
                        sortOrderAdapter?.submitList(updatedTasksSortOrderList)
                    }

                    Tabs.COMPLETED_TAB.ordinal -> {
                        sortOrder = sort
                        val updatedCompletedSortOrderList =
                            sortOrderAdapter?.currentList?.mapIndexed { index, sort ->
                                sort.copy(isSelected = position == index)
                            }
                        sortOrderAdapter?.submitList(updatedCompletedSortOrderList)
                    }
                }
            }
            sortOrderRV.adapter = sortOrderAdapter

            when (selectedTab) {
                Tabs.TASKS_TAB.ordinal -> {
                    val allTasksSortingValues = prefs.getAllTasksSortingValues
                    sortByList.forEach {
                        it.isSelected = it.sortName == allTasksSortingValues[0]
                        if (it.isSelected) sortBy = it
                    }
                    sortOrderList.forEach {
                        it.isSelected = it.sortName == allTasksSortingValues[1]
                        if (it.isSelected) sortOrder = it
                    }
                    sortByAdapter.submitList(sortByList)
                    sortOrderAdapter.submitList(sortOrderList)
                }

                Tabs.COMPLETED_TAB.ordinal -> {
                    val completedTasksSortingValues = prefs.getCompletedTasksSortingValues
                    sortByList.forEach {
                        it.isSelected = it.sortName == completedTasksSortingValues[0]
                        if (it.isSelected) sortBy = it
                    }
                    sortOrderList.forEach {
                        it.isSelected = it.sortName == completedTasksSortingValues[1]
                        if (it.isSelected) sortOrder = it
                    }
                    sortByAdapter.submitList(sortByList)
                    sortOrderAdapter.submitList(sortOrderList)
                }
            }

            cancelBtn.setOnClickListener {
                if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed) {
                    sortingAlertDialog.dismiss()
                }
            }

            sortBtn.setOnClickListener {
                when (selectedTab) {
                    Tabs.TASKS_TAB.ordinal -> {
                        prefs.saveAllTasksSortingValues(sortBy?.sortName, sortOrder?.sortName)
                        startFABAnimation()
                    }

                    Tabs.COMPLETED_TAB.ordinal -> {
                        prefs.saveCompletedTasksSortingValues(sortBy?.sortName, sortOrder?.sortName)
                    }
                }
                displayAllTasksOnRecyclerView()
                if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed) {
                    sortingAlertDialog.dismiss()
                }
            }
        }
    }

    private fun showMaterialDatePicker(addAndUpdateTasksDialogLayoutBinding: AddAndUpdateTasksDialogLayoutBinding) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
        datePicker.apply {
            setTitleText(com.example.core.R.string.select_date_text)
            setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
            val picker = build()
            picker.show(requireActivity().supportFragmentManager, "MATERIAL_DATE_PICKER")
            picker.addOnPositiveButtonClickListener { selection: Long? ->
                val date: String = simpleDateFormat.format(selection)
                addAndUpdateTasksDialogLayoutBinding.apply {
                    dateTIL.editText?.setText(date)
                    if (prefs.isDarkModeEnable) {
                        dateTIL.editText?.isFocusableInTouchMode = true
                        dateTIL.editText?.requestFocus()
                        dateTIL.setBoxStrokeColorStateList(textInputLayoutDarkModeStrokeColor)
                    }
                }
            }
        }
    }

    private fun showMaterialTimePicker(addAndUpdateTasksDialogLayoutBinding: AddAndUpdateTasksDialogLayoutBinding) {
        val builder = MaterialTimePicker.Builder()
        builder.apply {
            setTitleText(com.example.core.R.string.select_time_text)
            setTimeFormat(TimeFormat.CLOCK_12H)
            setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
            setHour(calendar[Calendar.HOUR])
            setMinute(calendar[Calendar.MINUTE])
            val materialTimePicker = build()
            materialTimePicker.show(requireActivity().supportFragmentManager, "MATERIAL_TIME_PICKER")
            materialTimePicker.addOnPositiveButtonClickListener { _: View? ->
                calendar.set(Calendar.HOUR_OF_DAY, materialTimePicker.hour)
                calendar.set(Calendar.MINUTE, materialTimePicker.minute)
                val time: String = simpleTimeFormat.format(calendar.time)
                addAndUpdateTasksDialogLayoutBinding.apply {
                    timeTIL.editText?.setText(time)
                    if (prefs.isDarkModeEnable) {
                        timeTIL.editText?.isFocusableInTouchMode = true
                        timeTIL.editText?.requestFocus()
                        timeTIL.setBoxStrokeColorStateList(textInputLayoutDarkModeStrokeColor)
                    }
                }
            }
        }
    }

    private fun openTaskDetailActivity(toDoTask: ToDoTask) {
        val toDoTaskDetailIntent = Intent(fragmentContext, ToDoTaskDetailActivity::class.java)
        toDoTaskDetailIntent.apply {
            putExtra("taskDetail", toDoTask)
            startActivity(this)
        }
    }

    private fun showDeleteTaskDialog(toDoTask: ToDoTask) {
        val deleteTaskDialogLayoutBinding = DeleteTaskDialogLayoutBinding.inflate(layoutInflater)

        val deleteTaskDialogBuilder = AlertDialog.Builder(fragmentContext)
        deleteTaskDialogBuilder.apply {
            setView(deleteTaskDialogLayoutBinding.root)
            setCancelable(true)
            setOnDismissListener {
                if (selectedTab == Tabs.TASKS_TAB.ordinal) {
                    startFABAnimation()
                }
            }
        }
        val deleteTaskAlertDialog = deleteTaskDialogBuilder.create()
        if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed && !deleteTaskAlertDialog.isShowing) {
            deleteTaskAlertDialog.apply {
                window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                window?.setWindowAnimations(R.style.dialogBoxesAnimation)
                show()
            }
        }

        deleteTaskDialogLayoutBinding.apply {
            deleteIV.startAnimation(animation)

            stopFABAnimation()

            noButton.setOnClickListener { _: View? ->
                if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed) {
                    deleteTaskAlertDialog.dismiss()
                }
                if (selectedTab == Tabs.TASKS_TAB.ordinal) {
                    startFABAnimation()
                }
            }

            yesButton.setOnClickListener { _: View? ->
                lifecycleScope.launch(Dispatchers.IO) {
                    tasksViewModel.deleteTask(toDoTask = toDoTask, isDeleteCallback = {
                        if (it) {
                            toastController.showToast(fragmentContext, getString(com.example.core.R.string.deleted_successfully_toast_text), true)
                            if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed) {
                                deleteTaskAlertDialog.dismiss()
                            }
                            if (selectedTab == Tabs.TASKS_TAB.ordinal) {
                                startFABAnimation()
                            }
                        } else {
                            toastController.showToast(fragmentContext, getString(com.example.core.R.string.deleted_unsuccessfully_toast_text), false)
                        }
                    })
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}