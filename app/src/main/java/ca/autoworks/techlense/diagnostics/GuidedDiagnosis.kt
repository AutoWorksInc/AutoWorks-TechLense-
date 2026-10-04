package ca.autoworks.techlense.diagnostics

enum class StepStatus { PENDING, ACTIVE, PASSED, FAILED, SKIPPED }

data class DiagnosticStep(
    val id: String,
    val instruction: String,
    val expectedResult: String? = null,
    val status: StepStatus = StepStatus.PENDING,
    val technicianResult: String? = null
)

data class GuidedDiagnosis(
    val title: String,
    val sourceLabel: String,
    val isVerifiedRepairInfo: Boolean,
    val steps: List<DiagnosticStep>
)

object DemoGuidedDiagnosis {
    fun misfireLean(): GuidedDiagnosis = GuidedDiagnosis(
        title = "Misfire / lean-condition guided diagnosis",
        sourceLabel = "SIMULATED TRAINING DATA",
        isVerifiedRepairInfo = false,
        steps = listOf(
            DiagnosticStep("1", "Review DTCs and freeze-frame data. Record engine speed, load and fuel trims.", "Fault conditions documented."),
            DiagnosticStep("2", "Inspect for obvious disconnected, split or damaged intake/vacuum hoses.", "No unmetered-air path found."),
            DiagnosticStep("3", "Compare relevant live-data values at idle and elevated RPM.", "Use the pattern to choose the next directed test."),
            DiagnosticStep("4", "Perform the appropriate directed component/circuit test before replacing parts.", "Root cause verified by test.")
        )
    )
}
