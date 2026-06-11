# Sentinel IDS Platform — End-to-End Flow

A realistic scenario you can replay manually with cURL/Postman against QA
(`https://qa-api.puk3p.online`) or a local backend (`http://localhost:8082`).

How a real event travels:

```
OpenWrt agent ──POST──▶ Backend (MongoDB) ──poll──▶ ingestion ──▶ Kafka ids.alerts
                                                                    ├──▶ Flink → traffic-anomalies (real-time correlation)
                                                                    └──▶ Spark stream → S3 Parquet lake → Spark batch reports
Dashboard ◀──GET── Backend            Kafka UI: https://kafka.puk3p.online
```

`BASE=https://qa-api.puk3p.online`

## 1. Happy path

### 1.1 Register the device (what the agent does on boot)

```bash
curl -i -X POST "$BASE/api/devices/register" -H 'Content-Type: application/json' -d '{
  "name": "lab-router", "ipAddress": "192.168.1.1",
  "firmwareVersion": "23.05.3", "model": "TP-Link Archer C7"
}'
```

Expect **201 Created**, envelope `{"success":true,"message":"Device registered","data":{...,"deviceId":"<uuid>",...,"links":[...]}}`.
State: a `devices` document is inserted (status ONLINE, lastSeenAt=now). Save the `deviceId`.

### 1.2 Heartbeat (agent, periodic — now reports its own clock)

```bash
curl -i -X POST "$BASE/api/devices/$DEVICE_ID/heartbeat" -H 'Content-Type: application/json' \
  -d "{\"seenAt\": \"$(date -u +%Y-%m-%dT%H:%M:%SZ)\"}"
```

Expect **200 OK** with the device status model. State: `lastSeenAt` updated → device shows ONLINE.

### 1.3 Traffic window (agent, every window)

```bash
curl -i -X POST "$BASE/api/traffic/stats" -H 'Content-Type: application/json' -d "{
  \"deviceId\": \"$DEVICE_ID\", \"timestamp\": \"$(date -u +%Y-%m-%dT%H:%M:%SZ)\",
  \"totalPackets\": 1200, \"tcpPackets\": 800, \"udpPackets\": 400,
  \"totalBytes\": 600000, \"tcpBytes\": 450000, \"udpBytes\": 150000, \"windowSeconds\": 5
}"
```

Expect **201 Created**. State: `traffic_stats` document inserted; `/api/traffic/summary` aggregates update.

### 1.4 The router detects an anomaly → alert

```bash
curl -i -X POST "$BASE/api/alerts" -H 'Content-Type: application/json' -d "{
  \"deviceId\": \"$DEVICE_ID\", \"timestamp\": \"$(date -u +%Y-%m-%dT%H:%M:%SZ)\",
  \"type\": \"PORT_SCAN_SUSPECTED\", \"severity\": \"HIGH\", \"protocol\": \"TCP\",
  \"sourceIp\": \"203.0.113.66\", \"destinationIp\": \"192.168.1.5\",
  \"sourcePort\": 40000, \"destinationPort\": 22,
  \"packetCount\": 90, \"bytesCount\": 7200, \"windowSeconds\": 5,
  \"description\": \"sequential ports probed from one source\"
}"
```

Expect **201 Created**; `data.alertId` is a server-generated UUID; HATEOAS links include
`self`, `device`, `forensics`, `acknowledge`.

State changes that follow automatically (no further calls):
1. ≤5 s later the **ingestion** service polls `GET /api/alerts`, publishes the new alert to Kafka `ids.alerts` (confirmed send, durable watermark).
2. **Flink** windows it by `sourceIp`; ≥3 alerts from the same source within 60 s ⇒ a correlated `ESCALATED_SOURCE` anomaly on `traffic-anomalies` (visible in Kafka UI).
3. The **Spark stream** appends it as Parquet to `s3a://sentinel-ids-lake/alerts/dt=<today>/`; nightly batch reports aggregate it.

### 1.5 Forensics packet evidence (optional, agent)

```bash
curl -i -X POST "$BASE/api/forensics/packets" -H 'Content-Type: application/json' -d "{
  \"deviceId\": \"$DEVICE_ID\", \"alertId\": \"$ALERT_ID\",
  \"timestamp\": \"$(date -u +%Y-%m-%dT%H:%M:%SZ)\", \"protocol\": \"TCP\",
  \"sourceIp\": \"203.0.113.66\", \"destinationIp\": \"192.168.1.5\",
  \"sourcePort\": 40000, \"destinationPort\": 22, \"packetSize\": 60, \"tcpFlags\": \"SYN\"
}"
```

Expect **201 Created**. `GET /api/forensics/by-alert/$ALERT_ID` then returns it.

### 1.6 Dashboard reads

```bash
curl -s "$BASE/api" | jq .                                  # 200 — HATEOAS index
curl -s "$BASE/api/alerts?severity=HIGH&from=2026-01-01T00:00:00Z&size=20" | jq .   # 200 — time-filtered (the former 500)
curl -s "$BASE/api/traffic/summary" | jq .data              # 200 — protocol mix percentages
curl -s "$BASE/actuator/health" | jq .                      # 200 — {"status":"UP"}
```

### 1.7 Analyst acknowledges the alert

```bash
curl -i -X PATCH "$BASE/api/alerts/$ALERT_ID/acknowledge"
```

Expect **200 OK**, `data.acknowledged=true`, and the `acknowledge` link disappears from the resource.

## 2. Failure paths

| Scenario | Call | Expect |
|---|---|---|
| Malformed JSON body | `POST /api/alerts` body `{not json` | **400**, `message: "Malformed or unreadable request body"` |
| Validation failure | `POST /api/alerts` with `"sourcePort": 70000` | **400**, `validationErrors.sourcePort` |
| Bad enum | `POST /api/alerts` with `"severity": "EXTREME"` | **400** |
| Bad time filter | `GET /api/alerts?from=yesterday` | **400**, message names ISO-8601 |
| Negative page | `GET /api/alerts?page=-1` | **400** |
| Oversized page | `GET /api/alerts?size=100000` | **400** (max 500) |
| Unsafe sort | `GET /api/alerts?sortBy=$where` | **400** (whitelist) |
| Missing resource | `GET /api/alerts/does-not-exist` | **404**, ErrorResponse envelope |
| Unknown route | `GET /api/nope` | **404** (was 500 before 1.1.5) |
| Wrong method | `DELETE /api/alerts` | **405** |

Error envelope, always:

```json
{"timestamp":"...","status":400,"error":"Bad Request","message":"...","path":"/api/alerts","validationErrors":null}
```

## 3. Verifying the async pipeline (VPS)

```bash
# Kafka UI (browser): https://kafka.puk3p.online  → topics ids.alerts / traffic-anomalies,
#   consumer group flink-anomaly-detector with offsets/lag
# CLI:
ssh sentinel-vps 'C=$(docker ps -q -f name=^/sentinel-kafka$); \
  docker exec $C /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic traffic-anomalies --from-beginning --timeout-ms 10000'
# S3 lake: alerts under s3://sentinel-ids-lake/alerts/dt=YYYY-MM-DD/, reports under /reports
```

To trip the Flink correlation on demand, POST three step-1.4 alerts (same `sourceIp`) within a minute, then watch `traffic-anomalies` emit one `ESCALATED_SOURCE` with `severity: CRITICAL` and the merged `alertTypes`/`deviceIds`.
