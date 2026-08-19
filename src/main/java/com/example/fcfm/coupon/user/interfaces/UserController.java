package com.example.fcfm.coupon.user.interfaces;

import com.example.fcfm.coupon.user.application.UserApplicationService;
import com.example.fcfm.coupon.user.application.dto.UserCommand;
import com.example.fcfm.coupon.user.application.dto.UserRequest;
import com.example.fcfm.coupon.user.application.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {
    private final UserApplicationService service;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody UserRequest request) {
        return ResponseEntity.ok(service.register(new UserCommand(request.name())));
    }

    @GetMapping("/search")
    public ResponseEntity<UserResponse> getUser(@RequestBody UserRequest request) {
        return ResponseEntity.ok(service.getUser(new UserCommand(request.name())));
    }
}
