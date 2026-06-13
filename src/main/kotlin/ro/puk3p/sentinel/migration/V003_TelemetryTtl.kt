package ro.puk3p.sentinel.migration

import io.mongock.api.annotations.ChangeUnit
import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackExecution
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Index
import java.time.Duration

@ChangeUnit(id = "003-telemetry-ttl", order = "003", author = "ids-platform")
class V003TelemetryTtl {
    @Execution
    fun execution(mongoTemplate: MongoTemplate) {
        val ttlSeconds = System.getenv("ALERTS_TTL_SECONDS")?.toLongOrNull()?.coerceAtLeast(60L) ?: DEFAULT_TTL_SECONDS
        val ttl = Duration.ofSeconds(ttlSeconds)
        mongoTemplate.indexOps("traffic_stats")
            .createIndex(Index().on("createdAt", Sort.Direction.ASC).named("traffic_stats_ttl").expire(ttl))
        mongoTemplate.indexOps("packet_summaries")
            .createIndex(Index().on("createdAt", Sort.Direction.ASC).named("packet_summaries_ttl").expire(ttl))
    }

    @RollbackExecution
    fun rollback(mongoTemplate: MongoTemplate) {
        mongoTemplate.indexOps("traffic_stats").dropIndex("traffic_stats_ttl")
        mongoTemplate.indexOps("packet_summaries").dropIndex("packet_summaries_ttl")
    }

    companion object {
        private const val DEFAULT_TTL_SECONDS = 3600L
    }
}
