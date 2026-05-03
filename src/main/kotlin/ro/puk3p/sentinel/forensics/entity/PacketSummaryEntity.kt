package ro.puk3p.sentinel.forensics.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import ro.puk3p.sentinel.alert.model.Protocol
import java.time.Instant

@Entity
@Table(name = "packet_summaries")
class PacketSummaryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false, length = 64)
    var deviceId: String = "",
    @Column(length = 64)
    var alertId: String? = null,
    @Column(nullable = false)
    var timestamp: Instant = Instant.now(),
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    var protocol: Protocol = Protocol.UNKNOWN,
    @Column(nullable = false, length = 64)
    var sourceIp: String = "",
    @Column(nullable = false, length = 64)
    var destinationIp: String = "",
    @Column(nullable = false)
    var sourcePort: Int = 0,
    @Column(nullable = false)
    var destinationPort: Int = 0,
    @Column(nullable = false)
    var packetSize: Long = 0,
    @Column(length = 64)
    var tcpFlags: String? = null,
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
