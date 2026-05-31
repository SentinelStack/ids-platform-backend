package ro.puk3p.sentinel.traffic.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "traffic_stats")
class TrafficStatsEntity(
    @Id
    var id: String? = null,
    @Indexed
    var deviceId: String = "",
    @Indexed
    var timestamp: Instant = Instant.now(),
    var totalPackets: Long = 0,
    var tcpPackets: Long = 0,
    var udpPackets: Long = 0,
    var totalBytes: Long = 0,
    var tcpBytes: Long = 0,
    var udpBytes: Long = 0,
    var windowSeconds: Int = 0,
    @CreatedDate
    var createdAt: Instant? = null,
)
