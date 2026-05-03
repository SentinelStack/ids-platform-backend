package ro.puk3p.sentinel.common.exception

import java.time.Instant
import java.util.Collections

class ErrorResponse(
    val timestamp: Instant = Instant.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    validationErrors: Map<String, String>? = null,
) {
    private val validationErrorsCopy: Map<String, String>? = validationErrors?.toMap()

    val validationErrors: Map<String, String>?
        get() = validationErrorsCopy?.let { Collections.unmodifiableMap(it) }
}
