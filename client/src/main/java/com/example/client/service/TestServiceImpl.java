package com.example.client.service;

import com.example.grpc.GreeterGrpc;
import com.example.grpc.HelloRequest;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class TestServiceImpl implements ITestService {

    @GrpcClient("grpc-client")
    private GreeterGrpc.GreeterBlockingStub greeterBlockingStub;

    @Override
    public String message(HelloRequest request) {
        return greeterBlockingStub.sayHello(request).getMessage();
    }
}
