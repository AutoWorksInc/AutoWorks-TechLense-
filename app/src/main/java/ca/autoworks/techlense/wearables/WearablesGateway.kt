package ca.autoworks.techlense.wearables

/** Boundary around Meta Wearables DAT so real and mock hardware can share the same UI. */
interface WearablesGateway {
    val connectionLabel: String
    suspend fun connect(): Result<Unit>
    suspend fun capturePointOfViewPhoto(): Result<ByteArray>
}

class PrototypeWearablesGateway : WearablesGateway {
    override val connectionLabel: String = "Meta integration ready"
    override suspend fun connect() = Result.success(Unit)
    override suspend fun capturePointOfViewPhoto(): Result<ByteArray> =
        Result.failure(IllegalStateException("Camera stream adapter is the next hardware milestone."))
}
