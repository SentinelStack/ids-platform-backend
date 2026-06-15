package ro.puk3p.sentinel.account.service

import jakarta.servlet.http.HttpServletRequest
import org.springframework.data.domain.PageRequest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import ro.puk3p.sentinel.account.dto.AccountView
import ro.puk3p.sentinel.account.dto.AuditView
import ro.puk3p.sentinel.account.dto.ChangePasswordRequest
import ro.puk3p.sentinel.account.dto.LoginRequest
import ro.puk3p.sentinel.account.dto.LoginResponse
import ro.puk3p.sentinel.account.dto.NotificationsRequest
import ro.puk3p.sentinel.account.dto.PreferencesRequest
import ro.puk3p.sentinel.account.dto.PreferencesView
import ro.puk3p.sentinel.account.dto.ProfileUpdateRequest
import ro.puk3p.sentinel.account.dto.RegisterRequest
import ro.puk3p.sentinel.account.dto.SessionView
import ro.puk3p.sentinel.account.entity.AuditRecord
import ro.puk3p.sentinel.account.entity.SessionRecord
import ro.puk3p.sentinel.account.entity.UserAccount
import ro.puk3p.sentinel.account.repository.AuditRepository
import ro.puk3p.sentinel.account.repository.SessionRepository
import ro.puk3p.sentinel.account.repository.UserAccountRepository
import ro.puk3p.sentinel.account.security.JwtService
import ro.puk3p.sentinel.common.exception.BadRequestException
import ro.puk3p.sentinel.common.exception.ResourceNotFoundException
import ro.puk3p.sentinel.common.exception.UnauthorizedException
import java.time.Instant
import java.util.UUID

@Service
class AccountService(
    private val users: UserAccountRepository,
    private val sessions: SessionRepository,
    private val audit: AuditRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
) {
    // ── Auth ────────────────────────────────────────────────────────────────
    fun register(request: RegisterRequest): AccountView {
        if (users.existsByUsername(request.username)) {
            throw BadRequestException("Username already taken: ${request.username}")
        }
        val user =
            UserAccount(
                username = request.username,
                email = request.email,
                fullName = request.fullName.ifBlank { request.username },
                passwordHash = passwordEncoder.encode(request.password)!!,
                passwordChangedAt = Instant.now(),
            )
        return toView(users.save(user))
    }

    fun login(
        request: LoginRequest,
        http: HttpServletRequest,
    ): LoginResponse {
        val user = users.findByUsername(request.username).orElse(null)
        if (user == null || !passwordEncoder.matches(request.password, user.passwordHash)) {
            recordAudit(request.username, "Login attempt", http, "Failed")
            throw UnauthorizedException("Invalid username or password")
        }
        val jti = UUID.randomUUID().toString()
        sessions.save(
            SessionRecord(
                tokenId = jti,
                username = user.username,
                device = deviceOf(http),
                userAgent = http.getHeader("User-Agent") ?: "",
                ip = ipOf(http),
            ),
        )
        recordAudit(user.username, "Login", http, "Success")
        return LoginResponse(jwtService.issue(user.username, jti), toView(user))
    }

    fun logout(
        jti: String?,
        username: String,
        http: HttpServletRequest,
    ) {
        if (jti != null) {
            sessions.deleteByTokenId(jti)
        }
        recordAudit(username, "Logout", http, "Success")
    }

    // ── Account ───────────────────────────────────────────────────────────
    fun me(username: String): AccountView = toView(require(username))

    fun updateProfile(
        username: String,
        req: ProfileUpdateRequest,
        http: HttpServletRequest,
    ): AccountView {
        val u = require(username)
        req.fullName?.let { u.fullName = it }
        req.email?.let { u.email = it }
        req.phone?.let { u.phone = it }
        req.language?.let { u.language = it }
        req.timezone?.let { u.timezone = it }
        val saved = users.save(u)
        recordAudit(username, "Updated profile", http, "Success")
        return toView(saved)
    }

    fun updatePreferences(
        username: String,
        req: PreferencesRequest,
    ): AccountView {
        val u = require(username)
        req.theme?.let { u.theme = it }
        req.density?.let { u.density = it }
        req.landingPage?.let { u.landingPage = it }
        req.timeFormat?.let { u.timeFormat = it }
        req.autoRefresh?.let { u.autoRefresh = it }
        return toView(users.save(u))
    }

    fun updateNotifications(
        username: String,
        req: NotificationsRequest,
    ): AccountView {
        val u = require(username)
        req.notifications.forEach { (k, v) -> u.notificationPrefs[k] = v }
        return toView(users.save(u))
    }

    fun changePassword(
        username: String,
        req: ChangePasswordRequest,
        http: HttpServletRequest,
    ) {
        val u = require(username)
        if (!passwordEncoder.matches(req.currentPassword, u.passwordHash)) {
            recordAudit(username, "Password change", http, "Failed")
            throw BadRequestException("Current password is incorrect")
        }
        u.passwordHash = passwordEncoder.encode(req.newPassword)!!
        u.passwordChangedAt = Instant.now()
        users.save(u)
        // Force re-login everywhere except the current session.
        recordAudit(username, "Changed password", http, "Success")
    }

    fun setMfa(
        username: String,
        enabled: Boolean,
        http: HttpServletRequest,
    ): AccountView {
        val u = require(username)
        u.mfaEnabled = enabled
        recordAudit(username, if (enabled) "Enabled MFA" else "Disabled MFA", http, "Success")
        return toView(users.save(u))
    }

    // ── Sessions & activity ─────────────────────────────────────────────────
    fun listSessions(
        username: String,
        currentJti: String?,
    ): List<SessionView> =
        sessions.findByUsernameOrderByLastSeenAtDesc(username).map {
            SessionView(it.tokenId, it.device, it.ip, it.createdAt, it.lastSeenAt, it.tokenId == currentJti)
        }

    fun revokeOtherSessions(
        username: String,
        currentJti: String?,
        http: HttpServletRequest,
    ) {
        sessions.deleteByUsernameAndTokenIdNot(username, currentJti ?: "")
        recordAudit(username, "Signed out other sessions", http, "Success")
    }

    fun activity(
        username: String,
        limit: Int,
    ): List<AuditView> =
        audit.findByUsernameOrderByAtDesc(username, PageRequest.of(0, limit.coerceIn(1, 100)))
            .map { AuditView(it.at, it.action, it.device, it.ip, it.status) }

    // ── Helpers ─────────────────────────────────────────────────────────────
    private fun require(username: String): UserAccount =
        users.findByUsername(username).orElseThrow { ResourceNotFoundException("Account not found: $username") }

    private fun recordAudit(
        username: String,
        action: String,
        http: HttpServletRequest,
        status: String,
    ) {
        audit.save(
            AuditRecord(
                username = username,
                action = action,
                device = deviceOf(http),
                ip = ipOf(http),
                status = status,
            ),
        )
    }

    private fun toView(u: UserAccount): AccountView =
        AccountView(
            username = u.username,
            email = u.email,
            fullName = u.fullName,
            phone = u.phone,
            role = u.role,
            language = u.language,
            timezone = u.timezone,
            accountId = "USR-" + (u.id?.takeLast(4)?.uppercase() ?: "0000"),
            mfaEnabled = u.mfaEnabled,
            apiAccessEnabled = u.apiAccessEnabled,
            sessionTimeoutMinutes = u.sessionTimeoutMinutes,
            passwordChangedAt = u.passwordChangedAt,
            preferences = PreferencesView(u.theme, u.density, u.landingPage, u.timeFormat, u.autoRefresh),
            notifications = LinkedHashMap(u.notificationPrefs),
        )

    private fun ipOf(http: HttpServletRequest): String =
        http.getHeader("X-Forwarded-For")?.split(",")?.firstOrNull()?.trim()
            ?: http.remoteAddr ?: "—"

    private fun deviceOf(http: HttpServletRequest): String {
        val ua = http.getHeader("User-Agent") ?: return "Unknown device"
        val os =
            when {
                ua.contains("iPhone") -> "iPhone"
                ua.contains("Android") -> "Android"
                ua.contains("Macintosh") || ua.contains("Mac OS") -> "Mac"
                ua.contains("Windows") -> "Windows"
                ua.contains("Linux") -> "Linux"
                else -> "Device"
            }
        val browser =
            when {
                ua.contains("Edg/") -> "Edge"
                ua.contains("Chrome/") && !ua.contains("Edg/") -> "Chrome"
                ua.contains("Firefox/") -> "Firefox"
                ua.contains("Safari/") && !ua.contains("Chrome/") -> "Safari"
                else -> ""
            }
        return if (browser.isNotEmpty()) "$os · $browser" else os
    }
}
