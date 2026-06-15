package ro.puk3p.sentinel.account.repository

import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import ro.puk3p.sentinel.account.entity.AuditRecord
import ro.puk3p.sentinel.account.entity.SessionRecord
import ro.puk3p.sentinel.account.entity.UserAccount
import java.util.Optional

interface UserAccountRepository : MongoRepository<UserAccount, String> {
    fun findByUsername(username: String): Optional<UserAccount>

    fun existsByUsername(username: String): Boolean
}

interface SessionRepository : MongoRepository<SessionRecord, String> {
    fun findByTokenId(tokenId: String): Optional<SessionRecord>

    fun findByUsernameOrderByLastSeenAtDesc(username: String): List<SessionRecord>

    fun deleteByTokenId(tokenId: String)

    fun deleteByUsernameAndTokenIdNot(
        username: String,
        tokenId: String,
    )
}

interface AuditRepository : MongoRepository<AuditRecord, String> {
    fun findByUsernameOrderByAtDesc(
        username: String,
        pageable: Pageable,
    ): List<AuditRecord>
}
