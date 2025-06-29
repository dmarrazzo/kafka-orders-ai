package com.redhat.demo.kstreams.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Order {

    public String id;
    public int amount;
    public String client;
    public String country;

    public Order() {
    }

    public Order(String id, int amount, String client, String country) {
        this.id = id;
        this.amount = amount;
        this.client = client;
        this.country = country;
    }

    @Override
    public String toString() {
        return "Order [id=" + id + ", amount=" + amount + ", client=" + client + ", country=" + country + "]";
    }

}