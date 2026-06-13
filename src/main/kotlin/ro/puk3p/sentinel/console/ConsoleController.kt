package ro.puk3p.sentinel.console

import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ro.puk3p.sentinel.alert.controller.AlertController
import ro.puk3p.sentinel.common.response.ApiResponse
import ro.puk3p.sentinel.console.dto.DashboardView
import ro.puk3p.sentinel.console.dto.IncidentForensicsView
import ro.puk3p.sentinel.console.dto.IncidentsView
import ro.puk3p.sentinel.console.dto.TrafficView

@RestController
@RequestMapping("/api/console")
class ConsoleController(
    private val consoleService: ConsoleService,
) {
    @GetMapping("/incidents")
    fun incidents(): ApiResponse<EntityModel<IncidentsView>> {
        val model =
            EntityModel.of(
                consoleService.incidents(),
                linkTo(methodOn(ConsoleController::class.java).incidents()).withSelfRel(),
                linkTo(methodOn(ConsoleController::class.java).dashboard()).withRel("dashboard"),
                linkTo(methodOn(ConsoleController::class.java).traffic()).withRel("traffic"),
                alertsLink(),
            )
        return ApiResponse(success = true, message = "Incidents view", data = model)
    }

    @GetMapping("/incidents/{alertId}/forensics")
    fun incidentForensics(
        @PathVariable alertId: String,
    ): ApiResponse<EntityModel<IncidentForensicsView>> {
        val model =
            EntityModel.of(
                consoleService.incidentForensics(alertId),
                linkTo(methodOn(ConsoleController::class.java).incidentForensics(alertId)).withSelfRel(),
                linkTo(methodOn(AlertController::class.java).getById(alertId)).withRel("alert"),
                linkTo(methodOn(ConsoleController::class.java).incidents()).withRel("incidents"),
            )
        return ApiResponse(success = true, message = "Incident forensics", data = model)
    }

    @GetMapping("/traffic")
    fun traffic(): ApiResponse<EntityModel<TrafficView>> {
        val model =
            EntityModel.of(
                consoleService.traffic(),
                linkTo(methodOn(ConsoleController::class.java).traffic()).withSelfRel(),
                linkTo(methodOn(ConsoleController::class.java).dashboard()).withRel("dashboard"),
                linkTo(methodOn(ConsoleController::class.java).incidents()).withRel("incidents"),
            )
        return ApiResponse(success = true, message = "Traffic view", data = model)
    }

    @GetMapping("/dashboard")
    fun dashboard(): ApiResponse<EntityModel<DashboardView>> {
        val model =
            EntityModel.of(
                consoleService.dashboard(),
                linkTo(methodOn(ConsoleController::class.java).dashboard()).withSelfRel(),
                linkTo(methodOn(ConsoleController::class.java).traffic()).withRel("traffic"),
                linkTo(methodOn(ConsoleController::class.java).incidents()).withRel("incidents"),
                alertsLink(),
            )
        return ApiResponse(success = true, message = "Dashboard view", data = model)
    }

    private fun alertsLink() =
        linkTo(methodOn(AlertController::class.java).getAll(null, null, null, null, null, null, 0, 20, "timestamp", "desc"))
            .withRel("alerts")
}
