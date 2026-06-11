package ro.puk3p.sentinel.alert.controller

import jakarta.validation.Valid
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import ro.puk3p.sentinel.alert.assembler.AlertModelAssembler
import ro.puk3p.sentinel.alert.dto.AlertCreateRequest
import ro.puk3p.sentinel.alert.dto.AlertResponse
import ro.puk3p.sentinel.alert.model.AlertFilter
import ro.puk3p.sentinel.alert.model.Protocol
import ro.puk3p.sentinel.alert.model.Severity
import ro.puk3p.sentinel.alert.service.AlertService
import ro.puk3p.sentinel.common.hateoas.PageLinks
import ro.puk3p.sentinel.common.response.ApiResponse
import ro.puk3p.sentinel.common.response.PagedResponse
import ro.puk3p.sentinel.common.web.QueryParams

@RestController
@RequestMapping("/api/alerts")
class AlertController(
    private val alertService: AlertService,
    private val alertModelAssembler: AlertModelAssembler,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Valid @RequestBody request: AlertCreateRequest,
    ): ApiResponse<EntityModel<AlertResponse>> {
        return ApiResponse(
            success = true,
            message = "Alert created",
            data = alertModelAssembler.toModel(alertService.createAlert(request)),
        )
    }

    @GetMapping
    fun getAll(
        @RequestParam(required = false) severity: Severity?,
        @RequestParam(required = false) protocol: Protocol?,
        @RequestParam(required = false) deviceId: String?,
        @RequestParam(required = false) sourceIp: String?,
        @RequestParam(required = false) from: String?,
        @RequestParam(required = false) to: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "timestamp") sortBy: String,
        @RequestParam(defaultValue = "desc") direction: String,
    ): ApiResponse<PagedResponse<EntityModel<AlertResponse>>> {
        val sort = QueryParams.sort(sortBy, direction, SORTABLE_FIELDS)
        val pageable = QueryParams.pageRequest(page, size, sort)
        val filter =
            AlertFilter(
                severity,
                protocol,
                deviceId,
                sourceIp,
                QueryParams.parseInstant("from", from),
                QueryParams.parseInstant("to", to),
            )
        val result = alertService.getAlerts(filter, pageable)

        val paged =
            PagedResponse(
                content = result.content.map(alertModelAssembler::toModel),
                page = result.number,
                size = result.size,
                totalElements = result.totalElements,
                totalPages = result.totalPages,
                isLast = result.isLast,
            )
        PageLinks.apply(paged, result) { p ->
            linkTo(methodOn(AlertController::class.java).getAll(severity, protocol, deviceId, sourceIp, from, to, p, size, sortBy, direction))
        }

        return ApiResponse(success = true, message = "Alerts retrieved", data = paged)
    }

    @GetMapping("/{alertId}")
    fun getById(
        @PathVariable alertId: String,
    ): ApiResponse<EntityModel<AlertResponse>> {
        return ApiResponse(success = true, message = "Alert retrieved", data = alertModelAssembler.toModel(alertService.getByAlertId(alertId)))
    }

    @GetMapping("/latest")
    fun getLatest(): ApiResponse<EntityModel<AlertResponse>> {
        return ApiResponse(success = true, message = "Latest alert retrieved", data = alertModelAssembler.toModel(alertService.getLatest()))
    }

    @GetMapping("/by-device/{deviceId}")
    fun getByDevice(
        @PathVariable deviceId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ApiResponse<PagedResponse<EntityModel<AlertResponse>>> {
        val pageable = QueryParams.pageRequest(page, size, org.springframework.data.domain.Sort.by("timestamp").descending())
        val result = alertService.getByDevice(deviceId, pageable)

        val paged =
            PagedResponse(
                content = result.content.map(alertModelAssembler::toModel),
                page = result.number,
                size = result.size,
                totalElements = result.totalElements,
                totalPages = result.totalPages,
                isLast = result.isLast,
            )
        PageLinks.apply(paged, result) { p ->
            linkTo(methodOn(AlertController::class.java).getByDevice(deviceId, p, size))
        }

        return ApiResponse(success = true, message = "Device alerts retrieved", data = paged)
    }

    @PatchMapping("/{alertId}/acknowledge")
    fun acknowledge(
        @PathVariable alertId: String,
    ): ApiResponse<EntityModel<AlertResponse>> {
        return ApiResponse(success = true, message = "Alert acknowledged", data = alertModelAssembler.toModel(alertService.acknowledge(alertId)))
    }

    companion object {
        private val SORTABLE_FIELDS = setOf("timestamp", "severity", "type", "protocol", "deviceId", "sourceIp", "createdAt")
    }
}
