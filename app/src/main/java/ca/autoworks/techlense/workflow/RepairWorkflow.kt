package ca.autoworks.techlense.workflow

enum class RepairStage {
    RO_LOADED,
    PRE_SCAN,
    RESEARCH,
    GUIDED_DIAGNOSIS,
    FAILURE_DOCUMENTED,
    ESTIMATE_PREPARED,
    AWAITING_CUSTOMER,
    APPROVED,
    PARTS_ORDERED,
    GUIDED_REPAIR,
    POST_SCAN,
    CONFIRMED_FIX,
    COMPLETE
}

data class RepairWorkflowState(
    val repairOrderId: String,
    val stage: RepairStage,
    val technicianFindings: List<String> = emptyList(),
    val mediaEvidenceIds: List<String> = emptyList()
)
