package com.redhat.demo.kstreams.model;

import java.io.Serializable;

public class OrderAggregate implements Serializable {
    private String client;
    private double sum;
    private long count;

    public OrderAggregate() {
        this.sum = 0.0;
        this.count = 0;
    }

    public OrderAggregate(double sum, long count) {
        this.sum = sum;
        this.count = count;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        if (this.client == null)
            this.client = client;
        else if (!client.equals(this.client))
            throw new RuntimeException("Invalid aggregation of different clients");
    }


    public double getSum() {
        return sum;
    }

    public long getCount() {
        return count;
    }

    public OrderAggregate aggregate(Order order) {
        setClient(order.client);
        this.sum += order.amount;
        this.count++;
        return this;
    }

    public void merge(OrderAggregate other) {
        this.sum += other.sum;
        this.count += other.count;
    }

    public double getAverage() {
        return (count == 0) ? 0.0 : sum / count;
    }

    @Override
    public String toString() {
        return "OrderAggregate [ client=" + client + ", sum=" + sum + ", count=" + count + ", average="
                + getAverage() + "]";
    }

}