package com.example.client.controller;

import com.example.client.service.ITestService;
import com.example.grpc.HelloRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    private ITestService testService;



    @PostMapping("sayHello")
    public String sayHello(@RequestBody HelloRequest request){
        return testService.message(request);
    }


}
