package ca.autoworks.techlense.integrations

interface TechLenseConnector {
    val providerName: String
    val status: ConnectorStatus
}

enum class ConnectorStatus { NOT_CONFIGURED, READY, CONNECTED, ERROR }
