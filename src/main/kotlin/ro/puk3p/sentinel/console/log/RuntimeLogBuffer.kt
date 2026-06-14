package ro.puk3p.sentinel.console.log

import ro.puk3p.sentinel.console.dto.RuntimeLogLine
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.atomic.AtomicInteger

/**
 * Process-wide ring buffer of the backend's own most recent log lines.
 *
 * Logback owns the appender lifecycle (it instantiates [RingBufferAppender]
 * outside the Spring context), so the buffer is a singleton object both the
 * appender and the Spring layer can reach without wiring. It keeps the last
 * [CAPACITY] lines and drops the oldest — bounded memory, no I/O.
 */
object RuntimeLogBuffer {
    private const val CAPACITY = 500

    private val lines = ConcurrentLinkedDeque<RuntimeLogLine>()
    private val size = AtomicInteger(0)

    fun add(line: RuntimeLogLine) {
        lines.addLast(line)
        if (size.incrementAndGet() > CAPACITY) {
            lines.pollFirst()
            size.decrementAndGet()
        }
    }

    /** The most recent [limit] lines, newest first. */
    fun snapshot(limit: Int): List<RuntimeLogLine> =
        lines.descendingIterator().asSequence().take(limit.coerceIn(1, CAPACITY)).toList()
}
