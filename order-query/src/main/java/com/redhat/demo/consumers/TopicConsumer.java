package com.redhat.demo.consumers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.jboss.logging.Logger;

import com.redhat.demo.kstreams.model.Order;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class TopicConsumer implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(TopicConsumer.class);

    @Inject
    KafkaConsumerConfig config;

    private Consumer<String, Order> consumer;

    void onStart(@Observes StartupEvent ev) {
        LOG.info("The application is starting...");
        consumer = new KafkaConsumer<>(config.props());
    }

    public List<Order> getLastOrder(long size) {
        LOG.info("getMessages");

        var orderList = new ArrayList<Order>();

        try {
            // 1. Discover Topic Partitions
            List<TopicPartition> partitions = new ArrayList<>();
            consumer.partitionsFor(config.topic()).forEach(
                    partitionInfo -> partitions.add(new TopicPartition(config.topic(), partitionInfo.partition())));

            // Assign the consumer to all partitions of the topic
            consumer.assign(partitions);

            // 2. Seek to End Offsets and 3. Calculate Start Offsets
            Map<TopicPartition, Long> endOffsets = consumer.endOffsets(partitions);
            Map<TopicPartition, Long> startOffsets = new HashMap<>();

            var tailSize = size / partitions.size();
            for (TopicPartition tp : partitions) {
                long endOffset = endOffsets.get(tp);
                long startOffset = Math.max(0, endOffset - tailSize);
                startOffsets.put(tp, startOffset);
                consumer.seek(tp, startOffset); // Seek to the calculated start offset
            }

            ConsumerRecords<String, Order> records = consumer.poll(Duration.ofMillis(200));

            for (ConsumerRecord<String, Order> record : records) {
                LOG.debug("Received - key: " + record.key() + ", value: " + record.value());
                orderList.add(record.value());
            }

            LOG.info("records retrieved: " + records.count());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return orderList;
    }

    @Override
    public void close() throws Exception {
        consumer.close();
    }
}
