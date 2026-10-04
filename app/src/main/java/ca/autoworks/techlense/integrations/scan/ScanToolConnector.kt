package ca.autoworks.techlense.integrations.scan

import ca.autoworks.techlense.integrations.TechLenseConnector

data class DiagnosticTroubleCode(val code: String, val description: String?)
data class ScanSnapshot(
    val vin: String?,
    val dtcs: List<DiagnosticTroubleCode>,
    val pidValues: Map<String, String>,
    val capturedAtEpochMs: Long
)

interface ScanToolConnector : TechLenseConnector {
    suspend fun scanVehicle(): Result<ScanSnapshot>
    suspend fun captureLiveData(pids: Set<String>): Result<ScanSnapshot>
}
