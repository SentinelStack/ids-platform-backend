package ro.puk3p.sentinel.console

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ro.puk3p.sentinel.common.response.ApiResponse
import ro.puk3p.sentinel.console.dto.DashboardView
import ro.puk3p.sentinel.console.dto.IncidentsView
import ro.puk3p.sentinel.console.dto.TrafficView

@RestController
@RequestMapping("/api/console")
class ConsoleController(
    private val consoleService: ConsoleService,
) {
    @GetMapping("/incidents")
    fun incidents(): ApiResponse<IncidentsView> =
        ApiResponse(success = true, message = "Incidents view", data = consoleService.incidents())

    @GetMapping("/traffic")
    fun traffic(): ApiResponse<TrafficView> =
        ApiResponse(success = true, message = "Traffic view", data = consoleService.traffic())

    @GetMapping("/dashboard")
    fun dashboard(): ApiResponse<DashboardView> =
        ApiResponse(success = true, message = "Dashboard view", data = consoleService.dashboard())
}
