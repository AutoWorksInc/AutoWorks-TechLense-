package ca.autoworks.techlense.integrations.parts

import ca.autoworks.techlense.integrations.TechLenseConnector

data class PartQuote(
    val supplier: String,
    val partNumber: String,
    val description: String,
    val shopCost: Money,
    val availableQuantity: Int?,
    val eta: String?
)

data class Money(val amountMinor: Long, val currency: String = "CAD")

interface PartsConnector : TechLenseConnector {
    suspend fun quote(vin: String, search: String): Result<List<PartQuote>>
    suspend fun submitOrder(quote: PartQuote, confirmedByUser: Boolean): Result<String>
}
