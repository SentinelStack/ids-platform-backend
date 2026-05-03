package ro.puk3p.sentinel.device.mapper

import ro.puk3p.sentinel.device.dto.DeviceResponse
import ro.puk3p.sentinel.device.dto.DeviceStatusResponse
import ro.puk3p.sentinel.device.entity.DeviceEntity

object DeviceMapper {
    fun toResponse(entity: DeviceEntity): DeviceResponse {
        return DeviceResponse(
            deviceId = entity.deviceId,
            name = entity.name,
            ipAddress = entity.ipAddress,
            status = entity.status,
            lastSeenAt = entity.lastSeenAt,
            firmwareVersion = entity.firmwareVersion,
            model = entity.model,
            createdAt = entity.createdAt!!,
            updatedAt = entity.updatedAt!!,
        )
    }

    fun toStatusResponse(entity: DeviceEntity): DeviceStatusResponse {
        return DeviceStatusResponse(
            deviceId = entity.deviceId,
            status = entity.status,
            lastSeenAt = entity.lastSeenAt,
        )
    }
}
