package ro.puk3p.sentinel.console.dto

data class IncidentKpis(
    val open: Int,
    val openDelta: String,
    val critical: Int,
    val investigating: Int,
    val resolved: Int,
    val uniqueSources: Int,
)

data class TimelineBar(
    val height: Int,
    val level: String,
)

data class IncidentRow(
    val id: String,
    val incId: String,
    val timestamp: String,
    val severity: String,
    val title: String,
    val source: String,
    val target: String,
    val category: String,
    val status: String,
    val statusIcon: String,
    val assignee: String,
    val acknowledged: Boolean,
    val confidence: Int,
    val anomalyScore: String,
    val packetRate: String,
    val volume: String,
    val targetPort: Int,
    val protocol: String,
)

data class SeverityDistribution(
    val critical: Int,
    val high: Int,
    val medium: Int,
    val low: Int,
    val total: Int,
)

data class AffectedAsset(
    val name: String,
    val ip: String,
    val icon: String,
    val level: String,
)

data class IncidentsView(
    val kpis: IncidentKpis,
    val timeline: List<TimelineBar>,
    val queue: List<IncidentRow>,
    val severity: SeverityDistribution,
    val assets: List<AffectedAsset>,
)
