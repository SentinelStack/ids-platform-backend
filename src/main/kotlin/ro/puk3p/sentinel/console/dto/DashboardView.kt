package ro.puk3p.sentinel.console.dto

data class DashboardKpis(
    val totalAlerts: String,
    val uniqueSources: String,
    val systemHealth: String,
)

data class LiveAlert(
    val severity: String,
    val level: String,
    val timestamp: String,
    val title: String,
    val source: String,
    val destination: String,
)

data class VolumeBar(
    val height: Int,
    val hot: Boolean,
)

data class Origin(
    val name: String,
    val pct: Double,
    val rank: Int,
)

data class ThreatArc(
    val lat: Double,
    val lng: Double,
    val sourceIp: String,
    val country: String,
    val level: String,
)

data class DashboardView(
    val kpis: DashboardKpis,
    val liveAlerts: List<LiveAlert>,
    val volumeDelta: String,
    val volume: List<VolumeBar>,
    val origins: List<Origin>,
    val arcs: List<ThreatArc>,
    val deviceLat: Double,
    val deviceLng: Double,
)
