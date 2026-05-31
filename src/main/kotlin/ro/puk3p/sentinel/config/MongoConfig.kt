package ro.puk3p.sentinel.config

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import io.mongock.runner.springboot.EnableMongock
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory

@Configuration
@EnableMongoAuditing
@EnableMongock
class MongoConfig {
    @Bean
    fun mongoClient(
        @Value("\${spring.data.mongodb.uri}") uri: String,
    ): MongoClient = MongoClients.create(uri)

    @Bean
    fun mongoDatabaseFactory(
        mongoClient: MongoClient,
        @Value("\${spring.data.mongodb.database:ids_platform}") database: String,
    ): MongoDatabaseFactory = SimpleMongoClientDatabaseFactory(mongoClient, database)
}
