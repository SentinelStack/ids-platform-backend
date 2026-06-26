package ro.puk3p.sentinel.client.dto

import jakarta.validation.constraints.NotBlank

data class ClientItem(
    @field:NotBlank(message = "ip is required")
    val ip: String,
    @field:NotBlank(message = "mac is required")
    val mac: String,
    val name: String? = null,
    val online: Boolean = false,
)
