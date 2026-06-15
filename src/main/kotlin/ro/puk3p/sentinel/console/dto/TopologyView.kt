package ro.puk3p.sentinel.console.dto

/**
 * A single line in the topology "live feed". Synthesised from real domain
 * activity (alerts, device heartbeats, containment) — not mock data.
 *
 * @param at ISO-8601 instant the event occurred.
 * @param kind short uppercase event code (NODE_UP, ANOMALY_DETECTION, …).
 * @param message human-readable description.
 * @param tone display hint: primary | secondary | error | warning | muted.
 */
data class TopologyEvent(
    val at: String,
    val kind: String,
    val message: String,
    val tone: String,
)

/**
 * A raw runtime log line from the backend service itself, captured by an
 * in-memory ring-buffer appender (see RingBufferAppender).
 *
 * @param at ISO-8601 instant the line was logged.
 * @param level log level (ERROR, WARN, INFO, …).
 * @param logger short logger name.
 * @param message the formatted log message.
 */
data class RuntimeLogLine(
    val at: String,
    val level: String,
    val logger: String,
    val message: String,
)

/** Live detail for a topology node, computed from real device + traffic + alerts. */
data class NodeDetailView(
    val deviceId: String,
    val name: String,
    val ip: String,
    val status: String,
    val statusTone: String,
    val load: String,
    val risk: String,
    val riskTone: String,
    val activity: String,
    val detections: List<NodeDetection>,
)

data class NodeDetection(
    val label: String,
    val level: String,
)
