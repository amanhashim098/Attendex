package com.example.attendex

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.attendex.R
import java.util.*

class MedicalActivity : AppCompatActivity() {

    private lateinit var startDateInput: EditText
    private lateinit var endDateInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medical)

        startDateInput = findViewById(R.id.start_date_input)
        endDateInput = findViewById(R.id.end_date_input)

        val teacherSpinner: AutoCompleteTextView = findViewById(R.id.teacher_spinner)
        val teachers = arrayOf("Fabiola Pohrmen", "Resmi K R", "Vineetha K R", "Arokia Paul")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, teachers)
        teacherSpinner.setAdapter(adapter)

        startDateInput.setOnClickListener {
            showDatePickerDialog { date ->
                startDateInput.setText(date)
            }
        }

        endDateInput.setOnClickListener {
            showDatePickerDialog { date ->
                endDateInput.setText(date)
            }
        }
    }

    private fun showDatePickerDialog(onDateSet: (String) -> Unit) {
        // Get current date
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Create a DatePickerDialog
        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            // Format the selected date as desired
            val formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
            onDateSet(formattedDate)
        }, year, month, day)

        // Show the DatePickerDialog
        datePickerDialog.show()
    }
}
