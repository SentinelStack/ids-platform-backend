package ro.puk3p.sentinel.device.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import ro.puk3p.sentinel.device.model.DeviceStatus
import java.time.Instant

@Document(collection = "devices")
class DeviceEntity(
    @Id
    var id: String? = null,
    @Indexed(unique = true)
    var deviceId: String = "",
    var name: String = "",
    var ipAddress: String = "",
    var status: DeviceStatus = DeviceStatus.UNKNOWN,
    var lastSeenAt: Instant = Instant.now(),
    var firmwareVersion: String? = null,
    var model: String? = null,
    @CreatedDate
    var createdAt: Instant? = null,
    @LastModifiedDate
    var updatedAt: Instant? = null,
)
