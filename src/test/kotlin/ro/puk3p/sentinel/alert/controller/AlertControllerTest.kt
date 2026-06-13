package ro.puk3p.sentinel.alert.controller

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import ro.puk3p.sentinel.alert.assembler.AlertModelAssembler
import ro.puk3p.sentinel.alert.dto.AlertCreateRequest
import ro.puk3p.sentinel.alert.dto.AlertResponse
import ro.puk3p.sentinel.alert.dto.ContainmentResponse
import ro.puk3p.sentinel.alert.model.AlertFilter
import ro.puk3p.sentinel.alert.model.AlertType
import ro.puk3p.sentinel.alert.model.Protocol
import ro.puk3p.sentinel.alert.model.Severity
import ro.puk3p.sentinel.alert.service.AlertService
import ro.puk3p.sentinel.common.exception.GlobalExceptionHandler
import ro.puk3p.sentinel.common.exception.ResourceNotFoundException
import java.time.Instant

class AlertControllerTest {
    private lateinit var mockMvc: MockMvc
    private val fakeService = FakeAlertService()

    @BeforeEach
    fun setUp() {
        val validator = LocalValidatorFactoryBean().apply { afterPropertiesSet() }
        mockMvc =
            MockMvcBuilders
                .standaloneSetup(AlertController(fakeService, AlertModelAssembler()))
                .setControllerAdvice(GlobalExceptionHandler())
                .setValidator(validator)
                .build()
    }

    @Test
    fun `POST alerts returns 201 Created`() {
        mockMvc.perform(
            post("/api/alerts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_ALERT_JSON),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.alertId").value("a-1"))
    }

    @Test
    fun `POST alerts with invalid body returns 400 with validation errors`() {
        val invalid = VALID_ALERT_JSON.replace("\"sourcePort\": 5353", "\"sourcePort\": 70000")
        mockMvc.perform(
            post("/api/alerts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalid),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.validationErrors.sourcePort").exists())
    }

    @Test
    fun `POST alerts with malformed JSON returns 400 not 500`() {
        mockMvc.perform(
            post("/api/alerts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{not json"),
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `GET alerts with from and to returns 200 and self link keeps the filters`() {
        mockMvc.perform(get("/api/alerts").param("from", "2026-01-01T00:00:00Z").param("to", "2026-12-31T00:00:00Z"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.links[0].rel").value("self"))
            .andExpect(jsonPath("$.data.links[0].href").value(org.hamcrest.Matchers.containsString("from=2026-01-01T00%3A00%3A00Z")))
    }

    @Test
    fun `GET alerts with malformed from returns 400`() {
        mockMvc.perform(get("/api/alerts").param("from", "not-a-date"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("ISO-8601")))
    }

    @Test
    fun `GET alerts with negative page returns 400`() {
        mockMvc.perform(get("/api/alerts").param("page", "-1"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `GET alerts with oversized size returns 400`() {
        mockMvc.perform(get("/api/alerts").param("size", "100000"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `GET alerts with non-whitelisted sortBy returns 400`() {
        mockMvc.perform(get("/api/alerts").param("sortBy", "\$where"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `GET unknown alert returns 404`() {
        mockMvc.perform(get("/api/alerts/missing-id"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
    }

    @Test
    fun `PATCH acknowledge returns 200 and acknowledged true`() {
        mockMvc.perform(patch("/api/alerts/a-1/acknowledge"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.acknowledged").value(true))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
    }

    private class FakeAlertService : AlertService {
        override fun createAlert(request: AlertCreateRequest): AlertResponse = sample(acknowledged = false)

        override fun getAlerts(filter: AlertFilter, pageable: Pageable): Page<AlertResponse> =
            PageImpl(listOf(sample(acknowledged = false)), pageable, 1)

        override fun getByAlertId(alertId: String): AlertResponse =
            if (alertId == "a-1") sample(acknowledged = false) else throw ResourceNotFoundException("Alert $alertId not found")

        override fun getLatest(): AlertResponse = sample(acknowledged = false)

        override fun getByDevice(deviceId: String, pageable: Pageable): Page<AlertResponse> =
            PageImpl(listOf(sample(acknowledged = false)), pageable, 1)

        override fun acknowledge(alertId: String): AlertResponse = sample(acknowledged = true)

        override fun analysts(): List<String> = listOf("Ana Popescu", "Mihai Ionescu")

        override fun assign(
            alertId: String,
            analyst: String?,
        ): AlertResponse = sample(acknowledged = false, assignee = analyst ?: "Ana Popescu")

        override fun contain(alertId: String): ContainmentResponse =
            ContainmentResponse(
                containmentId = "c-1",
                alertId = alertId,
                sourceIp = "203.0.113.10",
                deviceId = "router-1",
                reason = "Containment for PORT_SCAN_SUSPECTED from 203.0.113.10",
                severity = Severity.HIGH,
                active = true,
                createdAt = Instant.parse("2026-06-01T12:00:02Z"),
                alreadyActive = false,
            )

        private fun sample(
            acknowledged: Boolean,
            assignee: String? = null,
        ): AlertResponse =
            AlertResponse(
                alertId = "a-1",
                deviceId = "router-1",
                timestamp = Instant.parse("2026-06-01T12:00:00Z"),
                type = AlertType.PORT_SCAN_SUSPECTED,
                severity = Severity.HIGH,
                protocol = Protocol.TCP,
                sourceIp = "203.0.113.10",
                destinationIp = "192.168.1.5",
                sourcePort = 5353,
                destinationPort = 22,
                packetCount = 100,
                bytesCount = 9000,
                windowSeconds = 5,
                description = null,
                acknowledged = acknowledged,
                assignee = assignee,
                contained = false,
                createdAt = Instant.parse("2026-06-01T12:00:01Z"),
            )
    }

    companion object {
        private val VALID_ALERT_JSON =
            """
            {
              "deviceId": "router-1",
              "timestamp": "2026-06-01T12:00:00Z",
              "type": "PORT_SCAN_SUSPECTED",
              "severity": "HIGH",
              "protocol": "TCP",
              "sourceIp": "203.0.113.10",
              "destinationIp": "192.168.1.5",
              "sourcePort": 5353,
              "destinationPort": 22,
              "packetCount": 100,
              "bytesCount": 9000,
              "windowSeconds": 5
            }
            """.trimIndent()
    }
}
