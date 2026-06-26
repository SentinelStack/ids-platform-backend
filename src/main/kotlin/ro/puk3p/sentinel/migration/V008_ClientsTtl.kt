@file:Suppress("ktlint:standard:filename")

package ro.puk3p.sentinel.migration

import io.mongock.api.annotations.ChangeUnit
import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackExecution
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Index
import java.time.Duration

@ChangeUnit(id = "008-clients-ttl", order = "008", author = "ids-platform")
class V008ClientsTtl {
    @Execution
    fun execution(mongoTemplate: MongoTemplate) {
        if (!mongoTemplate.collectionExists("clients")) {
            mongoTemplate.createCollection("clients")
        }
        mongoTemplate.indexOps("clients").createIndex(Index().on("deviceId", Sort.Direction.ASC))
        mongoTemplate.indexOps("clients").createIndex(Index().on("mac", Sort.Direction.ASC))
        mongoTemplate.indexOps("clients").createIndex(Index().on("lastSeen", Sort.Direction.DESC))

        val ttlSeconds = System.getenv("ALERTS_TTL_SECONDS")?.toLongOrNull()?.coerceAtLeast(60L) ?: DEFAULT_TTL_SECONDS
        val ttl = Duration.ofSeconds(ttlSeconds)
        mongoTemplate.indexOps("clients")
            .createIndex(Index().on("createdAt", Sort.Direction.ASC).named("clients_ttl").expire(ttl))
    }

    @RollbackExecution
    fun rollback(mongoTemplate: MongoTemplate) {
        mongoTemplate.indexOps("clients").dropIndex("clients_ttl")
        mongoTemplate.dropCollection("clients")
    }

    companion object {
        private const val DEFAULT_TTL_SECONDS = 3600L
    }
}
