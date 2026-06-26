package ro.puk3p.sentinel.dns.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "dns_queries")
class DnsQueryEntity(
    @Id
    var id: String? = null,
    @Indexed
    var deviceId: String = "",
    @Indexed
    var clientIp: String = "",
    var domain: String = "",
    var count: Int = 0,
    @Indexed
    var timestamp: Instant = Instant.now(),
    @CreatedDate
    var createdAt: Instant? = null,
)
