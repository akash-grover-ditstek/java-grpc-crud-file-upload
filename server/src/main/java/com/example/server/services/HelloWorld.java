package com.example.server.services;

import com.example.grpc.GreeterGrpc;
import com.example.grpc.HelloReply;
import com.example.grpc.HelloRequest;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class HelloWorld extends GreeterGrpc.GreeterImplBase {


    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        StringBuilder message = new StringBuilder();
        message.append(request.getName());
        message.append(", ");
        message.append("Welcome To GRPC World ");
        HelloReply reply = HelloReply.newBuilder().setMessage(message.toString()).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
