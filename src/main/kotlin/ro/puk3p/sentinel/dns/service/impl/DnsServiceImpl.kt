package ro.puk3p.sentinel.dns.service.impl

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import ro.puk3p.sentinel.dns.dto.DnsQueryBatchRequest
import ro.puk3p.sentinel.dns.dto.DnsQueryResponse
import ro.puk3p.sentinel.dns.mapper.DnsQueryMapper
import ro.puk3p.sentinel.dns.repository.DnsQueryRepository
import ro.puk3p.sentinel.dns.service.DnsService

@Service
class DnsServiceImpl(
    private val dnsQueryRepository: DnsQueryRepository,
) : DnsService {
    override fun recordBatch(request: DnsQueryBatchRequest): Int {
        val saved = dnsQueryRepository.saveAll(DnsQueryMapper.toEntities(request))
        return saved.size
    }

    override fun recent(pageable: Pageable): Page<DnsQueryResponse> {
        return dnsQueryRepository.findAllByOrderByTimestampDesc(pageable).map(DnsQueryMapper::toResponse)
    }
}
