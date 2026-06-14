package ro.puk3p.sentinel.rule.dto

import jakarta.validation.constraints.NotBlank
import ro.puk3p.sentinel.alert.model.Severity
import ro.puk3p.sentinel.rule.model.RuleAction
import ro.puk3p.sentinel.rule.model.RuleCategory
import ro.puk3p.sentinel.rule.model.RuleMode
import java.time.Instant

/** Full rule view for the details panel. */
data class RuleResponse(
    val ruleId: String,
    val name: String,
    val signal: String,
    val category: RuleCategory,
    val interfaceScope: String,
    val severity: Severity,
    val mode: RuleMode,
    val enabled: Boolean,
    val version: String,
    val targetDeviceId: String,
    val logic: List<String>,
    val actions: List<RuleAction>,
    val thresholds: Map<String, Long>,
    val cpuImpact: String,
    val memImpact: String,
    val evalMs: Double,
    val matches: Long,
    val deployedAt: Instant?,
    val updatedAt: Instant?,
)

/** Create/update payload. */
data class RuleUpsertRequest(
    @field:NotBlank val name: String = "",
    val ruleId: String? = null,
    val signal: String = "",
    val category: RuleCategory = RuleCategory.OTHER,
    val interfaceScope: String = "wan",
    val severity: Severity = Severity.MEDIUM,
    val mode: RuleMode = RuleMode.IDS,
    val enabled: Boolean = true,
    val version: String = "v1.0",
    val targetDeviceId: String = "",
    val logic: List<String> = emptyList(),
    val actions: List<RuleAction> = emptyList(),
    val thresholds: Map<String, Long> = emptyMap(),
    val cpuImpact: String = "LOW",
    val memImpact: String = "LOW",
    val evalMs: Double = 0.0,
)

/**
 * The compact, agent-facing ruleset a router pulls from
 * GET /api/devices/{deviceId}/ruleset and applies to its anomaly detector.
 */
data class AgentRuleset(
    val deviceId: String,
    val version: String,
    val generatedAt: Instant,
    /** Merged tunables the detector reads directly (e.g. udpPacketThreshold). */
    val thresholds: Map<String, Long>,
    val rules: List<AgentRule>,
)

data class AgentRule(
    val ruleId: String,
    val type: String,
    val mode: RuleMode,
    val severity: Severity,
    val interfaceScope: String,
    val thresholds: Map<String, Long>,
)
