package com.example.client.controller;

import com.example.client.dto.ClientOrderRequest;
import com.example.client.dto.ClientOrderResponse;
import com.example.client.service.impl.OrderClientServiceImpl;
import com.example.grpc.OrderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderClientServiceImpl orderService;

    @PostMapping("/create")
    public ResponseEntity<ClientOrderResponse> createOrder(@RequestBody ClientOrderRequest request) {
        ClientOrderResponse response = orderService.createOrder(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getOrder")
    public ResponseEntity<OrderResponse> getOrder(@RequestParam int orderId) {
        OrderResponse response = orderService.getOrder(orderId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/updateOrder/{orderId}")
    public ResponseEntity<ClientOrderResponse> updateOrder(@PathVariable int orderId, @RequestBody ClientOrderRequest request) {
        ClientOrderResponse response = orderService.updateOrder(orderId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/deleteOrder/{orderId}")
    public ResponseEntity<ClientOrderResponse> deleteOrder(@PathVariable int orderId) {
        ClientOrderResponse response = orderService.deleteOrder(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getOrderStream")
    public ResponseEntity<List<OrderResponse>> orderStream() {
        List<OrderResponse> response = orderService.getOrderStream();
        return ResponseEntity.ok(response);
    }

}

