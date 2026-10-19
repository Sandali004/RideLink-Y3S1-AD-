# RideLink Ride Management Service

The Ride Management Service is one of the four RideLink backend microservices. It manages ride creation, retrieval, driver assignment, lifecycle transitions, cancellation, and completion.

The service owns its MongoDB ride data and communicates with the Driver & Vehicle Service and the Fare & Payment Service through REST APIs.

## Main Features

- Create and retrieve rides
- Enforce the ride lifecycle
- Cancel requested or assigned rides
- Validate a selected driver through the Driver & Vehicle Service
- Verify that the selected driver is active and `AVAILABLE`
- Calculate the final fare through the Fare & Payment Service
- Store completed-ride distance and final fare
- Validate request data and reject invalid transitions
- Return consistent error responses
- Publish Swagger/OpenAPI documentation
- Provide exported Postman collection and environment files
- Run automated unit and context tests
- Run CI tests through GitHub Actions

## Service Information

| Item | Value |
|---|---|
| Service name | `ride-management-service` |
| Port | `8083` |
| Database | MongoDB |
| Database name | `ridelink_rides` |
| Build tool | Gradle Kotlin DSL |
| Java version | 17 |
| API base path | `/api/rides` |

## Technology Stack

- Java 17
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data MongoDB
- Jakarta Bean Validation
- Spring `RestClient`
- Lombok
- Springdoc OpenAPI/Swagger UI
- JUnit 5
- Mockito
- Gradle Kotlin DSL
- Postman
- GitHub Actions

## Service and Data Boundary

This microservice owns:

- Ride identifier
- Passenger identifier
- Assigned driver identifier
- Pickup location
- Destination
- Ride status
- Completed distance
- Final fare
- Creation and update timestamps

The MongoDB database and collection are:

```text
Database: ridelink_rides
Collection: rides
```

Other RideLink services must not access this database directly. They communicate through REST APIs.

Ride identifiers are MongoDB String identifiers. The same String `rideId` contract is used when the Ride Management Service calls the Fare & Payment Service.

## Ride Lifecycle

```text
REQUESTED
    |
    v
ASSIGNED
    |
    v
ACCEPTED
    |
    v
IN_PROGRESS
    |
    v
COMPLETED
```

Cancellation is supported for:

```text
REQUESTED -> CANCELLED
ASSIGNED  -> CANCELLED
```

The service enforces these rules:

1. A driver can only be assigned to a `REQUESTED` ride.
2. The selected driver must be active and `AVAILABLE`.
3. Only an `ASSIGNED` ride can be accepted.
4. Only an `ACCEPTED` ride can be started.
5. Only an `IN_PROGRESS` ride can be completed.
6. Completion requires a distance of at least `0.1` km.
7. Only a `REQUESTED` or `ASSIGNED` ride can be cancelled.

Invalid lifecycle transitions return `400 Bad Request`.

## Prerequisites

Install or configure:

- Java Development Kit 17 or later
- MongoDB Community Server
- Git
- Postman, optional for API testing
- Driver & Vehicle Service for driver-assignment integration
- Fare & Payment Service for ride-completion integration

The Gradle Wrapper is included, so a separate Gradle installation is not required.

## Configuration

The main configuration file is:

```text
src/main/resources/application.properties
```

Default configuration:

```properties
spring.application.name=ride-management-service
server.port=8083

spring.mongodb.uri=${MONGODB_URI:mongodb://localhost:27017/ridelink_rides}

services.driver-vehicle.base-url=${DRIVER_VEHICLE_SERVICE_URL:http://localhost:8082}
services.fare-payment.base-url=${FARE_PAYMENT_SERVICE_URL:http://localhost:8084}
```

Supported environment variables:

| Variable | Required | Description |
|---|---:|---|
| `MONGODB_URI` | No | MongoDB connection URI; defaults to the local RideLink database |
| `DRIVER_VEHICLE_SERVICE_URL` | No | Driver & Vehicle Service base URL; defaults to `http://localhost:8082` |
| `FARE_PAYMENT_SERVICE_URL` | No | Fare & Payment Service base URL; defaults to `http://localhost:8084` |

No passwords, tokens, or other secrets are stored in this file.

## Local Start-up Order

For the integrated ride workflow, use this order:

1. Start MySQL and MongoDB.
2. Start the Driver & Vehicle Service on port `8082`.
3. Start the Fare & Payment Service on port `8084`.
4. Start the Ride Management Service on port `8083`.
5. Open Swagger UI or import the Postman files.
6. Ensure the Driver Service contains an active, `AVAILABLE` driver.
7. Run the ride workflow in lifecycle order.

For a complete four-service demonstration, also start the Account Service on port `8081`.

## Running the Application

Open PowerShell in `ride-management-service`.

Run all tests:

```powershell
.\gradlew.bat clean test
```

Start the application:

```powershell
.\gradlew.bat bootRun
```

The service runs at:

```text
http://localhost:8083
```

Press `Ctrl + C` to stop it.

## Swagger and OpenAPI

Swagger UI:

```text
http://localhost:8083/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8083/v3/api-docs
```

## API Endpoints

| Method | Endpoint | Description | Success |
|---|---|---|---|
| POST | `/api/rides` | Create a ride | `201 Created` |
| GET | `/api/rides` | Get all rides | `200 OK` |
| GET | `/api/rides/{rideId}` | Get a ride by ID | `200 OK` |
| PUT | `/api/rides/{rideId}/assign-driver/{driverId}` | Validate and assign a driver | `200 OK` |
| PUT | `/api/rides/{rideId}/accept` | Accept an assigned ride | `200 OK` |
| PUT | `/api/rides/{rideId}/start` | Start an accepted ride | `200 OK` |
| PUT | `/api/rides/{rideId}/complete?distanceKm={distance}` | Calculate fare and complete a ride | `200 OK` |
| PUT | `/api/rides/{rideId}/cancel` | Cancel a requested or assigned ride | `200 OK` |

## Create Ride

```http
POST /api/rides
Content-Type: application/json
```

Example body:

```json
{
  "passengerId": "P001",
  "pickup": "Malabe",
  "destination": "Colombo"
}
```

Example response:

```json
{
  "id": "68dc1234567890abcdef1234",
  "status": "REQUESTED",
  "passengerId": "P001",
  "driverId": null,
  "pickup": "Malabe",
  "destination": "Colombo",
  "distanceKm": null,
  "finalFare": null,
  "createdAt": "2026-10-04T00:00:00",
  "updatedAt": "2026-10-04T00:00:00"
}
```

## Get Rides

```http
GET /api/rides
GET /api/rides/{rideId}
```

## Assign Driver

```http
PUT /api/rides/{rideId}/assign-driver/{driverId}
```

Example:

```text
PUT http://localhost:8083/api/rides/68dc1234567890abcdef1234/assign-driver/1
```

Before assignment, the Ride Management Service calls:

```http
GET http://localhost:8082/api/drivers/1
```

Assignment succeeds only when the returned driver is active and has `AVAILABLE` status.

Required transition:

```text
REQUESTED -> ASSIGNED
```

## Accept and Start Ride

```http
PUT /api/rides/{rideId}/accept
PUT /api/rides/{rideId}/start
```

Required transitions:

```text
ASSIGNED -> ACCEPTED
ACCEPTED -> IN_PROGRESS
```

## Complete Ride

```http
PUT /api/rides/{rideId}/complete?distanceKm=5.0
```

Required transition:

```text
IN_PROGRESS -> COMPLETED
```

During completion, the service calls:

```http
POST http://localhost:8084/api/fares/final?rideId={rideId}&distanceKm=5.0
```

The ride changes to `COMPLETED` only after a successful fare response. The distance and returned `totalFare` are stored as `distanceKm` and `finalFare`.

Example response:

```json
{
  "id": "68dc1234567890abcdef1234",
  "status": "COMPLETED",
  "passengerId": "P001",
  "driverId": "1",
  "pickup": "Malabe",
  "destination": "Colombo",
  "distanceKm": 5.0,
  "finalFare": 500.0
}
```

## Cancel Ride

```http
PUT /api/rides/{rideId}/cancel
```

Allowed transitions:

```text
REQUESTED -> CANCELLED
ASSIGNED  -> CANCELLED
```

## Inter-Service Communication

Two meaningful HTTP interactions are implemented.

### 1. Driver validation

| Item | Value |
|---|---|
| Calling service | Ride Management Service |
| Target service | Driver & Vehicle Service |
| Target endpoint | `GET /api/drivers/{driverId}` |
| Trigger | Driver assignment |
| Validation | Driver must be active and `AVAILABLE` |
| Failure behaviour | Ride remains `REQUESTED`; error returned to the caller |

### 2. Final-fare calculation

| Item | Value |
|---|---|
| Calling service | Ride Management Service |
| Target service | Fare & Payment Service |
| Target endpoint | `POST /api/fares/final` |
| Trigger | Ride completion |
| Request data | String `rideId` and `distanceKm` |
| Success behaviour | Stores `distanceKm` and `finalFare`, then marks the ride `COMPLETED` |
| Failure behaviour | Ride remains `IN_PROGRESS`; it is not saved as completed |

These clients are implemented using Spring `RestClient` and configurable service base URLs.

## Request Validation

Ride creation requires:

- Non-empty passenger ID
- Non-empty pickup location
- Non-empty destination

Ride completion requires:

- An `IN_PROGRESS` ride
- `distanceKm` of at least `0.1`

Example validation response:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Distance must be at least 0.1 km"
}
```

## Error Handling

| Status | Meaning |
|---|---|
| `400 Bad Request` | Invalid request or lifecycle transition |
| `400 Bad Request` | Driver is inactive, unavailable, or cannot be validated |
| `400 Bad Request` | Fare cannot be calculated or the dependency is unavailable |
| `404 Not Found` | Ride ID does not exist |

Example not-found response:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Ride not found"
}
```

## Postman Collection and Environment

Exported files:

```text
postman/RideLink - Ride Management Service API.postman_collection.json
postman/RideLink Ride Local Environment.postman_environment.json
```

Environment variables:

| Variable | Example value |
|---|---|
| `baseUrl` | `http://localhost:8083` |
| `passengerId` | `P001` |
| `driverId` | `1` |
| `distanceKm` | `5.0` |
| `rideId` | Saved after Create Ride |
| `cancelRideId` | Saved after Create Ride for Cancellation |
| `invalidRideId` | Non-existing example ID |

Usage:

1. Import both JSON files into Postman.
2. Select `RideLink Ride Local Environment`.
3. Start MongoDB and the three services required by the integrated workflow.
4. Ensure Driver ID `1` exists, is active, and is `AVAILABLE`, or update `driverId`.
5. Run the requests in `Complete Ride Workflow` order.
6. Use the Negative Tests folder for failure evidence.

No real passwords, tokens, or credentials are stored in the Postman files.

## Testing

The verified test suite contains:

- 14 `RideServiceTest` unit tests
- 1 application-context test
- 15 total tests
- 0 failures, 0 errors, and 0 skipped tests

The tests cover:

- Normal ride creation and lifecycle transitions
- Missing rides
- Invalid lifecycle transitions
- Successful Driver Service validation
- Rejection of an unavailable driver
- Successful Fare Service calculation
- Invalid completion distance
- Fare Service failure without incorrectly completing the ride
- Requested and assigned ride cancellation

Run all tests:

```powershell
.\gradlew.bat clean test
```

Run only service tests:

```powershell
.\gradlew.bat test --tests "com.ridelink.rideservice.RideServiceTest"
```

Verified result:

```text
RideServiceTest: 14 tests, 0 failures, 0 errors
Application context: 1 test, 0 failures, 0 errors
BUILD SUCCESSFUL
```

## Continuous Integration

Workflow file:

```text
.github/workflows/ride-management-service-ci.yml
```

The workflow:

- Uses Java 17
- Uses the Gradle Wrapper
- Builds and tests the Ride Management Service
- Runs for relevant pushes and pull requests
- Supports manual execution

## Project Structure

```text
ride-management-service/
|-- postman/
|-- src/
|   |-- main/
|   |   |-- java/com/ridelink/rideservice/
|   |   |   |-- client/
|   |   |   |   |-- DriverVehicleClient.java
|   |   |   |   `-- FarePaymentClient.java
|   |   |   |-- controller/
|   |   |   |-- dto/
|   |   |   |-- exception/
|   |   |   |-- model/
|   |   |   |-- repository/
|   |   |   `-- service/
|   |   `-- resources/application.properties
|   `-- test/java/com/ridelink/rideservice/
|       |-- RideManagementServiceApplicationTests.java
|       `-- RideServiceTest.java
|-- build.gradle.kts
|-- settings.gradle.kts
|-- gradlew
|-- gradlew.bat
`-- README.md
```

## Security Notes

- No database password is committed.
- Service URLs are configurable using environment variables.
- Postman files contain non-sensitive example values only.
- Request bodies and business transitions are validated.
- Real passwords, JWTs, and credentials must never be committed.
- Platform-wide authentication is provided by the Account Service; direct enforcement across Ride endpoints remains a future gateway/shared-security task.

## Current Limitations

- Driver selection is supplied by ID; automatic nearest-driver selection is not implemented.
- Driver availability is validated but is not automatically changed to `ON_TRIP` or restored after completion.
- Ride completion calculates and stores the fare but does not automatically create a payment.
- Cross-service retry, circuit-breaker, and distributed tracing policies are not implemented.
- Ride endpoints do not yet enforce the Account Service JWT directly.

## Individual Contribution

**Student ID:** IT24103927  
**Student Name:** Hettiarachchi H.S.A

Original service contribution:

- Ride Management Service implementation
- Ride lifecycle implementation
- MongoDB data configuration
- Request validation
- Exception handling
- Swagger/OpenAPI configuration
- Unit and context tests
- GitHub Actions CI workflow
- Ride Management Service documentation
- Postman API verification

Cross-service clients, shared ID alignment, integrated tests, and final documentation updates were completed during the group project-finalization work. The Git history and final pull request preserve these contributions separately.
