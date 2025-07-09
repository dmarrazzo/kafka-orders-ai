package com.redhat.demo.kstreams;

import java.time.Duration;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.state.WindowStore;

import com.redhat.demo.kstreams.model.Order;
import com.redhat.demo.kstreams.model.OrderAggregate;
import com.redhat.demo.kstreams.model.OrderAggregateSerde;

import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class TopologyProducer {
    public static String STORE_NAME = "order-aggregates-store";
    public static String TOPIC = "order-aggregates";

    @Produces
    public Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        var orderSerde = new ObjectMapperSerde<>(Order.class);

        var materializedStore = Materialized
                .<String, OrderAggregate, WindowStore<Bytes, byte[]>>as(STORE_NAME)
                .withKeySerde(Serdes.String())
                .withValueSerde(new OrderAggregateSerde());
                
        KStream<String, Order> orderStream = builder.stream("orders",
                Consumed.with(Serdes.String(), orderSerde));

        orderStream
                .groupByKey()
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(600)))
                .aggregate(
                        OrderAggregate::new,
                        (k, order, aggregator) -> aggregator.aggregate(order),
                        materializedStore);

        var topology = builder.build();

        System.out.println(topology.describe());

        return topology;
    }
}
