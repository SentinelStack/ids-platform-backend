package ro.puk3p.sentinel.forensics.assembler

import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.server.RepresentationModelAssembler
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn
import org.springframework.stereotype.Component
import ro.puk3p.sentinel.alert.controller.AlertController
import ro.puk3p.sentinel.device.controller.DeviceController
import ro.puk3p.sentinel.forensics.controller.ForensicsController
import ro.puk3p.sentinel.forensics.dto.ForensicsTimelineEntry

@Component
class ForensicsTimelineModelAssembler : RepresentationModelAssembler<ForensicsTimelineEntry, EntityModel<ForensicsTimelineEntry>> {
    override fun toModel(entry: ForensicsTimelineEntry): EntityModel<ForensicsTimelineEntry> {
        val model =
            EntityModel.of(
                entry,
                linkTo(methodOn(ForensicsController::class.java).timeline(null, null, 0, 20)).withSelfRel(),
                linkTo(methodOn(DeviceController::class.java).getById(entry.deviceId)).withRel("device"),
            )

        entry.alertId?.takeIf { it.isNotBlank() }?.let { alertId ->
            model.add(linkTo(methodOn(AlertController::class.java).getById(alertId)).withRel("alert"))
        }

        return model
    }
}
