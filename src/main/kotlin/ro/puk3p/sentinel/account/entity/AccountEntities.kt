package ro.puk3p.sentinel.account.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

/** A platform operator account. Password is stored as a BCrypt hash. */
@Document(collection = "users")
class UserAccount(
    @Id
    var id: String? = null,
    @Indexed(unique = true)
    var username: String = "",
    var email: String = "",
    var fullName: String = "",
    var phone: String = "",
    var role: String = "SOC Analyst",
    var language: String = "English (UK)",
    var timezone: String = "Europe/Bucharest",
    var passwordHash: String = "",
    var mfaEnabled: Boolean = false,
    // Base32 TOTP secret. Set during 2FA setup; null/blank when 2FA is off.
    var mfaSecret: String? = null,
    // Google subject id for accounts linked to "Sign in with Google".
    @Indexed(unique = true, sparse = true)
    var googleId: String? = null,
    var apiAccessEnabled: Boolean = false,
    var sessionTimeoutMinutes: Int = 30,
    // Preferences.
    var theme: String = "Dark",
    var density: String = "Compact",
    var landingPage: String = "Overview",
    var timeFormat: String = "24-hour",
    var autoRefresh: Boolean = true,
    var notificationPrefs: MutableMap<String, Boolean> = defaultNotificationPrefs(),
    var passwordChangedAt: Instant? = null,
    @CreatedDate
    var createdAt: Instant? = null,
    @LastModifiedDate
    var updatedAt: Instant? = null,
) {
    companion object {
        fun defaultNotificationPrefs(): MutableMap<String, Boolean> =
            linkedMapOf(
                "critical" to true,
                "incident" to true,
                "weekly" to true,
                "email" to true,
                "push" to false,
                "maintenance" to true,
            )
    }
}

/** An authenticated session, keyed by the JWT id (jti). Revoking deletes it. */
@Document(collection = "sessions")
class SessionRecord(
    @Id
    var id: String? = null,
    @Indexed(unique = true)
    var tokenId: String = "",
    @Indexed
    var username: String = "",
    var device: String = "",
    var userAgent: String = "",
    var ip: String = "",
    var createdAt: Instant = Instant.now(),
    var lastSeenAt: Instant = Instant.now(),
)

/** Server-side account audit record. */
@Document(collection = "audit_log")
class AuditRecord(
    @Id
    var id: String? = null,
    @Indexed
    var username: String = "",
    var at: Instant = Instant.now(),
    var action: String = "",
    var device: String = "",
    var ip: String = "",
    var status: String = "Success",
)
