package ro.puk3p.sentinel.traffic.assembler

import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.server.RepresentationModelAssembler
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn
import org.springframework.stereotype.Component
import ro.puk3p.sentinel.device.controller.DeviceController
import ro.puk3p.sentinel.traffic.controller.TrafficStatsController
import ro.puk3p.sentinel.traffic.dto.TrafficStatsResponse

@Component
class TrafficStatsModelAssembler : RepresentationModelAssembler<TrafficStatsResponse, EntityModel<TrafficStatsResponse>> {
    override fun toModel(stats: TrafficStatsResponse): EntityModel<TrafficStatsResponse> {
        return EntityModel.of(
            stats,
            linkTo(methodOn(TrafficStatsController::class.java).byDevice(stats.deviceId, 0, 20)).withSelfRel(),
            linkTo(methodOn(DeviceController::class.java).getById(stats.deviceId)).withRel("device"),
            linkTo(methodOn(TrafficStatsController::class.java).latest()).withRel("latest"),
            linkTo(methodOn(TrafficStatsController::class.java).summary()).withRel("summary"),
        )
    }
}
