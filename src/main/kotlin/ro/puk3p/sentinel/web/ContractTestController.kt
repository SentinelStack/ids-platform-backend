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
    fun readOriginalContract(): ResponseEntity<String> =
        readClasspathFile("openapi/ids-backend.yaml")

    @GetMapping("/test/contract/bundled")
    fun readBundledContract(): ResponseEntity<String> =
        readClasspathFile("openapi/ids-backend-bundled.yaml")

    private fun readClasspathFile(path: String): ResponseEntity<String> {
        return try {
            val resource = ClassPathResource(path)

            if (!resource.exists()) {
                ResponseEntity.internalServerError()
                    .body("Contract file was not found in classpath: $path")
            } else {
                val content = String(resource.inputStream.readAllBytes(), StandardCharsets.UTF_8)
                ResponseEntity.ok(content)
            }
        } catch (exception: Exception) {
            ResponseEntity.internalServerError()
                .body("Failed to read contract from classpath: ${exception.message}")
        }
    }
}
