package com.redhat.demo.producer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import com.redhat.demo.model.Order;

import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/order")
public class OrderResource {

    private static final Logger LOG = Logger.getLogger(OrderResource.class);

    // Inject a Reactive Messaging Emitter to send order to the order channel
    @Channel("order")
    Emitter<Order> orderEmitter;

    /**
     * Endpoint to generate orders to sent in the order channel
     */
    @POST
    @Path("/generate")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Order> generateOrders(@QueryParam("qty") int qty) {
        LOG.info("generate orders, qty = " + qty);

        List<Order> generatedOrders = new ArrayList<>();

        // Initialize a Random object for generating random values
        Random random = new Random();

        // Define literal arrays of possible clients and countries
        String[] clients = { "Acme Corp", "Globex Inc.", "Soylent Corp", "Omni Consumer Products", "Stark Industries" };
        String[] countries = { "USA", "Canada", "Mexico", "Germany", "France", "Japan", "Australia", "Brazil", "India",
                "Italy" };

        // Number of orders to generate
        int numberOfOrders = 3 + random.nextInt(10);
        if (qty>0)
            numberOfOrders = qty;

        // Loop to generate random orders
        for (int i = 0; i < numberOfOrders; i++) {
            // Generate a random UUID for the order ID
            String id = UUID.randomUUID().toString();

            int amount = random.nextInt(99) * 100 + 200;

            // Select a random client from the clients array
            String client = clients[random.nextInt(clients.length)];

            // Select a random country from the countries array
            String country = countries[random.nextInt(countries.length)];

            // Create a new Order object with the generated random values
            Order order = new Order(id, amount, client, country);

            generatedOrders.add(order); // Removed as per user request context

            OutgoingKafkaRecordMetadata<String> metadata = OutgoingKafkaRecordMetadata
                    .<String>builder()
                    .withKey(order.client)
                    .build();
                    
            Message<Order> message = Message.of(order).addMetadata(metadata);
            orderEmitter.send(message);

        }
        return generatedOrders;
    }
}