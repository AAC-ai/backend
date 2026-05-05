# CLAUDE.md - AAC Backend

## Role

Claude is an implementation assistant for the AAC Backend repository.

Your primary job is to implement user requests in a way that fits the existing codebase, architecture, and team conventions. You may also help with GitHub issues, branches, commits, pushes, and pull requests when the user explicitly asks for that workflow.

Keep changes small, focused, and reviewable. Do not modify unrelated files or logic.

## Instruction Priority

Follow instructions in this order:

1. Explicit instructions from the user in the current conversation
2. Instructions in this file
3. Existing repository structure, code patterns, and conventions
4. General language and framework conventions

If instructions conflict and the conflict affects correctness, safety, data, security, or Git history, ask the user before proceeding.

## GitHub Workflow

Create GitHub issues, branches, commits, pushes, and pull requests only when the user explicitly asks for the GitHub workflow.

Examples of explicit requests:

- "Create an issue and PR for this"
- "Start from an issue and open a PR"
- "Create a branch and work on this"
- "Follow the GitHub workflow"
- "Make a PR"

For simple code changes, analysis, review, or explanation requests, do not change GitHub state.

After each GitHub step, report the created issue link, branch name, PR link, or relevant status.

## Branch Strategy

| Branch | Purpose |
|--------|---------|
| `main` | Production deployment |
| `develop` | Integration branch and default PR base |
| `feat/<english-kebab-case>` | Feature work |
| `fix/<english-kebab-case>` | Bug fixes |
| `refactor/<english-kebab-case>` | Refactoring |

Create work branches from `develop` by default.

Branch names must use English kebab-case.

Examples:

```text
feat/add-user-profile
fix/login-token-expiry
refactor/payment-service
```

## Before Starting Work

Before changing files, inspect the repository state.

- Check the current branch.
- Run `git status`.
- Do not revert user or teammate changes.
- Do not touch unrelated changes.
- If existing changes may conflict with the requested work, ask the user before proceeding.

Before creating a branch, committing, pushing, or opening a PR, check the worktree state again.

## Implementation Principles

- Follow the existing architecture and code style.
- Reuse existing utilities, services, DTOs, test helpers, and local patterns before creating new ones.
- Keep the diff limited to what the request actually needs.
- Do not perform unrelated refactors, formatting changes, or file moves.
- Add new dependencies only when clearly necessary.
- Add new abstractions only when they reduce real duplication or complexity.
- Prefer the smallest maintainable change over a speculative redesign.
- Follow existing patterns for error handling, logging, validation, and configuration.

## Testing And Verification

When behavior changes, add or update relevant tests.

After implementation, run the smallest relevant verification for the change.

Use the repository's actual test, lint, and build commands. Check files such as `build.gradle`, `gradlew`, `pom.xml`, `package.json`, `README`, or CI configuration when unsure.

Common examples:

```bash
./gradlew test
./gradlew build
mvn test
npm test
npm run lint
npm run build
```

If tests or builds cannot be run, report the reason and the remaining risk.

## Security And Sensitive Data

Never commit or include the following in a PR:

- API keys
- Access tokens
- Refresh tokens
- Secrets
- Passwords
- Private keys
- `.env` files
- Local configuration files
- Personal data
- Sensitive logs

For changes involving authentication, authorization, personal data, payment, tokens, sessions, or credentials, explicitly mention the risk even if the code change is small.

## Database And Migrations

If a schema change is required, include the appropriate migration.

For migration work, consider:

- Impact on existing data
- Rollback strategy
- Nullability
- Default values
- Indexes and constraints
- Deployment order

Do not perform destructive database changes without explicit user approval.

Do not run commands that directly affect production data.

## API Changes

When request or response schemas change, update the related code and tests.

Check related areas such as:

- DTOs
- Validation
- Serializers or mappers
- Controllers and services
- Tests
- API documentation or examples

Mark changes as breaking when they may break existing clients.

Follow the existing project pattern for error responses.

## Commit Rules

Create commits only when the user explicitly asks for a commit or asks for the GitHub workflow.

Commit message format:

```text
<type>: <Korean summary> (#<issue-number>)
```

Allowed types:

- `feat`
- `fix`
- `refactor`
- `chore`
- `docs`
- `test`

Examples:

```text
feat: 사용자 프로필 조회 API 추가 (#123)
fix: 만료된 토큰 처리 오류 수정 (#124)
```

Before committing, review changed files and make sure unrelated changes are not included.

## PR Rules

Open PRs against `develop` by default.

Every PR must include the related issue number when an issue exists.

PR title format:

```text
<type>: <Korean summary>
```

PR body template:

```md
## 작업 내용
- <summary of changes>

## 변경 이유
<why this change is needed>

## 테스트
- <tests or checks run>

Closes #<issue-number>
```

After creating a PR, report the PR link to the user.

## Merge Rules

Merge a PR only when the user explicitly asks for it.

Allowed merge requests include:

- "머지해줘"
- "merge해줘"
- "Merge this PR"
- "이 PR 머지해줘"

Use squash merge by default:

```bash
gh pr merge <PR-number> --repo AAC-ai/backend --squash --delete-branch
```

Never merge automatically.

## Forbidden Actions

Do not perform these actions without explicit user approval:

- Push directly to `main`
- Force push
- Automatically merge a PR
- Modify unrelated files
- Revert user or teammate changes
- Run destructive Git commands
- Add or remove dependencies
- Perform destructive database migrations
- Run commands that affect production data
- Commit secrets, tokens, credentials, personal data, or sensitive logs
- Format the whole project or unrelated files

Do not run these commands unless the user explicitly requests them:

```bash
git reset --hard
git clean -fd
git push --force
git push --force-with-lease
```

## Completion Report

When the work is complete, report briefly and concretely.

Use this format:

```md
## Summary
- <what changed>

## Changed Files
- `<path>`

## Verification
- `<command>`: passed/failed
- If not run, explain why

## Links
- Issue: <link or none>
- PR: <link or none>

## Remaining Risk
- <risk or "None">
```

Avoid long explanations. Ask questions only when the user needs to make a decision.
