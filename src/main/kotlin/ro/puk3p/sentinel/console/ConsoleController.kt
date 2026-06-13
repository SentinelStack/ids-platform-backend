package ro.puk3p.sentinel.console

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ro.puk3p.sentinel.common.response.ApiResponse
import ro.puk3p.sentinel.console.dto.DashboardView
import ro.puk3p.sentinel.console.dto.IncidentForensicsView
import ro.puk3p.sentinel.console.dto.IncidentsView
import ro.puk3p.sentinel.console.dto.TrafficView
import java.nio.charset.StandardCharsets
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/api/console")
class ConsoleController(
    private val consoleService: ConsoleService,
) {
    @GetMapping("/incidents")
    fun incidents(): ApiResponse<IncidentsView> =
        ApiResponse(success = true, message = "Incidents view", data = consoleService.incidents())

    @GetMapping("/incidents/{alertId}/forensics")
    fun incidentForensics(
        @PathVariable alertId: String,
    ): ApiResponse<IncidentForensicsView> =
        ApiResponse(success = true, message = "Incident forensics", data = consoleService.incidentForensics(alertId))

    @GetMapping("/incidents/export", produces = ["text/csv"])
    fun exportIncidents(): ResponseEntity<ByteArray> {
        val csv = consoleService.incidentsCsv().toByteArray(StandardCharsets.UTF_8)
        val stamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneOffset.UTC).format(java.time.Instant.now())
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"sentinel-incidents-$stamp.csv\"")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csv)
    }

    @GetMapping("/traffic")
    fun traffic(): ApiResponse<TrafficView> =
        ApiResponse(success = true, message = "Traffic view", data = consoleService.traffic())

    @GetMapping("/dashboard")
    fun dashboard(): ApiResponse<DashboardView> =
        ApiResponse(success = true, message = "Dashboard view", data = consoleService.dashboard())
}
