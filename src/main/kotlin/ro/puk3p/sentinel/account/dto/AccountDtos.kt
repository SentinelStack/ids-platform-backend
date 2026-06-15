package ro.puk3p.sentinel.account.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.Instant

data class LoginRequest(
    @field:NotBlank val username: String = "",
    @field:NotBlank val password: String = "",
)

data class LoginResponse(
    val token: String,
    val account: AccountView,
)

data class RegisterRequest(
    @field:NotBlank val username: String = "",
    @field:NotBlank @field:Size(min = 8) val password: String = "",
    val email: String = "",
    val fullName: String = "",
)

data class AccountView(
    val username: String,
    val email: String,
    val fullName: String,
    val phone: String,
    val role: String,
    val language: String,
    val timezone: String,
    val accountId: String,
    val mfaEnabled: Boolean,
    val apiAccessEnabled: Boolean,
    val sessionTimeoutMinutes: Int,
    val passwordChangedAt: Instant?,
    val preferences: PreferencesView,
    val notifications: Map<String, Boolean>,
)

data class PreferencesView(
    val theme: String,
    val density: String,
    val landingPage: String,
    val timeFormat: String,
    val autoRefresh: Boolean,
)

data class ProfileUpdateRequest(
    @field:Size(min = 2, max = 80, message = "Full name must be 2–80 characters")
    val fullName: String? = null,
    @field:Email(message = "Enter a valid email address")
    @field:Size(max = 120, message = "Email is too long")
    val email: String? = null,
    @field:Pattern(
        regexp = "^$|^[+]?[0-9 ()-]{7,20}\$",
        message = "Enter a valid phone number (7–20 digits)",
    )
    val phone: String? = null,
    @field:Size(max = 40, message = "Language is too long")
    val language: String? = null,
    @field:Size(max = 60, message = "Timezone is too long")
    val timezone: String? = null,
)

data class PreferencesRequest(
    @field:Size(max = 20, message = "Invalid theme")
    val theme: String? = null,
    @field:Size(max = 20, message = "Invalid density")
    val density: String? = null,
    @field:Size(max = 30, message = "Invalid landing page")
    val landingPage: String? = null,
    @field:Size(max = 20, message = "Invalid time format")
    val timeFormat: String? = null,
    val autoRefresh: Boolean? = null,
)

data class NotificationsRequest(
    val notifications: Map<String, Boolean> = emptyMap(),
)

data class ChangePasswordRequest(
    @field:NotBlank(message = "Current password is required")
    val currentPassword: String = "",
    @field:NotBlank(message = "New password is required")
    @field:Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,128}\$",
        message = "Password must be 8–128 characters with at least one letter and one number",
    )
    val newPassword: String = "",
)

data class MfaRequest(
    val enabled: Boolean = false,
)

data class SessionView(
    val id: String,
    val device: String,
    val ip: String,
    val createdAt: Instant,
    val lastSeenAt: Instant,
    val current: Boolean,
)

data class AuditView(
    val at: Instant,
    val action: String,
    val device: String,
    val ip: String,
    val status: String,
)
