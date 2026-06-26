package ro.puk3p.sentinel.client.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import ro.puk3p.sentinel.client.dto.ClientBatchRequest
import ro.puk3p.sentinel.client.service.ClientService
import ro.puk3p.sentinel.common.response.ApiResponse

@RestController
@RequestMapping("/api/clients")
class ClientController(
    private val clientService: ClientService,
) {
    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    fun ingest(
        @Valid @RequestBody request: ClientBatchRequest,
    ): ApiResponse<Map<String, Int>> {
        val saved = clientService.recordBatch(request)
        return ApiResponse(
            success = true,
            message = "Clients stored",
            data = mapOf("saved" to saved),
        )
    }
}
