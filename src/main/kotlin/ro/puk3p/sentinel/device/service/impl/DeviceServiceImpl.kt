package ro.puk3p.sentinel.device.service.impl

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import ro.puk3p.sentinel.common.exception.ResourceNotFoundException
import ro.puk3p.sentinel.device.dto.DeviceHeartbeatRequest
import ro.puk3p.sentinel.device.dto.DeviceRegisterRequest
import ro.puk3p.sentinel.device.dto.DeviceResponse
import ro.puk3p.sentinel.device.dto.DeviceStatusResponse
import ro.puk3p.sentinel.device.entity.DeviceEntity
import ro.puk3p.sentinel.device.mapper.DeviceMapper
import ro.puk3p.sentinel.device.model.DeviceStatus
import ro.puk3p.sentinel.device.repository.DeviceRepository
import ro.puk3p.sentinel.device.service.DeviceService
import java.time.Instant
import java.util.UUID

@Service
class DeviceServiceImpl(
    private val deviceRepository: DeviceRepository,
) : DeviceService {
    override fun register(request: DeviceRegisterRequest): DeviceResponse {
        val publicDeviceId = request.deviceId?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()

        val existing = deviceRepository.findByDeviceId(publicDeviceId).orElse(null)
        val entity =
            if (existing == null) {
                DeviceEntity(
                    deviceId = publicDeviceId,
                    name = request.name,
                    ipAddress = request.ipAddress,
                    status = DeviceStatus.ONLINE,
                    lastSeenAt = Instant.now(),
                    firmwareVersion = request.firmwareVersion,
                    model = request.model,
                )
            } else {
                existing.name = request.name
                existing.ipAddress = request.ipAddress
                existing.firmwareVersion = request.firmwareVersion
                existing.model = request.model
                // Re-enrolment does not lift containment — a quarantined device
                // stays isolated until an operator explicitly releases it.
                if (existing.status != DeviceStatus.QUARANTINED) {
                    existing.status = DeviceStatus.ONLINE
                }
                existing.lastSeenAt = Instant.now()
                existing
            }

        return DeviceMapper.toResponse(deviceRepository.save(entity))
    }

    override fun heartbeat(
        deviceId: String,
        request: DeviceHeartbeatRequest,
    ): DeviceStatusResponse {
        val entity =
            deviceRepository.findByDeviceId(deviceId)
                .orElseThrow { ResourceNotFoundException("Device not found: $deviceId") }

        entity.lastSeenAt = request.seenAt ?: Instant.now()
        // Containment is sticky: a quarantined device records its heartbeat but
        // is NOT brought back ONLINE. Only an explicit release lifts isolation.
        if (entity.status != DeviceStatus.QUARANTINED) {
            entity.status = DeviceStatus.ONLINE
        }

        return DeviceMapper.toStatusResponse(deviceRepository.save(entity))
    }

    override fun quarantine(deviceId: String): DeviceStatusResponse {
        val entity =
            deviceRepository.findByDeviceId(deviceId)
                .orElseThrow { ResourceNotFoundException("Device not found: $deviceId") }

        if (entity.status != DeviceStatus.QUARANTINED) {
            entity.status = DeviceStatus.QUARANTINED
            entity.quarantinedAt = Instant.now()
        }

        return DeviceMapper.toStatusResponse(deviceRepository.save(entity))
    }

    override fun release(deviceId: String): DeviceStatusResponse {
        val entity =
            deviceRepository.findByDeviceId(deviceId)
                .orElseThrow { ResourceNotFoundException("Device not found: $deviceId") }

        if (entity.status == DeviceStatus.QUARANTINED) {
            // Drop to OFFLINE; the next heartbeat restores ONLINE normally.
            entity.status = DeviceStatus.OFFLINE
            entity.quarantinedAt = null
        }

        return DeviceMapper.toStatusResponse(deviceRepository.save(entity))
    }

    override fun getAll(pageable: Pageable): Page<DeviceResponse> {
        return deviceRepository.findAll(pageable).map(DeviceMapper::toResponse)
    }

    override fun getByDeviceId(deviceId: String): DeviceResponse {
        val entity =
            deviceRepository.findByDeviceId(deviceId)
                .orElseThrow { ResourceNotFoundException("Device not found: $deviceId") }

        return DeviceMapper.toResponse(entity)
    }

    override fun getStatus(deviceId: String): DeviceStatusResponse {
        val entity =
            deviceRepository.findByDeviceId(deviceId)
                .orElseThrow { ResourceNotFoundException("Device not found: $deviceId") }

        return DeviceMapper.toStatusResponse(entity)
    }
}
