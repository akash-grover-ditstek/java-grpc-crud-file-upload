package com.example.client.service.impl;

import com.example.client.dto.ClientOrderRequest;
import com.example.client.dto.ClientOrderResponse;
import com.example.client.service.IOrderService;
import com.example.grpc.OrderRequest;
import com.example.grpc.OrderResponse;
import com.example.grpc.OrderServiceGrpc;
import com.example.grpc.blank;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Service
public class OrderClientServiceImpl implements IOrderService {

    @GrpcClient("grpc-client")
    private OrderServiceGrpc.OrderServiceBlockingStub orderServiceBlockingStub;

    @GrpcClient("grpc-client")
    private OrderServiceGrpc.OrderServiceStub orderServiceAsyncStub;

    @Override
    public ClientOrderResponse createOrder(ClientOrderRequest request) {

        OrderRequest grpcRequest = OrderRequest.newBuilder().setItem(request.getItem()).setPrice(request.getPrice()).build();

        var grpcResponse = orderServiceBlockingStub.createOrder(grpcRequest);

        ClientOrderResponse response = new ClientOrderResponse.ClientOrderResponseBuilder()
                .setOrderId(grpcResponse.getOrderId())
                .setItem(grpcResponse.getItem())
                .setPrice(grpcResponse.getPrice())
                .setStatus(grpcResponse.getStatus())
                .build();

        return response;
    }

    @Override
    public OrderResponse getOrder(int orderId) {
        OrderRequest request = OrderRequest.newBuilder().setOrderId(orderId).build();

        return orderServiceBlockingStub.getOrder(request);
    }

    @Override
    public ClientOrderResponse updateOrder(int orderId, ClientOrderRequest request) {
        OrderRequest grpcRequest = OrderRequest.newBuilder()
                .setOrderId(orderId)
                .setItem(request.getItem())
                .setPrice(request.getPrice())
                .build();

        var grpcResponse = orderServiceBlockingStub.updateOrder(grpcRequest);

        ClientOrderResponse response = new ClientOrderResponse.ClientOrderResponseBuilder()
                .setOrderId(grpcResponse.getOrderId())
                .setItem(grpcResponse.getItem())
                .setPrice(grpcResponse.getPrice())
                .setStatus(grpcResponse.getStatus())
                .build();
        return response;

    }

    @Override
    public ClientOrderResponse deleteOrder(int orderId) {
        OrderRequest grpcRequest = OrderRequest.newBuilder()
                .setOrderId(orderId)
                .build();

        var grpcResponse = orderServiceBlockingStub.deleteOrder(grpcRequest);

        ClientOrderResponse response = new ClientOrderResponse.ClientOrderResponseBuilder()
                .setOrderId(grpcResponse.getOrderId())
                .setStatus(grpcResponse.getStatus())
                .build();
        return response;
    }

    @Override
    public List<OrderResponse> getOrderStream() {
        List<OrderResponse> clientOrderList = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(1);

        orderServiceAsyncStub.getOrderStream(blank.newBuilder().build(), new StreamObserver<OrderResponse>() {
            @Override
            public void onNext(OrderResponse value) {
                clientOrderList.add(value);
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Stream interrupted", e);
        }

        return clientOrderList;
    }
}
