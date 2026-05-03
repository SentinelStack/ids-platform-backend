package ro.puk3p.sentinel.device.dto

import jakarta.validation.constraints.NotBlank

data class DeviceRegisterRequest(
    val deviceId: String? = null,
    @field:NotBlank(message = "Device name is required")
    val name: String,
    @field:NotBlank(message = "IP address is required")
    val ipAddress: String,
    val firmwareVersion: String? = null,
    val model: String? = null,
)
