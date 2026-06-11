package ro.puk3p.sentinel.common.web

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.Sort
import ro.puk3p.sentinel.common.exception.BadRequestException
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNull

class QueryParamsTest {
    @Test
    fun `parseInstant accepts ISO-8601 and returns the instant`() {
        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), QueryParams.parseInstant("from", "2026-01-01T00:00:00Z"))
    }

    @Test
    fun `parseInstant returns null for null or blank`() {
        assertNull(QueryParams.parseInstant("from", null))
        assertNull(QueryParams.parseInstant("from", "  "))
    }

    @Test
    fun `parseInstant rejects malformed values with BadRequestException`() {
        assertThrows<BadRequestException> { QueryParams.parseInstant("from", "yesterday") }
        assertThrows<BadRequestException> { QueryParams.parseInstant("to", "2026-13-99") }
    }

    @Test
    fun `pageRequest rejects negative page and out-of-range size`() {
        assertThrows<BadRequestException> { QueryParams.pageRequest(-1, 20, Sort.unsorted()) }
        assertThrows<BadRequestException> { QueryParams.pageRequest(0, 0, Sort.unsorted()) }
        assertThrows<BadRequestException> { QueryParams.pageRequest(0, QueryParams.MAX_PAGE_SIZE + 1, Sort.unsorted()) }
    }

    @Test
    fun `pageRequest accepts valid bounds`() {
        val pr = QueryParams.pageRequest(2, QueryParams.MAX_PAGE_SIZE, Sort.by("timestamp"))
        assertEquals(2, pr.pageNumber)
        assertEquals(QueryParams.MAX_PAGE_SIZE, pr.pageSize)
    }

    @Test
    fun `sort rejects fields outside the whitelist`() {
        assertThrows<BadRequestException> { QueryParams.sort("\$where", "desc", setOf("timestamp")) }
    }

    @Test
    fun `sort honors direction`() {
        val asc = QueryParams.sort("timestamp", "ASC", setOf("timestamp"))
        val desc = QueryParams.sort("timestamp", "desc", setOf("timestamp"))
        assertEquals(Sort.Direction.ASC, asc.getOrderFor("timestamp")?.direction)
        assertEquals(Sort.Direction.DESC, desc.getOrderFor("timestamp")?.direction)
    }
}
