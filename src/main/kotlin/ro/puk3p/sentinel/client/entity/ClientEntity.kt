package ro.puk3p.sentinel.client.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "clients")
class ClientEntity(
    @Id
    var id: String? = null,
    @Indexed
    var deviceId: String = "",
    @Indexed
    var mac: String = "",
    var ip: String = "",
    var name: String? = null,
    var online: Boolean = false,
    @CreatedDate
    var createdAt: Instant? = null,
    var lastSeen: Instant = Instant.now(),
)
