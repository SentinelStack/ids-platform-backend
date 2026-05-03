package ro.puk3p.sentinel.alert.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import ro.puk3p.sentinel.alert.entity.AlertEntity
import java.util.Optional

interface AlertRepository : JpaRepository<AlertEntity, Long>, JpaSpecificationExecutor<AlertEntity> {
    fun findByAlertId(alertId: String): Optional<AlertEntity>

    fun findTopByOrderByTimestampDesc(): AlertEntity?

    fun findByDeviceIdOrderByTimestampDesc(
        deviceId: String,
        pageable: Pageable,
    ): Page<AlertEntity>
}
