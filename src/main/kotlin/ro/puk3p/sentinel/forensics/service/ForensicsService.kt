package ro.puk3p.sentinel.forensics.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ro.puk3p.sentinel.forensics.dto.ForensicsTimelineEntry
import ro.puk3p.sentinel.forensics.dto.PacketSummaryCreateRequest
import ro.puk3p.sentinel.forensics.dto.PacketSummaryResponse
import java.time.Instant

interface ForensicsService {
    fun createPacketSummary(request: PacketSummaryCreateRequest): PacketSummaryResponse

    fun getPackets(pageable: Pageable): Page<PacketSummaryResponse>

    fun getByAlert(
        alertId: String,
        pageable: Pageable,
    ): Page<PacketSummaryResponse>

    fun getTimeline(
        from: Instant?,
        to: Instant?,
        pageable: Pageable,
    ): Page<ForensicsTimelineEntry>
}
