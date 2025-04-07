package com.example.client.service;

import com.example.grpc.HelloRequest;

public interface ITestService {

    public String message(HelloRequest request);
}
