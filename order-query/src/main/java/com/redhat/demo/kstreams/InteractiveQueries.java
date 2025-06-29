package com.redhat.demo.kstreams;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.jboss.logging.Logger;

import com.redhat.demo.kstreams.model.OrderAggregate;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class InteractiveQueries {
    private static final Logger LOG = Logger.getLogger(InteractiveQueries.class);

    @Inject
    KafkaStreams streams;

    public ArrayList<OrderAggregate> getOrderAggregate(long seconds) {
        LOG.info("getOrderAggregate");
        var list = new ArrayList<OrderAggregate>();

        try {
            // 1. Create StoreQueryParameters

            StoreQueryParameters<ReadOnlyWindowStore<String, OrderAggregate>> queryParams = StoreQueryParameters
                    .fromNameAndType(
                            TopologyProducer.STORE_NAME,
                            QueryableStoreTypes.windowStore());

            // 2. Pass the StoreQueryParameters object to the store method

            ReadOnlyWindowStore<String, OrderAggregate> keyValueStore = streams.store(queryParams);

            Instant timeTo = Instant.now();
            Instant timeFrom = timeTo.minus(Duration.ofSeconds(seconds));
            // Now you can use keyValueStore to query your data
            // You can also iterate through the store
            keyValueStore
                    .backwardFetchAll(timeFrom, timeTo)
                    .forEachRemaining(
                            kv -> {
                                kv.value.setClient(kv.key.key());
                                list.add(kv.value);
                            }
                    );
            LOG.info("list size: " + list.size());

        } catch (org.apache.kafka.streams.errors.InvalidStateStoreException e) {
            // This happens if the stream is still starting up or rebalancing
            // You might need to retry or handle this gracefully in a real application.
            LOG.error("State store is not yet ready for querying: " + e.getMessage());
        }

        return list;
    }
}
