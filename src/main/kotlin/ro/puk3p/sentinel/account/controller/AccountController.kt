package ro.puk3p.sentinel.account.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ro.puk3p.sentinel.account.dto.AccountView
import ro.puk3p.sentinel.account.dto.AuditView
import ro.puk3p.sentinel.account.dto.ChangePasswordRequest
import ro.puk3p.sentinel.account.dto.MfaRequest
import ro.puk3p.sentinel.account.dto.NotificationsRequest
import ro.puk3p.sentinel.account.dto.PreferencesRequest
import ro.puk3p.sentinel.account.dto.ProfileUpdateRequest
import ro.puk3p.sentinel.account.dto.SessionView
import ro.puk3p.sentinel.account.security.SESSION_JTI_ATTR
import ro.puk3p.sentinel.account.service.AccountService
import ro.puk3p.sentinel.common.response.ApiResponse

@RestController
@RequestMapping("/api/account")
class AccountController(
    private val accountService: AccountService,
) {
    private fun currentUser(): String = SecurityContextHolder.getContext().authentication?.name ?: ""

    private fun currentJti(http: HttpServletRequest): String? = http.getAttribute(SESSION_JTI_ATTR) as String?

    @GetMapping
    fun me(): ApiResponse<EntityModel<AccountView>> {
        val model =
            EntityModel.of(
                accountService.me(currentUser()),
                linkTo(methodOn(AccountController::class.java).me()).withSelfRel(),
                linkTo(methodOn(AccountController::class.java).sessions(null)).withRel("sessions"),
                linkTo(methodOn(AccountController::class.java).activity(8)).withRel("activity"),
            )
        return ApiResponse(success = true, message = "Account", data = model)
    }

    @PutMapping("/profile")
    fun updateProfile(
        @RequestBody request: ProfileUpdateRequest,
        http: HttpServletRequest,
    ): ApiResponse<AccountView> =
        ApiResponse(success = true, message = "Profile updated", data = accountService.updateProfile(currentUser(), request, http))

    @PutMapping("/preferences")
    fun updatePreferences(
        @RequestBody request: PreferencesRequest,
    ): ApiResponse<AccountView> =
        ApiResponse(success = true, message = "Preferences updated", data = accountService.updatePreferences(currentUser(), request))

    @PutMapping("/notifications")
    fun updateNotifications(
        @RequestBody request: NotificationsRequest,
    ): ApiResponse<AccountView> =
        ApiResponse(success = true, message = "Notifications updated", data = accountService.updateNotifications(currentUser(), request))

    @PostMapping("/password")
    fun changePassword(
        @Valid @RequestBody request: ChangePasswordRequest,
        http: HttpServletRequest,
    ): ApiResponse<Unit> {
        accountService.changePassword(currentUser(), request, http)
        return ApiResponse(success = true, message = "Password changed", data = Unit)
    }

    @PostMapping("/mfa")
    fun setMfa(
        @RequestBody request: MfaRequest,
        http: HttpServletRequest,
    ): ApiResponse<AccountView> =
        ApiResponse(success = true, message = "MFA updated", data = accountService.setMfa(currentUser(), request.enabled, http))

    @GetMapping("/sessions")
    fun sessions(http: HttpServletRequest?): ApiResponse<CollectionModel<SessionView>> {
        val jti = http?.let { currentJti(it) }
        val model =
            CollectionModel.of(
                accountService.listSessions(currentUser(), jti),
                linkTo(methodOn(AccountController::class.java).sessions(null)).withSelfRel(),
            )
        return ApiResponse(success = true, message = "Sessions", data = model)
    }

    @PostMapping("/sessions/revoke-others")
    fun revokeOthers(http: HttpServletRequest): ApiResponse<Unit> {
        accountService.revokeOtherSessions(currentUser(), currentJti(http), http)
        return ApiResponse(success = true, message = "Other sessions revoked", data = Unit)
    }

    @GetMapping("/activity")
    fun activity(
        @RequestParam(defaultValue = "8") limit: Int,
    ): ApiResponse<CollectionModel<AuditView>> {
        val model =
            CollectionModel.of(
                accountService.activity(currentUser(), limit),
                linkTo(methodOn(AccountController::class.java).activity(limit)).withSelfRel(),
            )
        return ApiResponse(success = true, message = "Activity", data = model)
    }
}
