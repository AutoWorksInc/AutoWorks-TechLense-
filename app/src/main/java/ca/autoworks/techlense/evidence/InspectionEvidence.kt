package ca.autoworks.techlense.evidence

enum class EvidenceMediaType { PHOTO, VIDEO }

data class InspectionEvidence(
    val id: String,
    val type: EvidenceMediaType,
    val note: String,
    val capturedAtEpochMs: Long,
    val uri: String? = null,
    val simulated: Boolean = true
)
