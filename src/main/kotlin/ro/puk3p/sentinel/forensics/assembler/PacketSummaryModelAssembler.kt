package ro.puk3p.sentinel.forensics.assembler

import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.server.RepresentationModelAssembler
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn
import org.springframework.stereotype.Component
import ro.puk3p.sentinel.alert.controller.AlertController
import ro.puk3p.sentinel.device.controller.DeviceController
import ro.puk3p.sentinel.forensics.controller.ForensicsController
import ro.puk3p.sentinel.forensics.dto.PacketSummaryResponse

@Component
class PacketSummaryModelAssembler : RepresentationModelAssembler<PacketSummaryResponse, EntityModel<PacketSummaryResponse>> {
    override fun toModel(packet: PacketSummaryResponse): EntityModel<PacketSummaryResponse> {
        val model =
            EntityModel.of(
                packet,
                linkTo(methodOn(ForensicsController::class.java).getPackets(0, 20)).withSelfRel(),
                linkTo(methodOn(DeviceController::class.java).getById(packet.deviceId)).withRel("device"),
                linkTo(methodOn(ForensicsController::class.java).timeline(null, null, 0, 20)).withRel("timeline"),
            )

        packet.alertId?.takeIf { it.isNotBlank() }?.let { alertId ->
            model.add(linkTo(methodOn(AlertController::class.java).getById(alertId)).withRel("alert"))
        }

        return model
    }
}
