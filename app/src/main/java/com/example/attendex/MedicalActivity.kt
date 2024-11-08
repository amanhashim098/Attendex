package com.example.attendex

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MedicalActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var classInput: EditText
    private lateinit var rollInput: EditText
    private lateinit var reasonInput: EditText
    private lateinit var teacherSpinner: AutoCompleteTextView
    private lateinit var startDateInput: EditText
    private lateinit var endDateInput: EditText
    private lateinit var attachCertificateButton: MaterialButton
    private lateinit var otherDocumentsButton: MaterialButton
    private lateinit var previewCertificateButton: MaterialButton
    private lateinit var submitButton: MaterialButton

    private var certificateUri: Uri? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_CAMERA_PERMISSION = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medical)

        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        setupViews()
        setupDatePickers()
        setupButtons()
    }

    private fun setupViews() {
        nameInput = findViewById(R.id.name_input)
        classInput = findViewById(R.id.class_input)
        rollInput = findViewById(R.id.roll_input)
        reasonInput = findViewById(R.id.reason_input)
        teacherSpinner = findViewById(R.id.teacher_spinner)
        startDateInput = findViewById(R.id.start_date_input)
        endDateInput = findViewById(R.id.end_date_input)
        attachCertificateButton = findViewById(R.id.attach_certificate_button)
        previewCertificateButton = findViewById(R.id.preview_certificate_button)
        otherDocumentsButton = findViewById(R.id.other_documents_button)
        submitButton = findViewById(R.id.submit_button)

        val teachers = arrayOf("Fabiola Pohrmen", "Resmi K R", "Vineetha K R", "Arokia Paul")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, teachers)
        teacherSpinner.setAdapter(adapter)
    }

    private fun setupDatePickers() {
        startDateInput.setOnClickListener {
            showDatePickerDialog { date -> startDateInput.setText(date) }
        }

        endDateInput.setOnClickListener {
            showDatePickerDialog { date -> endDateInput.setText(date) }
        }
    }

    private fun setupButtons() {
        attachCertificateButton.setOnClickListener { checkCameraPermissionAndCapture() }

        submitButton.setOnClickListener { validateAndSubmitForm() }

        previewCertificateButton.isEnabled = false
    }

    private fun showDatePickerDialog(onDateSet: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
            onDateSet(formattedDate)
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun checkCameraPermissionAndCapture() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        } else {
            dispatchTakePictureIntent()
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val photoFile: File? = try { createImageFile() } catch (ex: IOException) { null }
            photoFile?.let {
                val photoURI: Uri = FileProvider.getUriForFile(this, "com.example.attendex.fileprovider", it)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply { certificateUri = Uri.fromFile(this) }
    }

    private fun validateAndSubmitForm() {
        val name = nameInput.text.toString().trim()
        val className = classInput.text.toString().trim()
        val regNo = rollInput.text.toString().trim()
        val reason = reasonInput.text.toString().trim()
        val teacher = teacherSpinner.text.toString().trim()
        val startDate = startDateInput.text.toString().trim()
        val endDate = endDateInput.text.toString().trim()

        when {
            name.isEmpty() -> showToast("Name is required.")
            className.isEmpty() -> showToast("Class is required.")
            regNo.length != 7 -> showToast("Roll number must be 7 digits.")
            reason.isEmpty() -> showToast("Reason for leave is required.")
            teacher.isEmpty() -> showToast("Please select a teacher.")
            startDate.isEmpty() -> showToast("Start date is required.")
            endDate.isEmpty() -> showToast("End date is required.")
            certificateUri == null -> showToast("Medical certificate is required.")
            startDate > endDate -> showToast("End date cannot be before start date.")
            else -> uploadFormData(name, className, regNo, reason, teacher, startDate, endDate)
        }
    }

    private fun uploadFormData(
        name: String,
        className: String,
        regNo: String,
        reason: String,
        teacher: String,
        startDate: String,
        endDate: String
    ) {
        // Firestore Submission Code
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
