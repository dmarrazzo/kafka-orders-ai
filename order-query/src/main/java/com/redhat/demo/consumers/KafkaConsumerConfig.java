package com.redhat.demo.consumers;

import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import com.redhat.demo.kstreams.model.OrderDeserializer;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "kafkaconsumer")
public interface KafkaConsumerConfig {

        @WithDefault("orders")
        String topic();

        @WithDefault("localhost:9092")
        String bootstrapServer();

        @WithDefault("5")
        long lastMessagesLength();

        default Properties props() {
                Properties props = new Properties();

                // configure the bootstrap server
                props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer());

                props.put(ConsumerConfig.GROUP_ID_CONFIG, "orders-cgroup");
                props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
                props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

                props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
                props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, OrderDeserializer.class.getName());
                return props;
        }
}
