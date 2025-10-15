# Apache Kafka in Smart Delivery System

> Version: 2025-10-15

Badges
- ![Kafka Badge](https://img.shields.io/badge/Apache%20Kafka-Event%20Streaming-black?logo=apachekafka)
- ![License](https://img.shields.io/badge/License-MIT-green)
- ![Java](https://img.shields.io/badge/Java-17+-red)
- ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)

Logo
- ![Kafka Logo](https://images.icon-icons.com/2699/PNG/128/apache_kafka_logo_icon_167866.png)

Overview
- Apache Kafka is a distributed event streaming platform used as the messaging backbone of this project.
- In this microservices architecture, Kafka decouples producers and consumers, enabling reliable, scalable, and asynchronous communication.

Core Concepts
- Topic: A named stream of records. This project uses topics such as order-events, payment-events, delivery-events, notification-events, and all-events.
- Partition: A topic is split into partitions for scalability and parallelism. Ordering is guaranteed within a partition.
- Offset: A monotonically increasing sequence number per partition that identifies each record’s position.
- Producer: A client that publishes records to a topic (e.g., order-service emits OrderEvent).
- Consumer: A client that subscribes to topics and processes records (e.g., payment-service consumes OrderEvent).
- Consumer Group: A set of consumers that coordinate to consume partitions of a topic, enabling horizontal scaling and fault tolerance.
- Broker: A Kafka server that stores data and serves clients. Clusters typically consist of multiple brokers.
- Zookeeper / KRaft: Cluster metadata management. Recent Kafka versions use KRaft (without ZooKeeper). Our local Docker setup may still use ZooKeeper for simplicity.
- Schema & Serialization: Messages are serialized (e.g., JSON). In advanced setups, Avro/Protobuf + Schema Registry can be used.

How Kafka fits this project
- order-service: Produces order lifecycle events (OrderEvent) to topic order-events.
- payment-service: Consumes order-events, processes payments, and produces payment-events (PaymentEvent).
- notification-service: Consumes order-events, payment-events, and delivery-events to send notifications.
- monitoring-service: Consumes from multiple topics to record events, metrics, and health for dashboards.
- common module: Hosts DTOs and constants to keep event contracts consistent across services.

Event Flow Example
- 1) Client POST /api/orders to order-service
- 2) order-service stores order in DB and publishes OrderEvent to order-events
- 3) payment-service receives OrderEvent, attempts payment, publishes PaymentEvent
- 4) notification-service receives both OrderEvent and PaymentEvent and sends appropriate notifications
- 5) monitoring-service listens to all topics for observability

Reliability and Delivery Semantics
- At-least-once: Default posture with Spring Kafka; consumers must be idempotent.
- Exactly-once: Achievable using transactional producers and idempotent consumers, but adds complexity.
- Ordering: Guaranteed per partition; key events with orderId to ensure all events for a given order go to the same partition.

Topic Design
- Naming: kebab-case with domain context: order-events, payment-events, delivery-events, notification-events
- Partitions: Start with 3 partitions in local/dev; scale based on throughput.
- Replication Factor: 1 locally; >=3 in production for fault tolerance.
- Retention: Defaults are fine for dev; in prod define time/size retention per topic.

Configuration in the repo
- order-service/src/main/java/.../config/KafkaProducerConfig.java: Producer configuration and KafkaTemplate bean.
- payment-service/src/main/java/.../config/KafkaConsumerConfig.java: Consumer factories and listener container configs.
- notification-service/src/main/java/.../config/KafkaConsumerConfig.java: Consumer configuration.
- monitoring-service/src/main/java/.../config/KafkaConsumerConfig.java: Consumer configuration for observability.
- application.yml/properties: bootstrap-servers and topic names.

Local Kafka Setup
- Option A: Docker (simple) — see docs/RUNBOOK.md section 4.2 for commands.
- Option B: docker-compose — see kafka/docker-compose.yml if present.
- Verify topics:
  - docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 --list
  - Create if missing:
    - docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 --create --topic order-events --partitions 3 --replication-factor 1

Spring Kafka Essentials
- ProducerTemplate (KafkaTemplate) publishes messages:
  - Key: orderId (String/UUID)
  - Value: JSON-serialized DTO (OrderEvent, PaymentEvent)
- @KafkaListener in consumers with groupId to scale horizontally.
- Error handling: seek to current, DLT (dead-letter topic) pattern recommended for production.
- Backpressure & Retries: Configure max-poll-interval, concurrency, and retry/backoff policies.

Security (Production Considerations)
- TLS: enable SSL between clients and brokers.
- SASL: use SASL/PLAIN, SCRAM, or OAuth for authentication.
- ACLs: restrict topics per service.

Observability
- Consumer lag: monitor with tools like Kafka UI / Conduktor / Burrow.
- Metrics: Expose Micrometer metrics; scrape with Prometheus/Grafana.
- Logging: Correlate orderId across services for traceability.

Testing Strategy
- Unit tests: Mock KafkaTemplate and @KafkaListener methods.
- Integration tests: Use Testcontainers Kafka to run ephemeral Kafka brokers during CI.
- Contract tests: Validate DTO schemas and backward compatibility.

Capacity & Tuning (Prod)
- Batch size, linger.ms, compression.type for producers.
- max.poll.records, fetch.min.bytes for consumers.
- Partitioning strategy to ensure key-based locality.

FAQ
- Q: How do I change Kafka broker URL?
  - A: Set spring.kafka.bootstrap-servers in the relevant application.yml or via env var SPRING_KAFKA_BOOTSTRAP_SERVERS.
- Q: Where are topic names defined?
  - A: In service config files and/or constants, typically KafkaTopics.java in common.
- Q: Do I need ZooKeeper?
  - A: Not for recent Kafka in KRaft mode, but our sample docker commands use ZooKeeper for simplicity.

References
- Kafka: https://kafka.apache.org/
- Spring for Apache Kafka: https://docs.spring.io/spring-kafka/docs/current/reference/html/
- Testcontainers Kafka: https://www.testcontainers.org/modules/kafka/

Changelog
- 2025-10-15: Initial version.
