package ca.autoworks.techlense.model

data class VehicleSession(
    val vin: String = "",
    val year: String = "",
    val make: String = "",
    val model: String = "",
    val engine: String = "",
    val mileageKm: String = "",
    val complaint: String = "",
    val dtcs: String = ""
)

enum class EvidenceSource(val label: String) {
    VERIFIED_REPAIR_INFO("Verified repair information"),
    AI_SUGGESTION("AI diagnostic suggestion"),
    TECHNICIAN_FINDING("Technician finding")
}
