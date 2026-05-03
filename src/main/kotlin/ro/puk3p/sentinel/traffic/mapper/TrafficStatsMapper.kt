package ro.puk3p.sentinel.traffic.mapper

import ro.puk3p.sentinel.traffic.dto.TrafficStatsCreateRequest
import ro.puk3p.sentinel.traffic.dto.TrafficStatsResponse
import ro.puk3p.sentinel.traffic.entity.TrafficStatsEntity

object TrafficStatsMapper {
    fun toEntity(request: TrafficStatsCreateRequest): TrafficStatsEntity {
        return TrafficStatsEntity(
            deviceId = request.deviceId,
            timestamp = request.timestamp,
            totalPackets = request.totalPackets,
            tcpPackets = request.tcpPackets,
            udpPackets = request.udpPackets,
            totalBytes = request.totalBytes,
            tcpBytes = request.tcpBytes,
            udpBytes = request.udpBytes,
            windowSeconds = request.windowSeconds,
        )
    }

    fun toResponse(entity: TrafficStatsEntity): TrafficStatsResponse {
        return TrafficStatsResponse(
            deviceId = entity.deviceId,
            timestamp = entity.timestamp,
            totalPackets = entity.totalPackets,
            tcpPackets = entity.tcpPackets,
            udpPackets = entity.udpPackets,
            totalBytes = entity.totalBytes,
            tcpBytes = entity.tcpBytes,
            udpBytes = entity.udpBytes,
            windowSeconds = entity.windowSeconds,
            createdAt = entity.createdAt!!,
        )
    }
}
