package ro.puk3p.sentinel.traffic.controller

import jakarta.validation.Valid
import org.springframework.data.domain.Sort
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import ro.puk3p.sentinel.common.hateoas.PageLinks
import ro.puk3p.sentinel.common.response.ApiResponse
import ro.puk3p.sentinel.common.response.PagedResponse
import ro.puk3p.sentinel.common.web.QueryParams
import ro.puk3p.sentinel.traffic.assembler.TrafficStatsModelAssembler
import ro.puk3p.sentinel.traffic.dto.TrafficStatsCreateRequest
import ro.puk3p.sentinel.traffic.dto.TrafficStatsResponse
import ro.puk3p.sentinel.traffic.dto.TrafficSummaryResponse
import ro.puk3p.sentinel.traffic.service.TrafficStatsService

@RestController
@RequestMapping("/api/traffic")
class TrafficStatsController(
    private val trafficStatsService: TrafficStatsService,
    private val trafficStatsModelAssembler: TrafficStatsModelAssembler,
) {
    @PostMapping("/stats")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Valid @RequestBody request: TrafficStatsCreateRequest,
    ): ApiResponse<EntityModel<TrafficStatsResponse>> {
        return ApiResponse(
            success = true,
            message = "Traffic stats stored",
            data = trafficStatsModelAssembler.toModel(trafficStatsService.create(request)),
        )
    }

    @GetMapping("/stats/latest")
    fun latest(): ApiResponse<EntityModel<TrafficStatsResponse>> {
        return ApiResponse(
            success = true,
            message = "Latest traffic stats retrieved",
            data = trafficStatsModelAssembler.toModel(trafficStatsService.getLatest()),
        )
    }

    @GetMapping("/stats/by-device/{deviceId}")
    fun byDevice(
        @PathVariable deviceId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ApiResponse<PagedResponse<EntityModel<TrafficStatsResponse>>> {
        val result = trafficStatsService.getByDevice(deviceId, QueryParams.pageRequest(page, size, Sort.by("timestamp").descending()))

        val paged =
            PagedResponse(
                content = result.content.map(trafficStatsModelAssembler::toModel),
                page = result.number,
                size = result.size,
                totalElements = result.totalElements,
                totalPages = result.totalPages,
                isLast = result.isLast,
            )
        PageLinks.apply(paged, result) { p ->
            linkTo(methodOn(TrafficStatsController::class.java).byDevice(deviceId, p, size))
        }

        return ApiResponse(success = true, message = "Traffic stats by device retrieved", data = paged)
    }

    @GetMapping("/summary")
    fun summary(): ApiResponse<EntityModel<TrafficSummaryResponse>> {
        val model =
            EntityModel.of(
                trafficStatsService.getSummary(),
                linkTo(methodOn(TrafficStatsController::class.java).summary()).withSelfRel(),
                linkTo(methodOn(TrafficStatsController::class.java).latest()).withRel("latest"),
            )
        return ApiResponse(success = true, message = "Traffic summary retrieved", data = model)
    }
}
