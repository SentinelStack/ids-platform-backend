package ro.puk3p.sentinel.alert.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ro.puk3p.sentinel.alert.dto.AlertCreateRequest
import ro.puk3p.sentinel.alert.dto.AlertResponse
import ro.puk3p.sentinel.alert.model.AlertFilter

interface AlertService {
    fun createAlert(request: AlertCreateRequest): AlertResponse

    fun getAlerts(
        filter: AlertFilter,
        pageable: Pageable,
    ): Page<AlertResponse>

    fun getByAlertId(alertId: String): AlertResponse

    fun getLatest(): AlertResponse

    fun getByDevice(
        deviceId: String,
        pageable: Pageable,
    ): Page<AlertResponse>

    fun acknowledge(alertId: String): AlertResponse
}
