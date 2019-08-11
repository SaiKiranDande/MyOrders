package com.example.myorders

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import java.util.*

class SetTime(
    val onCLickView: View, val listener: SetTime.SetonDateTimeSelectListener,
     val minDate: Long? = null) : View.OnClickListener, TimePickerDialog.OnTimeSetListener,
    DatePickerDialog.OnDateSetListener {

    private val calendar: Calendar

    init {
        onCLickView.setOnClickListener(this)
        calendar = Calendar.getInstance()
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
        listener.onDateTimeSelected(calendar, onCLickView)
    }

    override fun onClick(v: View) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dateDialog = DatePickerDialog(v.context, this, year, month, day)
        minDate?.let { dateDialog.datePicker.minDate = minDate }
        dateDialog.setOnCancelListener { listener.onDateDismissed(calendar, onCLickView) }
        dateDialog.setOnDismissListener { listener.onDateDismissed(calendar, onCLickView) }
        dateDialog.show()
    }

    override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, monthOfYear)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        listener.onDateTimeSelected(calendar, onCLickView)

    }

    interface SetonDateTimeSelectListener {
        fun onDateTimeSelected(calendar: Calendar, view: View)
        fun onDateDismissed(calendar: Calendar, view: View) {
        }

        fun onTimeDismissed(calendar: Calendar, view: View) {
        }
    }
}