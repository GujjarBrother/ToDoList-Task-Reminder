package com.todo.list.fragments

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.todo.list.R
import com.todo.list.activities.ToDoTaskDetailActivity
import com.todo.list.adapters.TasksRecyclerViewAdapter
import com.todo.list.application.Application.Companion.prefs
import com.todo.list.application.Application.Companion.typeface
import com.todo.list.base.BaseFragment
import com.todo.list.databinding.DeleteTaskDialogLayoutBinding
import com.todo.list.databinding.FragmentCompletedTasksBinding
import com.todo.list.db.ToDoTask
import com.todo.list.enums.TabsEnum
import com.todo.list.utils.CommonFunctions.applyAnimation
import java.util.Collections

class CompletedTasksFragment : BaseFragment(), View.OnClickListener {

    private lateinit var binding: FragmentCompletedTasksBinding
    private lateinit var completedTasksArrayList: ArrayList<ToDoTask>
    private var aboveTempValue = 1
    private var belowTempValue = 7
    private var aboveSortedValue = 1
    private var belowSortedValue = 7
    private var isAboveSortingValueSelected = false
    private var isBelowSortingValueSelected = false
    private lateinit var adapter: TasksRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCompletedTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        completedTasksArrayList = ArrayList()
//        readCompletedTasks()
    }

    override fun onResume() {
        super.onResume()

        applyLightAndDarkModeOnCompletedTasksFragment()

        /*if (isSomethingChanged) {
            isSomethingChanged = false
            if (::adapter.isInitialized) {
                adapter.isTextSizeChanged = true
                adapter.notifyDataSetChanged()
            }
        }*/
    }

    private fun applyCustomFont() {
        with(binding) {
            listAndGridViewStylesTextView.typeface = typeface
            sortingTextView.typeface = typeface
            nothingInHereTextView.typeface = typeface
        }
    }

    private fun applyLightAndDarkModeOnCompletedTasksFragment() {
        with(binding) {
            if (prefs.isDarkModeEnable) {
                completedTasksFragmentCardView.setCardBackgroundColor(screensNightModeColor)
                nothingInHereTextView.setTextColor(darkModeTextColor)
                sortingCardView.setCardBackgroundColor(cardsNightModeColor)
                sortingImageView.setColorFilter(whiteColor)
                sortingTextView.setTextColor(whiteColor)
                stylesCardView.setCardBackgroundColor(cardsNightModeColor)
                listAndGridViewStylesImageView.setColorFilter(whiteColor)
                listAndGridViewStylesTextView.setTextColor(whiteColor)
                deletedPermanentlyTextView.setBackgroundColor(cardsNightModeColor)
                deletedPermanentlyTextView.setTextColor(darkModeTextColor)
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
                        deletedPermanentlyTextView.setBackgroundColor(defaultColor)
                    }

                    1 -> {
                        sortingImageView.setColorFilter(darkYellowColor)
                        listAndGridViewStylesImageView.setColorFilter(darkYellowColor)
                        nothingInHereTextView.setTextColor(darkYellowColor)
                        deletedPermanentlyTextView.setBackgroundColor(darkYellowColor)
                    }

                    2 -> {
                        sortingImageView.setColorFilter(orangeColor)
                        listAndGridViewStylesImageView.setColorFilter(orangeColor)
                        nothingInHereTextView.setTextColor(orangeColor)
                        deletedPermanentlyTextView.setBackgroundColor(orangeColor)
                    }

                    3 -> {
                        sortingImageView.setColorFilter(lightGreenColor)
                        listAndGridViewStylesImageView.setColorFilter(lightGreenColor)
                        nothingInHereTextView.setTextColor(lightGreenColor)
                        deletedPermanentlyTextView.setBackgroundColor(lightGreenColor)
                    }

                    4 -> {
                        sortingImageView.setColorFilter(blueColor)
                        listAndGridViewStylesImageView.setColorFilter(blueColor)
                        nothingInHereTextView.setTextColor(blueColor)
                        deletedPermanentlyTextView.setBackgroundColor(blueColor)
                    }

                    5 -> {
                        sortingImageView.setColorFilter(cyanColor)
                        listAndGridViewStylesImageView.setColorFilter(cyanColor)
                        nothingInHereTextView.setTextColor(cyanColor)
                        deletedPermanentlyTextView.setBackgroundColor(cyanColor)
                    }

                    6 -> {
                        sortingImageView.setColorFilter(pinkColor)
                        listAndGridViewStylesImageView.setColorFilter(pinkColor)
                        nothingInHereTextView.setTextColor(pinkColor)
                        deletedPermanentlyTextView.setBackgroundColor(pinkColor)
                    }

                    7 -> {
                        sortingImageView.setColorFilter(darkBlueColor)
                        listAndGridViewStylesImageView.setColorFilter(darkBlueColor)
                        nothingInHereTextView.setTextColor(darkBlueColor)
                        deletedPermanentlyTextView.setBackgroundColor(darkBlueColor)
                    }

                    8 -> {
                        sortingImageView.setColorFilter(redColor)
                        listAndGridViewStylesImageView.setColorFilter(redColor)
                        nothingInHereTextView.setTextColor(redColor)
                        deletedPermanentlyTextView.setBackgroundColor(redColor)
                    }

                    9 -> {
                        sortingImageView.setColorFilter(lightPurpleColor)
                        listAndGridViewStylesImageView.setColorFilter(lightPurpleColor)
                        nothingInHereTextView.setTextColor(lightPurpleColor)
                        deletedPermanentlyTextView.setBackgroundColor(lightPurpleColor)
                    }
                }
            }
        }
    }

    /*private fun readCompletedTasks() {
        with(binding) {
            if (completedTasksArrayList.isNotEmpty()) {
                completedTasksArrayList.clear()
            }
            completedTasksArrayList = ToDosDatabase.getDatabase(fragmentContext.applicationContext).dao().getAllTasks(true) as ArrayList<ToDoTask>
            if (completedTasksArrayList.size > 0) {
                group1.visibility = GONE
                group2.visibility = VISIBLE
                displayCompletedTasksOnRecyclerView(completedTasksArrayList)
            } else {
                group1.visibility = VISIBLE
                group2.visibility = GONE
            }
        }
    }*/

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
                colorsSchemeArray, true, TabsEnum.COMPLETED_TAB.ordinal
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
        }
        val deleteTaskAlertDialog = deleteTaskDialogBuilder.create()

        if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed && !deleteTaskAlertDialog.isShowing) {
            deleteTaskAlertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            deleteTaskAlertDialog.window?.setWindowAnimations(R.style.dialogBoxesAnimation)
            deleteTaskAlertDialog.show()
        }

        with(deleteTaskDialogLayoutBinding) {
            applyCustomFontOnDeleteTaskDialogViews(this)
            deleteIV.startAnimation(applyAnimation(fragmentContext))
            appColorSchemeORLightAndDarkModeOnDeleteTaskDialogViews(this)

            noButton.setOnClickListener { _: View? ->
                if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed) {
                    deleteTaskAlertDialog.dismiss()
                }
            }

            yesButton.setOnClickListener { _: View? ->
                /*val isDeleted = ToDosDatabase.getDatabase(fragmentContext.applicationContext).dao().deleteTask(toDoTask)
                if (isDeleted == 1) {
                    Toasty.success(fragmentContext, R.string.deleted_successfully_toast_text, Toasty.LENGTH_LONG).show()
//                    readCompletedTasks()
                    if (!fragmentContext.isFinishing && !fragmentContext.isDestroyed) {
                        deleteTaskAlertDialog.dismiss()
                    }
                } else {
                    Toasty.success(fragmentContext, R.string.deleted_unsuccessfully_toast_text, Toasty.LENGTH_LONG).show()
                }*/
            }
        }
    }

    private fun appColorSchemeORLightAndDarkModeOnDeleteTaskDialogViews(
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

    override fun onClick(v: View?) {
        with(binding) {
            when (v?.id) {
                R.id.sortingCardView -> {
//                    showSortingDialog()
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

    /*private fun showSortingDialog() {
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
    }*/

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

    /*private fun applyColorSchemeORLightAndDarkModeOnSortingDialog(
        sortingDialogLayoutBinding: SortingDialogLayoutBinding
    ) {
        with(sortingDialogLayoutBinding) {
            if (prefs.isDarkModeEnable) {
                sortingDialogRootLayout.background.colorFilter =
                    PorterDuffColorFilter(screensNightModeColor, PorterDuff.Mode.SRC_IN)
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
    }*/
}