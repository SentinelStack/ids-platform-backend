package ro.puk3p.sentinel.device.dto

import ro.puk3p.sentinel.device.model.DeviceStatus
import java.time.Instant

data class DeviceStatusResponse(
    val deviceId: String,
    val status: DeviceStatus,
    val lastSeenAt: Instant,
)
