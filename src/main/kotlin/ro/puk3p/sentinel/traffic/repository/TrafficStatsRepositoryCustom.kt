package ro.puk3p.sentinel.traffic.repository

data class TrafficTotals(
    val totalPackets: Long,
    val tcpPackets: Long,
    val udpPackets: Long,
    val totalBytes: Long,
    val tcpBytes: Long,
    val udpBytes: Long,
)

interface TrafficStatsRepositoryCustom {
    fun summarize(): TrafficTotals
}
