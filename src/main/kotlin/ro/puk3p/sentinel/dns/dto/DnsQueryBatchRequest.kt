package ro.puk3p.sentinel.dns.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant

data class DnsQueryBatchRequest(
    @field:NotBlank(message = "deviceId is required")
    val deviceId: String,
    @field:NotNull(message = "timestamp is required")
    val timestamp: Instant,
    val windowSeconds: Int = 0,
    @field:Valid
    val queries: List<DnsQueryItem> = emptyList(),
)
