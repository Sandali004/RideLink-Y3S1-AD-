# RideLink Fare & Payment Service

The Fare & Payment Service is one of the four RideLink backend microservices. It calculates estimated and final ride fares, processes simulated payments, and generates payment receipts.

The service owns its fare, payment, and receipt data. It also provides the final-fare API used by the Ride Management Service when a ride is completed.

## Service Information

| Item | Value |
|---|---|
| Service name | `fare-payment-service` |
| Port | `8084` |
| Database | H2 file database |
| Test database | H2 in-memory database |
| Build tool | Maven |
| Java version | 17 |

## Main Features

- Estimate a fare from ride distance
- Calculate the final ride fare
- Use a shared String `rideId` contract
- Process a simulated payment
- Generate unique transaction references
- Retrieve payments by ID
- Generate receipts from successful payments
- Retrieve receipts by ID
- Validate distance, amount, ride ID, and payment method
- Return not-found responses for missing payments and receipts
- Publish Swagger/OpenAPI documentation
- Provide Postman positive and negative workflows
- Run automated tests through GitHub Actions

## Technology Stack

- Java 17
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA
- Jakarta Bean Validation
- H2 Database
- Springdoc OpenAPI/Swagger UI
- Maven
- JUnit 5
- Mockito
- Postman
- GitHub Actions

## Service and Data Boundary

This service owns:

- Fare records
- Payment records
- Receipt records
- Payment status
- Transaction references

The service stores the Ride Management Service identifier as a String `rideId`. It does not access the Ride Management MongoDB database directly.

Primary identifiers remain numeric database-generated values:

| Entity | Primary ID | Shared ride reference |
|---|---|---|
| Fare | `Long` | String `rideId` |
| Payment | `Long` | String `rideId` |
| Receipt | `Long` | String `rideId` and `Long paymentId` |

## Prerequisites

- Java Development Kit 17 or later
- Git
- Postman, optional for API testing

The Maven Wrapper is included, so a separate Maven installation is not required.

## Database Configuration

The default database URL is:

```text
jdbc:h2:file:./data/farepaymentdb
```

Main configuration:

```properties
spring.application.name=fare-payment-service
server.port=8084

spring.datasource.url=${DB_URL:jdbc:h2:file:./data/farepaymentdb}
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=${DB_USERNAME:sa}
spring.datasource.password=${DB_PASSWORD:}

spring.jpa.hibernate.ddl-auto=update

spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

spring.jpa.show-sql=true
```

Supported environment variables:

| Variable | Required | Description |
|---|---:|---|
| `DB_URL` | No | Database connection URL |
| `DB_USERNAME` | No | Database username; defaults to `sa` |
| `DB_PASSWORD` | No | Database password; blank by default for local H2 |

The blank local H2 password is an intentional development default. No real password is committed.

## Test Database

Automated tests use an isolated H2 in-memory database configured in:

```text
src/test/resources/application.properties
```

The test database is recreated for test execution and does not modify local application data.

## Running the Service

From the repository root, run tests:

```powershell
.\fare-payment-service\mvnw.cmd `
-f ".\fare-payment-service\pom.xml" test
```

Start the application:

```powershell
.\fare-payment-service\mvnw.cmd `
-f ".\fare-payment-service\pom.xml" spring-boot:run
```

The service runs at:

```text
http://localhost:8084
```

Press `Ctrl + C` to stop it.

## API Endpoints

| Method | Endpoint | Description | Success |
|---|---|---|---|
| POST | `/api/fares/estimate?rideId={rideId}&distanceKm={distance}` | Estimate a fare | `200 OK` |
| POST | `/api/fares/final?rideId={rideId}&distanceKm={distance}` | Calculate a final fare | `200 OK` |
| POST | `/api/payments` | Process a simulated payment | `200 OK` |
| GET | `/api/payments/{id}` | Retrieve a payment | `200 OK` |
| POST | `/api/receipts/{paymentId}` | Generate a receipt | `200 OK` |
| GET | `/api/receipts/{id}` | Retrieve a receipt | `200 OK` |

## Fare Calculation

The current fare formula is:

```text
total fare = base fare + (distance in km x per-km rate)
```

Current values:

```text
Base fare: 100.00
Per-km rate: 80.00
```

For `5.0` km:

```text
100.00 + (5.0 x 80.00) = 500.00
```

## Estimate Fare

```http
POST /api/fares/estimate?rideId=RIDE-001&distanceKm=5.0
```

Example response:

```json
{
  "id": 1,
  "rideId": "RIDE-001",
  "distanceKm": 5.0,
  "baseFare": 100.0,
  "perKmRate": 80.0,
  "totalFare": 500.0
}
```

## Calculate Final Fare

```http
POST /api/fares/final?rideId=68dc1234567890abcdef1234&distanceKm=5.0
```

The Ride Management Service calls this endpoint when an `IN_PROGRESS` ride is completed. The returned `totalFare` is stored in the completed ride as `finalFare`.

The `rideId` is a String so it can accept the MongoDB identifier owned by the Ride Management Service.

## Process Payment

```http
POST /api/payments
Content-Type: application/json
```

Example request:

```json
{
  "rideId": "68dc1234567890abcdef1234",
  "amount": 500.0,
  "paymentMethod": "CARD"
}
```

Example response:

```json
{
  "id": 1,
  "rideId": "68dc1234567890abcdef1234",
  "amount": 500.0,
  "paymentMethod": "CARD",
  "paymentStatus": "SUCCESS",
  "transactionReference": "TXN-example-reference"
}
```

Payment processing is simulated. The service generates a transaction reference and returns `SUCCESS` without contacting an external provider.

## Retrieve Payment

```http
GET /api/payments/{paymentId}
```

A missing payment returns `404 Not Found`.

## Generate Receipt

After creating a payment, use its numeric payment ID:

```http
POST /api/receipts/{paymentId}
```

Example response:

```json
{
  "id": 1,
  "paymentId": 1,
  "rideId": "68dc1234567890abcdef1234",
  "amount": 500.0,
  "paymentMethod": "CARD",
  "transactionReference": "TXN-example-reference"
}
```

## Retrieve Receipt

```http
GET /api/receipts/{receiptId}
```

A missing receipt returns `404 Not Found`.

## Validation and Error Responses

The service validates:

- Fare `rideId` must not be blank
- Fare distance must be at least `0.1` km
- Payment `rideId` must not be blank
- Payment amount must be at least `0.01`
- Payment method must not be blank

Examples:

| Scenario | Status |
|---|---|
| Zero or negative fare distance | `400 Bad Request` |
| Negative payment amount | `400 Bad Request` |
| Empty payment method | `400 Bad Request` |
| Missing payment | `404 Not Found` |
| Missing receipt | `404 Not Found` |

## Ride Management Integration

The Ride Management Service calls this service during ride completion:

```http
POST /api/fares/final?rideId={stringRideId}&distanceKm={distance}
```

Integration contract:

| Field | Type | Description |
|---|---|---|
| `rideId` | String | Ride Management MongoDB ID |
| `distanceKm` | Double | Completed ride distance, minimum `0.1` |
| `totalFare` | Double | Calculated fare returned to Ride Management |

This is one of the two required meaningful RideLink inter-service interactions.

## Swagger and OpenAPI

Swagger UI:

```text
http://localhost:8084/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8084/v3/api-docs
```

## H2 Console

The local H2 console is available at:

```text
http://localhost:8084/h2-console
```

Default local values:

| Setting | Value |
|---|---|
| JDBC URL | `jdbc:h2:file:./data/farepaymentdb` |
| User | `sa` |
| Password | Blank |

These values are for local development only.

## Postman Collection and Environment

Exported files:

```text
postman/RideLink - Fare & Payment Service.postman_collection.json
postman/RideLink - Fare & Payment Service Environment.postman_environment.json
```

Environment variables:

| Variable | Example value |
|---|---|
| `baseUrl` | `http://localhost:8084` |
| `rideId` | `RIDE-001` |
| `distanceKm` | `5.0` |
| `amount` | Updated from the final-fare response |
| `paymentId` | Updated after payment creation |
| `receiptId` | Updated after receipt generation |

The collection contains:

- 6 happy-path requests
- 5 negative requests
- 11 total requests
- Automatic `amount`, `paymentId`, and `receiptId` updates
- No real passwords, tokens, or credentials

Run the Happy Path folder in request order. Run the Negative Scenarios folder separately for failure evidence.

## Testing

Run all tests:

```powershell
.\fare-payment-service\mvnw.cmd `
-f ".\fare-payment-service\pom.xml" test
```

Verified result:

```text
FarePaymentServiceApplicationTests: 1 test
FareServiceTest: 2 tests
PaymentServiceTest: 3 tests
ReceiptServiceTest: 4 tests

Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

The tests cover normal, boundary, and failure behaviour, including missing payments and receipts.

## Continuous Integration

Workflow file:

```text
.github/workflows/ci.yml
```

Workflow display name:

```text
Fare Payment Service CI
```

The workflow uses Java 17 and runs Maven tests for relevant repository events.

## Project Structure

```text
fare-payment-service/
|-- postman/
|-- src/
|   |-- main/
|   |   |-- java/fare_payment_service/
|   |   |   |-- controller/
|   |   |   |-- entity/
|   |   |   |-- exception/
|   |   |   |-- repository/
|   |   |   `-- service/
|   |   `-- resources/application.properties
|   `-- test/
|       |-- java/fare_payment_service/
|       `-- resources/application.properties
|-- pom.xml
|-- mvnw
|-- mvnw.cmd
`-- README.md
```

## Security Notes

- No real database credentials are committed.
- The default blank H2 password is only a local-development default.
- Postman files contain non-sensitive examples.
- Input values are validated before business processing.
- Real payment credentials must never be committed.
- Authentication is not yet enforced directly by this service.

## Current Limitations

- Payments are simulated and always return `SUCCESS` for valid input.
- No real payment gateway is connected.
- Fare values are fixed and do not include surge pricing, taxes, discounts, or currency conversion.
- Refunds and payment reversals are not implemented.
- The local application database is H2 rather than a production database.
- Retry, circuit-breaker, and distributed tracing policies are not implemented.
- The service does not directly enforce Account Service JWT authentication.

## Individual Contribution

**Student ID:** IT24103975
**Student Name:** Theertha H.A.S

Original service contribution:

- Fare calculation
- Payment processing
- Receipt generation
- JPA entities and repositories
- Validation and error handling
- Swagger/OpenAPI support
- Maven tests
- GitHub Actions CI workflow
- Fare & Payment Service documentation
- Postman API verification

String ride-ID alignment, expanded failure tests, isolated test configuration, integrated Postman workflow, and final documentation updates were completed during group project-finalization work. The Git history and final pull request preserve these contributions separately.
