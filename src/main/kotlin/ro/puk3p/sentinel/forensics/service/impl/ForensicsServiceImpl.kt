package ro.puk3p.sentinel.forensics.service.impl

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import ro.puk3p.sentinel.common.exception.BadRequestException
import ro.puk3p.sentinel.forensics.dto.ForensicsTimelineEntry
import ro.puk3p.sentinel.forensics.dto.PacketSummaryCreateRequest
import ro.puk3p.sentinel.forensics.dto.PacketSummaryResponse
import ro.puk3p.sentinel.forensics.mapper.PacketSummaryMapper
import ro.puk3p.sentinel.forensics.repository.PacketSummaryRepository
import ro.puk3p.sentinel.forensics.service.ForensicsService
import java.time.Instant

@Service
class ForensicsServiceImpl(
    private val packetSummaryRepository: PacketSummaryRepository,
) : ForensicsService {
    override fun createPacketSummary(request: PacketSummaryCreateRequest): PacketSummaryResponse {
        return PacketSummaryMapper.toResponse(packetSummaryRepository.save(PacketSummaryMapper.toEntity(request)))
    }

    override fun getPackets(pageable: Pageable): Page<PacketSummaryResponse> {
        return packetSummaryRepository.findAllByOrderByTimestampDesc(pageable).map(PacketSummaryMapper::toResponse)
    }

    override fun getByAlert(
        alertId: String,
        pageable: Pageable,
    ): Page<PacketSummaryResponse> {
        return packetSummaryRepository.findByAlertIdOrderByTimestampDesc(alertId, pageable).map(PacketSummaryMapper::toResponse)
    }

    override fun getTimeline(
        from: Instant?,
        to: Instant?,
        pageable: Pageable,
    ): Page<ForensicsTimelineEntry> {
        if (from != null && to != null && from.isAfter(to)) {
            throw BadRequestException("'from' must be before 'to'")
        }

        val result =
            if (from != null && to != null) {
                packetSummaryRepository.findByTimestampBetweenOrderByTimestampDesc(from, to, pageable)
            } else {
                packetSummaryRepository.findAllByOrderByTimestampDesc(pageable)
            }

        return result.map(PacketSummaryMapper::toTimelineEntry)
    }
}
