package ro.puk3p.sentinel.traffic.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero
import java.time.Instant

data class TrafficStatsCreateRequest(
    @field:NotBlank(message = "deviceId is required")
    val deviceId: String,
    @field:NotNull(message = "timestamp is required")
    val timestamp: Instant,
    @field:PositiveOrZero
    val totalPackets: Long,
    @field:PositiveOrZero
    val tcpPackets: Long,
    @field:PositiveOrZero
    val udpPackets: Long,
    @field:PositiveOrZero
    val totalBytes: Long,
    @field:PositiveOrZero
    val tcpBytes: Long,
    @field:PositiveOrZero
    val udpBytes: Long,
    @field:PositiveOrZero
    val windowSeconds: Int,
)
