package ro.puk3p.sentinel.device.model

enum class DeviceStatus {
    ONLINE,
    OFFLINE,

    /**
     * Operator-isolated. A quarantined device is contained: it stays in this
     * state regardless of incoming heartbeats, and is only lifted by an explicit
     * release. See DeviceService.quarantine / release.
     */
    QUARANTINED,
    UNKNOWN,
}
