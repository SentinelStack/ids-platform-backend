package ro.puk3p.sentinel.dns.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ro.puk3p.sentinel.dns.dto.DnsQueryBatchRequest
import ro.puk3p.sentinel.dns.dto.DnsQueryResponse

interface DnsService {
    fun recordBatch(request: DnsQueryBatchRequest): Int

    fun recent(pageable: Pageable): Page<DnsQueryResponse>
}
