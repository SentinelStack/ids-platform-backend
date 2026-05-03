package ro.puk3p.sentinel.alert.model

enum class AlertType {
    UDP_FLOOD_SUSPECTED,
    TCP_SPIKE_SUSPECTED,
    HIGH_TRAFFIC_VOLUME,
    PORT_SCAN_SUSPECTED,
    UNKNOWN,
}
