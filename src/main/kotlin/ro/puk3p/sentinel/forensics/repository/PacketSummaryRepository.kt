package ro.puk3p.sentinel.forensics.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import ro.puk3p.sentinel.forensics.entity.PacketSummaryEntity
import java.time.Instant

interface PacketSummaryRepository : JpaRepository<PacketSummaryEntity, Long> {
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
}
