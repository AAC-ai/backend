package com.aac.backend.controller;

import com.aac.backend.controller.dto.response.UserResponse;
import com.aac.backend.domain.User;
import com.aac.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // TODO: JWT 인증 구현 후 SecurityContext에서 userId 추출
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@RequestHeader("X-User-Id") Long userId) {
        User user = userService.getUser(userId);

        return ResponseEntity.ok(UserResponse.from(user));
    }
}
