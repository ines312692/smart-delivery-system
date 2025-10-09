# Payment Service Documentation

## Overview
The Payment Service is responsible for processing payments for orders in the Smart Delivery System. It listens to order creation events from Kafka, processes payments asynchronously using thread pools, and publishes payment status events.

---

## Architecture Components

### 1. **Event-Driven Processing**
- Consumes `order.created` events from Kafka
- Publishes `payment.completed` and `payment.failed` events
- Manual acknowledgment for reliable message processing

### 2. **Asynchronous Payment Processing**
- Thread pool executor for concurrent payment processing
- Separate retry executor for failed payment retries
- Non-blocking operations using CompletableFuture

### 3. **Retry Mechanism**
- Scheduled job runs every minute to retry failed payments
- Maximum 3 retry attempts per payment
- 5-minute delay between retry attempts

### 4. **Payment Simulation**
- Simulates real payment gateway (80% success rate)
- 2-second processing delay
- Random transaction IDs generation

---

## Database Schema

### Payments Table
```sql
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    payment_number VARCHAR(255) UNIQUE NOT NULL,
    order_id BIGINT NOT NULL,
    order_number VARCHAR(255) NOT NULL,
    customer_id VARCHAR(255) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(255),
    gateway_response TEXT,
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    next_retry_at TIMESTAMP,
    failure_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);

CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_customer_id ON payments(customer_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_retry ON payments(status, next_retry_at);
```

---

## Kafka Topics

### Consuming From:
- **Topic**: `order.created`
- **Group ID**: `payment-service-group`
- **Event Type**: OrderEvent

### Publishing To:
- **Topic**: `payment.completed` - When payment succeeds
- **Topic**: `payment.failed` - When payment fails

---

## API Endpoints (Internal)

Although the Payment Service primarily operates through Kafka events, you can add REST endpoints for admin purposes:

### Health Check
```bash
GET http://localhost:8082/actuator/health
```

### Metrics
```bash
GET http://localhost:8082/actuator/metrics
GET http://localhost:8082/actuator/prometheus
```

---

## Event Flow

### 1. Order Created Event Flow
```
Order Service → order.created topic
              ↓
Payment Service (OrderEventListener)
              ↓
PaymentService.initiatePayment()
              ↓
PaymentProcessorService (Async Thread Pool)
              ↓
Simulate Payment Gateway Processing
              ↓
Success? → payment.completed topic → Delivery Service
Failure? → payment.failed topic → Notification Service
              ↓
Retry Scheduler (if failed)
```

### 2. Retry Flow
```
RetryPaymentScheduler (Every 1 minute)
              ↓
Find payments where:
  - status = FAILED
  - retry_count < max_retries
  - next_retry_at <= now
              ↓
PaymentService.retryFailedPayment()
              ↓
PaymentProcessorService (Async)
```

---

## Event Schemas

### OrderEvent (Input)
```json
{
  "eventId": "uuid",
  "eventType": "ORDER_CREATED",
  "orderId": 123,
  "orderNumber": "ORD-1234567890-ABCD1234",
  "customerId": "CUST-001",
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "totalAmount": 99.99,
  "status": "CREATED",
  "timestamp": "2025-10-09T10:30:00"
}
```

### PaymentEvent (Output - Success)
```json
{
  "eventId": "uuid",
  "eventType": "PAYMENT_COMPLETED",
  "paymentId": 456,
  "paymentNumber": "PAY-1234567890-WXYZ5678",
  "orderId": 123,
  "orderNumber": "ORD-1234567890-ABCD1234",
  "customerId": "CUST-001",
  "amount": 99.99,
  "status": "COMPLETED",
  "transactionId": "TXN-uuid",
  "timestamp": "2025-10-09T10:30:05"
}
```

### PaymentEvent (Output - Failure)
```json
{
  "eventId": "uuid",
  "eventType": "PAYMENT_FAILED",
  "paymentId": 456,
  "paymentNumber": "PAY-1234567890-WXYZ5678",
  "orderId": 123,
  "orderNumber": "ORD-1234567890-ABCD1234",
  "customerId": "CUST-001",
  "amount": 99.99,
  "status": "FAILED",
  "failureReason": "Payment gateway declined the transaction",
  "timestamp": "2025-10-09T10:30:05"
}
```

---

## Configuration Properties

### Thread Pool Configuration
```yaml
# PaymentProcessorExecutor
Core Pool Size: 10
Max Pool Size: 20
Queue Capacity: 500
Thread Name Prefix: payment-processor-

# RetryExecutor
Core Pool Size: 5
Max Pool Size: 10
Queue Capacity: 100
Thread Name Prefix: payment-retry-
```

### Kafka Configuration
```yaml
Consumer:
  - Group ID: payment-service-group
  - Auto Offset Reset: earliest
  - Enable Auto Commit: false (manual acknowledgment)
  - Max Poll Records: 10
  - Concurrency: 3

Producer:
  - Acks: all
  - Retries: 3
  - Idempotence: true
```

### Retry Configuration
```yaml
Max Retry Attempts: 3
Initial Retry Delay: 5 minutes
Scheduler Interval: 1 minute
```

---

## Testing the Payment Service

### 1. Start the Service
```bash
# Local development
cd payment-service
mvn spring-boot:run

# Docker
docker-compose up payment-service
```

### 2. Monitor Logs
```bash
# View payment processing logs
tail -f logs/payment-service.log

# Docker logs
docker logs -f payment-service
```

### 3. Test End-to-End Flow
```bash
# Create an order (triggers payment processing)
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-001",
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "customerPhone": "+1234567890",
    "deliveryAddress": "123 Main St, City, State 12345",
    "items": [
      {
        "productId": "PROD-001",
        "productName": "Pizza Margherita",
        "quantity": 2,
        "unitPrice": 12.99
      }
    ]
  }'

# Check payment service logs to see:
# - Order event received
# - Payment initiated
# - Payment processing
# - Payment completed/failed event published
```

### 4. Check Kafka Topics
```bash
# List topics
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092

# Consume order.created events
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic order.created \
  --from-beginning

# Consume payment.completed events
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic payment.completed \
  --from-beginning

# Consume payment.failed events
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic payment.failed \
  --from-beginning
```

### 5. Query Database
```sql
-- Check all payments
SELECT * FROM payments ORDER BY created_at DESC;

-- Check payment status distribution
SELECT status, COUNT(*) as count 
FROM payments 
GROUP BY status;

-- Check pending retries
SELECT payment_number, status, retry_count, next_retry_at 
FROM payments 
WHERE status = 'FAILED' 
  AND retry_count < max_retries
  AND next_retry_at <= NOW();

-- Check payment success rate
SELECT 
  COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) * 100.0 / COUNT(*) as success_rate
FROM payments;
```

---

## Monitoring & Metrics

### Health Check
```bash
curl http://localhost:8082/actuator/health
```

Response:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "kafka": {
      "status": "UP"
    }
  }
}
```

### Prometheus Metrics
```bash
curl http://localhost:8082/actuator/prometheus
```

Key metrics:
- `payment_processing_duration_seconds` - Payment processing time
- `payment_retry_attempts_total` - Total retry attempts
- `payment_success_rate` - Percentage of successful payments
- `kafka_consumer_lag` - Consumer lag for order.created topic

---

## Troubleshooting

### Payment Not Processing
1. Check if Order Service published the event:
   ```bash
   docker exec -it kafka kafka-console-consumer \
     --bootstrap-server localhost:9092 \
     --topic order.created \
     --from-beginning
   ```

2. Check Payment Service logs:
   ```bash
   docker logs payment-service | grep "Received order created event"
   ```

3. Verify database connection:
   ```bash
   curl http://localhost:8082/actuator/health
   ```

### High Failure Rate
1. Check thread pool metrics:
   ```bash
   curl http://localhost:8082/actuator/metrics/executor.active
   ```

2. Review payment gateway simulator configuration
3. Check database performance

### Retry Not Working
1. Verify scheduler is running:
   ```bash
   docker logs payment-service | grep "Checking for failed payments"
   ```

2. Check retry_count and next_retry_at:
   ```sql
   SELECT * FROM payments WHERE status = 'FAILED';
   ```

3. Ensure scheduler is enabled in application.yml

---

## Performance Tuning

### Thread Pool Optimization
```yaml
# For high load
paymentProcessorExecutor:
  corePoolSize: 20
  maxPoolSize: 50
  queueCapacity: 1000
```

### Kafka Consumer Optimization
```yaml
# Process more messages in parallel
spring.kafka.listener.concurrency: 5

# Increase batch size
spring.kafka.consumer.max-poll-records: 50
```

### Database Connection Pool
```yaml
spring.datasource.hikari:
  maximum-pool-size: 30
  minimum-idle: 10
```

---

## Security Considerations

1. **Payment Data Encryption**
    - Encrypt sensitive payment data at rest
    - Use TLS for all communications

2. **PCI DSS Compliance**
    - Never store CVV/CVC codes
    - Tokenize card numbers
    - Implement audit logging

3. **API Security**
    - Add authentication/authorization
    - Rate limiting
    - Input validation

4. **Kafka Security**
    - Enable SASL/SSL
    - Use ACLs for topic access
    - Encrypt data in transit

---

## Future Enhancements

1. **Multiple Payment Gateways**
    - Strategy pattern for different providers
    - Fallback mechanism
    - Load balancing

2. **Real Payment Integration**
    - Stripe integration
    - PayPal integration
    - Bank transfer support

3. **Advanced Retry Logic**
    - Exponential backoff
    - Circuit breaker pattern
    - Dead letter queue

4. **Payment Reconciliation**
    - Daily reconciliation job
    - Mismatch detection
    - Automated refunds

5. **Fraud Detection**
    - ML-based fraud detection
    - Velocity checks
    - Geographic validation