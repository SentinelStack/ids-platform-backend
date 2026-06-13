package ro.puk3p.sentinel.forensics.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import ro.puk3p.sentinel.forensics.entity.PacketSummaryEntity
import java.time.Instant

interface PacketSummaryRepository : MongoRepository<PacketSummaryEntity, String> {
    fun findAllByOrderByTimestampDesc(pageable: Pageable): Page<PacketSummaryEntity>

    fun findByAlertIdOrderByTimestampDesc(
        alertId: String,
        pageable: Pageable,
    ): Page<PacketSummaryEntity>

    fun findByTimestampBetweenOrderByTimestampDesc(
        from: Instant,
        to: Instant,
        pageable: Pageable,
    ): Page<PacketSummaryEntity>

    fun findBySourceIpOrderByTimestampDesc(
        sourceIp: String,
        pageable: Pageable,
    ): Page<PacketSummaryEntity>
}
