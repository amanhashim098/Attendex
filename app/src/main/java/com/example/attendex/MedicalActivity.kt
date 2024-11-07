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
    private lateinit var previewCertificateButton: MaterialButton
    private lateinit var otherDocumentsButton: MaterialButton
    private lateinit var previewOtherDocumentsButton: MaterialButton
    private lateinit var submitButton: MaterialButton

    private lateinit var currentPhotoPath: String
    private var isCapturingMedicalCertificate = true
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
        attachCertificateButton = findViewById(R.id.attach_certificate_button)
        previewCertificateButton = findViewById(R.id.preview_certificate_button)
        otherDocumentsButton = findViewById(R.id.other_documents_button)
        previewOtherDocumentsButton = findViewById(R.id.preview_other_documents_button)
        submitButton = findViewById(R.id.submit_button)

        attachCertificateButton.setOnClickListener {
            isCapturingMedicalCertificate = true
            checkCameraPermissionAndCapture()
        }

        otherDocumentsButton.setOnClickListener {
            isCapturingMedicalCertificate = false
            checkCameraPermissionAndCapture()
        }

        previewCertificateButton.setOnClickListener { previewImage(isCapturingMedicalCertificate) }
        previewOtherDocumentsButton.setOnClickListener { previewImage(!isCapturingMedicalCertificate) }

        submitButton.setOnClickListener { uploadFormData() }

        previewCertificateButton.isEnabled = false
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


    private fun previewImage(isMedicalCertificate: Boolean) {
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

    private fun uploadFormData() {
        val name = nameInput.text.toString()
        val className = classInput.text.toString()
        val regNo = rollInput.text.toString()
        val reason = reasonInput.text.toString()
        val teacher = teacherSpinner.text.toString()
        val startDate = startDateInput.text.toString()
        val endDate = endDateInput.text.toString()

        if (name.isEmpty() || className.isEmpty() || regNo.isEmpty() || reason.isEmpty() || teacher.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val formData = hashMapOf(
            "name" to name,
            "class" to className,
            "regNo" to regNo,
            "reason" to reason,
            "teacher" to teacher,
            "startDate" to startDate,
            "endDate" to endDate,
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        db.collection("medicalClaims").document(regNo)
            .set(formData)
            .addOnSuccessListener {
                uploadImages(regNo)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error submitting form: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImages(regNo: String) {
        val certificateRef = storage.reference.child("medicalClaims/${regNo}_mc.jpg")
        val otherDocumentRef = storage.reference.child("medicalClaims/${regNo}_od.jpg")

        var uploadedCount = 0
        var totalUploads = 0

        certificateUri?.let {
            totalUploads++
            certificateRef.putFile(it)
                .addOnSuccessListener {
                    uploadedCount++
                    checkUploadCompletion(regNo, uploadedCount, totalUploads)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error uploading certificate: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        otherDocumentUri?.let {
            totalUploads++
            otherDocumentRef.putFile(it)
                .addOnSuccessListener {
                    uploadedCount++
                    checkUploadCompletion(regNo, uploadedCount, totalUploads)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error uploading other document: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        if (totalUploads == 0) {
            Toast.makeText(this, "Form submitted successfully", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun checkUploadCompletion(regNo: String, uploadedCount: Int, totalUploads: Int) {
        if (uploadedCount == totalUploads) {
            db.collection("medicalClaims").document(regNo)
                .update("imagesUploaded", true)
                .addOnSuccessListener {
                    Toast.makeText(this, "Form and images submitted successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error updating image status: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val photoUri = FileProvider.getUriForFile(
                this,
                "com.example.attendex.fileprovider",
                File(currentPhotoPath)
            )
            if (isCapturingMedicalCertificate) {
                certificateUri = photoUri
                attachCertificateButton.text = "Certificate Attached"
                previewCertificateButton.isEnabled = true
            } else {
                otherDocumentUri = photoUri
                otherDocumentsButton.text = "Document Attached"
                previewOtherDocumentsButton.isEnabled = true
            }
        }
    }

}