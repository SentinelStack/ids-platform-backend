package ro.puk3p.sentinel.migration

import io.mongock.api.annotations.ChangeUnit
import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackExecution
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Index
import java.time.Duration

@ChangeUnit(id = "002-alerts-ttl", order = "002", author = "ids-platform")
class V002AlertsTtl {
    @Execution
    fun execution(mongoTemplate: MongoTemplate) {
        val ttlSeconds = System.getenv("ALERTS_TTL_SECONDS")?.toLongOrNull()?.coerceAtLeast(60L) ?: DEFAULT_TTL_SECONDS
        mongoTemplate.indexOps("alerts")
            .createIndex(
                Index().on("createdAt", Sort.Direction.ASC).named(TTL_INDEX).expire(Duration.ofSeconds(ttlSeconds)),
            )
    }

    @RollbackExecution
    fun rollback(mongoTemplate: MongoTemplate) {
        mongoTemplate.indexOps("alerts").dropIndex(TTL_INDEX)
    }

    companion object {
        private const val TTL_INDEX = "alerts_ttl"
        private const val DEFAULT_TTL_SECONDS = 3600L
    }
}
