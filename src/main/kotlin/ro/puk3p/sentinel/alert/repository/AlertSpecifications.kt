package ro.puk3p.sentinel.alert.repository

import org.springframework.data.jpa.domain.Specification
import ro.puk3p.sentinel.alert.entity.AlertEntity
import ro.puk3p.sentinel.alert.model.AlertFilter

object AlertSpecifications {
    fun withFilter(filter: AlertFilter): Specification<AlertEntity> {
        return Specification { root, _, cb ->
            val predicates = mutableListOf<jakarta.persistence.criteria.Predicate>()

            filter.severity?.let { predicates += cb.equal(root.get<Any>("severity"), it) }
            filter.protocol?.let { predicates += cb.equal(root.get<Any>("protocol"), it) }
            filter.deviceId?.takeIf { it.isNotBlank() }?.let { predicates += cb.equal(root.get<String>("deviceId"), it) }
            filter.sourceIp?.takeIf { it.isNotBlank() }?.let { predicates += cb.equal(root.get<String>("sourceIp"), it) }
            filter.from?.let { predicates += cb.greaterThanOrEqualTo(root.get("timestamp"), it) }
            filter.to?.let { predicates += cb.lessThanOrEqualTo(root.get("timestamp"), it) }

            cb.and(*predicates.toTypedArray())
        }
    }
}
