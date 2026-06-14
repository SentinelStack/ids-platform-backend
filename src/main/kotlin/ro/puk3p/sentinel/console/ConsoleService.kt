package ro.puk3p.sentinel.console

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import ro.puk3p.sentinel.alert.entity.AlertEntity
import ro.puk3p.sentinel.alert.model.AlertType
import ro.puk3p.sentinel.alert.model.Severity
import ro.puk3p.sentinel.alert.repository.AlertRepository
import ro.puk3p.sentinel.common.exception.ResourceNotFoundException
import ro.puk3p.sentinel.console.dto.AffectedAsset
import ro.puk3p.sentinel.console.dto.DashboardKpis
import ro.puk3p.sentinel.console.dto.DashboardView
import ro.puk3p.sentinel.console.dto.DistBar
import ro.puk3p.sentinel.console.dto.FlowPoint
import ro.puk3p.sentinel.console.dto.ForensicPacket
import ro.puk3p.sentinel.console.dto.ForensicStat
import ro.puk3p.sentinel.console.dto.IncidentForensicsView
import ro.puk3p.sentinel.console.dto.IncidentKpis
import ro.puk3p.sentinel.console.dto.IncidentRow
import ro.puk3p.sentinel.console.dto.IncidentsView
import ro.puk3p.sentinel.console.dto.LiveAlert
import ro.puk3p.sentinel.console.dto.Origin
import ro.puk3p.sentinel.console.dto.PacketRow
import ro.puk3p.sentinel.console.dto.ProtocolShare
import ro.puk3p.sentinel.console.dto.RuleTrigger
import ro.puk3p.sentinel.console.dto.RulesConsoleView
import ro.puk3p.sentinel.console.dto.RulesKpis
import ro.puk3p.sentinel.console.dto.SeverityDistribution
import ro.puk3p.sentinel.console.dto.ThreatArc
import ro.puk3p.sentinel.console.dto.TimelineBar
import ro.puk3p.sentinel.console.dto.TopPort
import ro.puk3p.sentinel.console.dto.TopSource
import ro.puk3p.sentinel.console.dto.TopologyEvent
import ro.puk3p.sentinel.console.dto.TrafficView
import ro.puk3p.sentinel.console.dto.RuntimeLogLine
import ro.puk3p.sentinel.console.dto.VolumeBar
import ro.puk3p.sentinel.console.log.RuntimeLogBuffer
import ro.puk3p.sentinel.device.model.DeviceStatus
import ro.puk3p.sentinel.device.repository.DeviceRepository
import ro.puk3p.sentinel.forensics.repository.PacketSummaryRepository
import ro.puk3p.sentinel.rule.service.RuleService
import ro.puk3p.sentinel.traffic.repository.TrafficStatsRepository
import ro.puk3p.sentinel.traffic.service.TrafficStatsService
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

@Service
class ConsoleService(
    private val alertRepository: AlertRepository,
    private val trafficStatsRepository: TrafficStatsRepository,
    private val trafficStatsService: TrafficStatsService,
    private val packetSummaryRepository: PacketSummaryRepository,
    private val deviceRepository: DeviceRepository,
    private val ruleService: RuleService,
    private val geo: GeoLookup,
) {
    // ── Incidents ──────────────────────────────────────────────────────────
    fun incidents(): IncidentsView {
        val alerts = recentAlerts()
        return IncidentsView(
            kpis = incidentKpis(alerts),
            timeline = severityTimeline(alerts),
            queue = alerts.map(::toIncidentRow),
            severity = severityDistribution(alerts),
            assets = topAssets(alerts),
        )
    }

    private fun incidentKpis(alerts: List<AlertEntity>): IncidentKpis {
        val lastHour = Instant.now().minus(Duration.ofHours(1))
        val recent = alerts.count { (it.createdAt ?: it.timestamp).isAfter(lastHour) }
        return IncidentKpis(
            open = alerts.count { !it.acknowledged },
            openDelta = "+$recent / 1h",
            critical = alerts.count { it.severity == Severity.CRITICAL },
            investigating = alerts.count { isInvestigating(it) },
            resolved = alerts.count { it.acknowledged },
            uniqueSources = alerts.mapNotNull { it.sourceIp.ifBlank { null } }.toSet().size,
        )
    }

    private fun severityTimeline(alerts: List<AlertEntity>): List<TimelineBar> {
        val buckets = 12
        val crit = IntArray(buckets)
        val warn = IntArray(buckets)
        val total = IntArray(buckets)
        val now = Instant.now().toEpochMilli()
        val span = Duration.ofHours(24).toMillis()
        for (a in alerts) {
            val ago = now - a.timestamp.toEpochMilli()
            if (ago < 0 || ago > span) continue
            val idx = (buckets - 1 - ((ago.toDouble() / span) * buckets).toInt()).coerceIn(0, buckets - 1)
            total[idx]++
            when (a.severity) {
                Severity.CRITICAL -> crit[idx]++
                Severity.HIGH, Severity.MEDIUM -> warn[idx]++
                else -> {}
            }
        }
        val max = (total.maxOrNull() ?: 0).coerceAtLeast(1)
        return (0 until buckets).map { i ->
            TimelineBar(
                height = ((total[i].toDouble() / max) * 100).roundToInt(),
                level = if (crit[i] > 0) "crit" else if (warn[i] > 0) "warn" else "info",
            )
        }
    }

    private fun toIncidentRow(a: AlertEntity): IncidentRow {
        val status = statusOf(a)
        val base =
            when (a.severity) {
                Severity.CRITICAL -> 0.92
                Severity.HIGH -> 0.82
                Severity.MEDIUM -> 0.68
                else -> 0.52
            }
        val jitter = (a.packetCount % 7) / 100.0
        val rate = a.packetCount.toDouble() / a.windowSeconds.coerceAtLeast(1)
        return IncidentRow(
            id = a.alertId,
            incId = incidentId(a),
            timestamp = a.timestamp.toString(),
            severity = a.severity.name,
            title = TITLES[a.type] ?: humanize(a.type.name),
            source = a.sourceIp.ifBlank { "—" },
            target = a.destinationIp.ifBlank { "—" },
            category = CATEGORIES[a.type] ?: "Anomaly",
            status = status.first,
            statusIcon = status.second,
            assignee = a.assignee ?: if (a.acknowledged) "System" else "Unassigned",
            acknowledged = a.acknowledged,
            contained = a.contained,
            confidence = ((base + jitter) * 100).roundToInt(),
            anomalyScore = "%.2f".format(base + jitter - 0.02),
            packetRate = if (rate >= 1000) "%.1fk pkt/s".format(rate / 1000) else "${rate.roundToInt()} pkt/s",
            volume = formatBytes(a.bytesCount),
            targetPort = a.destinationPort,
            protocol = a.protocol.name,
        )
    }

    private fun severityDistribution(alerts: List<AlertEntity>): SeverityDistribution {
        val c = alerts.count { it.severity == Severity.CRITICAL }
        val h = alerts.count { it.severity == Severity.HIGH }
        val m = alerts.count { it.severity == Severity.MEDIUM }
        val l = alerts.count { it.severity == Severity.LOW }
        return SeverityDistribution(c, h, m, l, c + h + m + l)
    }

    private fun topAssets(alerts: List<AlertEntity>): List<AffectedAsset> {
        val icons = listOf("router", "cloud", "dns")
        val levels = listOf("crit", "warn", "info")
        val names = listOf("OpenWrt Edge Gateway", "API Gateway (Public)", "Core Service Node")
        return alerts.asSequence()
            .map { it.destinationIp }
            .filter { it.isNotBlank() }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(3)
            .mapIndexed { i, e ->
                AffectedAsset(names.getOrElse(i) { "Internal Host" }, e.key, icons.getOrElse(i) { "lan" }, levels.getOrElse(i) { "info" })
            }
    }

    /**
     * Forensic detail for a single incident. Captured packets aren't tagged with
     * an alertId, so we correlate by the attacker's source IP — "all traffic we
     * captured from this source" — and roll it up into a forensic summary.
     */
    fun incidentForensics(alertId: String): IncidentForensicsView {
        val a =
            alertRepository.findByAlertId(alertId)
                .orElseThrow { ResourceNotFoundException("Alert not found: $alertId") }
        val packets =
            packetSummaryRepository
                .findBySourceIpOrderByTimestampDesc(a.sourceIp, PageRequest.of(0, 60))
                .content

        val protoTotal = packets.size.coerceAtLeast(1)
        val protocols =
            packets.groupingBy { it.protocol.name }.eachCount().entries
                .sortedByDescending { it.value }
                .map { ProtocolShare(it.key, round1(it.value.toDouble() / protoTotal * 100)) }
        val topPorts =
            packets.groupingBy { it.destinationPort }.eachCount().entries
                .sortedByDescending { it.value }
                .take(4)
                .map { (port, count) ->
                    TopPort("$port" + (PORT_NAMES[port]?.let { " ($it)" } ?: ""), "${formatCount(count.toLong())} PKTS", port in RISKY_PORTS)
                }
        val targets = packets.map { it.destinationIp }.filter { it.isNotBlank() }.toSet()
        val span =
            if (packets.size >= 2) {
                humanizeDuration(Duration.between(packets.last().timestamp, packets.first().timestamp))
            } else {
                "—"
            }

        val stats =
            listOf(
                ForensicStat("Captured Packets", "${packets.size}"),
                ForensicStat("Total Volume", formatBytes(packets.sumOf { it.packetSize })),
                ForensicStat("Unique Targets", "${targets.size}"),
                ForensicStat("Capture Span", span),
                ForensicStat("Alert Window", "${a.windowSeconds}s"),
                ForensicStat("Flagged Packets", "${a.packetCount}"),
            )
        val packetRows =
            packets.take(20).map {
                ForensicPacket(
                    timestamp = it.timestamp.toString(),
                    protocol = it.protocol.name,
                    source = it.sourceIp.ifBlank { "—" },
                    destination = it.destinationIp.ifBlank { "—" },
                    port = it.destinationPort,
                    size = formatBytes(it.packetSize),
                    flags = it.tcpFlags ?: "—",
                    suspicious = it.destinationPort in RISKY_PORTS,
                )
            }

        return IncidentForensicsView(
            incId = incidentId(a),
            title = TITLES[a.type] ?: humanize(a.type.name),
            source = a.sourceIp.ifBlank { "—" },
            target = a.destinationIp.ifBlank { "—" },
            severity = a.severity.name,
            category = CATEGORIES[a.type] ?: "Anomaly",
            contained = a.contained,
            stats = stats,
            protocols = protocols,
            topPorts = topPorts,
            packets = packetRows,
            empty = packets.isEmpty(),
        )
    }

    private fun humanizeDuration(d: Duration): String {
        val s = d.seconds
        return when {
            s >= 3600 -> "%.1fh".format(s / 3600.0)
            s >= 60 -> "${s / 60}m ${s % 60}s"
            else -> "${s}s"
        }
    }

    // ── Traffic ────────────────────────────────────────────────────────────
    fun traffic(): TrafficView {
        val summary = trafficStatsService.getSummary()
        val device = deviceRepository.findAll().maxByOrNull { it.lastSeenAt }
        val series =
            device?.let {
                trafficStatsRepository.findByDeviceIdOrderByTimestampDesc(it.deviceId, PageRequest.of(0, 60)).content.reversed()
            } ?: emptyList()
        val packets = packetSummaryRepository.findAllByOrderByTimestampDesc(PageRequest.of(0, 80)).content

        val suspicious = packets.count { it.destinationPort in RISKY_PORTS }
        val suspPct = if (packets.isEmpty()) 0.0 else suspicious.toDouble() / packets.size * 100
        val maxFlow = (series.flatMap { listOf(it.tcpPackets, it.udpPackets) }.maxOrNull() ?: 1L).coerceAtLeast(1L)

        return TrafficView(
            totalPackets = formatCount(summary.totalPackets),
            tcpPct = summary.tcpPercentage.roundToInt(),
            udpPct = summary.udpPercentage.roundToInt(),
            suspiciousPct = round1(suspPct).toString(),
            protocols =
                listOf(
                    ProtocolShare("TCP", round1(summary.tcpPercentage)),
                    ProtocolShare("UDP", round1(summary.udpPercentage)),
                    ProtocolShare("ICMP", round1((100.0 - summary.tcpPercentage - summary.udpPercentage).coerceAtLeast(0.0))),
                ),
            flow =
                series.map {
                    FlowPoint(((it.tcpPackets.toDouble() / maxFlow) * 100).roundToInt(), ((it.udpPackets.toDouble() / maxFlow) * 100).roundToInt())
                },
            topPorts =
                packets.groupingBy { it.destinationPort }.eachCount().entries
                    .sortedByDescending { it.value }
                    .take(4)
                    .map { (port, count) ->
                        TopPort("$port" + (PORT_NAMES[port]?.let { " ($it)" } ?: ""), "${formatCount(count.toLong())} PKTS", port in RISKY_PORTS)
                    },
            topSources =
                packets.asSequence()
                    .map { it.sourceIp }
                    .filter { it.isNotBlank() }
                    .groupingBy { it }
                    .eachCount()
                    .entries
                    .sortedByDescending { it.value }
                    .take(4)
                    .map { TopSource(it.key, if (isPrivate(it.key)) "LOCAL" else "EXTERNAL") }
                    .toList(),
            packets =
                packets.take(12).map {
                    val susp = it.destinationPort in RISKY_PORTS
                    PacketRow(
                        it.timestamp.toString(),
                        if (susp) "SUSPICIOUS" else it.protocol.name,
                        it.sourceIp.ifBlank { "—" },
                        it.destinationIp.ifBlank { "—" },
                        it.destinationPort,
                        formatBytes(it.packetSize),
                        susp,
                    )
                },
            distribution = distribution(series.map { it.totalPackets }),
        )
    }

    // ── Dashboard ──────────────────────────────────────────────────────────
    fun dashboard(): DashboardView {
        val alerts = recentAlerts()
        val devices = deviceRepository.findAll()
        val uniqueSources = alerts.mapNotNull { it.sourceIp.ifBlank { null } }.toSet()

        val health =
            if (devices.isEmpty()) {
                "No devices"
            } else {
                val online = devices.count { it.status == DeviceStatus.ONLINE }
                val pct = online.toDouble() / devices.size * 100
                if (pct % 1.0 == 0.0) "${pct.toInt()}%" else "%.1f%%".format(pct)
            }

        val (volumeDelta, volume) = volume(alerts)
        val (origins, arcs) = originsAndArcs(alerts)

        return DashboardView(
            kpis = DashboardKpis(formatCount(alerts.size.toLong()), "${uniqueSources.size}", health),
            liveAlerts =
                alerts.take(6).map {
                    LiveAlert(it.severity.name, levelOf(it.severity), it.timestamp.toString(), humanize(it.type.name), it.sourceIp.ifBlank { "—" }, it.destinationIp.ifBlank { "—" })
                },
            volumeDelta = volumeDelta,
            volume = volume,
            origins = origins,
            arcs = arcs,
            deviceLat = DEVICE_LAT,
            deviceLng = DEVICE_LNG,
        )
    }

    // ── Topology live feed ─────────────────────────────────────────────────
    /**
     * Real topology events synthesised from live domain activity: recent
     * alerts (anomaly/containment), device registrations and heartbeat status.
     * Newest first, capped at [limit].
     */
    fun topologyEvents(limit: Int): List<TopologyEvent> {
        val cap = limit.coerceIn(1, 100)
        val events = mutableListOf<TopologyEvent>()

        alertRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp")).take(cap).forEach { a ->
            val src = a.sourceIp.ifBlank { "—" }
            val node = a.deviceId.ifBlank { "edge" }
            if (a.contained) {
                events += TopologyEvent(a.timestamp.toString(), "CONTAINMENT", "${humanize(a.type.name)} contained on $node", "primary")
            } else {
                val tone =
                    when (levelOf(a.severity)) {
                        "critical" -> "error"
                        "warning" -> "warning"
                        else -> "secondary"
                    }
                events += TopologyEvent(a.timestamp.toString(), "ANOMALY_DETECTION", "${humanize(a.type.name)} on $node from $src", tone)
            }
        }

        deviceRepository.findAll().forEach { d ->
            val ip = d.ipAddress.ifBlank { "—" }
            d.createdAt?.let {
                events += TopologyEvent(it.toString(), "NODE_UP", "${d.deviceId} registered on $ip", "primary")
            }
            when (d.status) {
                DeviceStatus.ONLINE ->
                    events += TopologyEvent(d.lastSeenAt.toString(), "HEARTBEAT", "${d.deviceId} responding ($ip)", "muted")
                DeviceStatus.OFFLINE ->
                    events += TopologyEvent(d.lastSeenAt.toString(), "DEVICE_WARN", "${d.deviceId} ($ip) is offline", "warning")
                DeviceStatus.QUARANTINED ->
                    events += TopologyEvent(
                        (d.quarantinedAt ?: d.lastSeenAt).toString(),
                        "QUARANTINE",
                        "${d.deviceId} ($ip) is isolated — containment active",
                        "primary",
                    )
                DeviceStatus.UNKNOWN -> Unit
            }
        }

        // ISO-8601 instants sort lexicographically, so this is true chronological order.
        return events.sortedByDescending { it.at }.take(cap)
    }

    /** The backend service's own most recent runtime log lines (newest first). */
    fun runtimeLogs(limit: Int): List<RuntimeLogLine> = RuntimeLogBuffer.snapshot(limit)

    // ── Rules console ──────────────────────────────────────────────────────
    /** Real KPI strip + trigger feed for the Rules page (alerts are rule matches). */
    fun rulesConsole(): RulesConsoleView {
        val devices = deviceRepository.findAll()
        val synced = devices.count { it.status == DeviceStatus.ONLINE }
        val dayAgo = Instant.now().minus(1, ChronoUnit.DAYS)

        val kpis =
            RulesKpis(
                activeRouterRules = ruleService.enabledCount(),
                agentsSynced = "$synced / ${devices.size}",
                rulesTriggeredToday = alertRepository.countByTimestampGreaterThanEqual(dayAgo),
                avgEvaluationMs = round1(ruleService.avgEvalMs()),
                localMitigations = alertRepository.countByContainedTrue(),
            )

        val triggers =
            alertRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp")).take(12).map { a ->
                val (ruleId, signal, iface) = ruleMatch(a.type.name)
                RuleTrigger(
                    at = a.timestamp.toString(),
                    ruleId = ruleId,
                    deviceId = a.deviceId.ifBlank { "edge-router" },
                    interfaceScope = iface,
                    signal = signal,
                    value = triggerValue(a.type.name, a.packetCount, a.bytesCount),
                    tone =
                        when (levelOf(a.severity)) {
                            "critical" -> "error"
                            "warning" -> "warning"
                            else -> "secondary"
                        },
                )
            }

        return RulesConsoleView(kpis, triggers)
    }

    /** Map an alert type to the edge rule that produced it. */
    private fun ruleMatch(type: String): Triple<String, String, String> =
        when (type) {
            "UDP_FLOOD_SUSPECTED" -> Triple("EDGE-RULE-DDOS-UDP-001", "packet_rate", "wan")
            "TCP_SPIKE_SUSPECTED" -> Triple("EDGE-RULE-SYN-FLOOD-001", "syn_ack_ratio", "wan")
            "PORT_SCAN_SUSPECTED" -> Triple("EDGE-RULE-PORTSCAN-002", "port_distribution", "br-lan")
            "HIGH_TRAFFIC_VOLUME" -> Triple("EDGE-RULE-OUTBOUND-001", "outbound_ratio", "wan")
            else -> Triple("EDGE-RULE-GENERIC", "anomaly_score", "wan")
        }

    private fun triggerValue(
        type: String,
        packets: Long,
        bytes: Long,
    ): String =
        when (type) {
            "HIGH_TRAFFIC_VOLUME" -> formatBytes(bytes)
            else -> "${formatCount(packets)} pkt/s"
        }

    private fun volume(alerts: List<AlertEntity>): Pair<String, List<VolumeBar>> {
        val buckets = LongArray(24)
        val now = Instant.now().toEpochMilli()
        val dayAgo = now - 24L * 3600 * 1000
        var recent = 0
        var previous = 0
        for (a in alerts) {
            val t = a.timestamp.toEpochMilli()
            if (t >= dayAgo) {
                val hoursAgo = ((now - t) / (3600 * 1000)).toInt().coerceIn(0, 23)
                buckets[23 - hoursAgo]++
                recent++
            } else if (t >= dayAgo - 24L * 3600 * 1000) {
                previous++
            }
        }
        val max = (buckets.maxOrNull() ?: 0).coerceAtLeast(1)
        val threshold = max * 0.75
        val bars = buckets.map { VolumeBar(((it.toDouble() / max) * 100).roundToInt(), it >= threshold && it > 0) }
        val delta =
            if (previous > 0) {
                val d = round1((recent - previous).toDouble() / previous * 100)
                "${if (d >= 0) "+" else ""}$d% vs Yesterday"
            } else {
                "$recent in last 24h"
            }
        return delta to bars
    }

    private fun originsAndArcs(alerts: List<AlertEntity>): Pair<List<Origin>, List<ThreatArc>> {
        val uniqueIps = alerts.mapNotNull { it.sourceIp.ifBlank { null } }.toSet()
        val worstRank =
            alerts.filter { it.sourceIp.isNotBlank() }
                .groupBy { it.sourceIp }
                .mapValues { (_, list) -> list.maxOf { sevRank(it.severity) } }

        val countryCounts = LinkedHashMap<String, Int>()
        val arcs = mutableListOf<ThreatArc>()
        var total = 0
        for (ip in uniqueIps) {
            val point = geo.locate(ip)
            val country = point?.country?.ifBlank { "Unknown" } ?: "Unknown"
            countryCounts[country] = (countryCounts[country] ?: 0) + 1
            total++
            if (point != null) {
                arcs.add(ThreatArc(point.lat, point.lng, ip, country, levelFromRank(worstRank[ip] ?: 1)))
            }
        }

        val denom = total.coerceAtLeast(1)
        val sorted = countryCounts.entries.sortedByDescending { it.value }
        val origins = mutableListOf<Origin>()
        sorted.take(4).forEachIndexed { i, e -> origins.add(Origin(e.key, round1(e.value.toDouble() / denom * 100), i)) }
        val otherCount = sorted.drop(4).sumOf { it.value }
        if (otherCount > 0) {
            origins.add(Origin("Other", round1(otherCount.toDouble() / denom * 100), 4))
        }
        return origins to arcs
    }

    // ── Shared helpers ─────────────────────────────────────────────────────
    private fun recentAlerts(): List<AlertEntity> =
        alertRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp")).take(MAX_ALERTS)

    private fun isInvestigating(a: AlertEntity): Boolean = !a.acknowledged && a.assignee != null

    private fun statusOf(a: AlertEntity): Pair<String, String> =
        when {
            a.acknowledged -> "Resolved" to "check_circle"
            isInvestigating(a) -> "Investigating" to "search"
            else -> "New" to "fiber_new"
        }

    private fun levelOf(s: Severity): String =
        when (s) {
            Severity.CRITICAL, Severity.HIGH -> "critical"
            Severity.MEDIUM -> "warning"
            else -> "info"
        }

    private fun sevRank(s: Severity): Int =
        when (s) {
            Severity.CRITICAL -> 4
            Severity.HIGH -> 3
            Severity.MEDIUM -> 2
            else -> 1
        }

    private fun levelFromRank(rank: Int): String =
        when {
            rank >= 3 -> "critical"
            rank == 2 -> "medium"
            else -> "low"
        }

    private fun distribution(values: List<Long>): List<DistBar> {
        val buckets = 24
        if (values.isEmpty()) return List(buckets) { DistBar(0, false) }
        val sums = LongArray(buckets)
        for (i in values.indices) {
            val b = ((i.toDouble() / values.size) * buckets).toInt().coerceIn(0, buckets - 1)
            sums[b] += values[i]
        }
        val max = (sums.maxOrNull() ?: 0).coerceAtLeast(1)
        val threshold = max * 0.8
        return sums.map { DistBar(((it.toDouble() / max) * 100).roundToInt(), it >= threshold && it > 0) }
    }

    private fun incidentId(a: AlertEntity): String {
        val d = (a.createdAt ?: a.timestamp).atZone(ZoneOffset.UTC)
        val ymd = "%04d-%02d%02d".format(d.year, d.monthValue, d.dayOfMonth)
        val suffix = a.alertId.filter { it.isLetterOrDigit() }.take(4).uppercase().ifBlank { "0000" }
        return "INC-$ymd-$suffix"
    }

    private fun humanize(type: String): String =
        type.lowercase().split('_').joinToString(" ") { it.replaceFirstChar(Char::uppercase) }

    private fun formatBytes(n: Long): String =
        when {
            n >= 1_000_000 -> "%.1f MB".format(n / 1_000_000.0)
            n >= 1000 -> "%.1f KB".format(n / 1000.0)
            else -> "$n B"
        }

    private fun formatCount(n: Long): String =
        when {
            n >= 1_000_000 -> "%.1fM".format(n / 1_000_000.0)
            n >= 1000 -> "%.1fK".format(n / 1000.0)
            else -> "$n"
        }

    private fun round1(x: Double): Double = (x * 10).roundToInt() / 10.0

    private fun isPrivate(ip: String): Boolean = PRIVATE_RANGE.containsMatchIn(ip)

    companion object {
        private const val MAX_ALERTS = 500
        private const val DEVICE_LAT = 44.4268
        private const val DEVICE_LNG = 26.1025
        private val PRIVATE_RANGE = Regex("^(10\\.|127\\.|169\\.254\\.|192\\.168\\.|172\\.(1[6-9]|2\\d|3[01])\\.)")
        private val RISKY_PORTS = setOf(22, 23, 3389, 445, 1433, 3306)
        private val PORT_NAMES =
            mapOf(443 to "HTTPS", 80 to "HTTP", 53 to "DNS", 22 to "SSH", 23 to "Telnet", 3389 to "RDP", 8080 to "HTTP-ALT")
        private val TITLES =
            mapOf(
                AlertType.UDP_FLOOD_SUSPECTED to "DDoS Vector Identified",
                AlertType.PORT_SCAN_SUSPECTED to "Port Scan Detected",
                AlertType.TCP_SPIKE_SUSPECTED to "Traffic Spike Detected",
                AlertType.HIGH_TRAFFIC_VOLUME to "Volumetric Anomaly",
            )
        private val CATEGORIES =
            mapOf(
                AlertType.UDP_FLOOD_SUSPECTED to "DDoS",
                AlertType.PORT_SCAN_SUSPECTED to "Recon",
                AlertType.TCP_SPIKE_SUSPECTED to "DoS",
                AlertType.HIGH_TRAFFIC_VOLUME to "Volumetric",
            )
    }
}
