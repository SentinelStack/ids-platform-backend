package ro.puk3p.sentinel.alert.dto

import ro.puk3p.sentinel.alert.model.Severity
import java.time.Instant

data class ContainmentResponse(
    val containmentId: String,
    val alertId: String,
    val sourceIp: String,
    val deviceId: String,
    val reason: String,
    val severity: Severity,
    val active: Boolean,
    val createdAt: Instant?,
    val alreadyActive: Boolean,
)
