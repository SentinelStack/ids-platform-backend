package ro.puk3p.sentinel.traffic.repository

import org.bson.Document
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import ro.puk3p.sentinel.traffic.entity.TrafficStatsEntity

class TrafficStatsRepositoryCustomImpl(
    private val mongoTemplate: MongoTemplate,
) : TrafficStatsRepositoryCustom {
    override fun summarize(): TrafficTotals {
        val group =
            Aggregation.group()
                .sum("totalPackets").`as`("totalPackets")
                .sum("tcpPackets").`as`("tcpPackets")
                .sum("udpPackets").`as`("udpPackets")
                .sum("totalBytes").`as`("totalBytes")
                .sum("tcpBytes").`as`("tcpBytes")
                .sum("udpBytes").`as`("udpBytes")

        val aggregation = Aggregation.newAggregation(group)
        val result =
            mongoTemplate.aggregate(aggregation, TrafficStatsEntity::class.java, Document::class.java)
                .uniqueMappedResult

        return TrafficTotals(
            totalPackets = result.asLong("totalPackets"),
            tcpPackets = result.asLong("tcpPackets"),
            udpPackets = result.asLong("udpPackets"),
            totalBytes = result.asLong("totalBytes"),
            tcpBytes = result.asLong("tcpBytes"),
            udpBytes = result.asLong("udpBytes"),
        )
    }

    private fun Document?.asLong(key: String): Long {
        val value = this?.get(key) as? Number ?: return 0L
        return value.toLong()
    }
}
