package ro.puk3p.sentinel.console.dto

/**
 * Unified log/event stream for the Log Viewer, merging real sources: the
 * backend's own runtime logs, real alerts (edge detections) and device events.
 */
data class LogStreamView(
    val kpis: LogKpis,
    val entries: List<LogEntry>,
    val pipeline: List<PipelineComponent>,
)

data class LogKpis(
    val logsIngested24h: Long,
    val edgeLogs: Long,
    val criticalEvents: Long,
    val warningEvents: Long,
    val ingestionDelayMs: Long,
)

data class LogEntry(
    val at: String,
    val severity: String,
    val source: String,
    val icon: String,
    val device: String,
    val eventType: String,
    val message: String,
    val traceId: String,
    val tone: String,
)

data class PipelineComponent(
    val name: String,
    val status: String,
)
