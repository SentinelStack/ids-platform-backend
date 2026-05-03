package ro.puk3p.sentinel.device.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import ro.puk3p.sentinel.device.model.DeviceStatus
import java.time.Instant

@Entity
@Table(name = "devices")
class DeviceEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false, unique = true, length = 64)
    var deviceId: String = "",
    @Column(nullable = false, length = 128)
    var name: String = "",
    @Column(nullable = false, length = 64)
    var ipAddress: String = "",
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    var status: DeviceStatus = DeviceStatus.UNKNOWN,
    @Column(nullable = false)
    var lastSeenAt: Instant = Instant.now(),
    @Column(length = 64)
    var firmwareVersion: String? = null,
    @Column(length = 64)
    var model: String? = null,
    @Column(nullable = false)
    var createdAt: Instant? = null,
    @Column(nullable = false)
    var updatedAt: Instant? = null,
) {
    @PrePersist
    fun prePersist() {
        val now = Instant.now()
        createdAt = now
        updatedAt = now
        if (lastSeenAt == Instant.EPOCH) {
            lastSeenAt = now
        }
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = Instant.now()
    }
}
