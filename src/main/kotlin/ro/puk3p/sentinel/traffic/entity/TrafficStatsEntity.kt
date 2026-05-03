package ro.puk3p.sentinel.traffic.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "traffic_stats")
class TrafficStatsEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false, length = 64)
    var deviceId: String = "",
    @Column(nullable = false)
    var timestamp: Instant = Instant.now(),
    @Column(nullable = false)
    var totalPackets: Long = 0,
    @Column(nullable = false)
    var tcpPackets: Long = 0,
    @Column(nullable = false)
    var udpPackets: Long = 0,
    @Column(nullable = false)
    var totalBytes: Long = 0,
    @Column(nullable = false)
    var tcpBytes: Long = 0,
    @Column(nullable = false)
    var udpBytes: Long = 0,
    @Column(nullable = false)
    var windowSeconds: Int = 0,
    @Column(nullable = false)
    var createdAt: Instant? = null,
) {
    @PrePersist
    fun prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now()
        }
    }
}
