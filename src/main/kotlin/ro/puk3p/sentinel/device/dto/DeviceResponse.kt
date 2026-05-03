package ro.puk3p.sentinel.device.dto

import ro.puk3p.sentinel.device.model.DeviceStatus
import java.time.Instant

data class DeviceResponse(
    val deviceId: String,
    val name: String,
    val ipAddress: String,
    val status: DeviceStatus,
    val lastSeenAt: Instant,
    val firmwareVersion: String?,
    val model: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
