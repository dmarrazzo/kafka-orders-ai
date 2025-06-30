package com.redhat.demo.tools;

import java.util.List;

import com.redhat.demo.consumers.TopicConsumer;
import com.redhat.demo.kstreams.InteractiveQueries;
import com.redhat.demo.kstreams.model.Order;
import com.redhat.demo.kstreams.model.OrderAggregate;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.inject.Inject;

public class OrderTool {

    @Inject
    InteractiveQueries queries;

    @Inject
    TopicConsumer consumer;

    @Tool(description = """
            Retrieve orders aggregated by client name over a periond of 120 seconds.
            Use this tool to retrieve orders for a time period.
            The result is the list of clients name with the sum of order amounts, the number of orders, and the average amount of the aggregated orders.
            """)
    public List<OrderAggregate> aggregate() {
        return queries.getOrderAggregate(120);
    }

    @Tool(description = """
            Retrieves the list with the latest orders, you can specify the size of the list through the parameter.
            The result is a list of orders in json containing order id, amount, client name, country.
            """)
    public List<Order> last(@ToolArg(description = "the number of orders to retrieve", defaultValue = "10", name = "size", required = false) long size) {
        if (size == 0)
            size = 10;
        return consumer.getLastOrder(size);
    }
}
