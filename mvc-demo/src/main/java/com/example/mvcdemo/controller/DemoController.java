package com.example.mvcdemo.controller;

import com.example.mvcdemo.model.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Demo REST Controller
 */
@RestController
@RequestMapping("/mvc/demo")
public class DemoController {

    /**
     * GET /mvc/demo/hello -> 返回 "yy"
     */
    @GetMapping("/hello")
    public ApiResponse<String> hello() {
        return ApiResponse.success("yy");
    }

    /**
     * GET /mvc/demo/hello/{name} -> 返回 name
     */
    @GetMapping("/hello/{name}")
    public ApiResponse<String> helloName(@PathVariable String name) {
        return ApiResponse.success(name);
    }
}
