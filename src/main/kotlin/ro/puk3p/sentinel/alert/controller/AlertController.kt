package ro.puk3p.sentinel.alert.controller

import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ro.puk3p.sentinel.alert.dto.AlertCreateRequest
import ro.puk3p.sentinel.alert.dto.AlertResponse
import ro.puk3p.sentinel.alert.model.AlertFilter
import ro.puk3p.sentinel.alert.model.Protocol
import ro.puk3p.sentinel.alert.model.Severity
import ro.puk3p.sentinel.alert.service.AlertService
import ro.puk3p.sentinel.common.response.ApiResponse
import ro.puk3p.sentinel.common.response.PagedResponse
import java.time.Instant

@RestController
@RequestMapping("/api/alerts")
class AlertController(
    private val alertService: AlertService,
) {
    @PostMapping
    fun create(
        @Valid @RequestBody request: AlertCreateRequest,
    ): ApiResponse<AlertResponse> {
        return ApiResponse(success = true, message = "Alert created", data = alertService.createAlert(request))
    }

    @GetMapping
    fun getAll(
        @RequestParam(required = false) severity: Severity?,
        @RequestParam(required = false) protocol: Protocol?,
        @RequestParam(required = false) deviceId: String?,
        @RequestParam(required = false) sourceIp: String?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: Instant?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: Instant?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "timestamp") sortBy: String,
        @RequestParam(defaultValue = "desc") direction: String,
    ): ApiResponse<PagedResponse<AlertResponse>> {
        val sort = if (direction.equals("asc", ignoreCase = true)) Sort.by(sortBy).ascending() else Sort.by(sortBy).descending()
        val pageable: Pageable = PageRequest.of(page, size, sort)
        val filter = AlertFilter(severity, protocol, deviceId, sourceIp, from, to)
        val result = alertService.getAlerts(filter, pageable)

        return ApiResponse(
            success = true,
            message = "Alerts retrieved",
            data =
                PagedResponse(
                    content = result.content,
                    page = result.number,
                    size = result.size,
                    totalElements = result.totalElements,
                    totalPages = result.totalPages,
                    isLast = result.isLast,
                ),
        )
    }

    @GetMapping("/{alertId}")
    fun getById(
        @PathVariable alertId: String,
    ): ApiResponse<AlertResponse> {
        return ApiResponse(success = true, message = "Alert retrieved", data = alertService.getByAlertId(alertId))
    }

    @GetMapping("/latest")
    fun getLatest(): ApiResponse<AlertResponse> {
        return ApiResponse(success = true, message = "Latest alert retrieved", data = alertService.getLatest())
    }

    @GetMapping("/by-device/{deviceId}")
    fun getByDevice(
        @PathVariable deviceId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ApiResponse<PagedResponse<AlertResponse>> {
        val result = alertService.getByDevice(deviceId, PageRequest.of(page, size, Sort.by("timestamp").descending()))
        return ApiResponse(
            success = true,
            message = "Device alerts retrieved",
            data =
                PagedResponse(
                    content = result.content,
                    page = result.number,
                    size = result.size,
                    totalElements = result.totalElements,
                    totalPages = result.totalPages,
                    isLast = result.isLast,
                ),
        )
    }

    @PatchMapping("/{alertId}/acknowledge")
    fun acknowledge(
        @PathVariable alertId: String,
    ): ApiResponse<AlertResponse> {
        return ApiResponse(success = true, message = "Alert acknowledged", data = alertService.acknowledge(alertId))
    }
}
