package ro.puk3p.sentinel.account.dto

import jakarta.validation.constraints.NotBlank
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
    val fullName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val language: String? = null,
    val timezone: String? = null,
)

data class PreferencesRequest(
    val theme: String? = null,
    val density: String? = null,
    val landingPage: String? = null,
    val timeFormat: String? = null,
    val autoRefresh: Boolean? = null,
)

data class NotificationsRequest(
    val notifications: Map<String, Boolean> = emptyMap(),
)

data class ChangePasswordRequest(
    @field:NotBlank val currentPassword: String = "",
    @field:NotBlank @field:Size(min = 8) val newPassword: String = "",
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
