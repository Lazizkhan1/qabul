# Data Model: Application Management

## Application

Primary admission record owned by an applicant.

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `id` | UUID v7 | Yes | Primary key |
| `user_id` | UUID v7 | Yes | FK to `users.id` (applicant) |
| `tuition_id` | UUID v7 | Yes | FK to `tuition.id` |
| `status` | enum `PENDING`, `IN_REVIEW`, `ACCEPTED`, `CANCELED` | Yes | Initial state depends on auto-accept |
| `firstname` | varchar | Yes | Applicant first name |
| `lastname` | varchar | Yes | Applicant last name |
| `middlename` | varchar | No | Applicant middle name |
| `birth_date` | date | Yes | Applicant birth date |
| `gender` | char(1) | Yes | `M` / `F` |
| `jshshir` | varchar | Yes | National identifier |
| `passport_series` | varchar | Yes | Passport series/number |
| `address` | varchar | Yes | Residential address |
| `additional_phone` | varchar | No | Secondary contact |
| `disability` | int | Yes | Defaults to `0` |
| `created_at` | timestamp | Yes | Set on insert |
| `updated_at` | timestamp | Yes | Set on update |

### Validation Rules

- Required fields from FR-APP-001 must be present.
- Applicant can have only one non-`CANCELED` application at a time.
- `tuition_id` must reference an active tuition row.
- Initial `status` is `ACCEPTED` when auto-accept is ON, otherwise `PENDING`.

### Relationships

- One `Application` belongs to one applicant `User`.
- One `Application` references one `Tuition`.
- One `Application` has many `Cert` records.

### State Transitions

```text
PENDING -> IN_REVIEW
PENDING -> ACCEPTED
PENDING -> CANCELED
IN_REVIEW -> ACCEPTED
IN_REVIEW -> CANCELED
ACCEPTED -> (terminal)
CANCELED -> (terminal)
```

## Cert

Certificate metadata attached to an application.

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `id` | UUID v7 | Yes | Primary key |
| `application_id` | UUID v7 | Yes | FK to `application.id` |
| `category_id` | int | Yes | FK to `cert_category.id` |
| `cert_number` | varchar | Yes | Certificate number |
| `score` | numeric/double | Yes | Certificate score |
| `file_url` | varchar | Yes | Uploaded file location |

### Validation Rules

- At least one certificate is supported; each certificate must include number, score, file URL, and category.
- `category_id` must point to an existing certificate category.

## CertCategory (Reference)

Reference classification for certificates.

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `id` | int | Yes | Primary key |
| `title` | varchar | Yes | Category name |
| `type` | int | Yes | 1 = national, 2 = language |

## Tuition (Reference)

Existing offering chosen during submission.

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `id` | UUID v7 | Yes | Primary key |
| `major_id` | int | Yes | Major reference |
| `major_type_id` | int | Yes | Major type reference |
| `major_lang_id` | int | Yes | Language reference |
| `degree` | enum | Yes | Degree level |
| `school_year` | int | Yes | School year reference |
| `amount` | numeric | Yes | Tuition amount |

### Validation Rules

- Submission must reject inactive/deleted tuition references.

## ApplicationSetting

Durable runtime setting for auto-accept behavior.

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `key` | varchar | Yes | Primary key, e.g. `AUTO_ACCEPT_APPLICATIONS` |
| `value_boolean` | boolean | Yes | Current toggle state |
| `updated_at` | timestamp | Yes | Last update time |
| `updated_by` | UUID v7 | No | Staff user who changed setting |

### Validation Rules

- Only `ADMIN`/`MODERATOR` can update auto-accept.
- Toggle applies only to new submissions; existing applications remain unchanged.
