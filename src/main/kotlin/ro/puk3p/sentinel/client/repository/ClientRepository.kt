package ro.puk3p.sentinel.client.repository

import org.springframework.data.mongodb.repository.MongoRepository
import ro.puk3p.sentinel.client.entity.ClientEntity
import java.util.Optional

interface ClientRepository : MongoRepository<ClientEntity, String> {
    fun findByDeviceIdAndMac(
        deviceId: String,
        mac: String,
    ): Optional<ClientEntity>

    fun findAllByOrderByLastSeenDesc(): List<ClientEntity>
}
