package ro.puk3p.sentinel.account.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import ro.puk3p.sentinel.account.repository.SessionRepository

/** Request attribute holding the authenticated session's JWT id. */
const val SESSION_JTI_ATTR = "sentinel.sessionJti"

/**
 * Authenticates dashboard requests from a `Bearer` JWT. The token's session id
 * (jti) must still exist in the session store, so revoking a session (logout /
 * sign-out-others) immediately invalidates its token.
 */
@Component
class JwtAuthFilter(
    private val jwtService: JwtService,
    private val sessionRepository: SessionRepository,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        if (SecurityContextHolder.getContext().authentication == null) {
            val header = request.getHeader("Authorization")
            if (header != null && header.startsWith("Bearer ")) {
                val claims = jwtService.parse(header.substring(7))
                val jti = claims?.id
                if (claims != null && jti != null && sessionRepository.findByTokenId(jti).isPresent) {
                    val auth =
                        UsernamePasswordAuthenticationToken(
                            claims.subject,
                            null,
                            listOf(SimpleGrantedAuthority("ROLE_USER")),
                        )
                    SecurityContextHolder.getContext().authentication = auth
                    // Expose the session id so controllers can flag the current session.
                    request.setAttribute(SESSION_JTI_ATTR, jti)
                }
            }
        }
        chain.doFilter(request, response)
    }
}

/**
 * Authenticates edge-agent requests by a shared API key, accepted either in the
 * `X-API-Key` header (TLS POSTs) or an `apiKey` query param (the uclient-fetch
 * ruleset GET, which can't set headers).
 */
@Component
class ApiKeyAuthFilter(
    @Value("\${app.agent.api-key}") private val apiKey: String,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        if (SecurityContextHolder.getContext().authentication == null) {
            val provided = request.getHeader("X-API-Key") ?: request.getParameter("apiKey")
            if (provided != null && apiKey.isNotBlank() && provided == apiKey) {
                val auth =
                    UsernamePasswordAuthenticationToken(
                        "edge-agent",
                        null,
                        listOf(SimpleGrantedAuthority("ROLE_AGENT")),
                    )
                SecurityContextHolder.getContext().authentication = auth
            }
        }
        chain.doFilter(request, response)
    }
}
