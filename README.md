# Scalable Notification System (Kafka-Based)

A **production-inspired event-driven notification system** built using **Spring Boot, Kafka, PostgreSQL, Redis, and Prometheus metrics**.  
The system demonstrates **reliable message processing, retry strategies, distributed system resilience, and observability**, similar to architectures used in modern backend microservices.

This project focuses on solving real-world problems such as:

- Reliable asynchronous processing
- Duplicate message handling
- Retry strategies with exponential backoff
- Dead-letter queue handling
- Rate limiting
- Consumer lag monitoring
- Backpressure control
- Observability and metrics

The goal of this project is to demonstrate **strong backend engineering concepts suitable for SDE roles**, particularly in **distributed systems and event-driven architectures**.

---

# Tech Stack

| Technology | Purpose |
|---|---|
| **Spring Boot** | Backend framework |
| **Apache Kafka** | Event streaming & asynchronous processing |
| **PostgreSQL** | Persistent data store |
| **Redis** | Deduplication cache and rate limiting |
| **Micrometer + Prometheus** | Metrics & observability |
| **Docker** | Infrastructure setup |
| **Kafka AdminClient** | Consumer lag monitoring |

---

# Core Concepts Demonstrated

This project demonstrates several important backend engineering concepts:

### Event Driven Architecture
The system decouples the API layer from the notification processing logic using Kafka.

### Asynchronous Processing
Notifications are processed by background workers instead of blocking API calls.

### Idempotent Processing
Duplicate messages are safely handled using both Redis and database state checks.

### Retry Mechanism
Failures trigger retries with exponential backoff.

### Dead Letter Queue
Messages that exceed maximum retry attempts are moved to a DLQ for investigation.

### Observability
Metrics are exposed through Prometheus to monitor system health and failures.

### Backpressure Control
The system detects consumer lag and throttles incoming requests to avoid overload.

---

# System Components

The system consists of the following major components:

## 1. Notification API
Handles incoming notification requests and publishes events to Kafka.

Responsibilities:

- Accept notification requests
- Enforce idempotency
- Apply backpressure when system is overloaded
- Persist notification metadata
- Publish notification events to Kafka

---

## 2. Kafka Event Stream

Kafka acts as the **asynchronous message broker** between the API and worker services.

Primary topics used:
- notification-events
- notification-events-retry-5s
- notification-events-retry-30s
- notification-events-dlq

These topics enable reliable message delivery and retry pipelines.

---

## 3. Notification Consumer Worker

The worker service consumes events from Kafka and processes notifications.

Responsibilities:

- Claim notification for processing
- Perform Redis deduplication check
- Apply rate limiting
- Send notification
- Update database state
- Retry failed messages
- Move permanently failed messages to DLQ

---

## 4. Redis Layer

Redis is used as a **fast in-memory guard layer**.

Use cases in this project:

### Deduplication Cache
Prevents duplicate Kafka messages from being processed.

Example key format: These topics enable reliable message delivery and retry pipelines.

---

## 3. Notification Consumer Worker

The worker service consumes events from Kafka and processes notifications.

Responsibilities:

- Claim notification for processing
- Perform Redis deduplication check
- Apply rate limiting
- Send notification
- Update database state
- Retry failed messages
- Move permanently failed messages to DLQ

---

## 4. Redis Layer

Redis is used as a **fast in-memory guard layer**.

Use cases in this project:

### Deduplication Cache
Prevents duplicate Kafka messages from being processed.

Example key format: notif:{notificationId}


### Rate Limiting
Limits notification frequency per user.

Example rule:
Max 10 notifications per user per minute

Redis ensures these checks happen in **milliseconds without hitting the database**.

---

## 5. PostgreSQL Database

PostgreSQL serves as the **source of truth** for notification state.

Main responsibilities:

- Store notification records
- Track processing status
- Store retry counts
- Maintain idempotency keys

Notification lifecycle states:
- PENDING
- PROCESSING
- SENT
- FAILED

Atomic updates ensure only one worker processes a notification.

---

## 6. Retry Pipeline

The retry mechanism is implemented using **dedicated Kafka retry topics**.

Retry flow:

1. First failure → message sent to retry-5s topic
2. Second failure → message sent to retry-30s topic
3. Third failure → message sent to DLQ

This ensures retries are **fault tolerant and survive application restarts**.

---

## 7. Dead Letter Queue (DLQ)

Messages that cannot be processed after multiple retries are moved to:
notification-events-dlq

These messages can later be inspected or reprocessed manually.

DLQ prevents broken messages from blocking the main processing pipeline.

---

## 8. Observability & Metrics

The system exposes metrics using **Spring Boot Actuator and Micrometer**.

Metrics endpoint: /actuator/prometheus

Example metrics tracked:
- notifications_success_total
- notifications_failure_total
- notifications_retry_total
- notifications_dlq_total
- kafka_consumer_lag


These metrics help monitor system performance and failures.

---

## 9. Kafka Consumer Lag Monitoring

A background service monitors consumer lag using Kafka AdminClient.

Lag is calculated as:
Lag = Latest Offset - Committed Offset


High lag indicates that consumers are unable to keep up with incoming traffic.

---

## 10. Backpressure Control

When lag exceeds a threshold, the system applies **backpressure**.

The API temporarily rejects new requests to prevent overload.

Example response:
HTTP 429 - Too Many Requests

This protects the system from cascading failures.

---

# Request Processing Flow

The following describes how a notification request is processed.

### Step 1 — Client Request

A client sends a request to create a notification.
POST /notifications

---

### Step 2 — API Layer

The API performs:

- Idempotency check
- Backpressure check
- Database insert

Then publishes the event to Kafka.

---

### Step 3 — Kafka Event Stream

Kafka receives the notification event and distributes it across partitions.

---

### Step 4 — Worker Processing

The consumer worker:

1. Checks Redis deduplication cache
2. Claims the notification via atomic database update
3. Applies Redis rate limiting
4. Sends the notification

---

### Step 5 — Success Path

If successful:
status → SENT


Metrics are updated accordingly.

---

### Step 6 — Failure Path

If sending fails:

- Retry count increases
- Message sent to retry topic

---

### Step 7 — Retry Pipeline

Retries occur via Kafka topics with increasing delay.

---

### Step 8 — DLQ Handling

If retries exceed maximum limit:
status → FAILED


Message is sent to DLQ.

---

# Idempotency Strategy

Two layers of idempotency protection exist:

### API Layer Idempotency
Prevents duplicate requests using idempotency keys.

### Consumer Layer Idempotency
Prevents duplicate Kafka message processing using:

- Redis deduplication
- Database atomic state transition

---

# Backpressure Strategy

Backpressure is applied when consumer lag crosses a threshold.

This prevents:

- queue explosion
- system overload
- downstream service failures

---

# Scalability Considerations

Kafka partitioning enables horizontal scaling.

Parallel processing is determined by the number of partitions.
Max parallel consumers = number of partitions


Partition keys are used to preserve ordering for notifications belonging to the same user.

---

# Failure Handling Strategy

The system is designed to tolerate multiple types of failures.

Handled scenarios:

- Consumer crashes
- Kafka redelivery
- Duplicate messages
- Temporary service failures
- Permanent message failures

Retries and DLQ ensure reliability.

---

# Running the Project

Start infrastructure services using Docker:
docker compose up -d


Start the Spring Boot application:
mvn spring-boot:run

Access metrics:
http://localhost:8080/actuator/prometheus


---

# Learning Objectives

This project demonstrates practical implementations of:

- Event-driven architecture
- Kafka message processing
- Distributed retry strategies
- Dead-letter queue design
- Redis caching strategies
- Observability and monitoring
- Consumer lag detection
- Backpressure mechanisms

---

# Future Improvements

Possible future enhancements include:

- Transactional outbox pattern
- Kafka Streams for advanced processing
- Grafana dashboards for monitoring
- Alerting via Prometheus Alertmanager
- Multi-region Kafka replication
- Distributed tracing with OpenTelemetry

---

# Conclusion

This project simulates a **real-world distributed notification system** focusing on reliability, scalability, and observability.

It demonstrates several key backend engineering practices used in production systems and provides a strong foundation for building resilient event-driven services.
