package ro.puk3p.sentinel.common.response

import java.util.Collections

class PagedResponse<T>(
    content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val isLast: Boolean,
) {
    private val contentCopy: List<T> = content.toList()

    val content: List<T>
        get() = Collections.unmodifiableList(contentCopy)
}
