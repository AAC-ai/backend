# CLAUDE.md — AAC Backend

## GitHub 워크플로우

Claude가 이슈 생성부터 머지까지 전 과정을 대신 처리한다.
사용자가 작업을 요청하면 아래 순서를 자동으로 수행하고, 각 단계 완료 시 링크를 보여준다.

### 브랜치 전략

| 브랜치 | 역할 |
|--------|------|
| `main` | 프로덕션 배포 |
| `develop` | 통합 브랜치 (PR 대상) |
| `feat/<영어-kebab-case>` | 기능 개발 |
| `fix/<영어-kebab-case>` | 버그 수정 |
| `refactor/<영어-kebab-case>` | 리팩토링 |

### 작업 순서

1. **이슈 생성** — 작업 내용을 이슈로 등록한다.
   ```
   gh issue create --repo AAC-ai/backend \
     --title "<제목>" \
     --body "<설명>" \
     --label "<label>"
   ```

2. **브랜치 생성** — `develop`에서 분기한다.
   ```
   git checkout develop && git pull
   git checkout -b feat/<name>
   ```

3. **코드 작업** — 구현 후 커밋한다.
   - 커밋 메시지 형식: `<type>: <한국어 설명> (#<이슈번호>)`
   - type: `feat` / `fix` / `refactor` / `chore` / `docs` / `test`

4. **푸시 및 PR 생성** — `develop` 브랜치로 PR을 연다.
   ```
   git push -u origin <브랜치명>
   gh pr create --repo AAC-ai/backend \
     --base develop \
     --title "<type>: <설명>" \
     --body "..."
   ```
   PR 본문에는 **관련 이슈 번호** (`Closes #<N>`)를 반드시 포함한다.

5. **머지** — 사용자가 명시적으로 머지를 요청할 때만 수행한다.
   ```
   gh pr merge <PR번호> --repo AAC-ai/backend --squash --delete-branch
   ```

### PR 본문 템플릿

```markdown
## 작업 내용
- <변경 사항 요약>

## 변경 이유
<왜 이 변경이 필요한지>

Closes #<이슈번호>
```

### 규칙

- 머지는 사용자가 "머지해줘" / "merge해줘"라고 명시적으로 요청한 경우에만 실행한다. 자동으로 머지하지 않는다.
- `main`으로 직접 push하지 않는다. 항상 `develop` 경유.
- force push 금지.
- PR은 항상 이슈와 연결(`Closes #N`)한다.
