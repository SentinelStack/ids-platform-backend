package ro.puk3p.sentinel.dns.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class DnsQueryItem(
    @field:NotBlank(message = "clientIp is required")
    val clientIp: String,
    @field:NotBlank(message = "domain is required")
    val domain: String,
    @field:Min(value = 0, message = "count must be >= 0")
    val count: Int,
)
