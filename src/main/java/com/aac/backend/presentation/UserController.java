package com.aac.backend.presentation;

import com.aac.backend.presentation.dto.response.UserResponse;
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

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@RequestHeader("X-User-Id") Long userId) {
        var user = userService.getUser(userId);

        return ResponseEntity.ok(UserResponse.from(user));
    }
}
