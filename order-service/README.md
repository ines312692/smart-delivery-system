# Order Service

The Order Service is the core domain service of the Smart Delivery System. It exposes REST APIs to create and query orders, persists order data, and publishes domain events to Kafka to integrate with other services (e.g., payment, notification, monitoring).

## Key Features
- RESTful APIs for creating and retrieving orders
- Asynchronous processing for order creation using a dedicated executor
- Kafka producer for publishing `order.created` events
- Validation for input payloads with detailed constraints
- Clean separation into controller, service, repository, model, and config layers

## Architecture Overview
- Controller handles HTTP requests and responses
- Service contains business logic and orchestrates persistence and event publication
- Repository uses Spring Data JPA for database access
- Kafka producer publishes domain events to the `order.created` topic
- Async configuration provides a named executor for non-blocking order creation

Packages:
- config
  - `AsyncConfig` – defines `orderTaskExecutor` for async operations
  - `KafkaProducerConfig` – configures Kafka producer factory and `KafkaTemplate`
- controller
  - `OrderController` – REST endpoints for orders
- service
  - `OrderService` – business logic: persist orders, publish events, queries
  - `KafkaProducerService` – publishes `order.created` events
- repository
  - `OrderRepository` – Spring Data JPA repository for `Order`
- model
  - `Order`, `OrderItem`, `OrderStatus`
- dto
  - `OrderRequest`, `OrderItemRequest`, `OrderResponse`, `OrderItemResponse`, `OrderEvent`
- exception
  - `OrderNotFoundException`, `GlobalExceptionHandler`

## Prerequisites
- Java 17+
- Maven 3.8+
- Kafka broker reachable per your configuration
- Relational database if JPA/persistence is enabled (check properties)

## Configuration
Application properties reside at `src/main/resources/application.properties`.

- Spring basics
  - `server.port=8081`
  - `spring.application.name=order-service`
- Kafka
  - `spring.kafka.bootstrap-servers=localhost:9092`
  - Producer is configured via `KafkaProducerConfig` with `JsonSerializer` (type headers disabled)
- Database (if JPA enabled)
  - Example (PostgreSQL):
    - `spring.datasource.url=jdbc:postgresql://localhost:5432/orders`
    - `spring.datasource.username=orders`
    - `spring.datasource.password=orders`
    - `spring.jpa.hibernate.ddl-auto=update`
- Actuator (recommended)
  - `management.endpoints.web.exposure.include=health,info,metrics,prometheus`
  - `management.endpoint.health.show-details=always`

Adjust values for your environment and deployment.

## REST API
Base path: `/api/orders`

- POST `/api/orders`
  - Description: Create a new order asynchronously. Returns created order details.
  - Request Body: `OrderRequest`
    - Fields include: customerId, customerName, customerEmail, customerPhone, deliveryAddress, items[]
    - Validation: email format, phone pattern, address length, at least one item, etc.
  - Responses:
    - 201 Created: `OrderResponse`
    - 500 Internal Server Error on unexpected failures

- GET `/api/orders/{id}`
  - Description: Fetch an order by database id
  - Response: `OrderResponse`

- GET `/api/orders/number/{orderNumber}`
  - Description: Fetch an order by its business order number
  - Response: `OrderResponse`

- GET `/api/orders/customer/{customerId}`
  - Description: List orders for a given customer
  - Response: `List<OrderResponse>`

## Kafka
- Topic(s)
  - `order.created` – emitted when an order is successfully created
- Producer
  - Implemented in `KafkaProducerService`
  - Sends key as `orderNumber` and value as `OrderEvent`
- Event payload
  - `OrderEvent` contains: eventId, eventType (e.g., ORDER_CREATED), orderId, orderNumber, customerId, name, email, phone, deliveryAddress, totalAmount, status, timestamp, items
- Consumers
  - Other services (e.g., notification-service) subscribe to `order.created`

## Data Model
- `Order`: id, orderNumber, customerId, customerName, customerEmail, customerPhone, deliveryAddress, totalAmount, status, createdAt, completedAt, items
- `OrderItem`: id, productId, productName, quantity, unitPrice, totalPrice
- `OrderStatus`: enum with values like CREATED, PROCESSING, SHIPPED, DELIVERED, CANCELLED (check code for an exact list)

## Build
From the repository root:
- `mvn -q -DskipTests package`

From this module only:
- `mvn -q -DskipTests package -pl order-service -am`

## Run
- `mvn spring-boot:run -pl order-service`

Or run the generated jar:
- `java -jar order-service/target/order-service-*.jar`

Ensure Kafka and the database (if configured) are running before starting the service.

## Development
- Run Kafka locally (e.g., Docker Compose) and set `spring.kafka.bootstrap-servers`
- Use profiles or environment variables for local vs. production configs
- Tests: `mvn test -pl order-service`
- Logging: adjust log levels via `application.properties` for controller/service/producer packages

## Troubleshooting
- Kafka publish failures: verify `bootstrap-servers`, topic existence, and serializer settings
- Validation errors: check request payload against constraints in DTOs
- Database issues: confirm JDBC URL, credentials, and driver on the classpath
- Async behavior: ensure `@EnableAsync` is active and the executor bean `orderTaskExecutor` exists

## See Also
- Root `README.md` for architecture and project setup
- `notification-service` README to see how `order.created` is consumed
- `common` module for shared DTOs and constants if/when aligned
