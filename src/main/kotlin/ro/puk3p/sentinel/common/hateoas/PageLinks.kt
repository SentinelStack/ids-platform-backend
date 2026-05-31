package ro.puk3p.sentinel.common.hateoas

import org.springframework.data.domain.Page
import org.springframework.hateoas.IanaLinkRelations
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder
import ro.puk3p.sentinel.common.response.PagedResponse

object PageLinks {
    fun <T> apply(
        target: PagedResponse<T>,
        page: Page<*>,
        linkBuilder: (Int) -> WebMvcLinkBuilder,
    ): PagedResponse<T> {
        target.add(linkBuilder(page.number).withSelfRel())

        if (page.hasPrevious()) {
            target.add(linkBuilder(0).withRel(IanaLinkRelations.FIRST))
            target.add(linkBuilder(page.number - 1).withRel(IanaLinkRelations.PREV))
        }

        if (page.hasNext()) {
            target.add(linkBuilder(page.number + 1).withRel(IanaLinkRelations.NEXT))
            target.add(linkBuilder(page.totalPages - 1).withRel(IanaLinkRelations.LAST))
        }

        return target
    }
}
