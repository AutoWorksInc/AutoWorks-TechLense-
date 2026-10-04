package ca.autoworks.techlense.integrations.repairinfo

import ca.autoworks.techlense.integrations.TechLenseConnector

enum class RepairInfoType { TSB, DIAGNOSTIC_PROCEDURE, REPAIR_PROCEDURE, SPECIFICATION, WIRING }

data class RepairInfoRequest(
    val vin: String,
    val dtcs: List<String> = emptyList(),
    val query: String,
    val types: Set<RepairInfoType> = RepairInfoType.entries.toSet()
)

data class VerifiedRepairInfo(
    val provider: String,
    val title: String,
    val content: String,
    val sourceReference: String?
)

interface RepairInformationConnector : TechLenseConnector {
    suspend fun search(request: RepairInfoRequest): Result<List<VerifiedRepairInfo>>
}
