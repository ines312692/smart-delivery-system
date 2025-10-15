Smart Delivery System Runbook

Purpose
- This runbook provides step-by-step instructions to install prerequisites, configure the environment, run the project locally and with Docker, and verify that each microservice works. It also includes common troubleshooting tips.

Audience
- Developers, DevOps engineers, and QA who want to build and run the system on Windows, macOS, or Linux.

System Components
- common: Shared DTOs, constants, and utilities.
- order-service: REST API for order lifecycle. Produces Kafka events.
- payment-service: Listens to order events, processes payments, produces events.
- notification-service: Consumes events and sends notifications (email/SMS/push; sample implementations).
- monitoring-service: Aggregates events, service health, and metrics; provides a simple dashboard.

1. Prerequisites
- Java: Java 17 (Temurin or Oracle JDK). Verify: java -version
- Maven: Included via Maven Wrapper (mvnw); separate Maven install optional. Verify: ./mvnw -v (Linux/macOS) or .\mvnw.cmd -v (Windows)
- Docker: For containerized runs. Verify: docker --version
- Docker Compose (optional): If you prefer orchestrating dependencies (Kafka, DB) locally.
- Git: To clone the repository. Verify: git --version
- Kafka: Local Kafka broker is required when running services that publish/consume events. You can use Docker to run Kafka (instructions below).
- Database: PostgreSQL (recommended) or MySQL. Example uses PostgreSQL.

2. Clone the Repository
- git clone https://github.com/your-repo/smart-delivery-system.git
- cd smart-delivery-system

3. Quick Start Matrix
- Local, in-process: Run with Java/Maven only (requires local Kafka and DB)
- Local with Docker: Run each service in Docker containers (requires Docker, optional external Kafka/DB)
- Hybrid: Run Kafka and DB in Docker; run services via Maven locally for easier debugging

4. Set Up External Dependencies
4.1 Start PostgreSQL via Docker
- docker run --name sds-postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_USER=postgres -e POSTGRES_DB=smart_delivery -p 5432:5432 -d postgres:15
- Connection URL: jdbc:postgresql://localhost:5432/smart_delivery

4.2 Start Kafka via Docker (single broker, with Zookeeper)
- Create a docker network: docker network create sds-net
- Zookeeper: docker run -d --name zookeeper --network sds-net -p 2181:2181 confluentinc/cp-zookeeper:7.5.0 zookeeper-server-start /etc/kafka/zookeeper.properties
- Kafka: docker run -d --name kafka --network sds-net -p 9092:9092 -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 confluentinc/cp-kafka:7.5.0
- Verify broker: docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 --list

Note: You may also use bitnami images or docker-compose; versions above are examples.

5. Configuration
- Each service has application.properties/yml. Minimal local overrides:

5.1 order-service (order-service/src/main/resources/application.yml)
- spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/smart_delivery
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: update
  kafka:
    bootstrap-servers: localhost:9092

5.2 payment-service (payment-service/src/main/resources/application.yml)
- spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/smart_delivery
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: update
  kafka:
    bootstrap-servers: localhost:9092

5.3 notification-service (notification-service/src/main/resources/application.properties)
- spring.kafka.bootstrap-servers=localhost:9092
- Configure email/SMS providers if used; defaults log notifications.

5.4 monitoring-service (monitoring-service/src/main/resources/application.properties)
- spring.kafka.bootstrap-servers=localhost:9092

6. Build the Project
- Windows: .\mvnw.cmd clean install
- Linux/macOS: ./mvnw clean install

7. Run Locally with Maven (Hybrid recommended)
7.1 Start dependencies (Kafka, Postgres) using Docker as above.
7.2 Run services (new terminals per service):
- order-service: .\mvnw.cmd spring-boot:run -pl order-service (Windows) or ./mvnw spring-boot:run -pl order-service
- payment-service: .\mvnw.cmd spring-boot:run -pl payment-service
- notification-service: .\mvnw.cmd spring-boot:run -pl notification-service
- monitoring-service: .\mvnw.cmd spring-boot:run -pl monitoring-service

Default ports (if unchanged):
- order-service: 8080
- payment-service: 8081
- notification-service: 8082
- monitoring-service: 8083

8. Run with Docker
8.1 Build images
- docker build -t sds/order-service ./order-service
- docker build -t sds/payment-service ./payment-service
- docker build -t sds/notification-service ./notification-service
- docker build -t sds/monitoring-service ./monitoring-service

8.2 Run containers (assumes Kafka and Postgres already running)
- docker run --rm --name order-service --network host -e SPRING_PROFILES_ACTIVE=docker -p 8080:8080 sds/order-service
- docker run --rm --name payment-service --network host -e SPRING_PROFILES_ACTIVE=docker -p 8081:8081 sds/payment-service
- docker run --rm --name notification-service --network host -e SPRING_PROFILES_ACTIVE=docker -p 8082:8082 sds/notification-service
- docker run --rm --name monitoring-service --network host -e SPRING_PROFILES_ACTIVE=docker -p 8083:8083 sds/monitoring-service

Note: If host network is not supported (e.g., Docker Desktop on Windows/macOS), replace --network host with -p mappings and set Kafka/Postgres host to host.docker.internal.

9. Smoke Tests
9.1 Create an order
- POST http://localhost:8080/api/orders
  Headers: Content-Type: application/json
  Body:
  {
    "customerName": "Alice",
    "items": [
      {"productId": "SKU-1", "quantity": 2, "price": 10.5},
      {"productId": "SKU-2", "quantity": 1, "price": 5.0}
    ],
    "paymentMethod": "CREDIT_CARD"
  }
- Expected: 201 Created, body includes order id and status
- Kafka: OrderEvent published; payment-service processes; notification-service logs notification; monitoring-service records event

9.2 Check payment-service logs
- Look for Payment processed for order <id>

9.3 Check notification-service logs
- Look for Notification sent for order <id>

10. Running Tests
- All modules: .\mvnw.cmd test (Windows) or ./mvnw test
- Single module: .\mvnw.cmd -pl payment-service test

11. CI/CD
- GitHub Actions workflows exist under .github/workflows for payment-service, order-service, notification-service. They build, test, and can publish images if configured.

12. Troubleshooting
- Port already in use: Change server.port in application.properties or stop the conflicting app.
- Kafka connection refused:
  - Ensure container is running and advertised listeners match client access (localhost:9092).
  - On Docker Desktop, prefer PLAINTEXT://host.docker.internal:9092 for containers accessing host broker.
- Database connection errors:
  - Verify credentials, ensure DB reachable. For Dockerized services, use host.docker.internal or container network names.
- Maven build fails due to JDK:
  - Ensure JAVA_HOME points to JDK 17+.
- Topic missing:
  - Create topics manually if auto-create is disabled: kafka-topics --bootstrap-server localhost:9092 --create --topic order-events --partitions 3 --replication-factor 1

13. Environment Variables and Profiles
- SPRING_PROFILES_ACTIVE:
  - local (default): uses application.yml
  - docker: uses application-docker.yml (present in payment-service and order-service)
- Override common props via env vars, e.g.:
  - SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD
  - SPRING_KAFKA_BOOTSTRAP_SERVERS

14. API Reference (order-service)
- POST /api/orders
- GET /api/orders/{id}
- GET /api/orders
- PUT /api/orders/{id}
- DELETE /api/orders/{id}

15. Clean Up
- Stop Java services: Ctrl+C in each terminal
- Stop Docker containers: docker stop order-service payment-service notification-service monitoring-service kafka zookeeper sds-postgres
- Remove containers: docker rm ...
- Remove network: docker network rm sds-net

16. Support
- Open an issue in the repository or email inestmimi1234@gamil.com
