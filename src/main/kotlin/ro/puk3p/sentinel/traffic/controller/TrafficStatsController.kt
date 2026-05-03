package ro.puk3p.sentinel.traffic.controller

import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ro.puk3p.sentinel.common.response.ApiResponse
import ro.puk3p.sentinel.common.response.PagedResponse
import ro.puk3p.sentinel.traffic.dto.TrafficStatsCreateRequest
import ro.puk3p.sentinel.traffic.dto.TrafficStatsResponse
import ro.puk3p.sentinel.traffic.dto.TrafficSummaryResponse
import ro.puk3p.sentinel.traffic.service.TrafficStatsService

@RestController
@RequestMapping("/api/traffic")
class TrafficStatsController(
    private val trafficStatsService: TrafficStatsService,
) {
    @PostMapping("/stats")
    fun create(
        @Valid @RequestBody request: TrafficStatsCreateRequest,
    ): ApiResponse<TrafficStatsResponse> {
        return ApiResponse(success = true, message = "Traffic stats stored", data = trafficStatsService.create(request))
    }

    @GetMapping("/stats/latest")
    fun latest(): ApiResponse<TrafficStatsResponse> {
        return ApiResponse(success = true, message = "Latest traffic stats retrieved", data = trafficStatsService.getLatest())
    }

    @GetMapping("/stats/by-device/{deviceId}")
    fun byDevice(
        @PathVariable deviceId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ApiResponse<PagedResponse<TrafficStatsResponse>> {
        val result = trafficStatsService.getByDevice(deviceId, PageRequest.of(page, size, Sort.by("timestamp").descending()))
        return ApiResponse(
            success = true,
            message = "Traffic stats by device retrieved",
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

    @GetMapping("/summary")
    fun summary(): ApiResponse<TrafficSummaryResponse> {
        return ApiResponse(success = true, message = "Traffic summary retrieved", data = trafficStatsService.getSummary())
    }
}
