package ro.puk3p.sentinel.common.hateoas

import org.springframework.hateoas.Link
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import ro.puk3p.sentinel.alert.controller.AlertController
import ro.puk3p.sentinel.common.response.ApiResponse
import ro.puk3p.sentinel.console.ConsoleController
import ro.puk3p.sentinel.device.controller.DeviceController
import ro.puk3p.sentinel.forensics.controller.ForensicsController
import ro.puk3p.sentinel.rule.controller.RuleController
import ro.puk3p.sentinel.traffic.controller.TrafficStatsController

open class ApiIndexModel : RepresentationModel<ApiIndexModel>()

@RestController
@RequestMapping("/api")
class ApiIndexController {
    @GetMapping
    fun index(): ApiResponse<ApiIndexModel> {
        val model = ApiIndexModel()

        model.add(linkTo(methodOn(ApiIndexController::class.java).index()).withSelfRel())
        model.add(linkTo(methodOn(DeviceController::class.java).getDevices(0, 20, "lastSeenAt", "desc")).withRel("devices"))
        model.add(
            linkTo(methodOn(AlertController::class.java).getAll(null, null, null, null, null, null, 0, 20, "timestamp", "desc"))
                .withRel("alerts"),
        )
        model.add(linkTo(methodOn(AlertController::class.java).getLatest()).withRel("latest-alert"))
        model.add(linkTo(methodOn(AlertController::class.java).analysts()).withRel("analysts"))
        model.add(linkTo(methodOn(TrafficStatsController::class.java).latest()).withRel("latest-traffic"))
        model.add(linkTo(methodOn(TrafficStatsController::class.java).summary()).withRel("traffic-summary"))
        model.add(linkTo(methodOn(ForensicsController::class.java).getPackets(0, 20)).withRel("forensics"))
        model.add(linkTo(methodOn(ForensicsController::class.java).timeline(null, null, 0, 20)).withRel("forensics-timeline"))

        // Dashboard console views.
        model.add(linkTo(methodOn(ConsoleController::class.java).dashboard()).withRel("console-dashboard"))
        model.add(linkTo(methodOn(ConsoleController::class.java).traffic()).withRel("console-traffic"))
        model.add(linkTo(methodOn(ConsoleController::class.java).incidents()).withRel("console-incidents"))
        // Topology live feed: real domain events + the backend's own runtime logs.
        model.add(linkTo(methodOn(ConsoleController::class.java).topologyEvents(40)).withRel("topology-events"))
        model.add(linkTo(methodOn(ConsoleController::class.java).topologyLogs(40)).withRel("topology-logs"))
        // Templated: live detail for a device-backed topology node.
        model.add(Link.of(nodeDetailTemplate()).withRel("topology-node"))
        // Edge detection rules + the rules console view.
        model.add(linkTo(methodOn(RuleController::class.java).list(null, null, null)).withRel("rules"))
        model.add(linkTo(methodOn(ConsoleController::class.java).rules()).withRel("console-rules"))

        // Report-export service (same gateway host, different upstream).
        model.add(Link.of(absolute("/api/reports/meta")).withRel("reports"))
        // 24h threat-volume histogram (ClickHouse-backed, full history).
        model.add(Link.of(absolute("/api/reports/volume")).withRel("threat-volume"))
        model.add(Link.of("/actuator/health").withRel("health"))

        return ApiResponse(success = true, message = "API index", data = model)
    }

    /** Absolute URL on the public host (honours nginx X-Forwarded-*). */
    private fun absolute(path: String): String =
        ServletUriComponentsBuilder.fromCurrentContextPath().path(path).build().toUriString()

    /** RFC 6570 templated link: …/api/console/topology/node/{deviceId}. */
    private fun nodeDetailTemplate(): String {
        val base = linkTo(methodOn(ConsoleController::class.java).topologyNode("X")).toUri().toString()
        return base.removeSuffix("X") + "{deviceId}"
    }
}
