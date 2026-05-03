package ro.puk3p.sentinel.alert.dto

import ro.puk3p.sentinel.alert.model.AlertType
import ro.puk3p.sentinel.alert.model.Protocol
import ro.puk3p.sentinel.alert.model.Severity
import java.time.Instant

data class AlertResponse(
    val alertId: String,
    val deviceId: String,
    val timestamp: Instant,
    val type: AlertType,
    val severity: Severity,
    val protocol: Protocol,
    val sourceIp: String,
    val destinationIp: String,
    val sourcePort: Int,
    val destinationPort: Int,
    val packetCount: Long,
    val bytesCount: Long,
    val windowSeconds: Int,
    val description: String?,
    val acknowledged: Boolean,
    val createdAt: Instant,
)
