package ro.puk3p.sentinel.alert.model

import java.time.Instant

data class AlertFilter(
    val severity: Severity? = null,
    val protocol: Protocol? = null,
    val deviceId: String? = null,
    val sourceIp: String? = null,
    val from: Instant? = null,
    val to: Instant? = null,
)
