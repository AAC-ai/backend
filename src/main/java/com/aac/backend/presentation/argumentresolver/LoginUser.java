package com.aac.backend.presentation.argumentresolver;

import java.util.Optional;

public record LoginUser(Optional<Long> userId) {

    public static LoginUser anonymous() {
        return new LoginUser(Optional.empty());
    }

    public static LoginUser of(Long userId) {
        return new LoginUser(Optional.of(userId));
    }
}
