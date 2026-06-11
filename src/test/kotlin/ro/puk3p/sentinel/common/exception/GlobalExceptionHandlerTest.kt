package ro.puk3p.sentinel.common.exception

import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.servlet.resource.NoResourceFoundException
import kotlin.test.assertEquals

class GlobalExceptionHandlerTest {
    private val handler = GlobalExceptionHandler()

    private fun request(uri: String): MockHttpServletRequest =
        MockHttpServletRequest("GET", uri)

    @Test
    fun `unknown route maps to 404 not 500`() {
        val response = handler.handleNoResource(NoResourceFoundException(HttpMethod.GET, "", "api/nope"), request("/api/nope"))
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals(404, response.body?.status)
        assertEquals("/api/nope", response.body?.path)
    }

    @Test
    fun `resource not found maps to 404`() {
        val response = handler.handleNotFound(ResourceNotFoundException("Alert x not found"), request("/api/alerts/x"))
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals("Alert x not found", response.body?.message)
    }

    @Test
    fun `bad request exception maps to 400`() {
        val response = handler.handleBadRequest(BadRequestException("'page' must be >= 0"), request("/api/alerts"))
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `missing parameter maps to 400`() {
        val response = handler.handleMissingParam(MissingServletRequestParameterException("deviceId", "String"), request("/api/alerts"))
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("Missing required parameter 'deviceId'", response.body?.message)
    }

    @Test
    fun `unsupported method maps to 405`() {
        val response = handler.handleMethodNotSupported(HttpRequestMethodNotSupportedException("DELETE"), request("/api/alerts"))
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.statusCode)
    }

    @Test
    fun `unexpected exception maps to 500`() {
        val response = handler.handleGeneric(IllegalStateException("boom"), request("/api/alerts"))
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
    }
}
