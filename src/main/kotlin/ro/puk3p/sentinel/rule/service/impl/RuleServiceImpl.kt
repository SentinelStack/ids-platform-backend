package ro.puk3p.sentinel.rule.service.impl

import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import ro.puk3p.sentinel.common.exception.ResourceNotFoundException
import ro.puk3p.sentinel.rule.dto.AgentRule
import ro.puk3p.sentinel.rule.dto.AgentRuleset
import ro.puk3p.sentinel.rule.dto.RuleResponse
import ro.puk3p.sentinel.rule.dto.RuleUpsertRequest
import ro.puk3p.sentinel.rule.entity.RuleEntity
import ro.puk3p.sentinel.rule.model.RuleCategory
import ro.puk3p.sentinel.rule.repository.RuleRepository
import ro.puk3p.sentinel.rule.service.RuleService
import java.time.Instant
import java.util.Locale

@Service
class RuleServiceImpl(
    private val ruleRepository: RuleRepository,
) : RuleService {
    override fun list(
        category: RuleCategory?,
        interfaceScope: String?,
        deviceId: String?,
    ): List<RuleResponse> =
        ruleRepository.findAll(Sort.by(Sort.Direction.DESC, "matches"))
            .asSequence()
            .filter { category == null || it.category == category }
            .filter { interfaceScope.isNullOrBlank() || it.interfaceScope.equals(interfaceScope, ignoreCase = true) }
            .filter { deviceId.isNullOrBlank() || it.targetDeviceId == deviceId || it.targetDeviceId.isBlank() }
            .map(RuleMapper::toResponse)
            .toList()

    override fun get(ruleId: String): RuleResponse = RuleMapper.toResponse(find(ruleId))

    override fun create(request: RuleUpsertRequest): RuleResponse {
        val ruleId = request.ruleId?.takeIf { it.isNotBlank() } ?: generateRuleId(request.category)
        if (ruleRepository.existsByRuleId(ruleId)) {
            throw IllegalArgumentException("Rule already exists: $ruleId")
        }
        val entity =
            RuleEntity(ruleId = ruleId).also { apply(it, request) }
        return RuleMapper.toResponse(ruleRepository.save(entity))
    }

    override fun update(
        ruleId: String,
        request: RuleUpsertRequest,
    ): RuleResponse {
        val entity = find(ruleId).also { apply(it, request) }
        return RuleMapper.toResponse(ruleRepository.save(entity))
    }

    override fun setEnabled(
        ruleId: String,
        enabled: Boolean,
    ): RuleResponse {
        val entity = find(ruleId)
        entity.enabled = enabled
        return RuleMapper.toResponse(ruleRepository.save(entity))
    }

    override fun deploy(ruleId: String): RuleResponse {
        val entity = find(ruleId)
        entity.deployedAt = Instant.now()
        // Deploying implies the rule is live on the router.
        entity.enabled = true
        return RuleMapper.toResponse(ruleRepository.save(entity))
    }

    override fun delete(ruleId: String) {
        val entity = find(ruleId)
        ruleRepository.delete(entity)
    }

    override fun agentRuleset(deviceId: String): AgentRuleset {
        // Rules targeting this router plus global rules (blank target).
        val rules = ruleRepository.findByEnabledTrueAndTargetDeviceIdIn(listOf(deviceId, ""))
        val merged = LinkedHashMap<String, Long>()
        rules.forEach { r ->
            r.thresholds.forEach { (k, v) ->
                // Most aggressive value wins on collision (lower threshold = fires sooner).
                merged.merge(k, v) { a, b -> minOf(a, b) }
            }
        }
        return AgentRuleset(
            deviceId = deviceId,
            version = rulesetVersion(rules),
            generatedAt = Instant.now(),
            thresholds = merged,
            rules =
                rules.map {
                    AgentRule(
                        ruleId = it.ruleId,
                        type = it.category.name,
                        mode = it.mode,
                        severity = it.severity,
                        interfaceScope = it.interfaceScope,
                        thresholds = it.thresholds,
                    )
                },
        )
    }

    override fun enabledCount(): Long = ruleRepository.countByEnabledTrue()

    override fun avgEvalMs(): Double {
        val enabled = ruleRepository.findAll().filter { it.enabled }
        if (enabled.isEmpty()) return 0.0
        return enabled.sumOf { it.evalMs } / enabled.size
    }

    private fun find(ruleId: String): RuleEntity =
        ruleRepository.findByRuleId(ruleId)
            .orElseThrow { ResourceNotFoundException("Rule not found: $ruleId") }

    private fun apply(
        e: RuleEntity,
        r: RuleUpsertRequest,
    ) {
        e.name = r.name
        e.signal = r.signal
        e.category = r.category
        e.interfaceScope = r.interfaceScope
        e.severity = r.severity
        e.mode = r.mode
        e.enabled = r.enabled
        e.version = r.version
        e.targetDeviceId = r.targetDeviceId
        e.logic = r.logic
        e.actions = r.actions
        e.thresholds = r.thresholds
        e.cpuImpact = r.cpuImpact
        e.memImpact = r.memImpact
        e.evalMs = r.evalMs
    }

    /** A small content signature so the agent can tell when its ruleset changed. */
    private fun rulesetVersion(rules: List<RuleEntity>): String {
        if (rules.isEmpty()) return "empty"
        val sig = rules.sortedBy { it.ruleId }.joinToString("|") { "${it.ruleId}:${it.version}:${it.enabled}" }
        return "rs-" + Integer.toHexString(sig.hashCode())
    }

    private fun generateRuleId(category: RuleCategory): String {
        val suffix = Integer.toHexString(Instant.now().nano).uppercase(Locale.ROOT).takeLast(4)
        return "EDGE-RULE-${category.name}-$suffix"
    }
}
