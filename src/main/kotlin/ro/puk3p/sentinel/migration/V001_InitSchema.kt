package ro.puk3p.sentinel.migration

import io.mongock.api.annotations.ChangeUnit
import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackExecution
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Index

@ChangeUnit(id = "001-init-schema", order = "001", author = "ids-platform")
class V001InitSchema {
    private val collections = listOf("devices", "alerts", "traffic_stats", "packet_summaries")

    @Execution
    fun execution(mongoTemplate: MongoTemplate) {
        collections.forEach { name ->
            if (!mongoTemplate.collectionExists(name)) {
                mongoTemplate.createCollection(name)
            }
        }

        mongoTemplate.indexOps("devices")
            .createIndex(Index().on("deviceId", Sort.Direction.ASC).unique())

        mongoTemplate.indexOps("alerts").apply {
            createIndex(Index().on("alertId", Sort.Direction.ASC).unique())
            createIndex(Index().on("deviceId", Sort.Direction.ASC))
            createIndex(Index().on("timestamp", Sort.Direction.DESC))
        }

        mongoTemplate.indexOps("traffic_stats").apply {
            createIndex(Index().on("deviceId", Sort.Direction.ASC))
            createIndex(Index().on("timestamp", Sort.Direction.DESC))
        }

        mongoTemplate.indexOps("packet_summaries").apply {
            createIndex(Index().on("deviceId", Sort.Direction.ASC))
            createIndex(Index().on("alertId", Sort.Direction.ASC))
            createIndex(Index().on("timestamp", Sort.Direction.DESC))
        }
    }

    @RollbackExecution
    fun rollback(mongoTemplate: MongoTemplate) {
        collections.forEach { name -> mongoTemplate.dropCollection(name) }
    }
}
