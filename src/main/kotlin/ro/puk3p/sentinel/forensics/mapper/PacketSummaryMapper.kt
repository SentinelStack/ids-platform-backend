package ro.puk3p.sentinel.forensics.mapper

import ro.puk3p.sentinel.forensics.dto.ForensicsTimelineEntry
import ro.puk3p.sentinel.forensics.dto.PacketSummaryCreateRequest
import ro.puk3p.sentinel.forensics.dto.PacketSummaryResponse
import ro.puk3p.sentinel.forensics.entity.PacketSummaryEntity

object PacketSummaryMapper {
    fun toEntity(request: PacketSummaryCreateRequest): PacketSummaryEntity {
        return PacketSummaryEntity(
            deviceId = request.deviceId,
            alertId = request.alertId,
            timestamp = request.timestamp,
            protocol = request.protocol,
            sourceIp = request.sourceIp,
            destinationIp = request.destinationIp,
            sourcePort = request.sourcePort,
            destinationPort = request.destinationPort,
            packetSize = request.packetSize,
            tcpFlags = request.tcpFlags,
        )
    }

    fun toResponse(entity: PacketSummaryEntity): PacketSummaryResponse {
        return PacketSummaryResponse(
            deviceId = entity.deviceId,
            alertId = entity.alertId,
            timestamp = entity.timestamp,
            protocol = entity.protocol,
            sourceIp = entity.sourceIp,
            destinationIp = entity.destinationIp,
            sourcePort = entity.sourcePort,
            destinationPort = entity.destinationPort,
            packetSize = entity.packetSize,
            tcpFlags = entity.tcpFlags,
            createdAt = entity.createdAt!!,
        )
    }

    fun toTimelineEntry(entity: PacketSummaryEntity): ForensicsTimelineEntry {
        return ForensicsTimelineEntry(
            timestamp = entity.timestamp,
            deviceId = entity.deviceId,
            alertId = entity.alertId,
            protocol = entity.protocol,
            sourceIp = entity.sourceIp,
            destinationIp = entity.destinationIp,
            packetSize = entity.packetSize,
        )
    }
}
