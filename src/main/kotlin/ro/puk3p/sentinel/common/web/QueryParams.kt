package ro.puk3p.sentinel.common.web

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import ro.puk3p.sentinel.common.exception.BadRequestException
import java.time.Instant
import java.time.format.DateTimeParseException

object QueryParams {
    const val MAX_PAGE_SIZE = 500

    fun parseInstant(
        name: String,
        value: String?,
    ): Instant? {
        if (value.isNullOrBlank()) {
            return null
        }
        return try {
            Instant.parse(value)
        } catch (ex: DateTimeParseException) {
            throw BadRequestException("'$name' must be an ISO-8601 instant (e.g. 2026-01-01T00:00:00Z), got '$value'")
        }
    }

    fun pageRequest(
        page: Int,
        size: Int,
        sort: Sort,
    ): PageRequest {
        if (page < 0) {
            throw BadRequestException("'page' must be >= 0, got $page")
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw BadRequestException("'size' must be between 1 and $MAX_PAGE_SIZE, got $size")
        }
        return PageRequest.of(page, size, sort)
    }

    fun sort(
        sortBy: String,
        direction: String,
        allowedFields: Set<String>,
    ): Sort {
        if (sortBy !in allowedFields) {
            throw BadRequestException("'sortBy' must be one of ${allowedFields.sorted()}, got '$sortBy'")
        }
        return if (direction.equals("asc", ignoreCase = true)) Sort.by(sortBy).ascending() else Sort.by(sortBy).descending()
    }
}
