package ro.puk3p.sentinel.common.hateoas

import org.springframework.hateoas.Link
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ro.puk3p.sentinel.alert.controller.AlertController
import ro.puk3p.sentinel.common.response.ApiResponse
import ro.puk3p.sentinel.device.controller.DeviceController
import ro.puk3p.sentinel.forensics.controller.ForensicsController
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
        model.add(linkTo(methodOn(TrafficStatsController::class.java).latest()).withRel("latest-traffic"))
        model.add(linkTo(methodOn(TrafficStatsController::class.java).summary()).withRel("traffic-summary"))
        model.add(linkTo(methodOn(ForensicsController::class.java).getPackets(0, 20)).withRel("forensics"))
        model.add(linkTo(methodOn(ForensicsController::class.java).timeline(null, null, 0, 20)).withRel("forensics-timeline"))
        model.add(Link.of("/actuator/health").withRel("health"))

        return ApiResponse(success = true, message = "API index", data = model)
    }
}
