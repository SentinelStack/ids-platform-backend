package ro.puk3p.sentinel.client.mapper

import ro.puk3p.sentinel.client.dto.ClientResponse
import ro.puk3p.sentinel.client.entity.ClientEntity

object ClientMapper {
    fun toResponse(entity: ClientEntity): ClientResponse {
        return ClientResponse(
            deviceId = entity.deviceId,
            ip = entity.ip,
            mac = entity.mac,
            name = entity.name,
            online = entity.online,
            createdAt = entity.createdAt!!,
            lastSeen = entity.lastSeen,
        )
    }
}
