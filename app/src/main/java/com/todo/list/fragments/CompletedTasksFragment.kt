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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.todo.list.R
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
import com.todo.list.databinding.FragmentCompletedTasksBinding
import com.todo.list.databinding.SortingDialogLayoutBinding
import com.todo.list.db.ToDoTask
import com.todo.list.listeners.CategorySelectionListener
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

class CompletedTasksFragment : BaseFragment(), View.OnClickListener, ToDoTaskDetailListener,
    TaskUpdateAndDeleteListener, CategorySelectionListener {

    private lateinit var binding: FragmentCompletedTasksBinding
    private lateinit var allTasksListLiveData: AllTasksListLiveData
    private lateinit var completedTasksListLiveData: CompletedTasksListLiveData
    private var category = 2
    private lateinit var completedTasksArrayList: ArrayList<ToDoTask>
    private var aboveTempValue = 1
    private var belowTempValue = 7
    private var aboveSortedValue = 1
    private var belowSortedValue = 7
    private var isAboveSortingValueSelected = false
    private var isBelowSortingValueSelected = false
    private lateinit var adapter: TasksRecyclerViewAdapter
    private lateinit var popupWindow: PopupWindow
    private lateinit var addAndUpdateTasksDialogLayoutBinding: AddAndUpdateTasksDialogLayoutBinding
    private var dateOfMonth = ""
    private var month = ""
    private var year = ""
    private lateinit var updatedToDoTask: ToDoTask

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCompletedTasksBinding.inflate(inflater, container, false)

        allTasksListLiveData = ViewModelProvider(requireActivity())[AllTasksListLiveData::class.java]
        completedTasksListLiveData = ViewModelProvider(requireActivity())[CompletedTasksListLiveData::class.java]
        completedTasksArrayList = ArrayList()
        readCompletedTasks()

        with(binding) {
            if (prefs.completedTasksStyleValue) {
                listAndGridViewStylesImageView.setImageDrawable(listViewStyleImage)
                listAndGridViewStylesTextView.text = getString(R.string.listview_text)
            } else {
                listAndGridViewStylesImageView.setImageDrawable(gridViewStyleImage)
                listAndGridViewStylesTextView.text = getString(R.string.gridview_text)
            }
            applyCustomFont()
            sortingCardView.setOnClickListener(this@CompletedTasksFragment)
            stylesCardView.setOnClickListener(this@CompletedTasksFragment)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        applyColorSchemeOrLightAndDarkModeOnCompletedTasksFragment()

        if (isSomethingChanged) {
            isSomethingChanged = false
            if (::adapter.isInitialized) {
                adapter.isTextSizeChanged = true
                adapter.notifyDataSetChanged()
            }
        }

        completedTasksListLiveData.mutableLiveData.observe(viewLifecycleOwner) { aBoolean: Boolean ->
            if (aBoolean) {
                readCompletedTasks()
            }
        }
    }

    private fun applyCustomFont() {
        with(binding) {
            listAndGridViewStylesTextView.typeface = typeface
            sortingTextView.typeface = typeface
            nothingInHereTextView.typeface = typeface
        }
    }

    private fun applyColorSchemeOrLightAndDarkModeOnCompletedTasksFragment() {
        with(binding) {
            if (prefs.dayAndNightModeSwitchValue) {
                completedTasksFragmentCardView.setCardBackgroundColor(screensNightModeColor)
                nothingInHereTextView.setTextColor(whiteColor)
                sortingCardView.setCardBackgroundColor(cardsNightModeColor)
                sortingImageView.setColorFilter(whiteColor)
                sortingTextView.setTextColor(whiteColor)
                stylesCardView.setCardBackgroundColor(cardsNightModeColor)
                listAndGridViewStylesImageView.setColorFilter(whiteColor)
                listAndGridViewStylesTextView.setTextColor(whiteColor)
            } else {
                completedTasksFragmentCardView.setCardBackgroundColor(fragmentsCardViewsColor)
                sortingCardView.setCardBackgroundColor(whiteColor)
                sortingTextView.setTextColor(blackColor)
                stylesCardView.setCardBackgroundColor(whiteColor)
                listAndGridViewStylesTextView.setTextColor(blackColor)
                when (prefs.colorSchemeValue) {
                    0 -> {
                        sortingImageView.setColorFilter(defaultColor)
                        listAndGridViewStylesImageView.setColorFilter(defaultColor)
                        nothingInHereTextView.setTextColor(defaultColor)
                    }

                    1 -> {
                        sortingImageView.setColorFilter(darkYellowColor)
                        listAndGridViewStylesImageView.setColorFilter(darkYellowColor)
                        nothingInHereTextView.setTextColor(darkYellowColor)
                    }

                    2 -> {
                        sortingImageView.setColorFilter(orangeColor)
                        listAndGridViewStylesImageView.setColorFilter(orangeColor)
                        nothingInHereTextView.setTextColor(orangeColor)
                    }

                    3 -> {
                        sortingImageView.setColorFilter(lightGreenColor)
                        listAndGridViewStylesImageView.setColorFilter(lightGreenColor)
                        nothingInHereTextView.setTextColor(lightGreenColor)
                    }

                    4 -> {
                        sortingImageView.setColorFilter(blueColor)
                        listAndGridViewStylesImageView.setColorFilter(blueColor)
                        nothingInHereTextView.setTextColor(blueColor)
                    }

                    5 -> {
                        sortingImageView.setColorFilter(cyanColor)
                        listAndGridViewStylesImageView.setColorFilter(cyanColor)
                        nothingInHereTextView.setTextColor(cyanColor)
                    }

                    6 -> {
                        sortingImageView.setColorFilter(pinkColor)
                        listAndGridViewStylesImageView.setColorFilter(pinkColor)
                        nothingInHereTextView.setTextColor(pinkColor)
                    }

                    7 -> {
                        sortingImageView.setColorFilter(darkBlueColor)
                        listAndGridViewStylesImageView.setColorFilter(darkBlueColor)
                        nothingInHereTextView.setTextColor(darkBlueColor)
                    }

                    8 -> {
                        sortingImageView.setColorFilter(redColor)
                        listAndGridViewStylesImageView.setColorFilter(redColor)
                        nothingInHereTextView.setTextColor(redColor)
                    }

                    9 -> {
                        sortingImageView.setColorFilter(lightPurpleColor)
                        listAndGridViewStylesImageView.setColorFilter(lightPurpleColor)
                        nothingInHereTextView.setTextColor(lightPurpleColor)
                    }
                }
            }
        }
    }

    private fun readCompletedTasks() {
        with(binding) {
            completedTasksArrayList.clear()
            completedTasksArrayList = toDosDatabase.dao().getAllTasks() as ArrayList<ToDoTask>
            if (completedTasksArrayList.size > 0) {
                group1.visibility = GONE
                group2.visibility = VISIBLE
                displayCompletedTasksOnRecyclerView(completedTasksArrayList)
            } else {
                group1.visibility = VISIBLE
                group2.visibility = GONE
            }
        }
    }

    private fun displayCompletedTasksOnRecyclerView(completedTasksArrayList: ArrayList<ToDoTask>) {
        val completedTasksSortingArray = prefs.completedTasksSortingValues
        aboveSortedValue = completedTasksSortingArray[0]
        belowSortedValue = completedTasksSortingArray[1]
        if (aboveSortedValue == 1) {
            if (belowSortedValue == 7) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    completedTasksArrayList.sortWith(Comparator.comparing(ToDoTask::title))
                }
            } else if (belowSortedValue == 8) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    completedTasksArrayList.sortWith(Collections.reverseOrder(Comparator.comparing(ToDoTask::title)))
                }
            }
        } else if (aboveSortedValue == 2) {
            if (belowSortedValue == 7) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    completedTasksArrayList.sortWith(Comparator.comparing(ToDoTask::day))
                }
            } else if (belowSortedValue == 8) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    completedTasksArrayList.sortWith(Collections.reverseOrder(Comparator.comparing(ToDoTask::day)))
                }
            }
        } else if (aboveSortedValue == 3) {
            if (belowSortedValue == 7) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    completedTasksArrayList.sortWith(Comparator.comparing(ToDoTask::date))
                }
            } else if (belowSortedValue == 8) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    completedTasksArrayList.sortWith(Collections.reverseOrder(Comparator.comparing(ToDoTask::date)))
                }
            }
        } else if (aboveSortedValue == 4) {
            if (belowSortedValue == 7) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    completedTasksArrayList.sortWith(Comparator.comparing(ToDoTask::month))
                }
            } else if (belowSortedValue == 8) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    completedTasksArrayList.sortWith(Collections.reverseOrder(Comparator.comparing(ToDoTask::month)))
                }
            }
        } else if (aboveSortedValue == 5) {
            if (belowSortedValue == 7) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    completedTasksArrayList.sortWith(Comparator.comparing(ToDoTask::year))
                }
            } else if (belowSortedValue == 8) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    completedTasksArrayList.sortWith(Collections.reverseOrder(Comparator.comparing(ToDoTask::year)))
                }
            }
        } else if (aboveSortedValue == 6) {
            if (belowSortedValue == 7) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    completedTasksArrayList.sortWith(Comparator.comparing(ToDoTask::time))
                }
            } else if (belowSortedValue == 8) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    completedTasksArrayList.sortWith(Collections.reverseOrder(Comparator.comparing(ToDoTask::time)))
                }
            }
        }

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
        val layoutManager: RecyclerView.LayoutManager = if (prefs.completedTasksStyleValue) {
                GridLayoutManager(fragmentContext, 2, GridLayoutManager.VERTICAL, false)
            } else {
                LinearLayoutManager(fragmentContext, LinearLayoutManager.VERTICAL, false)
            }

        with(binding) {
            completedTasksTasksRecyclerView.layoutManager = layoutManager
            if (::adapter.isInitialized) {
                completedTasksTasksRecyclerView.adapter = adapter
                adapter.submitList(completedTasksArrayList)
            }
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

    private fun showMaterialDatePickerToUpdate(addAndUpdateTasksDialogLayoutBinding: AddAndUpdateTasksDialogLayoutBinding) {
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
            val updatedDate = simpleDateFormat.format(selection)
            with(addAndUpdateTasksDialogLayoutBinding) {
                dateTextInputLayout.editText?.setText(updatedDate)
            }
        }
    }

    private fun showMaterialTimePickerToUpdate(addAndUpdateTasksDialogLayoutBinding: AddAndUpdateTasksDialogLayoutBinding) {
        val builder = MaterialTimePicker.Builder()
        builder.setTitleText(R.string.select_time_text)
        builder.setTimeFormat(TimeFormat.CLOCK_12H)
        builder.setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
        builder.setHour(calendar[Calendar.HOUR])
        builder.setMinute(calendar[Calendar.MINUTE])
        val materialTimePicker = builder.build()
        materialTimePicker.show(requireActivity().supportFragmentManager, "MATERIAL_TIME_PICKER")
        materialTimePicker.addOnPositiveButtonClickListener { _: View? ->
            calendar[Calendar.HOUR_OF_DAY] = materialTimePicker.hour
            calendar[Calendar.MINUTE] = materialTimePicker.minute
            val time = simpleTimeFormat.format(calendar.time)
            with(addAndUpdateTasksDialogLayoutBinding) {
                timeTextInputLayout.editText?.setText(time)
            }
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
                showUpdateTaskDialog(toDoTask)
            } else if (itemId == R.id.delete_item) {
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

    private fun showUpdateTaskDialog(toDoTask: ToDoTask) {
        addAndUpdateTasksDialogLayoutBinding = AddAndUpdateTasksDialogLayoutBinding.inflate(layoutInflater)

        val updateTasksDialogBuilder = AlertDialog.Builder(fragmentContext)
        updateTasksDialogBuilder.setView(addAndUpdateTasksDialogLayoutBinding.root)
        updateTasksDialogBuilder.setCancelable(false)
        val updateTasksAlertDialog = updateTasksDialogBuilder.create()

        if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed && !updateTasksAlertDialog.isShowing) {
            updateTasksAlertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            updateTasksAlertDialog.window?.setWindowAnimations(R.style.dialogBoxesAnimation)
            updateTasksAlertDialog.show()
        }

        with(addAndUpdateTasksDialogLayoutBinding) {
            applyCustomFontOnUpdateTaskDialogViews(this)
            applyColorSchemeORLightAndDarkModeOnUpdateTasksDialog(this)

            addAndEditImageView.setImageResource(R.drawable.update_image)
            addAndUpdateToDoTaskTextView.text = getString(R.string.update_todo_task_text)
            infoTextView.text = getString(R.string.update_info_message_text)
            titleTextInputLayout.editText?.setText(toDoTask.title)
            descriptionTextInputLayout.editText?.setText(toDoTask.description)
            dayOfWeekTextInputLayout.editText?.setText(toDoTask.day)
            dateTextInputLayout.editText?.setText("${toDoTask.month} ${toDoTask.date}, ${toDoTask.year}")
            timeTextInputLayout.editText?.setText(toDoTask.time)
            timeTextInputLayout.editText?.setText(toDoTask.time)
            timeTextInputLayout.editText?.setText(toDoTask.time)
            if (toDoTask.category == DEFAULT_CATEGORY || toDoTask.category == PERSONAL_CATEGORY) {
                selectCategoryTextView.text = getString(R.string.personal_text)
            } else if (toDoTask.category == WORK_CATEGORY) {
                selectCategoryTextView.text = getString(R.string.work_text)
            }
            selectCategoryTextView.setTextColor(blackColor)
            saveAndUpdateButton.text = getString(R.string.update_text)

            selectCategoryLayout.setOnClickListener { view: View ->
                showCustomPopupForCategorySelection(view)
            }

            dismissDialogImageView.setOnClickListener { _: View? ->
                if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed) {
                    updateTasksAlertDialog.dismiss()
                }
            }

            dateTextInputEditText.setOnClickListener { _: View? ->
                showMaterialDatePickerToUpdate(addAndUpdateTasksDialogLayoutBinding)
            }

            timeTextInputEditText.setOnClickListener { _: View? ->
                showMaterialTimePickerToUpdate(addAndUpdateTasksDialogLayoutBinding)
            }

            saveAndUpdateButton.setOnClickListener { _: View? ->
                val title = titleTextInputLayout.editText?.text.toString().trim()
                val description = descriptionTextInputLayout.editText?.text.toString().trim()
                val dayOfWeek = dayOfWeekTextInputLayout.editText?.text.toString().trim()
                val time = timeTextInputLayout.editText?.text.toString().trim()
                if (!(title.equals(toDoTask.title, ignoreCase = true)
                            && description.equals(toDoTask.description, ignoreCase = true)
                            && dayOfWeek.equals(toDoTask.day, ignoreCase = true)
                            && month.equals(toDoTask.month, ignoreCase = true)
                            && dateOfMonth.equals(toDoTask.date, ignoreCase = true)
                            && year.equals(toDoTask.year, ignoreCase = true)
                            && time.equals(toDoTask.time, ignoreCase = true)
                            && category == toDoTask.category)
                ) {
                    if (dateOfMonth.equals("", ignoreCase = true) && month.equals("", ignoreCase = true)
                        && year.equals("", ignoreCase = true)
                    ) {
                        dateOfMonth = toDoTask.date
                        month = toDoTask.month
                        year = toDoTask.year
                    }
                    if (category == DEFAULT_CATEGORY || category == PERSONAL_CATEGORY) {
                        updatedToDoTask = ToDoTask(
                            toDoTask.id, dayOfWeek, dateOfMonth, month, year,
                            title, description, time, PERSONAL_CATEGORY
                        )
                    } else if (category == WORK_CATEGORY) {
                        updatedToDoTask = ToDoTask(
                            toDoTask.id, dayOfWeek, dateOfMonth, month, year,
                            title, description, time, WORK_CATEGORY
                        )
                    }
                    val isUpdated = toDosDatabase.dao().updateTask(updatedToDoTask)
                    if (isUpdated == 1) {
                        dateOfMonth = ""
                        month = ""
                        year = ""
                        Toasty.success(fragmentContext, R.string.updated_successfully_toast_text, Toasty.LENGTH_LONG).show()
                        readCompletedTasks()
                        if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed) {
                            updateTasksAlertDialog.dismiss()
                        }
                        if (category == PERSONAL_CATEGORY) {
                            allTasksListLiveData.setMutableLiveDataValue(true)
                        }
                    } else {
                        Toasty.success(fragmentContext, R.string.not_updated_successfully_toast_text, Toasty.LENGTH_LONG).show()
                    }
                } else if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed) {
                    updateTasksAlertDialog.dismiss()
                }
            }
        }
    }

    private fun showCustomPopupForCategorySelection(view: View) {
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
            add(PERSONAL_CATEGORY)
            add(WORK_CATEGORY)
        }

        val categoryAdapter = CategoryAdapter(this, "Category")
        with(customPopupMenuLayoutBinding) {
            customPopUpMenuRecyclerView.adapter = categoryAdapter
        }
        categoryAdapter.submitList(categoryArrayList)
        popupWindow.showAsDropDown(view)
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

        with(deleteTaskDialogLayoutBinding) {
            applyCustomFontOnDeleteTaskDialogViews(this)
            deleteImageView.startAnimation(applyAnimation(fragmentContext))
            appColorSchemeORLightAndDarkModeOnDeleteTaskDialogViews(this)

            noButton.setOnClickListener { _: View? ->
                if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed) {
                    deleteTaskAlertDialog.dismiss()
                }
            }

            yesButton.setOnClickListener { _: View? ->
                val isDeleted = toDosDatabase.dao().deleteTask(toDoTask)
                if (isDeleted == 1) {
                    Toasty.success(fragmentContext, R.string.deleted_successfully_toast_text, Toasty.LENGTH_LONG).show()
                    readCompletedTasks()
                    if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed) {
                        deleteTaskAlertDialog.dismiss()
                    }
                } else {
                    Toasty.success(fragmentContext, R.string.deleted_unsuccessfully_toast_text, Toasty.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun appColorSchemeORLightAndDarkModeOnDeleteTaskDialogViews(
        deleteTaskDialogLayoutBinding: DeleteTaskDialogLayoutBinding
    ) {
        with(deleteTaskDialogLayoutBinding) {
            if (prefs.dayAndNightModeSwitchValue) {
                deleteTaskDialogRootLayout.setBackgroundResource(dialogBoxesDarkModeBackground)
                deleteImageView.setColorFilter(whiteColor)
                deleteMessageTextView.setTextColor(whiteColor)
                noButton.background.colorFilter =
                    PorterDuffColorFilter(whiteColor, PorterDuff.Mode.SRC_IN)
                noButton.setTextColor(whiteColor)
                yesButton.background.colorFilter =
                    PorterDuffColorFilter(whiteColor, PorterDuff.Mode.SRC_IN)
                yesButton.setTextColor(whiteColor)
            } else {
                when (prefs.colorSchemeValue) {
                    0 -> {
                        deleteImageView.setColorFilter(defaultColor)
                        noButton.background.colorFilter =
                            PorterDuffColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
                        yesButton.background.colorFilter =
                            PorterDuffColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
                    }

                    1 -> {
                        deleteImageView.setColorFilter(darkYellowColor)
                        noButton.background.colorFilter =
                            PorterDuffColorFilter(darkYellowColor, PorterDuff.Mode.SRC_IN)
                        yesButton.background.colorFilter =
                            PorterDuffColorFilter(darkYellowColor, PorterDuff.Mode.SRC_IN)
                    }

                    2 -> {
                        deleteImageView.setColorFilter(orangeColor)
                        noButton.background.colorFilter =
                            PorterDuffColorFilter(orangeColor, PorterDuff.Mode.SRC_IN)
                        yesButton.background.colorFilter =
                            PorterDuffColorFilter(orangeColor, PorterDuff.Mode.SRC_IN)
                    }

                    3 -> {
                        deleteImageView.setColorFilter(lightGreenColor)
                        noButton.background.colorFilter =
                            PorterDuffColorFilter(lightGreenColor, PorterDuff.Mode.SRC_IN)
                        yesButton.background.colorFilter =
                            PorterDuffColorFilter(lightGreenColor, PorterDuff.Mode.SRC_IN)
                    }

                    4 -> {
                        deleteImageView.setColorFilter(blueColor)
                        noButton.background.colorFilter =
                            PorterDuffColorFilter(blueColor, PorterDuff.Mode.SRC_IN)
                        yesButton.background.colorFilter =
                            PorterDuffColorFilter(blueColor, PorterDuff.Mode.SRC_IN)
                    }

                    5 -> {
                        deleteImageView.setColorFilter(cyanColor)
                        noButton.background.colorFilter =
                            PorterDuffColorFilter(cyanColor, PorterDuff.Mode.SRC_IN)
                        yesButton.background.colorFilter =
                            PorterDuffColorFilter(cyanColor, PorterDuff.Mode.SRC_IN)
                    }

                    6 -> {
                        deleteImageView.setColorFilter(pinkColor)
                        noButton.background.colorFilter =
                            PorterDuffColorFilter(pinkColor, PorterDuff.Mode.SRC_IN)
                        yesButton.background.colorFilter =
                            PorterDuffColorFilter(pinkColor, PorterDuff.Mode.SRC_IN)
                    }

                    7 -> {
                        deleteImageView.setColorFilter(darkBlueColor)
                        noButton.background.colorFilter =
                            PorterDuffColorFilter(darkBlueColor, PorterDuff.Mode.SRC_IN)
                        yesButton.background.colorFilter =
                            PorterDuffColorFilter(darkBlueColor, PorterDuff.Mode.SRC_IN)
                    }

                    8 -> {
                        deleteImageView.setColorFilter(redColor)
                        noButton.background.colorFilter =
                            PorterDuffColorFilter(redColor, PorterDuff.Mode.SRC_IN)
                        yesButton.background.colorFilter =
                            PorterDuffColorFilter(redColor, PorterDuff.Mode.SRC_IN)
                    }

                    9 -> {
                        deleteImageView.setColorFilter(lightPurpleColor)
                        noButton.background.colorFilter =
                            PorterDuffColorFilter(lightPurpleColor, PorterDuff.Mode.SRC_IN)
                        yesButton.background.colorFilter =
                            PorterDuffColorFilter(lightPurpleColor, PorterDuff.Mode.SRC_IN)
                    }
                }
            }
        }
    }

    private fun applyColorSchemeORLightAndDarkModeOnUpdateTasksDialog(
        addAndUpdateTasksDialogLayoutBinding: AddAndUpdateTasksDialogLayoutBinding
    ) {
        with(addAndUpdateTasksDialogLayoutBinding) {
            if (prefs.dayAndNightModeSwitchValue) {
                val whiteColorStateList = ColorStateList.valueOf(whiteColor)
                addToDoTaskDialogRootLayout.setBackgroundResource(dialogBoxesDarkModeBackground)
                dismissDialogImageView.setColorFilter(whiteColor)
                addAndEditImageView.setColorFilter(whiteColor)
                addAndUpdateToDoTaskTextView.setTextColor(whiteColor)
                infoTextView.setTextColor(whiteColor)

//            Here, We Change The Box Stroke Color Of TextInputLayout When That is Un-Focused...
                titleTextInputLayout.setBoxStrokeColorStateList(textInputLayoutBoxStrokeDarkModeColor)
                descriptionTextInputLayout.setBoxStrokeColorStateList(textInputLayoutBoxStrokeDarkModeColor)
                dayOfWeekTextInputLayout.setBoxStrokeColorStateList(textInputLayoutBoxStrokeDarkModeColor)
                dateTextInputLayout.setBoxStrokeColorStateList(textInputLayoutBoxStrokeDarkModeColor)
                timeTextInputLayout.setBoxStrokeColorStateList(textInputLayoutBoxStrokeDarkModeColor)
                titleTextInputLayout.boxStrokeColor = whiteColor
                titleTextInputLayout.setStartIconTintList(ColorStateList.valueOf(whiteColor))
                titleTextInputLayout.hintTextColor = whiteColorStateList
                titleTextInputLayout.boxStrokeErrorColor = whiteColorStateList
                titleTextInputEditText.setTextColor(whiteColor)
                descriptionTextInputLayout.boxStrokeColor = whiteColor
                descriptionTextInputLayout.setStartIconTintList(ColorStateList.valueOf(whiteColor))
                descriptionTextInputLayout.hintTextColor = whiteColorStateList
                descriptionTextInputLayout.boxStrokeErrorColor = whiteColorStateList
                descriptionTextInputEditText.setTextColor(whiteColor)
                dayOfWeekTextInputLayout.boxStrokeColor = whiteColor
                dayOfWeekTextInputLayout.setStartIconTintList(ColorStateList.valueOf(whiteColor))
                dayOfWeekTextInputLayout.hintTextColor = whiteColorStateList
                dayOfWeekTextInputLayout.boxStrokeErrorColor = whiteColorStateList
                dayOfWeekTextInputEditText.setTextColor(whiteColor)
                dateTextInputLayout.boxStrokeColor = whiteColor
                dateTextInputLayout.setStartIconTintList(ColorStateList.valueOf(whiteColor))
                dateTextInputLayout.hintTextColor = whiteColorStateList
                dateTextInputLayout.boxStrokeErrorColor = whiteColorStateList
                dateTextInputEditText.setTextColor(whiteColor)
                timeTextInputLayout.boxStrokeColor = whiteColor
                timeTextInputLayout.setStartIconTintList(ColorStateList.valueOf(whiteColor))
                timeTextInputLayout.hintTextColor = whiteColorStateList
                timeTextInputLayout.boxStrokeErrorColor = whiteColorStateList
                timeTextInputEditText.setTextColor(whiteColor)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    titleTextInputEditText.textCursorDrawable = editTextsCursorDarkModeColor
                    descriptionTextInputEditText.textCursorDrawable = editTextsCursorDarkModeColor
                    dayOfWeekTextInputEditText.textCursorDrawable = editTextsCursorDarkModeColor
                    dateTextInputEditText.textCursorDrawable = editTextsCursorDarkModeColor
                    timeTextInputEditText.textCursorDrawable = editTextsCursorDarkModeColor
                }
                saveAndUpdateButton.background.colorFilter =
                    PorterDuffColorFilter(whiteColor, PorterDuff.Mode.SRC_IN)
                saveAndUpdateButton.setTextColor(blackColor)
            } else {
                when (prefs.colorSchemeValue) {
                    0 -> {
                        val defaultColorStateList = ColorStateList.valueOf(defaultColor)
                        dismissDialogImageView.setColorFilter(defaultColor)
                        addAndEditImageView.setColorFilter(defaultColor)
                        addAndUpdateToDoTaskTextView.setTextColor(defaultColor)
                        titleTextInputLayout.setStartIconTintList(defaultColorStateList)
                        descriptionTextInputLayout.setStartIconTintList(defaultColorStateList)
                        dayOfWeekTextInputLayout.setStartIconTintList(defaultColorStateList)
                        dateTextInputLayout.setStartIconTintList(defaultColorStateList)
                        timeTextInputLayout.setStartIconTintList(defaultColorStateList)
                        saveAndUpdateButton.background.colorFilter =
                            PorterDuffColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
                    }

                    1 -> {
                        val darkYellowColorStateList = ColorStateList.valueOf(darkYellowColor)
                        dismissDialogImageView.setColorFilter(darkYellowColor)
                        addAndEditImageView.setColorFilter(darkYellowColor)
                        addAndUpdateToDoTaskTextView.setTextColor(darkYellowColor)
                        titleTextInputLayout.setStartIconTintList(darkYellowColorStateList)
                        descriptionTextInputLayout.setStartIconTintList(darkYellowColorStateList)
                        dayOfWeekTextInputLayout.setStartIconTintList(darkYellowColorStateList)
                        dateTextInputLayout.setStartIconTintList(darkYellowColorStateList)
                        timeTextInputLayout.setStartIconTintList(darkYellowColorStateList)
                        saveAndUpdateButton.background.colorFilter =
                            PorterDuffColorFilter(darkYellowColor, PorterDuff.Mode.SRC_IN)
                    }

                    2 -> {
                        val orangeColorStateList = ColorStateList.valueOf(orangeColor)
                        dismissDialogImageView.setColorFilter(orangeColor)
                        addAndEditImageView.setColorFilter(orangeColor)
                        addAndUpdateToDoTaskTextView.setTextColor(orangeColor)
                        titleTextInputLayout.setStartIconTintList(orangeColorStateList)
                        descriptionTextInputLayout.setStartIconTintList(orangeColorStateList)
                        dayOfWeekTextInputLayout.setStartIconTintList(orangeColorStateList)
                        dateTextInputLayout.setStartIconTintList(orangeColorStateList)
                        timeTextInputLayout.setStartIconTintList(orangeColorStateList)
                        saveAndUpdateButton.background.colorFilter =
                            PorterDuffColorFilter(orangeColor, PorterDuff.Mode.SRC_IN)
                    }

                    3 -> {
                        val lightGreenColorStateList = ColorStateList.valueOf(lightGreenColor)
                        dismissDialogImageView.setColorFilter(lightGreenColor)
                        addAndEditImageView.setColorFilter(lightGreenColor)
                        addAndUpdateToDoTaskTextView.setTextColor(lightGreenColor)
                        titleTextInputLayout.setStartIconTintList(lightGreenColorStateList)
                        descriptionTextInputLayout.setStartIconTintList(lightGreenColorStateList)
                        dayOfWeekTextInputLayout.setStartIconTintList(lightGreenColorStateList)
                        dateTextInputLayout.setStartIconTintList(lightGreenColorStateList)
                        timeTextInputLayout.setStartIconTintList(lightGreenColorStateList)
                        saveAndUpdateButton.background.colorFilter =
                            PorterDuffColorFilter(lightGreenColor, PorterDuff.Mode.SRC_IN)
                    }

                    4 -> {
                        val blueColorStateList = ColorStateList.valueOf(blueColor)
                        dismissDialogImageView.setColorFilter(blueColor)
                        addAndEditImageView.setColorFilter(blueColor)
                        addAndUpdateToDoTaskTextView.setTextColor(blueColor)
                        titleTextInputLayout.setStartIconTintList(blueColorStateList)
                        descriptionTextInputLayout.setStartIconTintList(blueColorStateList)
                        dayOfWeekTextInputLayout.setStartIconTintList(blueColorStateList)
                        dateTextInputLayout.setStartIconTintList(blueColorStateList)
                        timeTextInputLayout.setStartIconTintList(blueColorStateList)
                        saveAndUpdateButton.background.colorFilter =
                            PorterDuffColorFilter(blueColor, PorterDuff.Mode.SRC_IN)
                    }

                    5 -> {
                        val cyanColorStateList = ColorStateList.valueOf(cyanColor)
                        dismissDialogImageView.setColorFilter(cyanColor)
                        addAndEditImageView.setColorFilter(cyanColor)
                        addAndUpdateToDoTaskTextView.setTextColor(cyanColor)
                        titleTextInputLayout.setStartIconTintList(cyanColorStateList)
                        descriptionTextInputLayout.setStartIconTintList(cyanColorStateList)
                        dayOfWeekTextInputLayout.setStartIconTintList(cyanColorStateList)
                        dateTextInputLayout.setStartIconTintList(cyanColorStateList)
                        timeTextInputLayout.setStartIconTintList(cyanColorStateList)
                        saveAndUpdateButton.background.colorFilter =
                            PorterDuffColorFilter(cyanColor, PorterDuff.Mode.SRC_IN)
                    }

                    6 -> {
                        val pinkColorStateList = ColorStateList.valueOf(pinkColor)
                        dismissDialogImageView.setColorFilter(pinkColor)
                        addAndEditImageView.setColorFilter(pinkColor)
                        addAndUpdateToDoTaskTextView.setTextColor(pinkColor)
                        titleTextInputLayout.setStartIconTintList(pinkColorStateList)
                        descriptionTextInputLayout.setStartIconTintList(pinkColorStateList)
                        dayOfWeekTextInputLayout.setStartIconTintList(pinkColorStateList)
                        dateTextInputLayout.setStartIconTintList(pinkColorStateList)
                        timeTextInputLayout.setStartIconTintList(pinkColorStateList)
                        saveAndUpdateButton.background.colorFilter =
                            PorterDuffColorFilter(pinkColor, PorterDuff.Mode.SRC_IN)
                    }

                    7 -> {
                        val darkBlueColorStateList = ColorStateList.valueOf(darkBlueColor)
                        dismissDialogImageView.setColorFilter(darkBlueColor)
                        addAndEditImageView.setColorFilter(darkBlueColor)
                        addAndUpdateToDoTaskTextView.setTextColor(darkBlueColor)
                        titleTextInputLayout.setStartIconTintList(darkBlueColorStateList)
                        descriptionTextInputLayout.setStartIconTintList(darkBlueColorStateList)
                        dayOfWeekTextInputLayout.setStartIconTintList(darkBlueColorStateList)
                        dateTextInputLayout.setStartIconTintList(darkBlueColorStateList)
                        timeTextInputLayout.setStartIconTintList(darkBlueColorStateList)
                        saveAndUpdateButton.background.colorFilter =
                            PorterDuffColorFilter(darkBlueColor, PorterDuff.Mode.SRC_IN)
                    }

                    8 -> {
                        val redColorStateList = ColorStateList.valueOf(redColor)
                        dismissDialogImageView.setColorFilter(redColor)
                        addAndEditImageView.setColorFilter(redColor)
                        addAndUpdateToDoTaskTextView.setTextColor(redColor)
                        titleTextInputLayout.setStartIconTintList(redColorStateList)
                        descriptionTextInputLayout.setStartIconTintList(redColorStateList)
                        dayOfWeekTextInputLayout.setStartIconTintList(redColorStateList)
                        dateTextInputLayout.setStartIconTintList(redColorStateList)
                        timeTextInputLayout.setStartIconTintList(redColorStateList)
                        saveAndUpdateButton.background.colorFilter =
                            PorterDuffColorFilter(redColor, PorterDuff.Mode.SRC_IN)
                    }

                    9 -> {
                        val lightPurpleColorStateList = ColorStateList.valueOf(lightPurpleColor)
                        dismissDialogImageView.setColorFilter(lightPurpleColor)
                        addAndEditImageView.setColorFilter(lightPurpleColor)
                        addAndUpdateToDoTaskTextView.setTextColor(lightPurpleColor)
                        titleTextInputLayout.setStartIconTintList(lightPurpleColorStateList)
                        descriptionTextInputLayout.setStartIconTintList(lightPurpleColorStateList)
                        dayOfWeekTextInputLayout.setStartIconTintList(lightPurpleColorStateList)
                        dateTextInputLayout.setStartIconTintList(lightPurpleColorStateList)
                        timeTextInputLayout.setStartIconTintList(lightPurpleColorStateList)
                        saveAndUpdateButton.background.colorFilter =
                            PorterDuffColorFilter(lightPurpleColor, PorterDuff.Mode.SRC_IN)
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

    private fun applyCustomFontOnUpdateTaskDialogViews(
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

    private fun applyFontToPopupMenuItem(menuItem: MenuItem) {
        val spannableString = SpannableString(menuItem.title)
        spannableString.setSpan(
            CustomTypeFaceSpan("", typeface, Color.BLACK), 0, spannableString.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        menuItem.setTitle(spannableString)
    }

    override fun onClick(v: View?) {
        with(binding) {
            when (v?.id) {
                R.id.sortingCardView -> {
                    showSortingDialog()
                }

                R.id.stylesCardView -> {
                    if (prefs.completedTasksStyleValue) {
                        listAndGridViewStylesImageView.setImageDrawable(gridViewStyleImage)
                        listAndGridViewStylesTextView.text = getString(R.string.gridview_text)
                        prefs.completedTasksStyleValue = false
                        displayCompletedTasksOnRecyclerView(completedTasksArrayList)
                    } else {
                        listAndGridViewStylesImageView.setImageDrawable(listViewStyleImage)
                        listAndGridViewStylesTextView.text = getString(R.string.listview_text)
                        prefs.completedTasksStyleValue = true
                        displayCompletedTasksOnRecyclerView(completedTasksArrayList)
                    }
                }
            }
        }
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
            applyCustomFontOnSortingDialogViews(this)
            applyColorSchemeORLightAndDarkModeOnSortingDialog(this)

            val completedTasksSortingArray = prefs.completedTasksSortingValues
            aboveSortedValue = completedTasksSortingArray[0]
            belowSortedValue = completedTasksSortingArray[1]

            if (aboveSortedValue == 1) {
                sortingDialogLayoutBinding.titleRadioButton.isChecked = true
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
                sortRVAdapterList()
                if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed) {
                    sortingAlertDialog.dismiss()
                }
            }
        }
    }

    private fun sortRVAdapterList() {
        if (isAboveSortingValueSelected) {
            aboveSortedValue = aboveTempValue
        }

        if (isBelowSortingValueSelected) {
            belowSortedValue = belowTempValue
        }

        prefs.saveCompletedTasksSortingValues(aboveSortedValue, belowSortedValue)
        displayCompletedTasksOnRecyclerView(completedTasksArrayList)
    }

    private fun applyColorSchemeORLightAndDarkModeOnSortingDialog(
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
                cancelButton.background.colorFilter = PorterDuffColorFilter(whiteColor, PorterDuff.Mode.SRC_IN)
                cancelButton.setTextColor(whiteColor)
                sortButton.background.colorFilter = PorterDuffColorFilter(whiteColor, PorterDuff.Mode.SRC_IN)
                sortButton.setTextColor(whiteColor)
            } else {
                when (prefs.colorSchemeValue) {
                    0 -> {
                        sortByTextView.setTextColor(defaultColor)
                        titleRadioButton.buttonTintList = ColorStateList.valueOf(defaultColor)
                        dayOfWeekRadioButton.buttonTintList = ColorStateList.valueOf(defaultColor)
                        dateRadioButton.buttonTintList = ColorStateList.valueOf(defaultColor)
                        monthRadioButton.buttonTintList = ColorStateList.valueOf(defaultColor)
                        yearRadioButton.buttonTintList = ColorStateList.valueOf(defaultColor)
                        timeRadioButton.buttonTintList = ColorStateList.valueOf(defaultColor)
                        ascendingAToZRadioButton.buttonTintList = ColorStateList.valueOf(defaultColor)
                        descendingZToARadioButton.buttonTintList = ColorStateList.valueOf(defaultColor)
                        cancelButton.background.colorFilter = PorterDuffColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
                        sortButton.background.colorFilter = PorterDuffColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
                    }

                    1 -> {
                        sortByTextView.setTextColor(darkYellowColor)
                        titleRadioButton.buttonTintList = ColorStateList.valueOf(darkYellowColor)
                        dayOfWeekRadioButton.buttonTintList = ColorStateList.valueOf(darkYellowColor)
                        dateRadioButton.buttonTintList = ColorStateList.valueOf(darkYellowColor)
                        monthRadioButton.buttonTintList = ColorStateList.valueOf(darkYellowColor)
                        yearRadioButton.buttonTintList = ColorStateList.valueOf(darkYellowColor)
                        timeRadioButton.buttonTintList = ColorStateList.valueOf(darkYellowColor)
                        ascendingAToZRadioButton.buttonTintList = ColorStateList.valueOf(darkYellowColor)
                        descendingZToARadioButton.buttonTintList = ColorStateList.valueOf(darkYellowColor)
                        cancelButton.background.colorFilter = PorterDuffColorFilter(darkYellowColor, PorterDuff.Mode.SRC_IN)
                        sortButton.background.colorFilter = PorterDuffColorFilter(darkYellowColor, PorterDuff.Mode.SRC_IN)
                    }

                    2 -> {
                        sortByTextView.setTextColor(orangeColor)
                        titleRadioButton.buttonTintList = ColorStateList.valueOf(orangeColor)
                        dayOfWeekRadioButton.buttonTintList = ColorStateList.valueOf(orangeColor)
                        dateRadioButton.buttonTintList = ColorStateList.valueOf(orangeColor)
                        monthRadioButton.buttonTintList = ColorStateList.valueOf(orangeColor)
                        yearRadioButton.buttonTintList = ColorStateList.valueOf(orangeColor)
                        timeRadioButton.buttonTintList = ColorStateList.valueOf(orangeColor)
                        ascendingAToZRadioButton.buttonTintList = ColorStateList.valueOf(orangeColor)
                        descendingZToARadioButton.buttonTintList = ColorStateList.valueOf(orangeColor)
                        cancelButton.background.colorFilter = PorterDuffColorFilter(orangeColor, PorterDuff.Mode.SRC_IN)
                        sortButton.background.colorFilter = PorterDuffColorFilter(orangeColor, PorterDuff.Mode.SRC_IN)
                    }

                    3 -> {
                        sortByTextView.setTextColor(lightGreenColor)
                        titleRadioButton.buttonTintList = ColorStateList.valueOf(lightGreenColor)
                        dayOfWeekRadioButton.buttonTintList = ColorStateList.valueOf(lightGreenColor)
                        dateRadioButton.buttonTintList = ColorStateList.valueOf(lightGreenColor)
                        monthRadioButton.buttonTintList = ColorStateList.valueOf(lightGreenColor)
                        yearRadioButton.buttonTintList = ColorStateList.valueOf(lightGreenColor)
                        timeRadioButton.buttonTintList = ColorStateList.valueOf(lightGreenColor)
                        ascendingAToZRadioButton.buttonTintList = ColorStateList.valueOf(lightGreenColor)
                        descendingZToARadioButton.buttonTintList = ColorStateList.valueOf(lightGreenColor)
                        cancelButton.background.colorFilter = PorterDuffColorFilter(lightGreenColor, PorterDuff.Mode.SRC_IN)
                        sortButton.background.colorFilter = PorterDuffColorFilter(lightGreenColor, PorterDuff.Mode.SRC_IN)
                    }

                    4 -> {
                        sortByTextView.setTextColor(blueColor)
                        titleRadioButton.buttonTintList = ColorStateList.valueOf(blueColor)
                        dayOfWeekRadioButton.buttonTintList = ColorStateList.valueOf(blueColor)
                        dateRadioButton.buttonTintList = ColorStateList.valueOf(blueColor)
                        monthRadioButton.buttonTintList = ColorStateList.valueOf(blueColor)
                        yearRadioButton.buttonTintList = ColorStateList.valueOf(blueColor)
                        timeRadioButton.buttonTintList = ColorStateList.valueOf(blueColor)
                        ascendingAToZRadioButton.buttonTintList = ColorStateList.valueOf(blueColor)
                        descendingZToARadioButton.buttonTintList = ColorStateList.valueOf(blueColor)
                        cancelButton.background.colorFilter = PorterDuffColorFilter(blueColor, PorterDuff.Mode.SRC_IN)
                        sortButton.background.colorFilter = PorterDuffColorFilter(blueColor, PorterDuff.Mode.SRC_IN)
                    }

                    5 -> {
                        sortByTextView.setTextColor(cyanColor)
                        titleRadioButton.buttonTintList = ColorStateList.valueOf(cyanColor)
                        dayOfWeekRadioButton.buttonTintList = ColorStateList.valueOf(cyanColor)
                        dateRadioButton.buttonTintList = ColorStateList.valueOf(cyanColor)
                        monthRadioButton.buttonTintList = ColorStateList.valueOf(cyanColor)
                        yearRadioButton.buttonTintList = ColorStateList.valueOf(cyanColor)
                        timeRadioButton.buttonTintList = ColorStateList.valueOf(cyanColor)
                        ascendingAToZRadioButton.buttonTintList = ColorStateList.valueOf(cyanColor)
                        descendingZToARadioButton.buttonTintList = ColorStateList.valueOf(cyanColor)
                        cancelButton.background.colorFilter = PorterDuffColorFilter(cyanColor, PorterDuff.Mode.SRC_IN)
                        sortButton.background.colorFilter = PorterDuffColorFilter(cyanColor, PorterDuff.Mode.SRC_IN)
                    }

                    6 -> {
                        sortByTextView.setTextColor(pinkColor)
                        titleRadioButton.buttonTintList = ColorStateList.valueOf(pinkColor)
                        dayOfWeekRadioButton.buttonTintList = ColorStateList.valueOf(pinkColor)
                        dateRadioButton.buttonTintList = ColorStateList.valueOf(pinkColor)
                        monthRadioButton.buttonTintList = ColorStateList.valueOf(pinkColor)
                        yearRadioButton.buttonTintList = ColorStateList.valueOf(pinkColor)
                        timeRadioButton.buttonTintList = ColorStateList.valueOf(pinkColor)
                        ascendingAToZRadioButton.buttonTintList = ColorStateList.valueOf(pinkColor)
                        descendingZToARadioButton.buttonTintList = ColorStateList.valueOf(pinkColor)
                        cancelButton.background.colorFilter = PorterDuffColorFilter(pinkColor, PorterDuff.Mode.SRC_IN)
                        sortButton.background.colorFilter = PorterDuffColorFilter(pinkColor, PorterDuff.Mode.SRC_IN)
                    }

                    7 -> {
                        sortByTextView.setTextColor(darkBlueColor)
                        titleRadioButton.buttonTintList = ColorStateList.valueOf(darkBlueColor)
                        dayOfWeekRadioButton.buttonTintList = ColorStateList.valueOf(darkBlueColor)
                        dateRadioButton.buttonTintList = ColorStateList.valueOf(darkBlueColor)
                        monthRadioButton.buttonTintList = ColorStateList.valueOf(darkBlueColor)
                        yearRadioButton.buttonTintList = ColorStateList.valueOf(darkBlueColor)
                        timeRadioButton.buttonTintList = ColorStateList.valueOf(darkBlueColor)
                        ascendingAToZRadioButton.buttonTintList = ColorStateList.valueOf(darkBlueColor)
                        descendingZToARadioButton.buttonTintList = ColorStateList.valueOf(darkBlueColor)
                        cancelButton.background.colorFilter = PorterDuffColorFilter(darkBlueColor, PorterDuff.Mode.SRC_IN)
                        sortButton.background.colorFilter = PorterDuffColorFilter(darkBlueColor, PorterDuff.Mode.SRC_IN)
                    }

                    8 -> {
                        sortByTextView.setTextColor(redColor)
                        titleRadioButton.buttonTintList = ColorStateList.valueOf(redColor)
                        dayOfWeekRadioButton.buttonTintList = ColorStateList.valueOf(redColor)
                        dateRadioButton.buttonTintList = ColorStateList.valueOf(redColor)
                        monthRadioButton.buttonTintList = ColorStateList.valueOf(redColor)
                        yearRadioButton.buttonTintList = ColorStateList.valueOf(redColor)
                        timeRadioButton.buttonTintList = ColorStateList.valueOf(redColor)
                        ascendingAToZRadioButton.buttonTintList = ColorStateList.valueOf(redColor)
                        descendingZToARadioButton.buttonTintList = ColorStateList.valueOf(redColor)
                        cancelButton.background.colorFilter = PorterDuffColorFilter(redColor, PorterDuff.Mode.SRC_IN)
                        sortButton.background.colorFilter = PorterDuffColorFilter(redColor, PorterDuff.Mode.SRC_IN)
                    }

                    9 -> {
                        sortByTextView.setTextColor(lightPurpleColor)
                        titleRadioButton.buttonTintList = ColorStateList.valueOf(lightPurpleColor)
                        dayOfWeekRadioButton.buttonTintList = ColorStateList.valueOf(lightPurpleColor)
                        dateRadioButton.buttonTintList = ColorStateList.valueOf(lightPurpleColor)
                        monthRadioButton.buttonTintList = ColorStateList.valueOf(lightPurpleColor)
                        yearRadioButton.buttonTintList = ColorStateList.valueOf(lightPurpleColor)
                        timeRadioButton.buttonTintList = ColorStateList.valueOf(lightPurpleColor)
                        ascendingAToZRadioButton.buttonTintList = ColorStateList.valueOf(lightPurpleColor)
                        descendingZToARadioButton.buttonTintList = ColorStateList.valueOf(lightPurpleColor)
                        cancelButton.background.colorFilter = PorterDuffColorFilter(lightPurpleColor, PorterDuff.Mode.SRC_IN)
                        sortButton.background.colorFilter = PorterDuffColorFilter(lightPurpleColor, PorterDuff.Mode.SRC_IN)
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

    override fun selectCategory(category: Int) {
        if (category == DEFAULT_CATEGORY || category == PERSONAL_CATEGORY) {
            this.category = PERSONAL_CATEGORY
        } else if (category == WORK_CATEGORY) {
            this.category = WORK_CATEGORY
        }

        with(addAndUpdateTasksDialogLayoutBinding) {
            when (category) {
                DEFAULT_CATEGORY -> {
                    selectCategoryTextView.text = fragmentContext.getString(R.string.select_category_text)
                    selectCategoryTextView.setTextColor(Color.parseColor("#9E9E9E"))
                }

                PERSONAL_CATEGORY -> {
                    selectCategoryTextView.text = fragmentContext.getString(R.string.personal_text)
                    selectCategoryTextView.setTextColor(blackColor)
                }

                WORK_CATEGORY -> {
                    selectCategoryTextView.text = fragmentContext.getString(R.string.work_text)
                    selectCategoryTextView.setTextColor(blackColor)
                }
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
            if (fake and Typeface.BOLD != 0) {
                paint.isFakeBoldText = true
            }
            if (fake and Typeface.ITALIC != 0) {
                paint.textSkewX = -0.25f
            }
            paint.setTypeface(tf)
        }
    }
}