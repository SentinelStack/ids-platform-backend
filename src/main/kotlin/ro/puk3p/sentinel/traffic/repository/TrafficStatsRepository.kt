package ro.puk3p.sentinel.traffic.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import ro.puk3p.sentinel.traffic.entity.TrafficStatsEntity

interface TrafficStatsRepository : MongoRepository<TrafficStatsEntity, String>, TrafficStatsRepositoryCustom {
    fun findTopByOrderByTimestampDesc(): TrafficStatsEntity?

    fun findByDeviceIdOrderByTimestampDesc(
        deviceId: String,
        pageable: Pageable,
    ): Page<TrafficStatsEntity>
}
