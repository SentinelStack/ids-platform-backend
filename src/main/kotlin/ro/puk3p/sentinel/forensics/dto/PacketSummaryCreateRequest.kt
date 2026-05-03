package ro.puk3p.sentinel.forensics.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import ro.puk3p.sentinel.alert.model.Protocol
import java.time.Instant

data class PacketSummaryCreateRequest(
    @field:NotBlank(message = "deviceId is required")
    val deviceId: String,
    val alertId: String? = null,
    @field:NotNull(message = "timestamp is required")
    val timestamp: Instant,
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
    @field:Min(value = 0, message = "packetSize must be >= 0")
    val packetSize: Long,
    val tcpFlags: String? = null,
)
