package com.aac.backend.presentation;

import com.aac.backend.presentation.argumentresolver.CurrentUser;
import com.aac.backend.presentation.argumentresolver.LoginUser;
import com.aac.backend.global.exception.BusinessException;
import com.aac.backend.global.exception.ErrorCode;
import com.aac.backend.presentation.dto.response.ApiResponse;
import com.aac.backend.presentation.dto.response.UserResponse;
import com.aac.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(@CurrentUser LoginUser loginUser) {
        var userId = loginUser.userId()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        var user = userService.getUser(userId);
        return ResponseEntity.ok(ApiResponse.success(UserResponse.from(user)));
    }
}
