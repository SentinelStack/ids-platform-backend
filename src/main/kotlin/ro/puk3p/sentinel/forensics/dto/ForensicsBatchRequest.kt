package ro.puk3p.sentinel.forensics.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank

data class ForensicsBatchRequest(
    @field:NotBlank(message = "deviceId is required")
    val deviceId: String,
    @field:Valid
    val packets: List<ForensicsPacketItem> = emptyList(),
)
