# Monitoring Service

The Monitoring Service provides real-time visibility into the Smart Delivery System. It collects system health, service metrics, and domain events (via Kafka), stores them for querying, and streams updates to clients over WebSockets for live dashboards.

## Key Features
- Consume all domain events from Kafka and persist to an Event Log
- Periodic health checks for dependent services (via HTTP/Actuator)
- System metrics collection (CPU, memory, uptime)
- WebSocket broadcasting for real-time dashboards
- REST endpoints to query health, metrics, and event logs
- Spring Actuator for operational endpoints

## Architecture Overview
- Kafka consumer reads events and maps them to `EventLog` entries
- Scheduled services collect `SystemMetrics` and `ServiceHealth`
- WebSocket gateway pushes updates to subscribed clients
- Repositories provide read access for REST endpoints

Packages:
- config
  - `KafkaConsumerConfig` – Kafka consumer/serde setup
  - `WebSocketConfig` – STOMP/WebSocket endpoints
  - `CorsConfig` – CORS for dashboard clients
- controller
  - `DashboardController` – REST API for metrics/health/events
- entity
  - `EventLog`, `ServiceHealth`, `SystemMetrics`
- listner
  - `AllEventsListener` – Kafka listener consuming events
- repository
  - Spring Data repositories for entities
- serive
  - `MonitoringService`, `MetricsCollectorService`, `HealthCheckService`, `WebSocketService`

Note: Some package names contain typos (`listner`, `serive`) in the current codebase; keep imports consistent with the existing structure.

## Prerequisites
- Java 17+
- Maven 3.8+
- Kafka broker reachable according to your configuration
- (Optional) A relational database if JPA is configured (check `application.properties`)

## Configuration
Application properties are under `src/main/resources/application.properties`.
Common properties you may need to set via environment variables or properties files:

- Spring basics
  - `server.port=8083` (example)  
  - `spring.application.name=monitoring-service`
- Kafka
  - `spring.kafka.bootstrap-servers=localhost:9092`
  - `spring.kafka.consumer.group-id=monitoring-service`
  - `spring.kafka.consumer.auto-offset-reset=latest`
- Database (if using JPA / persistent storage)
  - `spring.datasource.url=jdbc:postgresql://localhost:5432/monitoring`
  - `spring.datasource.username=monitoring`
  - `spring.datasource.password=monitoring`
  - `spring.jpa.hibernate.ddl-auto=update`
- Actuator (recommended)
  - `management.endpoints.web.exposure.include=health,info,metrics,prometheus`
  - `management.endpoint.health.show-details=always`

Adjust according to your environment. For module-specific overrides, use `application.yml` or profiles.

## Build
From repository root:
- `mvn -q -DskipTests package`

From this module only:
- `mvn -q -DskipTests package -pl monitoring-service -am`

## Run
- `mvn spring-boot:run -pl monitoring-service`

Or run the generated jar:
- `java -jar monitoring-service/target/monitoring-service-*.jar`

Ensure Kafka and the database (if configured) are running before starting the service.

## Kafka
- Topics: consult `common` module constants (e.g., `KafkaTopics`) and your environment. The `AllEventsListener` is designed to subscribe to a catch-all or multiple topics of interest (orders, payments, notifications). Update topic configuration in `KafkaConsumerConfig` or properties.
- Payloads/DTOs: shared event DTOs are under `common` module (e.g., `OrderEvent`, `PaymentEvent`, `DeliveryEvent`).

## WebSocket
- Endpoint: `/ws` (configured in `WebSocketConfig`)
- STOMP application destination prefix: typically `/app`
- Broker destination prefix: typically `/topic`
- Example topics:
  - `/topic/metrics`
  - `/topic/health`
  - `/topic/events`

A dashboard client can subscribe to these topics to receive real-time updates.

## REST Endpoints
The `DashboardController` exposes read endpoints, commonly including (actual paths may vary; check the controller):
- `GET /api/metrics` – latest system metrics
- `GET /api/health` – service health snapshot
- `GET /api/events` – recent event logs with pagination and filters

## Data Model
- `EventLog`: id, type, payload, timestamp, source
- `ServiceHealth`: service name, status, last check, response time
- `SystemMetrics`: cpu, memory, uptime, load, timestamp

Refer to the `entity` package for exact fields.

## Actuator
- Health: `/actuator/health`
- Metrics: `/actuator/metrics`
- Info: `/actuator/info`
- Prometheus (if enabled): `/actuator/prometheus`

Expose actuator endpoints via `management.endpoints.web.exposure.include` as shown above.

## Development
- Run Kafka locally (e.g., via Docker Compose) and set `spring.kafka.bootstrap-servers` accordingly
- Use profile-specific configuration for local vs. production
- Tests: `mvn test -pl monitoring-service`
- Logs: configure log levels in `application.properties` for troubleshooting consumers and schedulers

## Troubleshooting
- Kafka connection issues: verify `bootstrap-servers`, security configs, and topic existence
- No events recorded: ensure listener group-id is unique and topics are correctly configured
- WebSocket not receiving updates: confirm STOMP endpoint, CORS settings, and that `WebSocketService` publishes to `/topic` destinations
- Database errors: verify JDBC URL, credentials, and driver on the classpath

## See Also
- Root `README.md` for overall architecture and project setup
- `common` module for shared DTOs and constants used by the monitoring service
