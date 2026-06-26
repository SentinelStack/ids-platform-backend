package ro.puk3p.sentinel.dns.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import ro.puk3p.sentinel.dns.entity.DnsQueryEntity
import java.time.Instant

interface DnsQueryRepository : MongoRepository<DnsQueryEntity, String> {
    fun findAllByOrderByTimestampDesc(pageable: Pageable): Page<DnsQueryEntity>

    fun findByTimestampAfterOrderByTimestampDesc(
        timestamp: Instant,
        pageable: Pageable,
    ): Page<DnsQueryEntity>
}
