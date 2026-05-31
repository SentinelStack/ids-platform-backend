package ro.puk3p.sentinel.alert.assembler

import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.server.RepresentationModelAssembler
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn
import org.springframework.stereotype.Component
import ro.puk3p.sentinel.alert.controller.AlertController
import ro.puk3p.sentinel.alert.dto.AlertResponse
import ro.puk3p.sentinel.device.controller.DeviceController
import ro.puk3p.sentinel.forensics.controller.ForensicsController

@Component
class AlertModelAssembler : RepresentationModelAssembler<AlertResponse, EntityModel<AlertResponse>> {
    override fun toModel(alert: AlertResponse): EntityModel<AlertResponse> {
        val model =
            EntityModel.of(
                alert,
                linkTo(methodOn(AlertController::class.java).getById(alert.alertId)).withSelfRel(),
                linkTo(methodOn(DeviceController::class.java).getById(alert.deviceId)).withRel("device"),
                linkTo(methodOn(ForensicsController::class.java).getByAlert(alert.alertId, 0, 20)).withRel("forensics"),
                linkTo(methodOn(AlertController::class.java).getAll(null, null, null, null, null, null, 0, 20, "timestamp", "desc"))
                    .withRel("alerts"),
            )

        if (!alert.acknowledged) {
            model.add(linkTo(methodOn(AlertController::class.java).acknowledge(alert.alertId)).withRel("acknowledge"))
        }

        return model
    }
}
