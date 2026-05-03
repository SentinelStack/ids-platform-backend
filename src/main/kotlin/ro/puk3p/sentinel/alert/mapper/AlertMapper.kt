package ro.puk3p.sentinel.alert.mapper

import ro.puk3p.sentinel.alert.dto.AlertCreateRequest
import ro.puk3p.sentinel.alert.dto.AlertResponse
import ro.puk3p.sentinel.alert.entity.AlertEntity

object AlertMapper {
    fun toEntity(request: AlertCreateRequest): AlertEntity {
        return AlertEntity(
            deviceId = request.deviceId,
            timestamp = request.timestamp,
            type = request.type,
            severity = request.severity,
            protocol = request.protocol,
            sourceIp = request.sourceIp,
            destinationIp = request.destinationIp,
            sourcePort = request.sourcePort,
            destinationPort = request.destinationPort,
            packetCount = request.packetCount,
            bytesCount = request.bytesCount,
            windowSeconds = request.windowSeconds,
            description = request.description,
            acknowledged = false,
        )
    }

    fun toResponse(entity: AlertEntity): AlertResponse {
        return AlertResponse(
            alertId = entity.alertId,
            deviceId = entity.deviceId,
            timestamp = entity.timestamp,
            type = entity.type,
            severity = entity.severity,
            protocol = entity.protocol,
            sourceIp = entity.sourceIp,
            destinationIp = entity.destinationIp,
            sourcePort = entity.sourcePort,
            destinationPort = entity.destinationPort,
            packetCount = entity.packetCount,
            bytesCount = entity.bytesCount,
            windowSeconds = entity.windowSeconds,
            description = entity.description,
            acknowledged = entity.acknowledged,
            createdAt = entity.createdAt!!,
        )
    }
}
