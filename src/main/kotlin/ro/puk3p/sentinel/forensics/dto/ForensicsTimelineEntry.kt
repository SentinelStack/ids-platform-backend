package ro.puk3p.sentinel.forensics.dto

import ro.puk3p.sentinel.alert.model.Protocol
import java.time.Instant

data class ForensicsTimelineEntry(
    val timestamp: Instant,
    val deviceId: String,
    val alertId: String?,
    val protocol: Protocol,
    val sourceIp: String,
    val destinationIp: String,
    val packetSize: Long,
)
