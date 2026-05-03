package ro.puk3p.sentinel.device.repository

import org.springframework.data.jpa.repository.JpaRepository
import ro.puk3p.sentinel.device.entity.DeviceEntity
import java.util.Optional

interface DeviceRepository : JpaRepository<DeviceEntity, Long> {
    fun findByDeviceId(deviceId: String): Optional<DeviceEntity>

    fun existsByDeviceId(deviceId: String): Boolean
}
