package ro.puk3p.sentinel.alert.service.impl

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import ro.puk3p.sentinel.alert.dto.AlertCreateRequest
import ro.puk3p.sentinel.alert.dto.AlertResponse
import ro.puk3p.sentinel.alert.mapper.AlertMapper
import ro.puk3p.sentinel.alert.model.AlertFilter
import ro.puk3p.sentinel.alert.repository.AlertRepository
import ro.puk3p.sentinel.alert.repository.AlertSpecifications
import ro.puk3p.sentinel.alert.service.AlertService
import ro.puk3p.sentinel.common.exception.ResourceNotFoundException
import java.util.UUID

@Service
class AlertServiceImpl(
    private val alertRepository: AlertRepository,
) : AlertService {
    override fun createAlert(request: AlertCreateRequest): AlertResponse {
        val entity = AlertMapper.toEntity(request)
        entity.alertId = UUID.randomUUID().toString()

        return AlertMapper.toResponse(alertRepository.save(entity))
    }

    override fun getAlerts(
        filter: AlertFilter,
        pageable: Pageable,
    ): Page<AlertResponse> {
        val specification = AlertSpecifications.withFilter(filter)
        return alertRepository.findAll(specification, pageable).map(AlertMapper::toResponse)
    }

    override fun getByAlertId(alertId: String): AlertResponse {
        val entity =
            alertRepository.findByAlertId(alertId)
                .orElseThrow { ResourceNotFoundException("Alert not found: $alertId") }

        return AlertMapper.toResponse(entity)
    }

    override fun getLatest(): AlertResponse {
        val entity =
            alertRepository.findTopByOrderByTimestampDesc()
                ?: throw ResourceNotFoundException("No alerts available")

        return AlertMapper.toResponse(entity)
    }

    override fun getByDevice(
        deviceId: String,
        pageable: Pageable,
    ): Page<AlertResponse> {
        return alertRepository.findByDeviceIdOrderByTimestampDesc(deviceId, pageable).map(AlertMapper::toResponse)
    }

    override fun acknowledge(alertId: String): AlertResponse {
        val entity =
            alertRepository.findByAlertId(alertId)
                .orElseThrow { ResourceNotFoundException("Alert not found: $alertId") }

        entity.acknowledged = true
        return AlertMapper.toResponse(alertRepository.save(entity))
    }
}
