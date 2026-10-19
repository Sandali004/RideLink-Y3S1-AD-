# RideLink Backend Microservices

RideLink is a backend ride-sharing platform developed for the SLIIT IT3130 Application Development group assignment. The system is divided into four independently developed Spring Boot microservices, each with its own responsibility, data store, tests, API documentation, Postman assets, and CI workflow.

## Project Status

- Four required microservices implemented
- Service-specific databases configured
- REST APIs documented with Swagger/OpenAPI
- Postman collections and environments included
- JWT authentication and role-based access implemented in Account Service
- Automated tests passing across all services
- Live Ride-to-Driver and Ride-to-Fare communication verified
- GitHub Actions CI configured for all four services

Last verified test total: **59 tests, 0 failures, 0 errors**.

## Microservices

| Service | Port | Main responsibility | Database | Build tool | Java |
|---|---:|---|---|---|---:|
| Account Service | `8081` | Registration, login, profiles, JWT and roles | MySQL | Maven | 17 |
| Driver & Vehicle Service | `8082` | Driver profiles, vehicles, availability and location | MySQL | Maven | 21 |
| Ride Management Service | `8083` | Ride lifecycle and service orchestration | MongoDB | Gradle Kotlin DSL | 17 |
| Fare & Payment Service | `8084` | Fare calculation, simulated payments and receipts | H2 file database | Maven | 17 |

All services currently use Spring Boot `4.1.1`.

## Verified Inter-Service Communication

### Driver validation

When a driver is assigned to a requested ride, Ride Management calls:

```http
GET http://localhost:8082/api/drivers/{driverId}
```

The assignment continues only when the driver exists, is active, and has `AVAILABLE` status. An invalid driver is rejected and the ride remains `REQUESTED`.

### Final fare calculation

When an in-progress ride is completed, Ride Management calls:

```http
POST http://localhost:8084/api/fares/final?rideId={rideId}&distanceKm={distanceKm}
```

The calculated fare is stored with the completed ride. The live workflow was verified with a distance of `5.0 km` and a final fare of `500.0`.

Ride identifiers shared between Ride Management and Fare & Payment are strings, matching MongoDB ride IDs.

## Ride Lifecycle

```text
REQUESTED -> ASSIGNED -> ACCEPTED -> IN_PROGRESS -> COMPLETED
     |           |
     +-----------+------------------------------> CANCELLED
```

Rules:

1. Only a `REQUESTED` ride can receive a driver.
2. Only an `ASSIGNED` ride can be accepted.
3. Only an `ACCEPTED` ride can be started.
4. Only an `IN_PROGRESS` ride can be completed.
5. Only `REQUESTED` or `ASSIGNED` rides can be cancelled.

## Repository Structure

```text
IT3130-RideLink/
|-- .github/workflows/
|-- account-service/
|-- driver-vehicle-service/
|-- fare-payment-service/
|-- ride-management-service/
`-- README.md
```

Each service directory contains its source code, tests, configuration, service README, and exported Postman files.

## Prerequisites

- Git
- Java 17
- Java 21 or a newer compatible JDK for local Driver Service execution
- MySQL Server
- MongoDB Community Server
- Postman, optional

Maven and Gradle do not need separate installations because wrapper scripts are included.

## Environment Variables

Never commit real passwords, tokens, or signing secrets.

### Account Service

| Variable | Required | Purpose |
|---|---:|---|
| `DB_PASSWORD` | Yes | MySQL password |
| `JWT_SECRET` | Yes | Secret used to sign JWT tokens |
| `DB_URL` | No | Account database URL |
| `DB_USERNAME` | No | MySQL username; defaults to `root` |
| `SERVER_PORT` | No | Defaults to `8081` |
| `JWT_EXPIRATION_MS` | No | Token lifetime |

### Driver & Vehicle Service

| Variable | Required | Purpose |
|---|---:|---|
| `DB_PASSWORD` | Yes | MySQL password |
| `DB_USERNAME` | No | MySQL username; defaults to `root` |

The default local schema is `ridelink_driver_db`.

### Ride Management Service

| Variable | Required | Purpose |
|---|---:|---|
| `MONGODB_URI` | No | Defaults to `mongodb://localhost:27017/ridelink_rides` |
| `DRIVER_VEHICLE_SERVICE_URL` | No | Defaults to `http://localhost:8082` |
| `FARE_PAYMENT_SERVICE_URL` | No | Defaults to `http://localhost:8084` |

### Fare & Payment Service

| Variable | Required | Purpose |
|---|---:|---|
| `DB_URL` | No | H2 JDBC URL |
| `DB_USERNAME` | No | Defaults to `sa` |
| `DB_PASSWORD` | No | Empty for the local H2 setup |

## Local Start-up Order

1. Start MySQL.
2. Start MongoDB.
3. Start Account Service.
4. Start Driver & Vehicle Service.
5. Start Fare & Payment Service.
6. Start Ride Management Service.

Ride Management should start after Driver & Vehicle and Fare & Payment when testing integrated workflows.

## Running the Services

Run the following commands from the repository root.

### Account Service

```powershell
.\account-service\mvnw.cmd -f '.\account-service\pom.xml' spring-boot:run
```

### Driver & Vehicle Service

```powershell
.\driver-vehicle-service\mvnw.cmd -f '.\driver-vehicle-service\pom.xml' spring-boot:run
```

### Fare & Payment Service

```powershell
.\fare-payment-service\mvnw.cmd -f '.\fare-payment-service\pom.xml' spring-boot:run
```

### Ride Management Service

```powershell
.\ride-management-service\gradlew.bat -p '.\ride-management-service' bootRun
```

## Swagger and OpenAPI

| Service | Swagger UI | OpenAPI JSON |
|---|---|---|
| Account | `http://localhost:8081/swagger-ui/index.html` | `http://localhost:8081/v3/api-docs` |
| Driver & Vehicle | `http://localhost:8082/swagger-ui/index.html` | `http://localhost:8082/v3/api-docs` |
| Ride Management | `http://localhost:8083/swagger-ui/index.html` | `http://localhost:8083/v3/api-docs` |
| Fare & Payment | `http://localhost:8084/swagger-ui/index.html` | `http://localhost:8084/v3/api-docs` |

## Main API Groups

| Service | API groups |
|---|---|
| Account | `/api/accounts`, `/api/accounts/login` |
| Driver & Vehicle | `/api/drivers`, `/api/vehicles` |
| Ride Management | `/api/rides` |
| Fare & Payment | `/api/fares`, `/api/payments`, `/api/receipts` |

Refer to each service README and Swagger UI for the complete endpoint contract.

## Automated Testing

Run tests from the repository root.

```powershell
.\account-service\mvnw.cmd -f '.\account-service\pom.xml' clean test
.\driver-vehicle-service\mvnw.cmd -f '.\driver-vehicle-service\pom.xml' clean test
.\fare-payment-service\mvnw.cmd -f '.\fare-payment-service\pom.xml' clean test
.\ride-management-service\gradlew.bat -p '.\ride-management-service' clean test
```

Verified results:

| Service | Tests | Failures | Errors |
|---|---:|---:|---:|
| Account Service | 21 | 0 | 0 |
| Driver & Vehicle Service | 13 | 0 | 0 |
| Ride Management Service | 15 | 0 | 0 |
| Fare & Payment Service | 10 | 0 | 0 |
| **Total** | **59** | **0** | **0** |

Tests use isolated test configuration and do not require production credentials.

## Postman

Exported Postman collections and environments are stored in:

```text
account-service/postman/
driver-vehicle-service/postman/
ride-management-service/postman/
fare-payment-service/postman/
```

The Ride collection covers the complete lifecycle and integrated fare calculation. The Fare collection covers fare estimation, final fare, payment, receipt, and negative scenarios. Exported environments contain only non-sensitive example values.

## Continuous Integration

GitHub Actions runs automated tests for each service.

| Workflow file | Service | Java |
|---|---|---:|
| `.github/workflows/account-service-ci.yml` | Account | 17 |
| `.github/workflows/driver-vehicle-service-ci.yml` | Driver & Vehicle | 21 |
| `.github/workflows/ride-management-service-ci.yml` | Ride Management | 17 |
| `.github/workflows/ci.yml` | Fare & Payment | 17 |

Pull requests should be merged only after the applicable CI checks pass.

## Security

- Passwords are stored using BCrypt in Account Service.
- Account Service issues signed JWT bearer tokens.
- Account endpoints use role-based authorization for `PASSENGER`, `DRIVER`, and `ADMIN`.
- Real passwords, database credentials, and JWT secrets are excluded from Git.
- Validation and structured error handling are implemented.
- Postman exports contain no real secrets or saved JWT values.

Account JWT security is currently local to Account Service. Centralized gateway authentication for all microservices is outside the present project scope.

## Git Workflow

Development uses feature branches and pull requests. Branch names follow this pattern:

```text
feature/{student-id}/{feature-name}
```

Changes are reviewed and tested before merging into `main`.

## Team Contributions

| Student ID | Member | Primary responsibility |
|---|---|---|
| IT24104020 | Hamdhi S.A | Driver & Vehicle Service; group integration and project finalization |
| IT24103976 | Bandara R.V.M.R.N | Account Service |
| IT24103927 | Hettiarachchi H.S.A | Ride Management Service |
| IT24103975 | Theertha H.A.S | Fare & Payment Service |

Final integration aligns the shared ride ID contract and connects Ride Management to Driver & Vehicle and Fare & Payment while preserving each member’s original service contribution.

## Current Limitations

- There is no API gateway or service-discovery server.
- Account JWT authorization is not yet propagated to every downstream service.
- Payment processing is simulated and does not contact a real payment provider.
- Driver availability is validated during assignment but is not automatically changed through every ride transition.
- Fare & Payment uses a local H2 file database.
- The project is designed for local academic demonstration rather than production deployment.

## Further Documentation

- [Account Service README](account-service/README.md)
- [Driver & Vehicle Service README](driver-vehicle-service/README.md)
- [Ride Management Service README](ride-management-service/README.md)
- [Fare & Payment Service README](fare-payment-service/README.md)

## Academic Context

This repository was developed as a group project for the SLIIT IT3130 Application Development module.
