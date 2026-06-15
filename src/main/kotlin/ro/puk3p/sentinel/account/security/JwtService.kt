package ro.puk3p.sentinel.account.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date

/** Issues and verifies HS256 JWTs. The secret must be at least 32 bytes. */
@Service
class JwtService(
    @Value("\${app.jwt.secret}") secret: String,
    @Value("\${app.jwt.ttl-minutes:720}") private val ttlMinutes: Long,
) {
    // Lazy so a misconfigured (too-short) secret doesn't throw from the constructor.
    private val key by lazy { Keys.hmacShaKeyFor(secret.toByteArray(Charsets.UTF_8)) }

    /** Build a signed token carrying the username (subject) and session id (jti). */
    fun issue(
        username: String,
        tokenId: String,
    ): String {
        val now = System.currentTimeMillis()
        return Jwts.builder()
            .subject(username)
            .id(tokenId)
            .issuedAt(Date(now))
            .expiration(Date(now + ttlMinutes * 60_000))
            .signWith(key)
            .compact()
    }

    /** Parse and verify a token, or return null if invalid/expired. */
    fun parse(token: String): Claims? =
        runCatching {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
        }.getOrNull()
}
