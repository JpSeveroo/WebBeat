package com.webbeat.webbeat.controller;

import com.webbeat.webbeat.dto.request.LoginRequest;
import com.webbeat.webbeat.dto.request.RegisterRequest;
import com.webbeat.webbeat.dto.response.LoginResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return null;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterRequest> register(@RequestBody LoginRequest request) {
        return null;
    }

}
