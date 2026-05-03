package ro.puk3p.sentinel.traffic.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ro.puk3p.sentinel.traffic.entity.TrafficStatsEntity

interface TrafficStatsRepository : JpaRepository<TrafficStatsEntity, Long> {
    fun findTopByOrderByTimestampDesc(): TrafficStatsEntity?

    fun findByDeviceIdOrderByTimestampDesc(
        deviceId: String,
        pageable: Pageable,
    ): Page<TrafficStatsEntity>

    @Query(
        """
        select
            coalesce(sum(t.totalPackets),0),
            coalesce(sum(t.tcpPackets),0),
            coalesce(sum(t.udpPackets),0),
            coalesce(sum(t.totalBytes),0),
            coalesce(sum(t.tcpBytes),0),
            coalesce(sum(t.udpBytes),0)
        from TrafficStatsEntity t
        """,
    )
    fun summarize(): Array<Number>
}
