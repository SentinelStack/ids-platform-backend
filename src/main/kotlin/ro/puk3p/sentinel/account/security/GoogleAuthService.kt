package ro.puk3p.sentinel.account.security

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ro.puk3p.sentinel.common.exception.BadRequestException
import ro.puk3p.sentinel.common.exception.UnauthorizedException

/** Verified identity extracted from a Google ID token. */
data class GoogleIdentity(
    val googleId: String,
    val email: String,
    val name: String,
    val emailVerified: Boolean,
)

/**
 * Verifies Google ID tokens (from Google Identity Services on the dashboard)
 * against the configured OAuth client id. Disabled when no client id is set.
 */
@Service
class GoogleAuthService(
    @Value("\${app.google.client-id:}") private val clientId: String,
) {
    val enabled: Boolean get() = clientId.isNotBlank()

    private val verifier: GoogleIdTokenVerifier by lazy {
        GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance())
            .setAudience(listOf(clientId))
            .build()
    }

    fun verify(idTokenString: String): GoogleIdentity {
        if (!enabled) {
            throw BadRequestException("Google sign-in is not configured")
        }
        val token: GoogleIdToken =
            runCatching { verifier.verify(idTokenString) }.getOrNull()
                ?: throw UnauthorizedException("Invalid Google token")
        val payload = token.payload
        val email = payload.email ?: throw UnauthorizedException("Google token has no email")
        return GoogleIdentity(
            googleId = payload.subject,
            email = email,
            name = (payload["name"] as? String) ?: email.substringBefore('@'),
            emailVerified = payload.emailVerified ?: false,
        )
    }
}
