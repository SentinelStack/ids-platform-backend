package ro.puk3p.sentinel.forensics.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import ro.puk3p.sentinel.alert.model.Protocol
import java.time.Instant

@Document(collection = "packet_summaries")
class PacketSummaryEntity(
    @Id
    var id: String? = null,
    @Indexed
    var deviceId: String = "",
    @Indexed
    var alertId: String? = null,
    @Indexed
    var timestamp: Instant = Instant.now(),
    var protocol: Protocol = Protocol.UNKNOWN,
    var sourceIp: String = "",
    var destinationIp: String = "",
    var sourcePort: Int = 0,
    var destinationPort: Int = 0,
    var packetSize: Long = 0,
    var tcpFlags: String? = null,
    @CreatedDate
    var createdAt: Instant? = null,
)
