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
import ro.puk3p.sentinel.account.dto.MfaSetupResponse
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
import ro.puk3p.sentinel.account.security.GoogleAuthService
import ro.puk3p.sentinel.account.security.GoogleIdentity
import ro.puk3p.sentinel.account.security.JwtService
import ro.puk3p.sentinel.account.security.TotpService
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
    private val totpService: TotpService,
    private val googleAuthService: GoogleAuthService,
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
        if (user == null || user.passwordHash.isBlank() ||
            !passwordEncoder.matches(request.password, user.passwordHash)
        ) {
            recordAudit(request.username, "Login attempt", http, "Failed")
            throw UnauthorizedException("Invalid username or password")
        }
        return issueOrChallenge(user, http)
    }

    /** Second login step: validate the MFA challenge + TOTP code, then issue a session. */
    fun loginMfa(
        mfaToken: String,
        code: String,
        http: HttpServletRequest,
    ): LoginResponse {
        val username =
            jwtService.parseMfaChallenge(mfaToken)
                ?: throw UnauthorizedException("Your verification session expired — sign in again")
        val user = require(username)
        val secret = user.mfaSecret
        if (!user.mfaEnabled || secret.isNullOrBlank() || !totpService.verify(secret, code)) {
            recordAudit(username, "2FA verification", http, "Failed")
            throw UnauthorizedException("Invalid verification code")
        }
        return LoginResponse(token = startSession(user, http, "Login"), account = toView(user))
    }

    /** Sign in (or sign up) with a verified Google ID token. */
    fun googleLogin(
        idToken: String,
        http: HttpServletRequest,
    ): LoginResponse {
        val id = googleAuthService.verify(idToken)
        if (!id.emailVerified) {
            throw UnauthorizedException("Your Google email is not verified")
        }
        val user =
            users.findByGoogleId(id.googleId).orElse(null)
                ?: users.findByEmail(id.email).orElse(null)?.also { it.googleId = id.googleId }
                ?: createGoogleAccount(id)
        users.save(user)
        return issueOrChallenge(user, http, "Signed in with Google")
    }

    /** Issue a session token, unless the account requires a second factor. */
    private fun issueOrChallenge(
        user: UserAccount,
        http: HttpServletRequest,
        action: String = "Login",
    ): LoginResponse {
        if (user.mfaEnabled && !user.mfaSecret.isNullOrBlank()) {
            recordAudit(user.username, "Login (2FA required)", http, "Success")
            return LoginResponse(mfaRequired = true, mfaToken = jwtService.issueMfaChallenge(user.username))
        }
        return LoginResponse(token = startSession(user, http, action), account = toView(user))
    }

    private fun startSession(
        user: UserAccount,
        http: HttpServletRequest,
        action: String,
    ): String {
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
        recordAudit(user.username, action, http, "Success")
        return jwtService.issue(user.username, jti)
    }

    private fun createGoogleAccount(id: GoogleIdentity): UserAccount =
        UserAccount(
            username = uniqueUsername(id.email.substringBefore('@')),
            email = id.email,
            fullName = id.name,
            googleId = id.googleId,
            passwordChangedAt = null,
        )

    private fun uniqueUsername(base: String): String {
        val cleaned = base.lowercase().replace(Regex("[^a-z0-9._-]"), "").ifBlank { "operator" }
        if (!users.existsByUsername(cleaned)) {
            return cleaned
        }
        var i = 1
        while (users.existsByUsername("$cleaned$i")) {
            i++
        }
        return "$cleaned$i"
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
        if (!u.mfaEnabled) {
            throw BadRequestException("Enable two-factor authentication before changing your password")
        }
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

    /** Begin 2FA enrollment: mint a secret and return the QR/otpauth for the app. */
    fun mfaSetup(username: String): MfaSetupResponse {
        val u = require(username)
        if (u.mfaEnabled) {
            throw BadRequestException("Two-factor authentication is already enabled")
        }
        val secret = totpService.newSecret()
        u.mfaSecret = secret
        users.save(u)
        return MfaSetupResponse(
            secret = secret,
            otpauthUri = totpService.otpauthUri(username, secret),
            qrDataUri = totpService.qrDataUri(username, secret),
        )
    }

    /** Confirm enrollment by verifying the first code, then switch 2FA on. */
    fun mfaEnable(
        username: String,
        code: String,
        http: HttpServletRequest,
    ): AccountView {
        val u = require(username)
        val secret = u.mfaSecret
        if (secret.isNullOrBlank()) {
            throw BadRequestException("Start 2FA setup first")
        }
        if (!totpService.verify(secret, code)) {
            recordAudit(username, "Enable 2FA", http, "Failed")
            throw BadRequestException("That code is incorrect — try again")
        }
        u.mfaEnabled = true
        recordAudit(username, "Enabled 2FA", http, "Success")
        return toView(users.save(u))
    }

    /** Turn 2FA off after verifying a current code, and forget the secret. */
    fun mfaDisable(
        username: String,
        code: String,
        http: HttpServletRequest,
    ): AccountView {
        val u = require(username)
        val secret = u.mfaSecret
        if (!u.mfaEnabled || secret.isNullOrBlank()) {
            throw BadRequestException("Two-factor authentication is not enabled")
        }
        if (!totpService.verify(secret, code)) {
            recordAudit(username, "Disable 2FA", http, "Failed")
            throw BadRequestException("That code is incorrect — try again")
        }
        u.mfaEnabled = false
        u.mfaSecret = null
        recordAudit(username, "Disabled 2FA", http, "Success")
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
