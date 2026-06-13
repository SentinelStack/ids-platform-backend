package ro.puk3p.sentinel.containment.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import ro.puk3p.sentinel.alert.model.Severity
import java.time.Instant

/**
 * An active containment (block) action for an attacking source IP — the
 * enforcement record a "Contain Traffic" operation produces. In a full
 * deployment this is what gets pushed to the edge agent's firewall.
 */
@Document(collection = "containments")
class ContainmentEntity(
    @Id
    var id: String? = null,
    @Indexed
    var containmentId: String = "",
    @Indexed
    var alertId: String = "",
    @Indexed
    var sourceIp: String = "",
    var deviceId: String = "",
    var reason: String = "",
    var severity: Severity = Severity.LOW,
    var active: Boolean = true,
    @CreatedDate
    var createdAt: Instant? = null,
)
