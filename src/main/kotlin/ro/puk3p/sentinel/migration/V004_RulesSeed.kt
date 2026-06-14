package ro.puk3p.sentinel.migration

import io.mongock.api.annotations.ChangeUnit
import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackExecution
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Index
import ro.puk3p.sentinel.alert.model.Severity
import ro.puk3p.sentinel.rule.entity.RuleEntity
import ro.puk3p.sentinel.rule.model.RuleAction
import ro.puk3p.sentinel.rule.model.RuleCategory
import ro.puk3p.sentinel.rule.model.RuleMode
import java.time.Instant

/**
 * Creates the `rules` collection and seeds the default OpenWrt edge detection
 * ruleset. The threshold keys `udpPacketThreshold`, `tcpPacketThreshold` and
 * `byteThreshold` are the ones the agent's anomaly detector reads, so these
 * seeds genuinely drive on-router behaviour once pulled.
 */
@ChangeUnit(id = "004-rules-seed", order = "004", author = "ids-platform")
class V004RulesSeed {
    @Execution
    fun execution(mongoTemplate: MongoTemplate) {
        if (!mongoTemplate.collectionExists("rules")) {
            mongoTemplate.createCollection("rules")
        }
        mongoTemplate.indexOps("rules").apply {
            createIndex(Index().on("ruleId", Sort.Direction.ASC).unique())
            createIndex(Index().on("targetDeviceId", Sort.Direction.ASC))
        }

        if (mongoTemplate.estimatedCount(RuleEntity::class.java) > 0) {
            return
        }

        val now = Instant.now()
        seed().forEach { rule ->
            rule.createdAt = now
            rule.updatedAt = now
            rule.deployedAt = now
            mongoTemplate.save(rule)
        }
    }

    @RollbackExecution
    fun rollback(mongoTemplate: MongoTemplate) {
        mongoTemplate.dropCollection("rules")
    }

    private fun seed(): List<RuleEntity> =
        listOf(
            RuleEntity(
                ruleId = "EDGE-RULE-DDOS-UDP-001",
                name = "DDoS UDP Spike Detection",
                signal = "packet_rate + source_diversity",
                category = RuleCategory.DDOS,
                interfaceScope = "wan",
                severity = Severity.CRITICAL,
                mode = RuleMode.IDS_IPS,
                version = "v1.4",
                targetDeviceId = "edge-router-01",
                logic =
                    listOf(
                        "IF packet_rate > 15000 pkt/s",
                        "AND unique_source_ips > 300",
                        "AND inbound_ratio > 80%",
                        "AND same_target_port_ratio > 70%",
                        "WITHIN 10 seconds",
                        "THEN generate CRITICAL alert.",
                    ),
                actions =
                    listOf(
                        RuleAction.GENERATE_ALERT,
                        RuleAction.PUBLISH_TO_BACKEND,
                        RuleAction.INCREASE_RISK_SCORE,
                        RuleAction.RATE_LIMIT,
                        RuleAction.DROP_TRAFFIC,
                    ),
                thresholds = mapOf("udpPacketThreshold" to 100L),
                evalMs = 3.8,
                matches = 18,
            ),
            RuleEntity(
                ruleId = "EDGE-RULE-SYN-FLOOD-001",
                name = "SYN Flood Detection",
                signal = "syn_ack_ratio",
                category = RuleCategory.SYN_FLOOD,
                interfaceScope = "wan",
                severity = Severity.CRITICAL,
                mode = RuleMode.IPS,
                version = "v1.2",
                targetDeviceId = "edge-router-02",
                logic =
                    listOf(
                        "IF syn_ack_ratio > 3.0",
                        "AND half_open_connections > 500",
                        "WITHIN 5 seconds",
                        "THEN generate CRITICAL alert.",
                    ),
                actions = listOf(RuleAction.GENERATE_ALERT, RuleAction.PUBLISH_TO_BACKEND, RuleAction.RATE_LIMIT),
                thresholds = mapOf("tcpPacketThreshold" to 150L),
                evalMs = 4.1,
                matches = 11,
            ),
            RuleEntity(
                ruleId = "EDGE-RULE-PORTSCAN-002",
                name = "Port Scan Detection",
                signal = "port_distribution",
                category = RuleCategory.PORT_SCAN,
                interfaceScope = "br-lan",
                severity = Severity.HIGH,
                mode = RuleMode.IDS,
                version = "v1.1",
                targetDeviceId = "",
                logic =
                    listOf(
                        "IF unique_dst_ports > 100",
                        "FROM single_source WITHIN 30 seconds",
                        "THEN generate HIGH alert.",
                    ),
                actions = listOf(RuleAction.GENERATE_ALERT, RuleAction.INCREASE_RISK_SCORE),
                thresholds = mapOf("uniquePortsThreshold" to 100L),
                evalMs = 2.6,
                matches = 7,
            ),
            RuleEntity(
                ruleId = "EDGE-RULE-DNS-BURST-003",
                name = "Suspicious DNS Burst",
                signal = "dns_query_rate",
                category = RuleCategory.DNS,
                interfaceScope = "lan",
                severity = Severity.MEDIUM,
                mode = RuleMode.IDS,
                version = "v1.0",
                targetDeviceId = "edge-router-03",
                logic =
                    listOf(
                        "IF dns_query_rate > 800 q/s",
                        "AND nxdomain_ratio > 40%",
                        "THEN generate MEDIUM alert.",
                    ),
                actions = listOf(RuleAction.GENERATE_ALERT, RuleAction.PUBLISH_TO_BACKEND),
                thresholds = mapOf("dnsQueryRateThreshold" to 800L),
                evalMs = 1.9,
                matches = 22,
            ),
            RuleEntity(
                ruleId = "EDGE-RULE-OUTBOUND-001",
                name = "Abnormal Outbound Traffic",
                signal = "outbound_ratio",
                category = RuleCategory.OUTBOUND,
                interfaceScope = "wan",
                severity = Severity.HIGH,
                mode = RuleMode.IDS,
                version = "v1.1",
                targetDeviceId = "",
                logic =
                    listOf(
                        "IF outbound_bytes > 500000 per window",
                        "AND outbound_ratio > 4.0",
                        "THEN generate HIGH alert.",
                    ),
                actions = listOf(RuleAction.GENERATE_ALERT, RuleAction.PUBLISH_TO_BACKEND),
                thresholds = mapOf("byteThreshold" to 500000L),
                evalMs = 3.2,
                matches = 5,
            ),
        )
}
