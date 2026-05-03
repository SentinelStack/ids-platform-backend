package ro.puk3p.sentinel.traffic.dto

import java.time.Instant

data class TrafficStatsResponse(
    val deviceId: String,
    val timestamp: Instant,
    val totalPackets: Long,
    val tcpPackets: Long,
    val udpPackets: Long,
    val totalBytes: Long,
    val tcpBytes: Long,
    val udpBytes: Long,
    val windowSeconds: Int,
    val createdAt: Instant,
)
