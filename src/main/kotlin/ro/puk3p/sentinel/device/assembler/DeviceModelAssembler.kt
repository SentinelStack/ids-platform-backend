package ro.puk3p.sentinel.device.assembler

import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.RepresentationModelAssembler
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn
import org.springframework.stereotype.Component
import ro.puk3p.sentinel.alert.controller.AlertController
import ro.puk3p.sentinel.device.controller.DeviceController
import ro.puk3p.sentinel.device.dto.DeviceResponse
import ro.puk3p.sentinel.device.model.DeviceStatus
import ro.puk3p.sentinel.traffic.controller.TrafficStatsController

@Component
class DeviceModelAssembler : RepresentationModelAssembler<DeviceResponse, EntityModel<DeviceResponse>> {
    override fun toModel(device: DeviceResponse): EntityModel<DeviceResponse> {
        return EntityModel.of(
            device,
            linkTo(methodOn(DeviceController::class.java).getById(device.deviceId)).withSelfRel(),
            linkTo(methodOn(DeviceController::class.java).getStatus(device.deviceId)).withRel("status"),
            linkTo(methodOn(DeviceController::class.java).heartbeat(device.deviceId, null)).withRel("heartbeat"),
            linkTo(methodOn(AlertController::class.java).getByDevice(device.deviceId, 0, 20)).withRel("alerts"),
            linkTo(methodOn(TrafficStatsController::class.java).byDevice(device.deviceId, 0, 20)).withRel("traffic"),
            linkTo(methodOn(DeviceController::class.java).getDevices(0, 20, "lastSeenAt", "desc")).withRel("devices"),
            // Containment affordance reflects the current state.
            containmentLink(device.deviceId, device.status),
        )
    }

    /** quarantine while open, release while contained. */
    private fun containmentLink(
        deviceId: String,
        status: DeviceStatus,
    ): Link =
        if (status == DeviceStatus.QUARANTINED) {
            linkTo(methodOn(DeviceController::class.java).release(deviceId)).withRel("release")
        } else {
            linkTo(methodOn(DeviceController::class.java).quarantine(deviceId)).withRel("quarantine")
        }
}
