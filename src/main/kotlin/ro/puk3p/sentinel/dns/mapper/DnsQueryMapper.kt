package ro.puk3p.sentinel.dns.mapper

import ro.puk3p.sentinel.dns.dto.DnsQueryBatchRequest
import ro.puk3p.sentinel.dns.dto.DnsQueryResponse
import ro.puk3p.sentinel.dns.entity.DnsQueryEntity

object DnsQueryMapper {
    fun toEntities(request: DnsQueryBatchRequest): List<DnsQueryEntity> {
        return request.queries.map { item ->
            DnsQueryEntity(
                deviceId = request.deviceId,
                clientIp = item.clientIp,
                domain = item.domain,
                count = item.count,
                timestamp = request.timestamp,
            )
        }
    }

    fun toResponse(entity: DnsQueryEntity): DnsQueryResponse {
        return DnsQueryResponse(
            deviceId = entity.deviceId,
            clientIp = entity.clientIp,
            domain = entity.domain,
            count = entity.count,
            timestamp = entity.timestamp,
            createdAt = entity.createdAt!!,
        )
    }
}
