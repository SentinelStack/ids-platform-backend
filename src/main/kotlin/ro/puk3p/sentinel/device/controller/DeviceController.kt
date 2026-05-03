package ro.puk3p.sentinel.device.controller

import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ro.puk3p.sentinel.common.response.ApiResponse
import ro.puk3p.sentinel.common.response.PagedResponse
import ro.puk3p.sentinel.device.dto.DeviceHeartbeatRequest
import ro.puk3p.sentinel.device.dto.DeviceRegisterRequest
import ro.puk3p.sentinel.device.dto.DeviceResponse
import ro.puk3p.sentinel.device.dto.DeviceStatusResponse
import ro.puk3p.sentinel.device.service.DeviceService

@RestController
@RequestMapping("/api/devices")
class DeviceController(
    private val deviceService: DeviceService,
) {
    @PostMapping("/register")
    fun register(
        @Valid @RequestBody request: DeviceRegisterRequest,
    ): ApiResponse<DeviceResponse> {
        return ApiResponse(success = true, message = "Device registered", data = deviceService.register(request))
    }

    @PostMapping("/{deviceId}/heartbeat")
    fun heartbeat(
        @PathVariable deviceId: String,
        @RequestBody(required = false) request: DeviceHeartbeatRequest?,
    ): ApiResponse<DeviceStatusResponse> {
        return ApiResponse(
            success = true,
            message = "Heartbeat received",
            data = deviceService.heartbeat(deviceId, request ?: DeviceHeartbeatRequest()),
        )
    }

    @GetMapping
    fun getDevices(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "lastSeenAt") sortBy: String,
        @RequestParam(defaultValue = "desc") direction: String,
    ): ApiResponse<PagedResponse<DeviceResponse>> {
        val sort = if (direction.equals("asc", ignoreCase = true)) Sort.by(sortBy).ascending() else Sort.by(sortBy).descending()
        val pageable: Pageable = PageRequest.of(page, size, sort)
        val result = deviceService.getAll(pageable)

        return ApiResponse(
            success = true,
            message = "Devices retrieved",
            data =
                PagedResponse(
                    content = result.content,
                    page = result.number,
                    size = result.size,
                    totalElements = result.totalElements,
                    totalPages = result.totalPages,
                    isLast = result.isLast,
                ),
        )
    }

    @GetMapping("/{deviceId}")
    fun getById(
        @PathVariable deviceId: String,
    ): ApiResponse<DeviceResponse> {
        return ApiResponse(success = true, message = "Device retrieved", data = deviceService.getByDeviceId(deviceId))
    }

    @GetMapping("/{deviceId}/status")
    fun getStatus(
        @PathVariable deviceId: String,
    ): ApiResponse<DeviceStatusResponse> {
        return ApiResponse(success = true, message = "Device status retrieved", data = deviceService.getStatus(deviceId))
    }
}
