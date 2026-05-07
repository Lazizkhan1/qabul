# Quickstart: Application Management

## Prerequisites

- OpenJDK 25
- PostgreSQL 18
- Gradle wrapper from this repository
- Existing authentication endpoints from feature `001-qabul-auth`

## Environment

Set runtime configuration before starting the service:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/qabul
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=123
QABUL_AUTH_JWT_SECRET=change-me-in-local-dev-change-before-production
```

## Run

```bash
./gradlew bootRun
```

## 1) Applicant submits an application

```bash
curl -X POST http://localhost:8080/api/v1/applications \
  -H "Authorization: Bearer <applicant-access-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "firstname":"Ali",
    "lastname":"Valiyev",
    "middlename":"Rustam o''g''li",
    "birth_date":"2003-04-15",
    "gender":"M",
    "jshshir":"12345678901234",
    "passport_series":"AA1234567",
    "address":"Tashkent, Yunusobod",
    "additional_phone":"998901112233",
    "disability":0,
    "tuition_id":"11111111-1111-7111-8111-111111111111",
    "certificates":[
      {
        "cert_number":"NAT-2026-0001",
        "score":78.5,
        "file_url":"https://files.example/cert-1.pdf",
        "category_id":1
      }
    ]
  }'
```

Expected: `201 Created` with application payload and status `PENDING` (or `ACCEPTED` if auto-accept is enabled).

## 2) Applicant reads own application

```bash
curl -X GET http://localhost:8080/api/v1/applications/me \
  -H "Authorization: Bearer <applicant-access-token>"
```

## 3) Moderator/admin lists applications with filters

```bash
curl -G http://localhost:8080/api/v1/applications \
  -H "Authorization: Bearer <moderator-access-token>" \
  --data-urlencode "page=1" \
  --data-urlencode "limit=20" \
  --data-urlencode "status=PENDING" \
  --data-urlencode "school_year=2026"
```

Expected response shape:

```json
{
  "page": 1,
  "limit": 20,
  "total": 1,
  "data": []
}
```

## 4) Moderator accepts or rejects

```bash
curl -X POST http://localhost:8080/api/v1/applications/<application-id>/accept \
  -H "Authorization: Bearer <moderator-access-token>"

curl -X POST http://localhost:8080/api/v1/applications/<application-id>/reject \
  -H "Authorization: Bearer <moderator-access-token>"
```

## 5) Toggle auto-accept

```bash
curl -X PATCH http://localhost:8080/api/v1/applications/settings/auto-accept \
  -H "Authorization: Bearer <moderator-access-token>" \
  -H "Content-Type: application/json" \
  -d '{"enabled": true}'
```

Expected: subsequent submissions are immediately created with status `ACCEPTED`; existing rows stay unchanged.

## Integration Tests

```bash
./gradlew test
```
