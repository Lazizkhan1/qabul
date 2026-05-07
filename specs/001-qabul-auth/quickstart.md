# Quickstart: Authentication

## Prerequisites

- OpenJDK 25
- Docker and Docker Compose for PostgreSQL 18 in local development
- Gradle wrapper from this repository

## Environment

Set the following variables before running the service:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/qabul
SPRING_DATASOURCE_USERNAME=qabul
SPRING_DATASOURCE_PASSWORD=qabul
QABUL_AUTH_JWT_SECRET=change-me-in-local-dev
QABUL_AUTH_ACCESS_TOKEN_TTL=PT15M
QABUL_AUTH_REFRESH_TOKEN_TTL=P7D
QABUL_AUTH_OTP_TTL=PT5M
QABUL_AUTH_ADMIN_USERNAME=admin
QABUL_AUTH_ADMIN_PASSWORD=admin-password
```

## Run

```bash
./gradlew bootRun
```

On startup, the service upserts the admin user from `QABUL_AUTH_ADMIN_USERNAME` and `QABUL_AUTH_ADMIN_PASSWORD`.

## Applicant Registration Flow

1. Submit phone number:

   ```bash
   curl -X POST http://localhost:8080/api/v1/auth/applicant/start \
     -H 'Content-Type: application/json' \
     -d '{"phone_number":"998901234567"}'
   ```

2. Read the 6-digit OTP from the local application console.

3. Verify OTP:

   ```bash
   curl -X POST http://localhost:8080/api/v1/auth/applicant/verify-otp \
     -H 'Content-Type: application/json' \
     -d '{"phone_number":"998901234567","otp":"123456"}'
   ```

4. Set password and receive tokens:

   ```bash
   curl -X POST http://localhost:8080/api/v1/auth/applicant/set-password \
     -H 'Content-Type: application/json' \
     -H 'Authorization: Bearer <verification-token>' \
     -d '{"password":"StrongPassword123!"}'
   ```

## Applicant Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/applicant/login \
  -H 'Content-Type: application/json' \
  -d '{"phone_number":"998901234567","password":"StrongPassword123!"}'
```

## Applicant Password Reset

1. Request OTP:

   ```bash
   curl -X POST http://localhost:8080/api/v1/auth/applicant/reset/start \
     -H 'Content-Type: application/json' \
     -d '{"phone_number":"998901234567"}'
   ```

2. Read the 6-digit OTP from the local application console.

3. Verify OTP:

   ```bash
   curl -X POST http://localhost:8080/api/v1/auth/applicant/reset/verify-otp \
     -H 'Content-Type: application/json' \
     -d '{"phone_number":"998901234567","otp":"123456"}'
   ```

4. Set a new password and receive tokens:

   ```bash
   curl -X POST http://localhost:8080/api/v1/auth/applicant/reset/set-password \
     -H 'Content-Type: application/json' \
     -H 'Authorization: Bearer <reset-token>' \
     -d '{"password":"NewStrongPassword123!"}'
   ```

## Staff Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/staff/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin-password"}'
```

## Refresh Access Token

```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H 'Cookie: refresh_token=<refresh-token>'
```

## Integration Tests

```bash
./gradlew test
```

Current workspace validation:

```bash
GRADLE_USER_HOME=/tmp/gradle ./gradlew --no-daemon test
```

Result on 2026-05-06: PASS.

Required integration coverage:

- New applicant phone starts OTP registration.
- Latest OTP replaces earlier OTP for the same phone.
- Valid OTP allows password creation and token issuance.
- Known applicant phone routes to password login.
- Applicant login returns JWT claims with `user_id` and `role=APPLICANT`.
- Staff login returns admin/moderator role tokens.
- Refresh token returns a new access token.
- Expired or invalid credentials return structured errors.
- Startup upserts configured admin credentials.
