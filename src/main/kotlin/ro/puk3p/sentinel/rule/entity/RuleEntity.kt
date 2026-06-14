package ro.puk3p.sentinel.rule.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import ro.puk3p.sentinel.alert.model.Severity
import ro.puk3p.sentinel.rule.model.RuleAction
import ro.puk3p.sentinel.rule.model.RuleCategory
import ro.puk3p.sentinel.rule.model.RuleMode
import java.time.Instant

/**
 * An edge detection rule executed directly on an OpenWrt router. The
 * `thresholds` map carries the tunable values the agent's anomaly detector
 * reads (e.g. udpPacketThreshold) — editing one here changes what the router
 * does once it next pulls its ruleset.
 */
@Document(collection = "rules")
class RuleEntity(
    @Id
    var id: String? = null,
    @Indexed(unique = true)
    var ruleId: String = "",
    var name: String = "",
    /** Short expression label shown under the name, e.g. "packet_rate + source_diversity". */
    var signal: String = "",
    var category: RuleCategory = RuleCategory.OTHER,
    /** Interface the rule binds to: wan, br-lan, lan, eth0, wlan0. */
    var interfaceScope: String = "wan",
    var severity: Severity = Severity.MEDIUM,
    var mode: RuleMode = RuleMode.IDS,
    var enabled: Boolean = true,
    var version: String = "v1.0",
    /** Router this rule targets; blank = applies to every edge agent. */
    @Indexed
    var targetDeviceId: String = "",
    /** Human-readable condition lines rendered in the Rule Logic panel. */
    var logic: List<String> = emptyList(),
    var actions: List<RuleAction> = emptyList(),
    /** Tunable detector values the agent applies (key -> value). */
    var thresholds: Map<String, Long> = emptyMap(),
    var cpuImpact: String = "LOW",
    var memImpact: String = "LOW",
    var evalMs: Double = 0.0,
    /** Lifetime match count, bumped as triggers arrive. */
    var matches: Long = 0,
    var deployedAt: Instant? = null,
    @CreatedDate
    var createdAt: Instant? = null,
    @LastModifiedDate
    var updatedAt: Instant? = null,
)
