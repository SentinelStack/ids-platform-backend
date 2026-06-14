package ro.puk3p.sentinel.rule.controller

import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ro.puk3p.sentinel.common.response.ApiResponse
import ro.puk3p.sentinel.rule.dto.AgentRuleset
import ro.puk3p.sentinel.rule.service.RuleService

/**
 * Device-facing endpoint. An OpenWrt edge agent pulls its effective ruleset
 * here and applies the thresholds to its on-router anomaly detector — this is
 * the wire between the rules repository and the router.
 */
@RestController
@RequestMapping("/api/devices")
class AgentRulesetController(
    private val ruleService: RuleService,
) {
    @GetMapping("/{deviceId}/ruleset")
    fun ruleset(
        @PathVariable deviceId: String,
    ): ApiResponse<EntityModel<AgentRuleset>> {
        val ruleset = ruleService.agentRuleset(deviceId)
        val model =
            EntityModel.of(
                ruleset,
                linkTo(methodOn(AgentRulesetController::class.java).ruleset(deviceId)).withSelfRel(),
                Link.of("/api/devices/$deviceId").withRel("device"),
                Link.of("/api/rules?deviceId=$deviceId").withRel("rules"),
            )
        return ApiResponse(success = true, message = "Agent ruleset", data = model)
    }
}
