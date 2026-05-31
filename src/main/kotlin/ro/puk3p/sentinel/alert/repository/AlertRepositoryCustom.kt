package ro.puk3p.sentinel.alert.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ro.puk3p.sentinel.alert.entity.AlertEntity
import ro.puk3p.sentinel.alert.model.AlertFilter

interface AlertRepositoryCustom {
    fun search(
        filter: AlertFilter,
        pageable: Pageable,
    ): Page<AlertEntity>
}
