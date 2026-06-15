package ro.puk3p.sentinel.migration

import io.mongock.api.annotations.ChangeUnit
import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackExecution
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update

/**
 * Real TOTP 2FA arrives in this release. Earlier accounts carried a cosmetic
 * `mfaEnabled=true` flag with no secret — that would now demand an
 * unsatisfiable code and lock them out. Clear the flag where no secret exists,
 * and add the sparse-unique index backing Google-linked accounts.
 */
@ChangeUnit(id = "006-mfa-reset", order = "006", author = "ids-platform")
class V006MfaReset {
    @Execution
    fun execution(mongoTemplate: MongoTemplate) {
        val noSecret =
            Query(
                Criteria().andOperator(
                    Criteria.where("mfaEnabled").`is`(true),
                    Criteria().orOperator(
                        Criteria.where("mfaSecret").exists(false),
                        Criteria.where("mfaSecret").`is`(null),
                    ),
                ),
            )
        mongoTemplate.updateMulti(noSecret, Update().set("mfaEnabled", false), "users")

        mongoTemplate.indexOps("users")
            .createIndex(Index().on("googleId", Sort.Direction.ASC).unique().sparse())
    }

    @RollbackExecution
    fun rollback() {
        // No-op: clearing an invalid flag is not meaningfully reversible.
    }
}
