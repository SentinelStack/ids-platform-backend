package ro.puk3p.sentinel.alert.dto

/**
 * Optional body for the assign endpoint. When [analyst] names a roster member
 * the incident is assigned to them; otherwise the backend auto-picks the
 * least-loaded analyst.
 */
data class AssignRequest(
    val analyst: String? = null,
)
