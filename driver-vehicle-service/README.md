# RideLink Driver & Vehicle Service

The Driver & Vehicle Service is one of the four RideLink microservices. It manages driver profiles, vehicle details, driver availability, current locations, and eligible-driver searches.

This service was developed using Java and Spring Boot. It maintains its own MySQL database named `ridelink_driver_db`.

## Main Features

- Register and manage drivers
- Register and manage vehicles
- Update driver availability
- Update driver location
- Find available drivers by service area
- Check licence expiry before returning available drivers
- Require an active vehicle for an available driver
- Soft-deactivate drivers and vehicles
- Validate API request data
- Provide Swagger/OpenAPI documentation
- Provide a Postman collection and environment
- Run automated tests through GitHub Actions

## Technology Stack

- Java 21
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA
- MySQL
- Hibernate
- Jakarta Bean Validation
- Lombok
- Springdoc OpenAPI/Swagger UI
- JUnit 5 and Mockito
- H2 database for tests
- Maven Wrapper
- GitHub Actions

## Service and Data Boundary

This microservice owns the following data:

- Driver profiles
- Driving licence information
- Driver service areas
- Driver availability
- Driver locations
- Vehicle information
- Driver-to-vehicle relationships

The service uses its own database:

```text
ridelink_driver_db
```

Other RideLink microservices must not directly access this database. They should communicate with this service through its REST API.

## Prerequisites

Install the following software before running the service:

- Java Development Kit 21
- MySQL Server 8 or later
- Git
- Postman, optional for API testing

The project includes the Maven Wrapper, so a separate Maven installation is not required.

## Database Setup

Start MySQL and create the service database:

```sql
CREATE DATABASE IF NOT EXISTS ridelink_driver_db;
```

The application uses Hibernate to create or update the required tables.

## Environment Variables

The database username and password are read from environment variables.

In PowerShell, set them before starting the application:

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "your_mysql_password"
```

Replace `your_mysql_password` with your actual MySQL password.

Do not place a real password inside `application.properties` or commit it to GitHub.

## Application Configuration

The main configuration file is:

```text
src/main/resources/application.properties
```

Important configuration values:

```properties
spring.application.name=driver-vehicle-service
server.port=8082
spring.datasource.url=jdbc:mysql://localhost:3306/ridelink_driver_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
```

## Start-up Order

For local development, use this order:

1. Start MySQL Server.
2. Confirm that `ridelink_driver_db` exists.
3. Set `DB_USERNAME` and `DB_PASSWORD`.
4. Start the Driver & Vehicle Service.
5. Open Swagger UI or Postman.

## Running the Application

Open PowerShell in the `driver-vehicle-service` directory.

Run all tests first:

```powershell
.\mvnw.cmd clean test
```

Start the application:

```powershell
.\mvnw.cmd spring-boot:run
```

The service runs at:

```text
http://localhost:8082
```

Stop the application by pressing:

```text
Ctrl + C
```

## Swagger and OpenAPI

After starting the application, open Swagger UI at:

```text
http://localhost:8082/swagger-ui.html
```

The OpenAPI JSON document is available at:

```text
http://localhost:8082/v3/api-docs
```

## Driver API Endpoints

Base path:

```text
/api/drivers
```

| Method | Endpoint | Description | Success status |
|---|---|---|---|
| POST | `/api/drivers` | Register a driver | `201 Created` |
| GET | `/api/drivers` | Get all active drivers | `200 OK` |
| GET | `/api/drivers/{driverId}` | Get an active driver by ID | `200 OK` |
| GET | `/api/drivers/available?serviceArea=Colombo` | Find eligible available drivers | `200 OK` |
| PUT | `/api/drivers/{driverId}` | Update driver details | `200 OK` |
| PATCH | `/api/drivers/{driverId}/availability` | Update availability | `200 OK` |
| PATCH | `/api/drivers/{driverId}/location` | Update location | `200 OK` |
| DELETE | `/api/drivers/{driverId}` | Soft-deactivate a driver | `204 No Content` |

## Vehicle API Endpoints

Base path:

```text
/api/vehicles
```

| Method | Endpoint | Description | Success status |
|---|---|---|---|
| POST | `/api/vehicles` | Register a vehicle for an active driver | `201 Created` |
| GET | `/api/vehicles` | Get all active vehicles | `200 OK` |
| GET | `/api/vehicles/{vehicleId}` | Get an active vehicle by ID | `200 OK` |
| GET | `/api/vehicles/driver/{driverId}` | Get active vehicles belonging to a driver | `200 OK` |
| PUT | `/api/vehicles/{vehicleId}` | Update vehicle details | `200 OK` |
| DELETE | `/api/vehicles/{vehicleId}` | Soft-deactivate a vehicle | `204 No Content` |

## Sample Driver Registration

Request:

```http
POST /api/drivers
Content-Type: application/json
```

Example request body:

```json
{
  "accountId": 1001,
  "licenseNumber": "B1234567",
  "licenseExpiryDate": "2027-12-31",
  "yearsOfExperience": 5,
  "serviceArea": "Colombo",
  "currentLatitude": 6.9271,
  "currentLongitude": 79.8612
}
```

A newly registered driver starts with the `OFFLINE` availability status.

## Sample Vehicle Registration

Register the driver first and use the returned driver ID:

```http
POST /api/vehicles
Content-Type: application/json
```

Example request body:

```json
{
  "driverId": 1,
  "registrationNumber": "CAB-1234",
  "make": "Toyota",
  "model": "Prius",
  "manufacturingYear": 2020,
  "color": "White",
  "vehicleType": "CAR",
  "seatCapacity": 4
}
```

Supported vehicle types:

- `CAR`
- `VAN`
- `THREE_WHEELER`
- `MOTORCYCLE`

## Sample Availability Update

```http
PATCH /api/drivers/1/availability
Content-Type: application/json
```

```json
{
  "availabilityStatus": "AVAILABLE"
}
```

Supported availability values:

- `AVAILABLE`
- `ON_TRIP`
- `OFFLINE`

## Sample Location Update

```http
PATCH /api/drivers/1/location
Content-Type: application/json
```

```json
{
  "latitude": 6.9271,
  "longitude": 79.8612
}
```

## Available Driver Rules

The available-driver endpoint returns a driver only when all these conditions are satisfied:

1. The driver is active.
2. The availability status is `AVAILABLE`.
3. The service area matches the requested service area.
4. The driving licence is not expired.
5. The driver has at least one active vehicle.

Example:

```text
GET http://localhost:8082/api/drivers/available?serviceArea=Colombo
```

## Typical API Responses

- `200 OK` – Request completed successfully
- `201 Created` – Driver or vehicle created successfully
- `204 No Content` – Resource deactivated successfully
- `400 Bad Request` – Request data failed validation
- `404 Not Found` – Active driver or vehicle was not found
- `409 Conflict` – Account ID, licence number, or registration number already exists

## Postman Collection and Environment

The exported Postman files are located at:

```text
postman/RideLink - Driver and Vehicle Service API.postman_collection.json
postman/RideLink Local Environment.postman_environment.json
```

The environment contains these non-sensitive example variables:

- `baseUrl`: `http://localhost:8082`
- `driverId`: `1`
- `vehicleId`: `1`
- `serviceArea`: `Colombo`

To use the files:

1. Open Postman.
2. Click **Import**.
3. Import the collection JSON file.
4. Import the environment JSON file.
5. Select **RideLink Local Environment** from the environment list.
6. Start the Driver & Vehicle Service.
7. Run **Register Driver** first.
8. Run **Register Vehicle** next.
9. Run the remaining requests as required.

The **Register Driver** request automatically saves the returned driver ID in `driverId`.

The **Register Vehicle** request automatically saves the returned vehicle ID in `vehicleId`.

No database passwords or sensitive credentials are stored in the Postman files.

## Testing

The project contains service-layer unit tests and an application context test.

Tests use an in-memory H2 database through the `test` profile. Therefore, local MySQL credentials are not required when running tests.

Run all tests:

```powershell
.\mvnw.cmd clean test
```

Current verified result:

```text
Tests run: 13
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

The tests cover successful and negative cases, including:

- Driver registration
- Duplicate account and licence validation
- Driver retrieval and updates
- Availability and location updates
- Available-driver filtering
- Active-vehicle eligibility
- Vehicle registration
- Duplicate registration-number validation
- Vehicle retrieval and updates
- Driver and vehicle deactivation
- Resource-not-found cases

## Continuous Integration

The GitHub Actions workflow is located at:

```text
.github/workflows/driver-vehicle-service-ci.yml
```

The workflow:

- Uses Java 21
- Uses the Maven Wrapper
- Runs `clean test`
- Runs when relevant service files are pushed
- Runs for relevant pull requests
- Can also be started manually

The GitHub Actions workflow must show a successful green result before merging the feature branch.

## Security Notes

- Database passwords are supplied through environment variables.
- Real credentials must never be committed to GitHub.
- Request data is checked using Jakarta Bean Validation.
- Duplicate account IDs, licence numbers, and registration numbers are rejected.
- The Postman environment contains only non-sensitive example values.
- Authentication and authorization should be enforced by the integrated RideLink security or API gateway component.

## Current Limitations

- This service does not provide a frontend application.
- Authentication and authorization are expected from the integrated gateway or security service.
- Driver location is updated through REST requests and is not streamed in real time.
- Integration with the other three RideLink microservices must be completed by the group.
- Production deployment configuration is outside the current local-development setup.

## Individual Contribution

**Student ID:** IT24104020

Main contribution:

- Driver management implementation
- Vehicle management implementation
- Validation and exception handling
- MySQL database configuration
- Swagger/OpenAPI documentation
- Postman collection and environment
- Unit and context tests
- GitHub Actions CI workflow
- Driver & Vehicle Service documentation