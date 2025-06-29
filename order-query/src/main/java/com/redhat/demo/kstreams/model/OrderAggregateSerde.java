package com.redhat.demo.kstreams.model;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.ByteBuffer;
import java.util.Map;

public class OrderAggregateSerde implements Serde<OrderAggregate> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public void close() {
    }

    @Override
    public Serializer<OrderAggregate> serializer() {
        return new Serializer<OrderAggregate>() {
            @Override
            public byte[] serialize(String topic, OrderAggregate data) {
                ByteBuffer buffer = ByteBuffer.allocate(16);
                buffer.putDouble(data.getSum());
                buffer.putLong(data.getCount());
                return buffer.array();
            }
        };
    }

    @Override
    public Deserializer<OrderAggregate> deserializer() {
        return new Deserializer<OrderAggregate>() {
            @Override
            public OrderAggregate deserialize(String topic, byte[] data) {
                ByteBuffer buffer = ByteBuffer.wrap(data);
                double sum = buffer.getDouble();
                long count = buffer.getLong();
                return new OrderAggregate(sum, count);
            }
        };
    }
}
