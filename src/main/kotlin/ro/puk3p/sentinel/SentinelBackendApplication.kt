package ro.puk3p.sentinel

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SentinelBackendApplication

fun main(args: Array<String>) {
    runApplication<SentinelBackendApplication>(*args)
}