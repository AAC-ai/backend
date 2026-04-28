package com.aac.backend.presentation;

import com.aac.backend.presentation.dto.request.ReissueRequest;
import com.aac.backend.presentation.dto.response.ApiResponse;
import com.aac.backend.presentation.dto.response.TokenResponse;
import com.aac.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponse>> reissue(@Valid @RequestBody ReissueRequest request) {
        var tokens = authService.reissue(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success(tokens));
    }
}
