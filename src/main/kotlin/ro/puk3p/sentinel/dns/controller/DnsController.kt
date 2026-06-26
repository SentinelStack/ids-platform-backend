package ro.puk3p.sentinel.dns.controller

import jakarta.validation.Valid
import org.springframework.data.domain.Sort
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
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
import ro.puk3p.sentinel.dns.dto.DnsQueryBatchRequest
import ro.puk3p.sentinel.dns.dto.DnsQueryResponse
import ro.puk3p.sentinel.dns.service.DnsService

@RestController
@RequestMapping("/api/dns")
class DnsController(
    private val dnsService: DnsService,
) {
    @PostMapping("/queries")
    @ResponseStatus(HttpStatus.CREATED)
    fun ingest(
        @Valid @RequestBody request: DnsQueryBatchRequest,
    ): ApiResponse<Map<String, Int>> {
        val saved = dnsService.recordBatch(request)
        return ApiResponse(
            success = true,
            message = "DNS queries stored",
            data = mapOf("saved" to saved),
        )
    }

    @GetMapping("/queries")
    fun recent(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ApiResponse<PagedResponse<DnsQueryResponse>> {
        val result = dnsService.recent(QueryParams.pageRequest(page, size, Sort.by("timestamp").descending()))

        val paged =
            PagedResponse(
                content = result.content,
                page = result.number,
                size = result.size,
                totalElements = result.totalElements,
                totalPages = result.totalPages,
                isLast = result.isLast,
            )
        PageLinks.apply(paged, result) { p ->
            linkTo(methodOn(DnsController::class.java).recent(p, size))
        }

        return ApiResponse(success = true, message = "DNS queries retrieved", data = paged)
    }
}
