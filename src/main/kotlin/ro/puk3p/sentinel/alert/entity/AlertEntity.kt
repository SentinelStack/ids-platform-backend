package ro.puk3p.sentinel.alert.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import ro.puk3p.sentinel.alert.model.AlertType
import ro.puk3p.sentinel.alert.model.Protocol
import ro.puk3p.sentinel.alert.model.Severity
import java.time.Instant

@Document(collection = "alerts")
class AlertEntity(
    @Id
    var id: String? = null,
    @Indexed(unique = true)
    var alertId: String = "",
    @Indexed
    var deviceId: String = "",
    @Indexed
    var timestamp: Instant = Instant.now(),
    var type: AlertType = AlertType.UNKNOWN,
    var severity: Severity = Severity.LOW,
    var protocol: Protocol = Protocol.UNKNOWN,
    var sourceIp: String = "",
    var destinationIp: String = "",
    var sourcePort: Int = 0,
    var destinationPort: Int = 0,
    var packetCount: Long = 0,
    var bytesCount: Long = 0,
    var windowSeconds: Int = 0,
    var description: String? = null,
    var acknowledged: Boolean = false,
    @CreatedDate
    var createdAt: Instant? = null,
)
