package ro.puk3p.sentinel.alert.service.impl

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import ro.puk3p.sentinel.alert.dto.AlertCreateRequest
import ro.puk3p.sentinel.alert.dto.AlertResponse
import ro.puk3p.sentinel.alert.dto.ContainmentResponse
import ro.puk3p.sentinel.alert.entity.AlertEntity
import ro.puk3p.sentinel.alert.mapper.AlertMapper
import ro.puk3p.sentinel.alert.model.AlertFilter
import ro.puk3p.sentinel.alert.repository.AlertRepository
import ro.puk3p.sentinel.alert.service.AlertService
import ro.puk3p.sentinel.common.exception.ResourceNotFoundException
import ro.puk3p.sentinel.containment.entity.ContainmentEntity
import ro.puk3p.sentinel.containment.repository.ContainmentRepository
import java.time.Instant
import java.util.UUID

@Service
class AlertServiceImpl(
    private val alertRepository: AlertRepository,
    private val containmentRepository: ContainmentRepository,
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
        return alertRepository.search(filter, pageable).map(AlertMapper::toResponse)
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
        val entity = requireAlert(alertId)
        entity.acknowledged = true
        return AlertMapper.toResponse(alertRepository.save(entity))
    }

    override fun analysts(): List<String> = ANALYSTS

    /**
     * Assign the incident to an analyst, making it "Investigating". A specific
     * analyst from the roster may be requested; otherwise the least-loaded
     * analyst (fewest currently-open assignments) is chosen automatically.
     */
    override fun assign(
        alertId: String,
        analyst: String?,
    ): AlertResponse {
        val entity = requireAlert(alertId)
        entity.assignee = analyst?.takeIf { it in ANALYSTS } ?: leastLoaded(entity.assignee)
        return AlertMapper.toResponse(alertRepository.save(entity))
    }

    private fun leastLoaded(current: String?): String {
        val candidates = if (current == null) ANALYSTS else ANALYSTS.filter { it != current }
        return candidates.minByOrNull { alertRepository.countByAssigneeAndAcknowledgedFalse(it) }
            ?: ANALYSTS.first()
    }

    /**
     * Contain the attacking source IP: record an active block for it (the rule
     * a full deployment pushes to the edge firewall) and flag the alert.
     * Idempotent — one active containment per source IP.
     */
    override fun contain(alertId: String): ContainmentResponse {
        val entity = requireAlert(alertId)
        val existing = containmentRepository.findFirstBySourceIpAndActiveTrue(entity.sourceIp)
        if (existing != null) {
            if (!entity.contained) {
                entity.contained = true
                entity.containedAt = existing.createdAt ?: Instant.now()
                alertRepository.save(entity)
            }
            return toContainmentResponse(existing, alreadyActive = true)
        }

        val containment =
            ContainmentEntity(
                containmentId = UUID.randomUUID().toString(),
                alertId = entity.alertId,
                sourceIp = entity.sourceIp,
                deviceId = entity.deviceId,
                reason = "Containment for ${entity.type.name} from ${entity.sourceIp}",
                severity = entity.severity,
                active = true,
            )
        val saved = containmentRepository.save(containment)
        entity.contained = true
        entity.containedAt = saved.createdAt ?: Instant.now()
        alertRepository.save(entity)
        return toContainmentResponse(saved, alreadyActive = false)
    }

    private fun requireAlert(alertId: String): AlertEntity =
        alertRepository.findByAlertId(alertId)
            .orElseThrow { ResourceNotFoundException("Alert not found: $alertId") }

    private fun toContainmentResponse(
        c: ContainmentEntity,
        alreadyActive: Boolean,
    ): ContainmentResponse =
        ContainmentResponse(
            containmentId = c.containmentId,
            alertId = c.alertId,
            sourceIp = c.sourceIp,
            deviceId = c.deviceId,
            reason = c.reason,
            severity = c.severity,
            active = c.active,
            createdAt = c.createdAt,
            alreadyActive = alreadyActive,
        )

    companion object {
        private val ANALYSTS = listOf("Ana Popescu", "Mihai Ionescu", "Elena Radu", "Andrei Stoica")
    }
}
