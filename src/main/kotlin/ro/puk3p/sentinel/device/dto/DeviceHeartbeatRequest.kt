package ro.puk3p.sentinel.device.dto

import java.time.Instant

data class DeviceHeartbeatRequest(
    val seenAt: Instant? = null,
    /** Router resource usage, reported by the agent (0–100). */
    val cpuPercent: Int? = null,
    val memPercent: Int? = null,
)
