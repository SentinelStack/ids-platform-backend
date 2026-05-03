package ro.puk3p.sentinel.alert.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero
import ro.puk3p.sentinel.alert.model.AlertType
import ro.puk3p.sentinel.alert.model.Protocol
import ro.puk3p.sentinel.alert.model.Severity
import java.time.Instant

data class AlertCreateRequest(
    @field:NotBlank(message = "deviceId is required")
    val deviceId: String,
    @field:NotNull(message = "timestamp is required")
    val timestamp: Instant,
    @field:NotNull(message = "type is required")
    val type: AlertType,
    @field:NotNull(message = "severity is required")
    val severity: Severity,
    @field:NotNull(message = "protocol is required")
    val protocol: Protocol,
    @field:NotBlank(message = "sourceIp is required")
    val sourceIp: String,
    @field:NotBlank(message = "destinationIp is required")
    val destinationIp: String,
    @field:Min(value = 0, message = "sourcePort must be >= 0")
    val sourcePort: Int,
    @field:Min(value = 0, message = "destinationPort must be >= 0")
    val destinationPort: Int,
    @field:PositiveOrZero(message = "packetCount must be >= 0")
    val packetCount: Long,
    @field:PositiveOrZero(message = "bytesCount must be >= 0")
    val bytesCount: Long,
    @field:PositiveOrZero(message = "windowSeconds must be >= 0")
    val windowSeconds: Int,
    val description: String? = null,
)
