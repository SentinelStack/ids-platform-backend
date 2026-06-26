package ro.puk3p.sentinel.client.service

import ro.puk3p.sentinel.client.dto.ClientBatchRequest

interface ClientService {
    fun recordBatch(request: ClientBatchRequest): Int
}
