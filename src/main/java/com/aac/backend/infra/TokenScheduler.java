package com.aac.backend.infra;

import com.aac.backend.domain.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void deleteExpiredRefreshTokens() {
        refreshTokenRepository.deleteAllExpired(LocalDateTime.now());
        log.info("만료된 refresh token 삭제 완료");
    }
}
