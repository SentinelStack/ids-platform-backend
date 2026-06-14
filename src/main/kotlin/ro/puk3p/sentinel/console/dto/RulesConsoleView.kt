package ro.puk3p.sentinel.console.dto

/** KPI strip + live trigger feed for the Rules page. All values are real. */
data class RulesConsoleView(
    val kpis: RulesKpis,
    val triggers: List<RuleTrigger>,
)

data class RulesKpis(
    val activeRouterRules: Long,
    val agentsSynced: String,
    val rulesTriggeredToday: Long,
    val avgEvaluationMs: Double,
    val localMitigations: Long,
)

/** One line in the "Router Rule Trigger Feed" — a real alert seen as a rule match. */
data class RuleTrigger(
    val at: String,
    val ruleId: String,
    val deviceId: String,
    val interfaceScope: String,
    val signal: String,
    val value: String,
    val tone: String,
)
