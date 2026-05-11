Copilot instructions — Qabul repository

Purpose
- Short guidance for Copilot sessions: how to build/test/run the app, where the important code lives, and repository-specific conventions to prefer when editing or generating code.

1) Build, run, test (concrete commands)
- Build (full): ./gradlew build
- Create runnable JAR: ./gradlew bootJar
- Run locally (dev): ./gradlew bootRun
- Build OCI image (buildpacks): ./gradlew bootBuildImage
- Docker (multi-stage Dockerfile): docker build . -t qabul:local && docker run -p 8080:8080 qabul:local
- Compose (local dev): docker compose up --build (uses docker-compose.yml / .env)

Tests
- Run full test suite: ./gradlew test
- Run a single test class: ./gradlew test --tests "fully.qualified.TestClassName"
  Example: ./gradlew test --tests "uz.umft.qabul.service.AuthServiceTest"
- Run a single test method: ./gradlew test --tests "uz.umft.qabul.service.AuthServiceTest.shouldCreateSessionTokens"
- Native (GraalVM) builds/tests (optional): ./gradlew nativeCompile, ./gradlew nativeTest

Linting
- No static-linter (Checkstyle/Spotless/PMD) is configured in build.gradle.kts. Add project linter task if needed.

2) High-level architecture (big picture)
- Framework: Spring Boot (Spring Web MVC, Spring Security, Spring Data JPA), Java toolchain set to Java 25.
- Layering:
  - controller/api: REST endpoints (base: /api/v1/**)
  - controller.dto: request/response DTOs
  - service: business logic (AuthService, ApplicationService, ExamService, FileService, JwtService)
  - repository: Spring Data JPA interfaces (persistence)
  - entity: JPA models; database is initialized/migrated via Flyway (src/main/resources/db/migration)
  - integration: external integrations (OtpService uses a Telegram endpoint when configured)
  - config: security and app properties (JwtAuthenticationFilter, SecurityConfig, AuthProperties, FileProperties)
- Runtime:
  - Uses Flyway migrations at startup (src/main/resources/db/migration/V*_*.sql).
  - Dockerfile is multi-stage: build uses ./gradlew bootJar; runtime copies the built jar.

Authentication & session model
- OTP registration/reset: flow endpoints under /api/v1/auth/** (AuthController). OTPs are issued (OtpChallenge entity) and in local dev they are logged for convenience.
- JWT token model: JwtService centralizes tokens. Tokens include a "token_type" claim; supported types: access, refresh, registration, password_reset, download.
- Access tokens: short-lived JWTs; Refresh tokens: long-lived JWTs recorded in sessions table (sessions.token) and validated on refresh.
- Role mapping: User.type enum values map to Spring roles as ROLE_<NAME> (JwtAuthenticationFilter builds SimpleGrantedAuthority("ROLE_" + user.getType().name())).

File handling
- Files are stored under a configurable base dir (qabul.files.* / FileProperties). Certificate upload/download uses FileService and FileController; downloads may require a JWT download token (created by JwtService.createDownloadToken).

3) Key conventions and patterns
- Configuration properties prefixes to check first: qabul.auth.*, qabul.otp.*, qabul.files.* (bound to AuthProperties / FileProperties records).
- JWT claim conventions: token_type, user_id, role, file_id. Use JwtService constants when generating or validating tokens.
- Refresh tokens are persisted in sessions table; refresh flow must verify Session.status and expiry (SessionRepository usage).
- Controllers expose API under /api/v1; keep DTOs in controller.dto and mappers in service package when available.
- Flyway migrations are authoritative DB schema changes (src/main/resources/db/migration).
- Logs folder (logs/) is mounted by docker-compose.yml; check logs/application.log for OTP values in local dev.
- Project assumes Jakarta APIs (jakarta.* packages) and Spring Boot 4 dependencies.

4) Useful files to check before code changes
- build.gradle.kts (tooling & deps)
- src/main/java/uz/umft/qabul/{config,controller,service,repository,entity,integration}
- src/main/resources/db/migration (Flyway)
- src/main/resources/application.yaml and .env (defaults used by docker-compose)
- Dockerfile and docker-compose.yml
- HELP.md (contains build-image / native build notes)

5) AI/assistant artifacts
If anything needs more detail (examples for common edit patterns, preferred code style, or specific tests to run), say which area and Copilot will add it.
