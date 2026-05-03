package ro.puk3p.sentinel.device.dto

import java.time.Instant

data class DeviceHeartbeatRequest(
    val seenAt: Instant? = null,
)
