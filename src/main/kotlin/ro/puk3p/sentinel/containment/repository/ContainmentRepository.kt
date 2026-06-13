package ro.puk3p.sentinel.containment.repository

import org.springframework.data.mongodb.repository.MongoRepository
import ro.puk3p.sentinel.containment.entity.ContainmentEntity
import java.util.Optional

interface ContainmentRepository : MongoRepository<ContainmentEntity, String> {
    fun findByAlertId(alertId: String): Optional<ContainmentEntity>

    fun findFirstBySourceIpAndActiveTrue(sourceIp: String): ContainmentEntity?

    fun countByActiveTrue(): Long
}
