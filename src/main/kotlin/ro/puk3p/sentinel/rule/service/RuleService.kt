package ro.puk3p.sentinel.rule.service

import ro.puk3p.sentinel.rule.dto.AgentRuleset
import ro.puk3p.sentinel.rule.dto.RuleResponse
import ro.puk3p.sentinel.rule.dto.RuleUpsertRequest
import ro.puk3p.sentinel.rule.model.RuleCategory

interface RuleService {
    fun list(
        category: RuleCategory?,
        interfaceScope: String?,
        deviceId: String?,
    ): List<RuleResponse>

    fun get(ruleId: String): RuleResponse

    fun create(request: RuleUpsertRequest): RuleResponse

    fun update(
        ruleId: String,
        request: RuleUpsertRequest,
    ): RuleResponse

    fun setEnabled(
        ruleId: String,
        enabled: Boolean,
    ): RuleResponse

    /** Mark the rule deployed to its target router (records deployedAt). */
    fun deploy(ruleId: String): RuleResponse

    fun delete(ruleId: String)

    /** The effective, agent-facing ruleset a router pulls and applies. */
    fun agentRuleset(deviceId: String): AgentRuleset

    /** Count of enabled rules — drives the "Active Router Rules" KPI. */
    fun enabledCount(): Long

    /** Mean evaluation time across enabled rules (ms). */
    fun avgEvalMs(): Double
}
