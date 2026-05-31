package ro.puk3p.sentinel.device.repository

import org.springframework.data.mongodb.repository.MongoRepository
import ro.puk3p.sentinel.device.entity.DeviceEntity
import java.util.Optional

interface DeviceRepository : MongoRepository<DeviceEntity, String> {
    fun findByDeviceId(deviceId: String): Optional<DeviceEntity>

    fun existsByDeviceId(deviceId: String): Boolean
}
