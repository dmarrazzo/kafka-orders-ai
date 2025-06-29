package com.redhat.demo.services;

import java.util.List;

import com.redhat.demo.consumers.TopicConsumer;
import com.redhat.demo.kstreams.InteractiveQueries;
import com.redhat.demo.kstreams.model.Order;
import com.redhat.demo.kstreams.model.OrderAggregate;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class Orders {
    
    @Inject
    InteractiveQueries queries;

    @Inject
    TopicConsumer consumer;

    @Path("aggregate")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<OrderAggregate> aggregate(@QueryParam("seconds") long seconds) {
        return queries.getOrderAggregate(seconds);
    }

    @Path("last")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Order> last(@QueryParam("size") long size) {
        if(size == 0)
            size = 10;
        return consumer.getLastOrder(size);
    }

}
