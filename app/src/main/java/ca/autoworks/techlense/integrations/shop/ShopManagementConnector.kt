package ca.autoworks.techlense.integrations.shop

import ca.autoworks.techlense.integrations.TechLenseConnector

data class RepairOrderRef(
    val id: String,
    val vehicleVin: String,
    val customerConcern: String
)

data class EstimateDraft(
    val repairOrderId: String,
    val summary: String,
    val technicianFindings: List<String>
)

interface ShopManagementConnector : TechLenseConnector {
    suspend fun activeRepairOrder(): Result<RepairOrderRef?>
    suspend fun prepareEstimate(draft: EstimateDraft): Result<String>
}
