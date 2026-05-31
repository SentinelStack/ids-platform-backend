package ro.puk3p.sentinel.forensics.controller

import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ro.puk3p.sentinel.common.hateoas.PageLinks
import ro.puk3p.sentinel.common.response.ApiResponse
import ro.puk3p.sentinel.common.response.PagedResponse
import ro.puk3p.sentinel.forensics.assembler.ForensicsTimelineModelAssembler
import ro.puk3p.sentinel.forensics.assembler.PacketSummaryModelAssembler
import ro.puk3p.sentinel.forensics.dto.ForensicsTimelineEntry
import ro.puk3p.sentinel.forensics.dto.PacketSummaryCreateRequest
import ro.puk3p.sentinel.forensics.dto.PacketSummaryResponse
import ro.puk3p.sentinel.forensics.service.ForensicsService
import java.time.Instant

@RestController
@RequestMapping("/api/forensics")
class ForensicsController(
    private val forensicsService: ForensicsService,
    private val packetSummaryModelAssembler: PacketSummaryModelAssembler,
    private val forensicsTimelineModelAssembler: ForensicsTimelineModelAssembler,
) {
    @PostMapping("/packets")
    fun createPacket(
        @Valid @RequestBody request: PacketSummaryCreateRequest,
    ): ApiResponse<EntityModel<PacketSummaryResponse>> {
        return ApiResponse(
            success = true,
            message = "Packet summary stored",
            data = packetSummaryModelAssembler.toModel(forensicsService.createPacketSummary(request)),
        )
    }

    @GetMapping("/packets")
    fun getPackets(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ApiResponse<PagedResponse<EntityModel<PacketSummaryResponse>>> {
        val result = forensicsService.getPackets(PageRequest.of(page, size, Sort.by("timestamp").descending()))

        val paged =
            PagedResponse(
                content = result.content.map(packetSummaryModelAssembler::toModel),
                page = result.number,
                size = result.size,
                totalElements = result.totalElements,
                totalPages = result.totalPages,
                isLast = result.isLast,
            )
        PageLinks.apply(paged, result) { p ->
            linkTo(methodOn(ForensicsController::class.java).getPackets(p, size))
        }

        return ApiResponse(success = true, message = "Packet summaries retrieved", data = paged)
    }

    @GetMapping("/by-alert/{alertId}")
    fun getByAlert(
        @PathVariable alertId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ApiResponse<PagedResponse<EntityModel<PacketSummaryResponse>>> {
        val result = forensicsService.getByAlert(alertId, PageRequest.of(page, size, Sort.by("timestamp").descending()))

        val paged =
            PagedResponse(
                content = result.content.map(packetSummaryModelAssembler::toModel),
                page = result.number,
                size = result.size,
                totalElements = result.totalElements,
                totalPages = result.totalPages,
                isLast = result.isLast,
            )
        PageLinks.apply(paged, result) { p ->
            linkTo(methodOn(ForensicsController::class.java).getByAlert(alertId, p, size))
        }

        return ApiResponse(success = true, message = "Forensics entries by alert retrieved", data = paged)
    }

    @GetMapping("/timeline")
    fun timeline(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: Instant?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: Instant?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ApiResponse<PagedResponse<EntityModel<ForensicsTimelineEntry>>> {
        val result = forensicsService.getTimeline(from, to, PageRequest.of(page, size, Sort.by("timestamp").descending()))

        val paged =
            PagedResponse(
                content = result.content.map(forensicsTimelineModelAssembler::toModel),
                page = result.number,
                size = result.size,
                totalElements = result.totalElements,
                totalPages = result.totalPages,
                isLast = result.isLast,
            )
        PageLinks.apply(paged, result) { p ->
            linkTo(methodOn(ForensicsController::class.java).timeline(from, to, p, size))
        }

        return ApiResponse(success = true, message = "Forensics timeline retrieved", data = paged)
    }
}
