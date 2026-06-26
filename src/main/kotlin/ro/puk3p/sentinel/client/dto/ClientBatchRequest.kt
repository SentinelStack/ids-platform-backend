package ro.puk3p.sentinel.client.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant

data class ClientBatchRequest(
    @field:NotBlank(message = "deviceId is required")
    val deviceId: String,
    @field:NotNull(message = "timestamp is required")
    val timestamp: Instant,
    @field:Valid
    val clients: List<ClientItem> = emptyList(),
)
