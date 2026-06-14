package ro.puk3p.sentinel.rule.repository

import org.springframework.data.mongodb.repository.MongoRepository
import ro.puk3p.sentinel.rule.entity.RuleEntity
import ro.puk3p.sentinel.rule.model.RuleCategory
import java.util.Optional

interface RuleRepository : MongoRepository<RuleEntity, String> {
    fun findByRuleId(ruleId: String): Optional<RuleEntity>

    fun existsByRuleId(ruleId: String): Boolean

    fun findByCategory(category: RuleCategory): List<RuleEntity>

    fun findByInterfaceScope(interfaceScope: String): List<RuleEntity>

    /** Enabled rules that target a specific router or every router (blank target). */
    fun findByEnabledTrueAndTargetDeviceIdIn(targetDeviceIds: List<String>): List<RuleEntity>

    fun countByEnabledTrue(): Long
}
