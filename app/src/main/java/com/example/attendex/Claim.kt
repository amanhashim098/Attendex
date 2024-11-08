package com.example.attendex

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

sealed class Claim {
    abstract val id: String
    abstract val name: String
    abstract val className: String
    abstract val teacher: String
    abstract val timestamp: Timestamp
    abstract val status: String
    abstract val claimType: ClaimType

    fun getFormattedTimestamp(): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy 'at' HH:mm:ss z", Locale.getDefault())
        return sdf.format(timestamp.toDate())
    }
}

enum class ClaimType {
    CO_CURRICULAR,
    MEDICAL,
    VISA
}

data class CoCurricularClaim(
    override val id: String,
    override val name: String,
    override val className: String,
    override val teacher: String,
    override val timestamp: Timestamp,
    override val status: String,
    val activityType: String,
    val date: String,
    val event: String,
    val imagesUploaded: Boolean,
    val periodsMissed: List<String>,
    val regNo: String,


    val photoUrl: String,

) : Claim() {
    override val claimType: ClaimType = ClaimType.CO_CURRICULAR
}

data class MedicalClaim(
    override val id: String,
    override val name: String,
    override val className: String,
    override val teacher: String,
    override val timestamp: Timestamp,
    override val status: String,
    val startDate: String,
    val endDate: String,
    val reason: String,
    val regNo: String
) : Claim() {
    override val claimType: ClaimType = ClaimType.MEDICAL
}

data class VisaClaim(
    override val id: String,
    override val name: String,
    override val className: String,
    override val teacher: String,
    override val timestamp: Timestamp,
    override val status: String,
    val startDate: String,
    val endDate: String,
    val flightTicketPath: String,
    val otherDocumentPath: String,
    val reason: String,
    val rollNumber: String
) : Claim() {
    override val claimType: ClaimType = ClaimType.VISA
}

fun createClaimFromFirestore(
    id: String,
    data: Map<String, Any>,
    type: ClaimType
): Claim {
    return when (type) {
        ClaimType.CO_CURRICULAR -> CoCurricularClaim(
            id = id,
            name = data["name"] as? String ?: "",
            className = data["class"] as? String ?: "",
            teacher = data["teacher"] as? String ?: "",
            timestamp = data["timestamp"] as? Timestamp ?: Timestamp.now(),
            status = data["status"] as? String ?: "Pending",
            activityType = data["activityType"] as? String ?: "",
            date = data["date"] as? String ?: "",
            event = data["event"] as? String ?: "",
            imagesUploaded = data["imagesUploaded"] as? Boolean ?: false,
            periodsMissed = (data["periodsMissed"] as? List<*>)?.mapNotNull { it as? String } ?: listOf(),
            regNo = data["regNo"] as? String ?: "",

            photoUrl = data["photoUrl"] as? String ?: "",

        )

        ClaimType.MEDICAL -> MedicalClaim(
            id = id,
            name = data["name"] as? String ?: "",
            className = data["class"] as? String ?: "",
            teacher = data["teacher"] as? String ?: "",
            timestamp = data["timestamp"] as? Timestamp ?: Timestamp.now(),
            status = data["status"] as? String ?: "Pending",
            startDate = data["startDate"] as? String ?: "",
            endDate = data["endDate"] as? String ?: "",
            reason = data["reason"] as? String ?: "",
            regNo = data["regNo"] as? String ?: ""
        )

        ClaimType.VISA -> VisaClaim(
            id = id,
            name = data["name"] as? String ?: "",
            className = data["class"] as? String ?: "",
            teacher = data["teacher"] as? String ?: "",
            timestamp = data["timestamp"] as? Timestamp ?: Timestamp.now(),
            status = data["status"] as? String ?: "Pending",
            startDate = data["startDate"] as? String ?: "",
            endDate = data["endDate"] as? String ?: "",
            flightTicketPath = data["flightTicketPath"] as? String ?: "",
            otherDocumentPath = data["otherDocumentPath"] as? String ?: "",
            reason = data["reason"] as? String ?: "",
            rollNumber = data["rollNumber"] as? String ?: ""
        )
    }
}

object ClaimStatus {
    const val PENDING = "Pending"
    const val APPROVED = "Approved"
    const val DENIED = "Denied"
}

object ClaimColors {
    val APPROVED = R.color.colorApproved
    val DENIED = R.color.colorDenied
    val PENDING = R.color.colorPending
}