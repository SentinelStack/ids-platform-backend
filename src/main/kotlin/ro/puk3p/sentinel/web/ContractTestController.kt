package ro.puk3p.sentinel.web

import org.springframework.core.io.ClassPathResource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.nio.charset.StandardCharsets

@RestController
class ContractTestController {

    @GetMapping("/test/ping")
    fun ping(): String = "pong"

    @GetMapping("/test/contract")
    fun readContractFromDependency(): ResponseEntity<String> {
        return try {
            val resource = ClassPathResource("openapi/ids-backend.yaml")

            if (!resource.exists()) {
                ResponseEntity.internalServerError()
                    .body("Contract file was not found in classpath.")
            } else {
                val content = resource.inputStream.readAllBytes().toString(StandardCharsets.UTF_8)
                ResponseEntity.ok(content)
            }
        } catch (exception: Exception) {
            ResponseEntity.internalServerError()
                .body("Failed to read contract from dependency: ${exception.message}")
        }
    }
}