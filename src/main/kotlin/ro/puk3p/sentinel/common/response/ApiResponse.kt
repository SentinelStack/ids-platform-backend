package ro.puk3p.sentinel.common.response

import java.time.Instant

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val timestamp: Instant = Instant.now(),
)
