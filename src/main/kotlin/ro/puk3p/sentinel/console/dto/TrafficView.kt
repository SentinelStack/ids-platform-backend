package ro.puk3p.sentinel.console.dto

data class FlowPoint(
    val tcp: Int,
    val udp: Int,
)

data class ProtocolShare(
    val name: String,
    val pct: Double,
)

data class TopPort(
    val label: String,
    val packets: String,
    val danger: Boolean,
)

data class TopSource(
    val ip: String,
    val scope: String,
)

data class PacketRow(
    val timestamp: String,
    val protocol: String,
    val source: String,
    val destination: String,
    val port: Int,
    val size: String,
    val suspicious: Boolean,
)

data class DistBar(
    val height: Int,
    val hot: Boolean,
)

data class TrafficView(
    val totalPackets: String,
    val tcpPct: Int,
    val udpPct: Int,
    val suspiciousPct: String,
    val protocols: List<ProtocolShare>,
    val flow: List<FlowPoint>,
    val topPorts: List<TopPort>,
    val topSources: List<TopSource>,
    val packets: List<PacketRow>,
    val distribution: List<DistBar>,
)
