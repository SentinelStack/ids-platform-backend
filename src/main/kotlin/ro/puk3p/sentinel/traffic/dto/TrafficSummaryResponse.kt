package ro.puk3p.sentinel.traffic.dto

data class TrafficSummaryResponse(
    val totalPackets: Long,
    val tcpPackets: Long,
    val udpPackets: Long,
    val totalBytes: Long,
    val tcpBytes: Long,
    val udpBytes: Long,
    val tcpPercentage: Double,
    val udpPercentage: Double,
)
