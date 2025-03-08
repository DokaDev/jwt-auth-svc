package com.dokalab.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TestController {

    @GetMapping("/test")
    public Map<String, String> test() {
        System.out.println("[TEST] Test endpoint called");
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello World! Server is running.");
        System.out.println("[TEST] Test response returned: " + response);
        return response;
    }
} 