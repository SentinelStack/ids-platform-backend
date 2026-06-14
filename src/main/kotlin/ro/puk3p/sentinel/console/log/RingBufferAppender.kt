package ro.puk3p.sentinel.console.log

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import ro.puk3p.sentinel.console.dto.RuntimeLogLine
import java.time.Instant

/**
 * Logback appender that mirrors every log line into [RuntimeLogBuffer] so the
 * topology console can surface the backend's real runtime logs. Wired in
 * logback-spring.xml alongside the console appender (stdout still flows to
 * journald) — this one just keeps an in-memory tail.
 */
class RingBufferAppender : AppenderBase<ILoggingEvent>() {
    override fun append(event: ILoggingEvent) {
        RuntimeLogBuffer.add(
            RuntimeLogLine(
                at = Instant.ofEpochMilli(event.timeStamp).toString(),
                level = event.level.toString(),
                logger = shortLogger(event.loggerName),
                message = event.formattedMessage,
            ),
        )
    }

    /** `ro.puk3p.sentinel.console.ConsoleService` -> `c.ConsoleService`. */
    private fun shortLogger(name: String): String {
        val parts = name.split('.')
        if (parts.size <= 1) return name
        return parts.dropLast(1).joinToString(".") { it.take(1) } + "." + parts.last()
    }
}
