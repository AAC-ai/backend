# AAC Backend

> AAC(보완대체의사소통) 사용자를 위한 AI 문장 생성 서비스

AAC 사용자가 기호(symbol)를 선택하면, 대화 맥락을 분석해 자연스러운 문장을 생성합니다.

<br>

## Tech Stack

| 분류 | 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| AI | Spring AI 1.0 / OpenAI GPT-4o |
| Database | MySQL / H2 (local) |
| Auth | Google OAuth2, JWT, HttpOnly Cookie |
| Infra | Docker, Caddy, GitHub Actions |

<br>

## AI 문장 생성 파이프라인

기호 입력 → 프롬프트 라우팅 → 컨텍스트 요약 → 문장 생성 → 로그 저장

```
WordController
  └─ WordService
       ├─ loadContext()               # 세션 히스토리 조회 (4시간 내)
       └─ WordSentenceGenerator
            ├─ PromptRouter           # 기호 수 기준 프롬프트 분기
            ├─ ConversationContextSummarizer  # 맥락 관련성 판단 및 요약
            └─ ChatClient             # GPT-4o 문장 생성
```

### Prompt Routing

기호 개수에 따라 프롬프트를 분기합니다. 기호가 1개일 때는 단독 표현에 최적화된 프롬프트를, 2개 이상일 때는 조합 문장 생성 프롬프트를 사용합니다.

```java
public String route(List<WordRequest> words) {
    if (words.size() == 1) {
        return singleSymbolPrompt;
    }
    return standardPrompt;
}
```

### Context Summary

이전 대화 히스토리를 GPT-4o-mini에 전달해 현재 기호와 관련이 있는지 판단합니다. 관련 있을 때만 상황 요약을 프롬프트에 주입해 문맥에 맞는 문장을 생성합니다.

```
이전 대화: "학교 가고싶어"
현재 기호: "먹고싶어"
→ 요약: "현재 학교에 있는 상황이다."
→ 프롬프트: {basePrompt}\n\n현재 상황: 현재 학교에 있는 상황이다.
```

관련 없는 경우 요약 없이 기본 프롬프트만 사용합니다.

### 세션 만료

마지막 메시지로부터 4시간이 지나면 히스토리를 초기화해 새 세션으로 시작합니다. HTTP stateless 환경에서 별도 세션 관리 없이 `createdAt` 기준으로 처리합니다.

```java
private boolean isSessionAlive(Long userId) {
    var threshold = LocalDateTime.now().minusHours(SESSION_EXPIRY_HOURS);
    return conversationMessageRepository.existsByUserIdAndCreatedAtAfter(userId, threshold);
}
```

### 비동기 로그 저장

`traceId`를 발급하고 AI 생성 결과를 `@Async` 이벤트 리스너로 비동기 저장합니다. 로그 저장이 응답 시간에 영향을 주지 않으며, 모델명 / latency / 토큰 수 / 성공 여부 / 컨텍스트 요약을 기록합니다.

```java
// WordService
var traceId = UUID.randomUUID().toString();
eventPublisher.publishEvent(new AiGenerationLogEvent(traceId, words, history, result));

// AiGenerationLogEventListener
@Async
@EventListener
public void handle(AiGenerationLogEvent event) { ... }
```

<br>

## 인증

Google OAuth2 인가 코드를 서버에서 교환해 사용자를 식별합니다. Spring Security 없이 인터셉터 기반으로 구현했습니다.

- **accessToken**: JWT, HttpOnly 쿠키로 전달
- **refreshToken**: UUID, DB 저장 + HttpOnly 쿠키로 전달
- refreshToken은 JWT 서명이 불필요하므로 UUID를 사용하고 DB에서 검증합니다.

<br>

## 아키텍처

```
presentation/   - Controller, DTO, Interceptor
service/        - 비즈니스 로직
domain/         - Entity, Repository
infra/          - AI, OAuth, JWT 등 외부 연동
analytics/      - AI 생성 로그
global/         - 예외 처리
```

도메인 계층이 외부 기술에 의존하지 않도록 서비스 계층에서 도메인 객체를 반환하고, DTO 변환은 프레젠테이션 계층에서 처리합니다.

<br>

## CI/CD

```
PR to develop  →  CI Build   (빌드 + 테스트)
push to main   →  CI Publish (Docker 이미지 빌드 후 Docker Hub 푸시)
               →  CD Deploy  (self-hosted 러너에서 docker compose up)
```

- 시크릿 설정값은 `backend-config` private 서브모듈로 분리 관리
- 프로덕션 환경은 Caddy가 리버스 프록시 및 HTTPS 자동 발급 처리

<br>

## 로컬 실행

```bash
# 서브모듈 초기화
git submodule update --init

# 실행
docker compose -f docker-compose.local.yml up
```
