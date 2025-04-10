package com.example.client.dto;

import lombok.Data;

@Data
public class ClientOrderResponse {

    private int orderId;
    private String item;
    private double price;
    private String status;

    public ClientOrderResponse(ClientOrderResponseBuilder builder) {
        this.orderId = builder.orderId;
        this.item = builder.item;
        this.price = builder.price;
        this.status = builder.status;
    }

    public static class ClientOrderResponseBuilder {
        private int orderId;
        private String item;
        private double price;
        private String status;

        public ClientOrderResponseBuilder setOrderId(int orderId) {
            this.orderId = orderId;
            return this;
        }

        public ClientOrderResponseBuilder setItem(String item) {
            this.item = item;
            return this;
        }

        public ClientOrderResponseBuilder setPrice(double price) {
            this.price = price;
            return this;
        }

        public ClientOrderResponseBuilder setStatus(String status) {
            this.status = status;
            return this;
        }

        public ClientOrderResponse build() {
            return new ClientOrderResponse(this);
        }

    }

}
