package ro.puk3p.sentinel.common.response

import java.time.Instant

/**
 * Standard response envelope. Declared `open` (rather than a `data class`) so
 * Spring HATEOAS can post-process responses that nest a RepresentationModel in
 * [data] — its return-value handler proxies the wrapper, which requires a
 * non-final type with overridable accessors.
 */
open class ApiResponse<T>(
    open val success: Boolean,
    open val message: String,
    open val data: T? = null,
    open val timestamp: Instant = Instant.now(),
)
