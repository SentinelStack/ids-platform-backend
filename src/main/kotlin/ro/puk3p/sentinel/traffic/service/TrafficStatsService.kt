package ro.puk3p.sentinel.traffic.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ro.puk3p.sentinel.traffic.dto.TrafficStatsCreateRequest
import ro.puk3p.sentinel.traffic.dto.TrafficStatsResponse
import ro.puk3p.sentinel.traffic.dto.TrafficSummaryResponse

interface TrafficStatsService {
    fun create(request: TrafficStatsCreateRequest): TrafficStatsResponse

    fun getLatest(): TrafficStatsResponse

    fun getByDevice(
        deviceId: String,
        pageable: Pageable,
    ): Page<TrafficStatsResponse>

    fun getSummary(): TrafficSummaryResponse
}
