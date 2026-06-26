package ro.puk3p.sentinel.dns.dto

import java.time.Instant

data class DnsQueryResponse(
    val deviceId: String,
    val clientIp: String,
    val domain: String,
    val count: Int,
    val timestamp: Instant,
    val createdAt: Instant,
)
