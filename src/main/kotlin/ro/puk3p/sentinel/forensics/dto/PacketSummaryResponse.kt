package ro.puk3p.sentinel.forensics.dto

import ro.puk3p.sentinel.alert.model.Protocol
import java.time.Instant

data class PacketSummaryResponse(
    val deviceId: String,
    val alertId: String?,
    val timestamp: Instant,
    val protocol: Protocol,
    val sourceIp: String,
    val destinationIp: String,
    val sourcePort: Int,
    val destinationPort: Int,
    val packetSize: Long,
    val tcpFlags: String?,
    val createdAt: Instant,
)
