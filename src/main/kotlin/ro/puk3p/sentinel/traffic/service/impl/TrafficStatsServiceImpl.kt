package ro.puk3p.sentinel.traffic.service.impl

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import ro.puk3p.sentinel.common.exception.ResourceNotFoundException
import ro.puk3p.sentinel.traffic.dto.TrafficStatsCreateRequest
import ro.puk3p.sentinel.traffic.dto.TrafficStatsResponse
import ro.puk3p.sentinel.traffic.dto.TrafficSummaryResponse
import ro.puk3p.sentinel.traffic.mapper.TrafficStatsMapper
import ro.puk3p.sentinel.traffic.repository.TrafficStatsRepository
import ro.puk3p.sentinel.traffic.service.TrafficStatsService

@Service
class TrafficStatsServiceImpl(
    private val trafficStatsRepository: TrafficStatsRepository,
) : TrafficStatsService {
    override fun create(request: TrafficStatsCreateRequest): TrafficStatsResponse {
        return TrafficStatsMapper.toResponse(trafficStatsRepository.save(TrafficStatsMapper.toEntity(request)))
    }

    override fun getLatest(): TrafficStatsResponse {
        val entity =
            trafficStatsRepository.findTopByOrderByTimestampDesc()
                ?: throw ResourceNotFoundException("No traffic stats available")

        return TrafficStatsMapper.toResponse(entity)
    }

    override fun getByDevice(
        deviceId: String,
        pageable: Pageable,
    ): Page<TrafficStatsResponse> {
        return trafficStatsRepository.findByDeviceIdOrderByTimestampDesc(deviceId, pageable).map(TrafficStatsMapper::toResponse)
    }

    override fun getSummary(): TrafficSummaryResponse {
        val summary = trafficStatsRepository.summarize()

        val totalPackets = summary[0].toLong()
        val tcpPackets = summary[1].toLong()
        val udpPackets = summary[2].toLong()
        val totalBytes = summary[3].toLong()
        val tcpBytes = summary[4].toLong()
        val udpBytes = summary[5].toLong()

        val tcpPercentage = if (totalPackets == 0L) 0.0 else (tcpPackets.toDouble() / totalPackets) * 100
        val udpPercentage = if (totalPackets == 0L) 0.0 else (udpPackets.toDouble() / totalPackets) * 100

        return TrafficSummaryResponse(
            totalPackets = totalPackets,
            tcpPackets = tcpPackets,
            udpPackets = udpPackets,
            totalBytes = totalBytes,
            tcpBytes = tcpBytes,
            udpBytes = udpBytes,
            tcpPercentage = tcpPercentage,
            udpPercentage = udpPercentage,
        )
    }
}
