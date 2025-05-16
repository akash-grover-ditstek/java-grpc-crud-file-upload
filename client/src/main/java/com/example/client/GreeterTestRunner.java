package com.example.client;

import com.example.grpc.greeter.GreeterGrpc;
import com.example.grpc.greeter.HelloReply;
import com.example.grpc.greeter.HelloRequest;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class GreeterTestRunner implements CommandLineRunner {

    @GrpcClient("grpc-client")
    private GreeterGrpc.GreeterBlockingStub greeterStub;

    @Override
    public void run(String... args) {
        System.out.println("Runner started!");

        if (greeterStub == null) {
            System.err.println("greeterStub is NULL. Client not injected.");
            return;
        }

        try {
            HelloRequest request = HelloRequest.newBuilder().setName("Test").build();
            HelloReply reply = greeterStub.sayHello(request);
            System.out.println("gRPC Response: " + reply.getMessage());
        } catch (Exception e) {
            System.err.println("gRPC call failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
