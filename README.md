# Stock Market Service

A simplified stock exchange simulation built with Spring Boot. The project implements a highly available, stateless backend architecture supported by a PostgreSQL database.

## Architecture and Technical Decisions

* **High Availability & Stateless Nodes:** The application runs as multiple replicas behind an Nginx load balancer. The application layer is entirely stateless; all persistent state is managed by PostgreSQL. The system survives node failures seamlessly (which can be tested via the `/chaos` endpoint).
* **Concurrency Handling:** To prevent race conditions during simultaneous buy/sell operations, the system utilizes pessimistic locking (`SELECT ... FOR UPDATE`) alongside database-level unique constraints.
* **Data Validation:** Input validation is handled natively via Spring Validation (`jakarta.validation`), keeping the business services clean and centralizing error handling via `@ControllerAdvice`.
* **Containerization:** The project uses a multi-stage Docker build. You do not need Maven or a local JDK installed on your host machine to compile and run the application.
* **Testing:** The test suite includes standard unit tests and integration tests. Integration tests use Testcontainers to spin up a real PostgreSQL instance, ensuring that queries, transactions, and locks are tested against the actual database engine used in production.

## Prerequisites

* Docker and Docker Compose v2 installed.

## Running the Application

The application can be started with a single command and binds to a parameterized port. It spins up a PostgreSQL database, an Nginx load balancer, and two instances of the Spring Boot application.

**Linux / macOS**
```bash
PORT=8080 docker compose up --build --scale app=2
```
**Windows (PowerShell)**
```powershell
$env:PORT=8080; docker compose up --build --scale app=2
```

**Windows (cmd)**
```bash
set PORT=8080 && docker compose up --build --scale app=2
```

## API Documentation

Once the containers are running, you can explore and interact with the endpoints using the Swagger UI:
http://localhost:8080/swagger-ui.html

(Adjust the port in the URL if you provided a different PORT parameter during startup).

## Running Tests

If you wish to run the test suite locally (this requires Java 21 and Maven installed on your host machine):
```bash
mvn verify
```

This command executes both unit tests and Testcontainers-backed integration tests. Ensure your Docker daemon is running before execution.