package ro.puk3p.sentinel.console.dto

data class ClientsSummary(
    val total: Int,
    val online: Int,
)

data class ClientRow(
    val name: String?,
    val ip: String,
    val mac: String,
    val online: Boolean,
    val lastSeen: String,
    val queryCount: Int,
    val topDestination: String,
)

data class ClientsView(
    val summary: ClientsSummary,
    val clients: List<ClientRow>,
)
