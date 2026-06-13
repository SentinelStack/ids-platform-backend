package ro.puk3p.sentinel.console

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap

/**
 * Server-side IP geolocation (ipwho.is). Doing this on the backend avoids the
 * browser-side CORS / Sec-Fetch blocking ipwho.is applies, and centralises the
 * lookup. Results are cached in-process per IP.
 */
@Component
class GeoLookup {
    private val log = LoggerFactory.getLogger(javaClass)
    private val client = RestClient.create()
    private val cache = ConcurrentHashMap<String, Optional<GeoPoint>>()
    private val privateRange = Regex("^(10\\.|127\\.|169\\.254\\.|192\\.168\\.|172\\.(1[6-9]|2\\d|3[01])\\.)")

    data class GeoPoint(val lat: Double, val lng: Double, val country: String)

    fun locate(ip: String): GeoPoint? {
        if (ip.isBlank() || privateRange.containsMatchIn(ip)) {
            return null
        }
        cache[ip]?.let { return it.orElse(null) }

        val result =
            try {
                val r = client.get().uri("https://ipwho.is/{ip}", ip).retrieve().body(IpWhoResponse::class.java)
                if (r?.success == true && r.latitude != null && r.longitude != null) {
                    GeoPoint(r.latitude, r.longitude, r.country ?: "Unknown")
                } else {
                    null
                }
            } catch (ex: Exception) {
                log.debug("geo lookup for {} failed: {}", ip, ex.message)
                null
            }
        cache[ip] = Optional.ofNullable(result)
        return result
    }
}

internal data class IpWhoResponse(
    val success: Boolean? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val country: String? = null,
)
