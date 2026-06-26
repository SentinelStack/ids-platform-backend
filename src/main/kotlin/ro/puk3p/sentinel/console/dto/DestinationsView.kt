package ro.puk3p.sentinel.console.dto

data class DomainHit(
    val domain: String,
    val count: Int,
    val category: String,
    val tracker: Boolean,
)

data class DestinationRow(
    val timestamp: String,
    val clientIp: String,
    val domain: String,
    val count: Int,
    val category: String,
    val tracker: Boolean,
)

data class TopDomain(
    val domain: String,
    val count: Int,
    val category: String,
    val tracker: Boolean,
)

data class ClientDomains(
    val clientIp: String,
    val name: String?,
    val queryCount: Int,
    val topDomain: String,
    val domains: List<DomainHit>,
)

data class CategoryCount(
    val category: String,
    val count: Int,
)

data class Summary(
    val totalQueries: Int,
    val uniqueDomains: Int,
    val activeClients: Int,
    val trackerQueries: Int,
)

data class DestinationsView(
    val summary: Summary,
    val categories: List<CategoryCount>,
    val topDomains: List<TopDomain>,
    val byClient: List<ClientDomains>,
    val recent: List<DestinationRow>,
)
