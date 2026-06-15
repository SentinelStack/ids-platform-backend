package ro.puk3p.sentinel.migration

import io.mongock.api.annotations.ChangeUnit
import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackExecution
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Index
import org.springframework.security.crypto.password.PasswordEncoder
import ro.puk3p.sentinel.account.entity.UserAccount
import java.time.Instant

/**
 * Creates the account collections/indexes and seeds the default operator.
 * Default credentials (override the password immediately in any real
 * deployment): username `george.lupu`, password `AegisSOC!2026`.
 */
@ChangeUnit(id = "005-account-seed", order = "005", author = "ids-platform")
class V005AccountSeed {
    @Execution
    fun execution(
        mongoTemplate: MongoTemplate,
        passwordEncoder: PasswordEncoder,
    ) {
        listOf("users", "sessions", "audit_log").forEach {
            if (!mongoTemplate.collectionExists(it)) {
                mongoTemplate.createCollection(it)
            }
        }
        mongoTemplate.indexOps("users").createIndex(Index().on("username", Sort.Direction.ASC).unique())
        mongoTemplate.indexOps("sessions").apply {
            createIndex(Index().on("tokenId", Sort.Direction.ASC).unique())
            createIndex(Index().on("username", Sort.Direction.ASC))
        }
        mongoTemplate.indexOps("audit_log").createIndex(Index().on("username", Sort.Direction.ASC))

        if (mongoTemplate.estimatedCount(UserAccount::class.java) > 0) {
            return
        }
        val now = Instant.now()
        mongoTemplate.save(
            UserAccount(
                username = "george.lupu",
                email = "george.lupu@aegis.local",
                fullName = "George Lupu",
                phone = "+40 700 000 000",
                role = "SOC Analyst",
                passwordHash = passwordEncoder.encode("AegisSOC!2026")!!,
                passwordChangedAt = now,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    @RollbackExecution
    fun rollback(mongoTemplate: MongoTemplate) {
        listOf("users", "sessions", "audit_log").forEach { mongoTemplate.dropCollection(it) }
    }
}
