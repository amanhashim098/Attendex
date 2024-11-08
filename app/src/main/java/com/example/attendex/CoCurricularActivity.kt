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
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CoCurricularActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var classInput: EditText
    private lateinit var rollInput: EditText
    private lateinit var eventInput: EditText
    private lateinit var teacherSpinner: AutoCompleteTextView
    private lateinit var dateInput: EditText
    private lateinit var attachCertificateButton: MaterialButton
    private lateinit var previewCertificateButton: MaterialButton
    private lateinit var otherDocumentsButton: MaterialButton
    private lateinit var previewOtherDocumentsButton: MaterialButton
    private lateinit var submitButton: MaterialButton
    private lateinit var coCurricularRadio: MaterialRadioButton
    private lateinit var extraCurricularRadio: MaterialRadioButton
    private lateinit var deptActivitiesRadio: MaterialRadioButton
    private lateinit var periodCheckboxes: List<MaterialCheckBox>

    private lateinit var currentPhotoPath: String
    private var isCapturingCertificate = true
    private var certificateUri: Uri? = null
    private var otherDocumentUri: Uri? = null

    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_CAMERA_PERMISSION = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cocurricular)

        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        setupViews()
        setupDatePicker()
        setupButtons()
        setupPeriodCheckboxes()
    }

    private fun setupViews() {
        nameInput = findViewById(R.id.name_input)
        classInput = findViewById(R.id.class_input)
        rollInput = findViewById(R.id.roll_input)
        eventInput = findViewById(R.id.event_input)
        teacherSpinner = findViewById(R.id.teacher_spinner)
        dateInput = findViewById(R.id.date_input)
        coCurricularRadio = findViewById(R.id.co_curricular_radio)
        extraCurricularRadio = findViewById(R.id.extra_curricular_radio)
        deptActivitiesRadio = findViewById(R.id.dept_activities_radio)

        val teachers = arrayOf("Fabiola Pohrmen", "Resmi K R", "Vineetha K R", "Arokia Paul")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, teachers)
        teacherSpinner.setAdapter(adapter)
    }

    private fun setupDatePicker() {
        dateInput.setOnClickListener {
            showDatePickerDialog { date ->
                dateInput.setText(date)
            }
        }
    }

    private fun setupButtons() {
        attachCertificateButton = findViewById(R.id.attach_certificate_button)
        previewCertificateButton = findViewById(R.id.preview_certificate_button)
        otherDocumentsButton = findViewById(R.id.other_documents_button)
        previewOtherDocumentsButton = findViewById(R.id.preview_other_documents_button)
        submitButton = findViewById(R.id.submit_button)

        attachCertificateButton.setOnClickListener {
            isCapturingCertificate = true
            checkCameraPermissionAndCapture()
        }

        otherDocumentsButton.setOnClickListener {
            isCapturingCertificate = false
            checkCameraPermissionAndCapture()
        }

        previewCertificateButton.setOnClickListener { previewImage(isCapturingCertificate) }
        previewOtherDocumentsButton.setOnClickListener { previewImage(!isCapturingCertificate) }

        submitButton.setOnClickListener {
            if (validateInputs()) {
                uploadFormData()
            }
        }

        previewCertificateButton.isEnabled = false
        previewOtherDocumentsButton.isEnabled = false
    }

    private fun setupPeriodCheckboxes() {
        periodCheckboxes = listOf(
            findViewById(R.id.p1),
            findViewById(R.id.p2),
            findViewById(R.id.p3),
            findViewById(R.id.p4),
            findViewById(R.id.p5),
            findViewById(R.id.p6)
        )
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        } else {
            dispatchTakePictureIntent()
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    Toast.makeText(this, "Error occurred while creating the File", Toast.LENGTH_SHORT).show()
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.attendex.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    dispatchTakePictureIntent()
                } else {
                    Toast.makeText(this, "Camera permission is required to take pictures", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun previewImage(isCertificate: Boolean) {
        val photoURI = FileProvider.getUriForFile(
            this,
            "com.example.attendex.fileprovider",
            File(currentPhotoPath)
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(photoURI, "image/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(intent)
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Name validation
        if (nameInput.text.toString().trim().isEmpty() || !nameInput.text.toString().matches(Regex("^[A-Za-z ]{2,}$"))) {
            nameInput.error = "Please enter a valid name (at least 2 letters, alphabets only)"
            isValid = false
        }

        // Class validation
        if (classInput.text.toString().trim().isEmpty()) {
            classInput.error = "Class cannot be empty"
            isValid = false
        }

        // Roll number validation (7 digits only)
        if (rollInput.text.toString().trim().isEmpty() || !rollInput.text.toString().matches(Regex("^\\d{7}$"))) {
            rollInput.error = "Enter a valid 7-digit roll number"
            isValid = false
        }

        // Event validation
        if (eventInput.text.toString().trim().isEmpty() || eventInput.text.toString().length < 3) {
            eventInput.error = "Event name must be at least 3 characters"
            isValid = false
        }

        // Teacher selection validation
        if (teacherSpinner.text.toString().trim().isEmpty() || !listOf(
                "Fabiola Pohrmen", "Resmi K R", "Vineetha K R", "Arokia Paul"
            ).contains(teacherSpinner.text.toString())) {
            teacherSpinner.error = "Please select a valid teacher"
            isValid = false
        }

        // Date validation
        val dateText = dateInput.text.toString()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        dateFormat.isLenient = false
        try {
            val date = dateFormat.parse(dateText)
            if (date.after(Date())) {
                dateInput.error = "Date cannot be in the future"
                isValid = false
            }
        } catch (e: Exception) {
            dateInput.error = "Please enter a valid date (DD/MM/YYYY)"
            isValid = false
        }

        // Periods missed validation (Optional but at least one if applicable)
        if (periodCheckboxes.none { it.isChecked }) {
            Toast.makeText(this, "Select at least one period missed", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        // Attachments validation (Mandatory)
        if (certificateUri == null && otherDocumentUri == null) {
            Toast.makeText(this, "Attach at least one document", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun uploadFormData() {
        val userData = hashMapOf(
            "name" to nameInput.text.toString(),
            "class" to classInput.text.toString(),
            "roll_no" to rollInput.text.toString(),
            "event" to eventInput.text.toString(),
            "teacher" to teacherSpinner.text.toString(),
            "date" to dateInput.text.toString(),
        )

        db.collection("users")
            .add(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Successfully Submitted", Toast.LENGTH_SHORT).show()
                resetInputs()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to submit data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun resetInputs() {
        nameInput.text.clear()
        classInput.text.clear()
        rollInput.text.clear()
        eventInput.text.clear()
        teacherSpinner.text.clear()
        dateInput.text.clear()
        periodCheckboxes.forEach { it.isChecked = false }
        certificateUri = null
        otherDocumentUri = null
    }

}
