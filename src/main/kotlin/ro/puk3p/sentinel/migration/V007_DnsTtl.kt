@file:Suppress("ktlint:standard:filename")

package ro.puk3p.sentinel.migration

import io.mongock.api.annotations.ChangeUnit
import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackExecution
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Index
import java.time.Duration

@ChangeUnit(id = "007-dns-ttl", order = "007", author = "ids-platform")
class V007DnsTtl {
    @Execution
    fun execution(mongoTemplate: MongoTemplate) {
        if (!mongoTemplate.collectionExists("dns_queries")) {
            mongoTemplate.createCollection("dns_queries")
        }
        mongoTemplate.indexOps("dns_queries").createIndex(Index().on("clientIp", Sort.Direction.ASC))
        mongoTemplate.indexOps("dns_queries").createIndex(Index().on("domain", Sort.Direction.ASC))
        mongoTemplate.indexOps("dns_queries").createIndex(Index().on("timestamp", Sort.Direction.DESC))

        val ttlSeconds = System.getenv("ALERTS_TTL_SECONDS")?.toLongOrNull()?.coerceAtLeast(60L) ?: DEFAULT_TTL_SECONDS
        val ttl = Duration.ofSeconds(ttlSeconds)
        mongoTemplate.indexOps("dns_queries")
            .createIndex(Index().on("createdAt", Sort.Direction.ASC).named("dns_queries_ttl").expire(ttl))
    }

    @RollbackExecution
    fun rollback(mongoTemplate: MongoTemplate) {
        mongoTemplate.indexOps("dns_queries").dropIndex("dns_queries_ttl")
        mongoTemplate.dropCollection("dns_queries")
    }

    companion object {
        private const val DEFAULT_TTL_SECONDS = 3600L
    }
}
