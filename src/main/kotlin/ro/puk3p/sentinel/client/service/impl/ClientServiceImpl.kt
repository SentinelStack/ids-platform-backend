package ro.puk3p.sentinel.client.service.impl

import org.springframework.stereotype.Service
import ro.puk3p.sentinel.client.dto.ClientBatchRequest
import ro.puk3p.sentinel.client.entity.ClientEntity
import ro.puk3p.sentinel.client.repository.ClientRepository
import ro.puk3p.sentinel.client.service.ClientService

@Service
class ClientServiceImpl(
    private val clientRepository: ClientRepository,
) : ClientService {
    override fun recordBatch(request: ClientBatchRequest): Int {
        val entities =
            request.clients.map { item ->
                val entity =
                    clientRepository.findByDeviceIdAndMac(request.deviceId, item.mac)
                        .orElseGet { ClientEntity(deviceId = request.deviceId, mac = item.mac) }
                entity.ip = item.ip
                entity.name = item.name
                entity.online = item.online
                entity.lastSeen = request.timestamp
                entity
            }
        return clientRepository.saveAll(entities).size
    }
}
