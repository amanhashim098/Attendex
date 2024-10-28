package com.example.attendex

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.attendex.databinding.ActivityVisaBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class VisaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVisaBinding
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVisaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setting click listeners for date inputs
        binding.startDateInput.setOnClickListener {
            showDatePickerDialog { date ->
                binding.startDateInput.setText(date)
            }
        }

        binding.endDateInput.setOnClickListener {
            showDatePickerDialog { date ->
                binding.endDateInput.setText(date)
            }
        }

        // Setting click listener for the Submit button
        binding.submitButton.setOnClickListener {
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
        val reason = binding.reasonInput.text.toString()
        val startDate = binding.startDateInput.text.toString()
        val endDate = binding.endDateInput.text.toString()

        if (reason.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
        } else {
            // You can handle the form submission logic here (e.g., store in Firestore)
            Toast.makeText(this, "Form Submitted Successfully", Toast.LENGTH_SHORT).show()
        }
    }
}
