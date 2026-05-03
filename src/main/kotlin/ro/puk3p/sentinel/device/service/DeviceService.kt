package ro.puk3p.sentinel.device.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ro.puk3p.sentinel.device.dto.DeviceHeartbeatRequest
import ro.puk3p.sentinel.device.dto.DeviceRegisterRequest
import ro.puk3p.sentinel.device.dto.DeviceResponse
import ro.puk3p.sentinel.device.dto.DeviceStatusResponse

interface DeviceService {
    fun register(request: DeviceRegisterRequest): DeviceResponse

    fun heartbeat(
        deviceId: String,
        request: DeviceHeartbeatRequest,
    ): DeviceStatusResponse

    fun getAll(pageable: Pageable): Page<DeviceResponse>

    fun getByDeviceId(deviceId: String): DeviceResponse

    fun getStatus(deviceId: String): DeviceStatusResponse
}
