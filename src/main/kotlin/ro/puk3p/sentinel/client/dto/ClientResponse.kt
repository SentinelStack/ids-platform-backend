package ro.puk3p.sentinel.client.dto

import java.time.Instant

data class ClientResponse(
    val deviceId: String,
    val ip: String,
    val mac: String,
    val name: String?,
    val online: Boolean,
    val createdAt: Instant,
    val lastSeen: Instant,
)
