# RideLink Account Service

The Account Service manages RideLink user registration, authentication, account profiles, account status, and role-based access control.

## Service Information

- Service name: `account-service`
- Default port: `8081`
- Database: MySQL
- Test database: H2 in-memory database
- Authentication: JWT Bearer token
- Password protection: BCrypt
- API documentation: Swagger/OpenAPI
- Build tool: Maven

## Main Features

- Register passenger and driver accounts
- Authenticate users and generate JWT tokens
- Retrieve and update account profiles
- Manage account status
- Delete accounts
- Role-based and owner-based authorization
- Request validation and consistent JSON error responses

## Technologies

- Java 17
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA
- Spring Security
- Jakarta Validation
- MySQL
- H2
- JJWT
- SpringDoc OpenAPI
- Maven
- JUnit 5 and Mockito

## Roles

The service supports:

- `PASSENGER`
- `DRIVER`
- `ADMIN`

Public registration allows passenger and driver accounts. An unauthenticated user cannot register an administrator account.

## Authorization Rules

| Operation | Access |
|---|---|
| Register account | Public |
| Login | Public |
| Get all accounts | Administrator only |
| Get account by ID | Account owner or administrator |
| Update account | Account owner or administrator |
| Change account status | Administrator only |
| Delete account | Administrator only |

## Prerequisites

- Java 17 or later
- MySQL 8
- Git
- Internet access for the first Maven dependency download

## Environment Variables

| Variable | Required | Description |
|---|---:|---|
| `DB_PASSWORD` | Yes | MySQL password |
| `JWT_SECRET` | Yes | Secret used to sign JWT tokens |
| `DB_URL` | No | MySQL connection URL |
| `DB_USERNAME` | No | MySQL username; default is `root` |
| `SERVER_PORT` | No | Service port; default is `8081` |
| `JWT_EXPIRATION_MS` | No | Token lifetime; default is `3600000` |

Use a random JWT secret of at least 64 characters. Never commit real passwords, tokens, or JWT secrets.

## Database

Default database URL:

```text
jdbc:mysql://localhost:3306/ridelink_account?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
```

The database is created automatically when the configured MySQL user has permission.

## Running the Service

Open PowerShell inside `account-service` and configure the required environment variables:

```powershell
$env:DB_PASSWORD = "<your-mysql-password>"
$env:JWT_SECRET = "<your-random-64-character-secret>"
```

Start the service:

```powershell
.\mvnw.cmd spring-boot:run
```

The service will be available at:

```text
http://localhost:8081
```

Environment variables must be configured in the same terminal used to start the service.

## API Endpoints

| Method | Endpoint | Description | Access |
|---|---|---|---|
| POST | `/api/accounts` | Register an account | Public |
| POST | `/api/accounts/login` | Login and receive a JWT | Public |
| GET | `/api/accounts` | Get all accounts | Administrator |
| GET | `/api/accounts/{id}` | Get an account | Owner or administrator |
| PUT | `/api/accounts/{id}` | Update an account | Owner or administrator |
| PATCH | `/api/accounts/{id}/status` | Change account status | Administrator |
| DELETE | `/api/accounts/{id}` | Delete an account | Administrator |

## Sample Registration

```json
{
  "name": "Test Passenger",
  "email": "passenger@example.com",
  "password": "TestPassword123",
  "role": "PASSENGER"
}
```

A successful registration returns `201 Created`.

## Sample Login

```json
{
  "email": "passenger@example.com",
  "password": "TestPassword123"
}
```

A successful login returns account information and a JWT token.

For protected endpoints, send:

```text
Authorization: Bearer <JWT_TOKEN>
```

The example credentials are test data only and must not be used in production.

## Account Status

Supported account statuses:

- `ACTIVE`
- `INACTIVE`

Only an administrator can change an account status. Inactive accounts cannot log in.

## Validation and Error Responses

The service validates names, email addresses, passwords, roles, and account statuses.

Common response codes:

| Status | Meaning |
|---:|---|
| `200` | Request completed |
| `201` | Account created |
| `204` | Account deleted |
| `400` | Invalid request |
| `401` | Missing, invalid, or expired JWT |
| `403` | Authenticated user lacks permission |
| `404` | Account not found |
| `409` | Email already exists |

## Swagger and OpenAPI

Swagger UI:

```text
http://localhost:8081/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8081/v3/api-docs
```

Use the Swagger **Authorize** button and enter:

```text
Bearer <JWT_TOKEN>
```

## Postman

The exported Postman files are located in:

```text
account-service/postman/
```

Files:

- `RideLink - Account Service.postman_collection.json`
- `RideLink - Account Service Environment.postman_environment.json`

The exported environment contains only non-sensitive example values. Insert a newly generated JWT when testing protected requests.

## Testing

Run all Account Service tests:

```powershell
.\mvnw.cmd test
```

Current automated test result:

```text
Tests run: 21, Failures: 0, Errors: 0, Skipped: 0
```

Tests cover service behavior, validation, authentication, owner authorization, administrator authorization, and negative security scenarios.

## Continuous Integration

The GitHub Actions workflow is:

```text
.github/workflows/account-service-ci.yml
```

It automatically builds the Account Service and runs its tests for configured pushes and pull requests.

## Security Notes

- Passwords are stored using BCrypt hashes.
- JWT tokens are validated for protected requests.
- The API uses stateless authentication.
- Account owners cannot access or update another user’s account.
- Administrator-only operations are protected by role checks.
- Real credentials and tokens must never be committed.