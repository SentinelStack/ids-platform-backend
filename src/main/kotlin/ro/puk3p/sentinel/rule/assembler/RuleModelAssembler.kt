package ro.puk3p.sentinel.rule.assembler

import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.RepresentationModelAssembler
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn
import org.springframework.stereotype.Component
import ro.puk3p.sentinel.rule.controller.RuleController
import ro.puk3p.sentinel.rule.dto.RuleResponse

@Component
class RuleModelAssembler : RepresentationModelAssembler<RuleResponse, EntityModel<RuleResponse>> {
    override fun toModel(rule: RuleResponse): EntityModel<RuleResponse> {
        val c = RuleController::class.java
        val links =
            mutableListOf(
                linkTo(methodOn(c).getById(rule.ruleId)).withSelfRel(),
                linkTo(methodOn(c).list(null, null, null)).withRel("rules"),
                linkTo(methodOn(c).update(rule.ruleId, null)).withRel("edit"),
                linkTo(methodOn(c).deploy(rule.ruleId)).withRel("deploy"),
                linkTo(methodOn(c).delete(rule.ruleId)).withRel("delete"),
            )
        // State-aware: offer the toggle that applies now.
        links +=
            if (rule.enabled) {
                linkTo(methodOn(c).disable(rule.ruleId)).withRel("disable")
            } else {
                linkTo(methodOn(c).enable(rule.ruleId)).withRel("enable")
            }
        // Where the router pulls its effective ruleset from.
        if (rule.targetDeviceId.isNotBlank()) {
            links += Link.of("/api/devices/${rule.targetDeviceId}/ruleset").withRel("ruleset")
        }
        return EntityModel.of(rule, links)
    }
}
