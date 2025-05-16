package com.example.client.service.impl;

import com.example.client.dto.ClientOrderRequest;
import com.example.client.dto.ClientOrderResponse;
import com.example.client.exception.OrderNotFoundException;
import com.example.client.service.IOrderService;
import com.example.grpc.order.OrderRequest;
import com.example.grpc.order.OrderResponse;
import com.example.grpc.order.OrderServiceGrpc;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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

        try {
            return orderServiceBlockingStub.getOrder(request);
        } catch (StatusRuntimeException e) {
            throw new OrderNotFoundException("Order not found with ID: " + orderId);
        }
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

        try {
            var grpcResponse = orderServiceBlockingStub.deleteOrder(grpcRequest);

            return new ClientOrderResponse.ClientOrderResponseBuilder()
                    .setOrderId(grpcResponse.getOrderId())
                    .setStatus(grpcResponse.getStatus())
                    .build();

        } catch (StatusRuntimeException e) {
            throw new OrderNotFoundException("No Order found for this ID: " + orderId);
        }
    }


    @Override
    public CompletableFuture<List<ClientOrderResponse>> getOrderStream() {

        CompletableFuture<List<ClientOrderResponse>> future = new CompletableFuture<>();

        List<ClientOrderResponse> responses = new ArrayList<>();

        orderServiceAsyncStub.getOrderStream(OrderRequest.newBuilder().build(), new StreamObserver<OrderResponse>() {
            @Override
            public void onNext(OrderResponse orderResponse) {
                ClientOrderResponse response = new ClientOrderResponse.ClientOrderResponseBuilder()
                        .setOrderId(orderResponse.getOrderId())
                        .setItem(orderResponse.getItem())
                        .setPrice(orderResponse.getPrice())
                        .setStatus(orderResponse.getStatus())
                        .build();
                responses.add(response);
            }

            @Override
            public void onError(Throwable throwable) {
                future.completeExceptionally(throwable);
            }

            @Override
            public void onCompleted() {
                future.complete(responses);
            }
        });

        return future;

    }

}
