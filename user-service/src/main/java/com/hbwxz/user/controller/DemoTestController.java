package com.hbwxz.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoTestController {
    @GetMapping("/hello")
    public String test() {
        return "hello!";
    }
}
