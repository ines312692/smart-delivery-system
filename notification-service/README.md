# Notification Service

The Notification Service consumes domain events from Kafka and sends customer notifications via Email, SMS, and Push. It centralizes all outbound communications related to orders, payments, and deliveries.

## Key Features
- Kafka consumers for order, payment, and delivery events
- Multi‑channel notifications: Email, SMS, Push (stubs with clear prod integration points)
- Retry mechanism for failed notifications (scheduled job)
- Spring Actuator for operational observability
- JPA repository for persisting notification records and statistics

## Architecture Overview
- Kafka listeners translate incoming events into notification intents
- NotificationService orchestrates channel selection and template data
- Channel services handle delivery specifics (Email/SMS/Push)
- Retry service periodically reprocesses failed notifications

Packages:
- config
  - `KafkaConsumerConfig` – Kafka consumer factory and listener container
  - `EmailConfig` – JavaMailSender configuration (dev defaults; provide env overrides)
- listener
  - `OrderEventListener` – consumes order.created/order.cancelled
  - `PaymentEventListener` – consumes payment.completed/payment.failed
  - `DeliveryEventListener` – consumes delivery.assigned/in-transit/completed
- service
  - `NotificationService` – core orchestration and persistence
  - `EmailNotificationService` – plain and HTML email (simulated by default)
  - `SmsNotificationService` – SMS sending stub (integrate Twilio/SNS in prod)
  - `PushNotificationService` – push sending stub (integrate FCM in prod)
  - `NotificationRetryService` – scheduled retries and hourly stats log
- repository
  - `NotificationRepository` – Spring Data JPA queries and stats
- dto
  - `OrderEvent`, `PaymentEvent`, `DeliveryEvent` – event payloads used by listeners

Note: Some classes currently reference model/dto packages as `com.delivery.notification.*`. Ensure these align with the actual shared DTO/model packages in your build. Adjust imports if you consolidate under the `common` module.

## Prerequisites
- Java 17+
- Maven 3.8+
- Kafka broker reachable per your configuration
- SMTP provider (for real email) and optional SMS/Push providers for production
- (Optional) PostgreSQL if using persistent storage (JPA)

## Configuration
Application properties live under `src/main/resources/application.properties`. Typical settings (use application.yml if preferred):

- Spring basics
  - `server.port=8084`
  - `spring.application.name=notification-service`
- Kafka
  - `spring.kafka.bootstrap-servers=localhost:9092`
  - `spring.kafka.consumer.group-id=notification-service-group`
  - `spring.kafka.consumer.auto-offset-reset=earliest`
  - `spring.kafka.consumer.properties.spring.json.trusted.packages=*`
- Database (if JPA enabled)
  - `spring.datasource.url=jdbc:postgresql://localhost:5432/notifications`
  - `spring.datasource.username=notifications`
  - `spring.datasource.password=notifications`
  - `spring.jpa.hibernate.ddl-auto=update`
- Mail (override defaults from EmailConfig)
  - `spring.mail.host=smtp.gmail.com`
  - `spring.mail.port=587`
  - `spring.mail.username=your-email@example.com`
  - `spring.mail.password=your-app-password`
  - `spring.mail.properties.mail.smtp.auth=true`
  - `spring.mail.properties.mail.smtp.starttls.enable=true`
- Actuator (recommended)
  - `management.endpoints.web.exposure.include=health,info,metrics,prometheus`
  - `management.endpoint.health.show-details=always`

Adjust according to your environment. The monitoring-service’s HealthCheckService assumes this service runs on port 8084 by default.

## Kafka
Listeners and topics used by this service:
- Order events
  - `order.created` → send order confirmation email
  - `order.cancelled` → send cancellation email
- Payment events
  - `payment.completed` → send success email + optional SMS
  - `payment.failed` → send failure email
- Delivery events
  - `delivery.assigned` → notify assignment
  - `delivery.in-transit` → notify progress
  - `delivery.completed` → notify completion

Event payloads are represented by the DTOs in this module (or shared via the `common` module if aligned): `OrderEvent`, `PaymentEvent`, `DeliveryEvent`.

## REST/Actuator
- Operational endpoints via Spring Actuator
  - Health: `/actuator/health`
  - Metrics: `/actuator/metrics`
  - Info: `/actuator/info`
  - Prometheus (if enabled): `/actuator/prometheus`

This service does not expose public REST APIs for sending notifications manually by default; notifications are triggered from Kafka events. If needed, add a controller for manual triggers or admin queries.

## Persistence Model
When persistence is enabled, notifications are stored with status updates for tracking and retry:
- `Notification` fields may include: id, recipient, subject, body, notificationType (EMAIL/SMS/PUSH), status (PENDING/SENT/FAILED/RETRY), retryCount, maxRetries, createdAt, sentAt, errorMessage, entityType, entityId.
- `NotificationRepository` exposes common queries and statistics.

## Retry Strategy
- `NotificationRetryService`
  - Scans for FAILED notifications with remaining retries every 5 minutes
  - Attempts resend and updates status to SENT/FAILED
  - Logs hourly stats for PENDING/SENT/FAILED counts

Tune schedule via `@Scheduled` expressions or externalize with properties if needed.

## Build
From repository root:
- `mvn -q -DskipTests package`

From this module only:
- `mvn -q -DskipTests package -pl notification-service -am`

## Run
- `mvn spring-boot:run -pl notification-service`

Or run the generated jar:
- `java -jar notification-service/target/notification-service-*.jar`

Ensure Kafka and SMTP (and DB if configured) are reachable before starting the service.

## Development
- Start Kafka locally (e.g., Docker Compose) and set `spring.kafka.bootstrap-servers`
- Configure mail/SMS/Push provider credentials via env vars or properties
- Enable/disable JPA depending on persistence needs
- Tests: `mvn test -pl notification-service`
- Logs: adjust logging in `application.properties` as needed

## Troubleshooting
- Kafka not consuming: verify `bootstrap-servers`, group-id, topic names, and consumer config
- Emails not delivered: verify SMTP creds and allow less secure/app passwords where applicable
- SMS/Push not sent: integrate a real provider (Twilio/SNS/FCM) and configure credentials
- Retries not happening: ensure `@EnableScheduling` is active in your app and cron/delay expressions are correct
- DB errors: verify JDBC URL, credentials, and driver

## See Also
- Root `README.md` for overall architecture and project setup
- `docs/KAFKA.md` and `docs/RUNBOOK.md` for end‑to‑end flows and ops guidance
- `monitoring-service` README for dashboard and observability details
