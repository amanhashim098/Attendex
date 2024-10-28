package com.example.attendex

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class VisaActivity : AppCompatActivity() {

    private val calendar = Calendar.getInstance()

    // Define UI elements
    private lateinit var reasonInput: EditText
    private lateinit var startDateInput: EditText
    private lateinit var endDateInput: EditText
    private lateinit var submitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visa)

        // Initialize UI elements using findViewById
        reasonInput = findViewById(R.id.reason_input)
        startDateInput = findViewById(R.id.start_date_input)
        endDateInput = findViewById(R.id.end_date_input)
        submitButton = findViewById(R.id.submit_button)

        val teacherSpinner: AutoCompleteTextView = findViewById(R.id.teacher_spinner)
        val teachers = arrayOf("Fabiola Pohrmen", "Resmi K R", "Vineetha K R", "Arokia Paul")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, teachers)
        teacherSpinner.setAdapter(adapter)

        // Setting click listeners for date inputs
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

        // Setting click listener for the Submit button
        submitButton.setOnClickListener {
            handleSubmit()
        }
    }

    // Function to show the DatePickerDialog and return the selected date
    private fun showDatePickerDialog(onDateSet: (String) -> Unit) {
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                onDateSet(dateFormat.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    // Handling the form submission
    private fun handleSubmit() {
        val reason = reasonInput.text.toString()
        val startDate = startDateInput.text.toString()
        val endDate = endDateInput.text.toString()

        if (reason.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
        } else {
            // You can handle the form submission logic here (e.g., store in Firestore)
            Toast.makeText(this, "Form Submitted Successfully", Toast.LENGTH_SHORT).show()
        }
    }
}
