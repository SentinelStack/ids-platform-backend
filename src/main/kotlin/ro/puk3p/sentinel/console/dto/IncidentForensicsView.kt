package ro.puk3p.sentinel.console.dto

data class ForensicStat(
    val label: String,
    val value: String,
)

data class ForensicPacket(
    val timestamp: String,
    val protocol: String,
    val source: String,
    val destination: String,
    val port: Int,
    val size: String,
    val flags: String,
    val suspicious: Boolean,
)

data class IncidentForensicsView(
    val incId: String,
    val title: String,
    val source: String,
    val target: String,
    val severity: String,
    val category: String,
    val contained: Boolean,
    val stats: List<ForensicStat>,
    val protocols: List<ProtocolShare>,
    val topPorts: List<TopPort>,
    val packets: List<ForensicPacket>,
    val empty: Boolean,
)
