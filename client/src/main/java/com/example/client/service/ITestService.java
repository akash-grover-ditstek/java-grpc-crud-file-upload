package com.example.client.service;


import com.example.grpc.greeter.HelloRequest;

public interface ITestService {

    public String message(HelloRequest request);
}
