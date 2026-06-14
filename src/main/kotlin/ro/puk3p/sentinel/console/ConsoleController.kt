package ro.puk3p.sentinel.console

import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ro.puk3p.sentinel.alert.controller.AlertController
import ro.puk3p.sentinel.common.response.ApiResponse
import ro.puk3p.sentinel.console.dto.DashboardView
import ro.puk3p.sentinel.console.dto.IncidentForensicsView
import ro.puk3p.sentinel.console.dto.IncidentsView
import ro.puk3p.sentinel.console.dto.RulesConsoleView
import ro.puk3p.sentinel.console.dto.RuntimeLogLine
import ro.puk3p.sentinel.console.dto.TopologyEvent
import ro.puk3p.sentinel.console.dto.TrafficView
import ro.puk3p.sentinel.rule.controller.RuleController

@RestController
@RequestMapping("/api/console")
class ConsoleController(
    private val consoleService: ConsoleService,
) {
    @GetMapping("/incidents")
    fun incidents(): ApiResponse<EntityModel<IncidentsView>> {
        val selfHref = linkTo(methodOn(ConsoleController::class.java).incidents()).toUri().toString()
        val model =
            EntityModel.of(
                consoleService.incidents(),
                linkTo(methodOn(ConsoleController::class.java).incidents()).withSelfRel(),
                linkTo(methodOn(ConsoleController::class.java).dashboard()).withRel("dashboard"),
                linkTo(methodOn(ConsoleController::class.java).traffic()).withRel("traffic"),
                // Templated per-incident links: the alert resource and its forensics view.
                Link.of(alertTemplate()).withRel("alert"),
                Link.of("$selfHref/{alertId}/forensics").withRel("forensics"),
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

    @GetMapping("/rules")
    fun rules(): ApiResponse<EntityModel<RulesConsoleView>> {
        val model =
            EntityModel.of(
                consoleService.rulesConsole(),
                linkTo(methodOn(ConsoleController::class.java).rules()).withSelfRel(),
                linkTo(methodOn(RuleController::class.java).list(null, null, null)).withRel("rules"),
                linkTo(methodOn(ConsoleController::class.java).dashboard()).withRel("dashboard"),
            )
        return ApiResponse(success = true, message = "Rules console", data = model)
    }

    @GetMapping("/topology/events")
    fun topologyEvents(
        @RequestParam(defaultValue = "40") limit: Int,
    ): ApiResponse<CollectionModel<TopologyEvent>> {
        val model =
            CollectionModel.of(
                consoleService.topologyEvents(limit),
                linkTo(methodOn(ConsoleController::class.java).topologyEvents(limit)).withSelfRel(),
                linkTo(methodOn(ConsoleController::class.java).topologyLogs(40)).withRel("topology-logs"),
                linkTo(methodOn(ConsoleController::class.java).dashboard()).withRel("dashboard"),
            )
        return ApiResponse(success = true, message = "Topology events", data = model)
    }

    @GetMapping("/topology/logs")
    fun topologyLogs(
        @RequestParam(defaultValue = "40") limit: Int,
    ): ApiResponse<CollectionModel<RuntimeLogLine>> {
        val model =
            CollectionModel.of(
                consoleService.runtimeLogs(limit),
                linkTo(methodOn(ConsoleController::class.java).topologyLogs(limit)).withSelfRel(),
                linkTo(methodOn(ConsoleController::class.java).topologyEvents(40)).withRel("topology-events"),
            )
        return ApiResponse(success = true, message = "Runtime logs", data = model)
    }

    private fun alertsLink() =
        linkTo(methodOn(AlertController::class.java).getAll(null, null, null, null, null, null, 0, 20, "timestamp", "desc"))
            .withRel("alerts")

    /** Templated link to a single alert resource: …/api/alerts/{alertId}. */
    private fun alertTemplate(): String {
        val base = linkTo(methodOn(AlertController::class.java).getById("X")).toUri().toString()
        return base.removeSuffix("X") + "{alertId}"
    }
}
