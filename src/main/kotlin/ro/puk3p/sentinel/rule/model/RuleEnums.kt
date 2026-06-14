package ro.puk3p.sentinel.rule.model

/** Detection category — also drives the dashboard filter chips. */
enum class RuleCategory {
    DDOS,
    SYN_FLOOD,
    PORT_SCAN,
    DNS,
    OUTBOUND,
    OTHER,
}

/** How aggressively the rule runs on the edge router. */
enum class RuleMode {
    /** Detect only — generate an alert. */
    IDS,

    /** Detect and actively mitigate (rate-limit / drop). */
    IPS,

    /** Both detection and inline prevention. */
    IDS_IPS,
}

/** Local action the router takes when a rule matches. */
enum class RuleAction {
    GENERATE_ALERT,
    PUBLISH_TO_BACKEND,
    INCREASE_RISK_SCORE,
    RATE_LIMIT,
    DROP_TRAFFIC,
}
