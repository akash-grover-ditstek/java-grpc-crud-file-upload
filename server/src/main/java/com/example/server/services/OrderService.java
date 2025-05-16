package com.example.server.services;

import com.example.grpc.order.OrderRequest;
import com.example.grpc.order.OrderResponse;
import com.example.grpc.order.OrderServiceGrpc;
import com.example.server.entity.Order;
import com.example.server.repository.OrderRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@GrpcService
public class OrderService extends OrderServiceGrpc.OrderServiceImplBase {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public void createOrder(OrderRequest request, StreamObserver<OrderResponse> responseObserver) {
        Order order = new Order();
        order.setItem(request.getItem());
        order.setPrice(request.getPrice());
        order.setStatus("PENDING");

        Order savedOrder = orderRepository.save(order);
        OrderResponse response = OrderResponse.newBuilder()
                .setOrderId(savedOrder.getOrderId())
                .setStatus("PENDING")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getOrder(OrderRequest request, StreamObserver<OrderResponse> responseObserver) {
        int orderId = request.getOrderId();

        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isPresent()) {
            OrderResponse response = OrderResponse.newBuilder()
                    .setItem(order.get().getItem())
                    .setPrice(order.get().getPrice())
                    .setStatus(order.get().getStatus())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(new RuntimeException("Order not found"));
        }
    }

    /**
     * First get the ID from request, its same as the post we are just sending some data to update the previous record
     * check if the order exists in repo
     * if it does, update the order with the new data
     * return the Order response with updated data
     *
     * @param request
     * @param responseObserver
     */

    @Override
    public void updateOrder(OrderRequest request, StreamObserver<OrderResponse> responseObserver) {

        int orderId = request.getOrderId();

        Optional<Order> order = orderRepository.findById(orderId);

        if (order.isPresent()) {
            Order existingOrder = order.get();
            existingOrder.setItem(request.getItem());
            existingOrder.setPrice(request.getPrice());
            existingOrder.setStatus("PENDING");

            // Save the updated order
            Order updatedOrder = orderRepository.save(existingOrder);

            // Create the response with updated order details
            OrderResponse response = OrderResponse.newBuilder()
                    .setOrderId(updatedOrder.getOrderId())
                    .setItem(updatedOrder.getItem())
                    .setPrice(updatedOrder.getPrice())
                    .setStatus(updatedOrder.getStatus())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Order not found with ID: " + orderId)
                    .asRuntimeException());
        }

    }

    @Override
    public void deleteOrder(OrderRequest request, StreamObserver<OrderResponse> responseObserver) {
        int orderId = request.getOrderId();

        Optional<Order> order = orderRepository.findById(orderId);

        if (order.isPresent()) {
            orderRepository.delete(order.get());
            OrderResponse response = OrderResponse.newBuilder()
                    .setOrderId(orderId)
                    .setStatus("DELETED")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Order not found with ID: " + orderId)
                    .asRuntimeException());
        }
    }

    @Override
    public void getOrderStream(OrderRequest request, StreamObserver<OrderResponse> responseObserver) {

        List<Order> orders = orderRepository.findAll();

        for (Order order : orders) {
            OrderResponse response = OrderResponse.newBuilder()
                    .setItem(order.getItem())
                    .setPrice(order.getPrice())
                    .setStatus(order.getStatus())
                    .build();

            responseObserver.onNext(response);
        }

        responseObserver.onCompleted();

    }
}
