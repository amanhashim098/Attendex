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

class VisaActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var classInput: EditText
    private lateinit var rollInput: EditText
    private lateinit var reasonInput: EditText
    private lateinit var teacherSpinner: AutoCompleteTextView
    private lateinit var startDateInput: EditText
    private lateinit var endDateInput: EditText
    private lateinit var attachFlightTicketButton: MaterialButton
    private lateinit var previewFlightTicketButton: MaterialButton
    private lateinit var otherDocumentsButton: MaterialButton
    private lateinit var previewOtherDocumentsButton: MaterialButton
    private lateinit var submitButton: MaterialButton

    private lateinit var currentPhotoPath: String
    private var isCapturingFlightTicket = true
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_CAMERA_PERMISSION = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visa)

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
        startDateInput = findViewById(R.id.start_date_input)
        endDateInput = findViewById(R.id.end_date_input)

        teacherSpinner = findViewById(R.id.teacher_spinner)
        val teachers = arrayOf("Fabiola Pohrmen", "Resmi K R", "Vineetha K R", "Arokia Paul")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, teachers)
        teacherSpinner.setAdapter(adapter)
    }

    private fun setupDatePickers() {
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

    private fun setupButtons() {
        attachFlightTicketButton = findViewById(R.id.attach_flight_ticket_button)
        previewFlightTicketButton = findViewById(R.id.preview_flight_ticket_button)
        otherDocumentsButton = findViewById(R.id.other_documents_button)
        previewOtherDocumentsButton = findViewById(R.id.preview_other_documents_button)
        submitButton = findViewById(R.id.submit_button)

        attachFlightTicketButton.setOnClickListener {
            isCapturingFlightTicket = true
            checkCameraPermissionAndCapture()
        }

        otherDocumentsButton.setOnClickListener {
            isCapturingFlightTicket = false
            checkCameraPermissionAndCapture()
        }

        previewFlightTicketButton.setOnClickListener { previewImage(isCapturingFlightTicket) }
        previewOtherDocumentsButton.setOnClickListener { previewImage(!isCapturingFlightTicket) }

        submitButton.setOnClickListener {
            handleSubmit()
        }

        previewFlightTicketButton.isEnabled = false
        previewOtherDocumentsButton.isEnabled = false
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            if (isCapturingFlightTicket) {
                attachFlightTicketButton.text = "Flight Ticket Attached"
                previewFlightTicketButton.isEnabled = true
            } else {
                otherDocumentsButton.text = "Document Attached"
                previewOtherDocumentsButton.isEnabled = true
            }
        }
    }

    private fun previewImage(isFlightTicket: Boolean) {
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

    private fun handleSubmit() {
        val name = nameInput.text.toString()
        val className = classInput.text.toString()
        val rollNumber = rollInput.text.toString()
        val reason = reasonInput.text.toString()
        val teacher = teacherSpinner.text.toString()
        val startDate = startDateInput.text.toString()
        val endDate = endDateInput.text.toString()

        if (name.isEmpty() || className.isEmpty() || rollNumber.isEmpty() || reason.isEmpty() || teacher.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        uploadImagesAndSubmitClaim(name, className, rollNumber, reason, teacher, startDate, endDate)
    }

    private fun uploadImagesAndSubmitClaim(name: String, className: String, rollNumber: String, reason: String, teacher: String, startDate: String, endDate: String) {
        val flightTicketFile = if (isCapturingFlightTicket) File(currentPhotoPath) else null
        val otherDocumentFile = if (!isCapturingFlightTicket) File(currentPhotoPath) else null

        val uploadTasks = mutableListOf<Pair<String, File?>>()
        if (flightTicketFile != null) {
            uploadTasks.add("flightTicket" to flightTicketFile)
        }
        if (otherDocumentFile != null) {
            uploadTasks.add("otherDocument" to otherDocumentFile)
        }

        val uploadedUrls = mutableMapOf<String, String>()

        fun uploadNextImage() {
            if (uploadTasks.isEmpty()) {
                // All images uploaded, now submit the claim
                submitClaim(name, className, rollNumber, reason, teacher, startDate, endDate, uploadedUrls)
                return
            }

            val (type, file) = uploadTasks.removeAt(0)
            val storageRef = storage.reference.child("visaClaims/${file?.name}")

            storageRef.putFile(Uri.fromFile(file))
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                        uploadedUrls[type] = uri.toString()
                        uploadNextImage()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to upload $type: ${e.message}", Toast.LENGTH_SHORT).show()
                    uploadNextImage()
                }
        }

        uploadNextImage()
    }

    private fun submitClaim(name: String, className: String, rollNumber: String, reason: String, teacher: String, startDate: String, endDate: String, uploadedUrls: Map<String, String>) {
        val visaClaim = hashMapOf(
            "name" to name,
            "class" to className,
            "rollNumber" to rollNumber,
            "reason" to reason,
            "teacher" to teacher,
            "startDate" to startDate,
            "endDate" to endDate,
            "flightTicketUrl" to (uploadedUrls["flightTicket"] ?: ""),
            "otherDocumentUrl" to (uploadedUrls["otherDocument"] ?: ""),
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        db.collection("visaClaims")
            .add(visaClaim)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Visa claim submitted successfully", Toast.LENGTH_SHORT).show()
                clearForm()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error submitting visa claim: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearForm() {
        nameInput.text.clear()
        classInput.text.clear()
        rollInput.text.clear()
        reasonInput.text.clear()
        teacherSpinner.text.clear()
        startDateInput.text.clear()
        endDateInput.text.clear()
        attachFlightTicketButton.text = "Attach Flight Ticket"
        otherDocumentsButton.text = "Other Documents"
        previewFlightTicketButton.isEnabled = false
        previewOtherDocumentsButton.isEnabled = false
        currentPhotoPath = ""
    }
}