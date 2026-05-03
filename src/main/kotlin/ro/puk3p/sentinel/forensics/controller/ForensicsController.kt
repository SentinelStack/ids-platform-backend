package ro.puk3p.sentinel.forensics.controller

import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ro.puk3p.sentinel.common.response.ApiResponse
import ro.puk3p.sentinel.common.response.PagedResponse
import ro.puk3p.sentinel.forensics.dto.ForensicsTimelineEntry
import ro.puk3p.sentinel.forensics.dto.PacketSummaryCreateRequest
import ro.puk3p.sentinel.forensics.dto.PacketSummaryResponse
import ro.puk3p.sentinel.forensics.service.ForensicsService
import java.time.Instant

@RestController
@RequestMapping("/api/forensics")
class ForensicsController(
    private val forensicsService: ForensicsService,
) {
    @PostMapping("/packets")
    fun createPacket(
        @Valid @RequestBody request: PacketSummaryCreateRequest,
    ): ApiResponse<PacketSummaryResponse> {
        return ApiResponse(success = true, message = "Packet summary stored", data = forensicsService.createPacketSummary(request))
    }

    @GetMapping("/packets")
    fun getPackets(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ApiResponse<PagedResponse<PacketSummaryResponse>> {
        val result = forensicsService.getPackets(PageRequest.of(page, size, Sort.by("timestamp").descending()))
        return ApiResponse(
            success = true,
            message = "Packet summaries retrieved",
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

    @GetMapping("/by-alert/{alertId}")
    fun getByAlert(
        @PathVariable alertId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ApiResponse<PagedResponse<PacketSummaryResponse>> {
        val result = forensicsService.getByAlert(alertId, PageRequest.of(page, size, Sort.by("timestamp").descending()))
        return ApiResponse(
            success = true,
            message = "Forensics entries by alert retrieved",
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

    @GetMapping("/timeline")
    fun timeline(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: Instant?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: Instant?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ApiResponse<PagedResponse<ForensicsTimelineEntry>> {
        val result = forensicsService.getTimeline(from, to, PageRequest.of(page, size, Sort.by("timestamp").descending()))
        return ApiResponse(
            success = true,
            message = "Forensics timeline retrieved",
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
}
