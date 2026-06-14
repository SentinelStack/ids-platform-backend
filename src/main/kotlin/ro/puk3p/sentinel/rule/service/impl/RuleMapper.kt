package ro.puk3p.sentinel.rule.service.impl

import ro.puk3p.sentinel.rule.dto.RuleResponse
import ro.puk3p.sentinel.rule.entity.RuleEntity

object RuleMapper {
    fun toResponse(e: RuleEntity): RuleResponse =
        RuleResponse(
            ruleId = e.ruleId,
            name = e.name,
            signal = e.signal,
            category = e.category,
            interfaceScope = e.interfaceScope,
            severity = e.severity,
            mode = e.mode,
            enabled = e.enabled,
            version = e.version,
            targetDeviceId = e.targetDeviceId,
            logic = e.logic,
            actions = e.actions,
            thresholds = e.thresholds,
            cpuImpact = e.cpuImpact,
            memImpact = e.memImpact,
            evalMs = e.evalMs,
            matches = e.matches,
            deployedAt = e.deployedAt,
            updatedAt = e.updatedAt,
        )
}
