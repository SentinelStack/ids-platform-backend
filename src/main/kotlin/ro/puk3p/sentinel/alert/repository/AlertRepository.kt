package ro.puk3p.sentinel.alert.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import ro.puk3p.sentinel.alert.entity.AlertEntity
import java.util.Optional

interface AlertRepository : MongoRepository<AlertEntity, String>, AlertRepositoryCustom {
    fun findByAlertId(alertId: String): Optional<AlertEntity>

    fun findTopByOrderByTimestampDesc(): AlertEntity?

    fun findByDeviceIdOrderByTimestampDesc(
        deviceId: String,
        pageable: Pageable,
    ): Page<AlertEntity>

    fun countByAssigneeAndAcknowledgedFalse(assignee: String): Long
}
