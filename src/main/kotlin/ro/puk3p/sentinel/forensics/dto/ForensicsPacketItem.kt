package ro.puk3p.sentinel.forensics.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import ro.puk3p.sentinel.alert.model.Protocol
import java.time.Instant

data class ForensicsPacketItem(
    @field:NotNull(message = "timestamp is required")
    val timestamp: Instant,
    @field:NotNull(message = "protocol is required")
    val protocol: Protocol,
    @field:NotBlank(message = "sourceIp is required")
    val sourceIp: String,
    @field:NotBlank(message = "destinationIp is required")
    val destinationIp: String,
    @field:Min(value = 0, message = "sourcePort must be >= 0")
    @field:Max(value = 65535, message = "sourcePort must be <= 65535")
    val sourcePort: Int,
    @field:Min(value = 0, message = "destinationPort must be >= 0")
    @field:Max(value = 65535, message = "destinationPort must be <= 65535")
    val destinationPort: Int,
    @field:Min(value = 0, message = "packetSize must be >= 0")
    val packetSize: Int,
)
