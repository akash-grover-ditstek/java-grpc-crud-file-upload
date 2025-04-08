package com.example.client.service;

import com.example.client.dto.ClientOrderRequest;
import com.example.client.dto.ClientOrderResponse;
import com.example.grpc.OrderResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IOrderService {
    ClientOrderResponse createOrder(ClientOrderRequest orderRequest);
    OrderResponse getOrder(int orderId);

    ClientOrderResponse updateOrder(int orderId, ClientOrderRequest orderRequest);

    ClientOrderResponse deleteOrder(int orderId);

    CompletableFuture<List<ClientOrderResponse>> getOrderStream();
}
