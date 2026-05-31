package ro.puk3p.sentinel.alert.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.support.PageableExecutionUtils
import ro.puk3p.sentinel.alert.entity.AlertEntity
import ro.puk3p.sentinel.alert.model.AlertFilter

class AlertRepositoryCustomImpl(
    private val mongoTemplate: MongoTemplate,
) : AlertRepositoryCustom {
    override fun search(
        filter: AlertFilter,
        pageable: Pageable,
    ): Page<AlertEntity> {
        val criteria = mutableListOf<Criteria>()

        filter.severity?.let { criteria += Criteria.where("severity").`is`(it) }
        filter.protocol?.let { criteria += Criteria.where("protocol").`is`(it) }
        filter.deviceId?.takeIf { it.isNotBlank() }?.let { criteria += Criteria.where("deviceId").`is`(it) }
        filter.sourceIp?.takeIf { it.isNotBlank() }?.let { criteria += Criteria.where("sourceIp").`is`(it) }
        filter.from?.let { criteria += Criteria.where("timestamp").gte(it) }
        filter.to?.let { criteria += Criteria.where("timestamp").lte(it) }

        val query = Query()
        if (criteria.isNotEmpty()) {
            query.addCriteria(Criteria().andOperator(*criteria.toTypedArray()))
        }
        query.with(pageable)

        val content = mongoTemplate.find(query, AlertEntity::class.java)

        return PageableExecutionUtils.getPage(content, pageable) {
            mongoTemplate.count(Query.of(query).limit(-1).skip(-1), AlertEntity::class.java)
        }
    }
}
