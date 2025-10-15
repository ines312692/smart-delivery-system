# Smart Delivery System - Makefile
# Cross-platform targets (primarily for Unix shells). On Windows, use Git Bash or WSL.
# For pure Windows PowerShell equivalents, see docs/RUNBOOK.md. This Makefile shells out to mvnw.cmd on Windows when possible.

SHELL := /bin/sh
OS := $(shell uname 2>/dev/null || echo Windows)
MVNW := ./mvnw
ifeq ($(OS),Windows)
	MVNW := .\mvnw.cmd
endif

# Modules
MODULES := common order-service payment-service notification-service monitoring-service

# Docker image names
ORDER_IMG := sds/order-service
PAYMENT_IMG := sds/payment-service
NOTIF_IMG := sds/notification-service
MON_IMG := sds/monitoring-service

# Kafka and DB via Docker quick commands
DOCKER_NET := sds-net
POSTGRES_CONT := sds-postgres
KAFKA_CONT := kafka
ZK_CONT := zookeeper

.PHONY: help init clean build test package run-all stop-all docker-net up-deps down-deps \
	run-order run-payment run-notification run-monitoring docker-build docker-run docker-stop \
	kafka-topics create-topics format fmt verify lint

help:
	@echo "Smart Delivery System - Common make targets"
	@echo "make init             - Verify prerequisites and print versions"
	@echo "make clean            - Clean all modules"
	@echo "make build            - Build all modules (mvn install)"
	@echo "make test             - Run all tests"
	@echo "make package          - Package JARs for all modules"
	@echo "make run-all          - Run all services (requires Kafka & DB)"
	@echo "make stop-all         - Stop all foreground runs (SIGINT)"
	@echo "make docker-net       - Create local docker network $(DOCKER_NET)"
	@echo "make up-deps          - Start Postgres + Kafka (Docker)"
	@echo "make down-deps        - Stop Postgres + Kafka (Docker)"
	@echo "make docker-build     - Build Docker images for all services"
	@echo "make docker-run       - Run all services in Docker (assumes deps up)"
	@echo "make docker-stop      - Stop all service containers"
	@echo "make kafka-topics     - List Kafka topics"
	@echo "make create-topics    - Create default topics"
	@echo "make verify           - mvn -P default verify"
	@echo "make lint             - Run basic checks (format + verify)"

init:
	@echo "Java:  $$(java -version 2>&1 | head -n 1)"
	@echo "Maven: $$( $(MVNW) -v | head -n 1)"
	@echo "Docker: $$(docker --version 2>/dev/null || echo 'not installed')"

clean:
	$(MVNW) -q -DskipTests clean

build:
	$(MVNW) -q -DskipTests clean install

verify:
	$(MVNW) -q verify

test:
	$(MVNW) -q test

package:
	$(MVNW) -q -DskipTests package

# Run modules locally (each blocks the terminal). Open multiple terminals.
run-order:
	$(MVNW) -q -pl order-service spring-boot:run

run-payment:
	$(MVNW) -q -pl payment-service spring-boot:run

run-notification:
	$(MVNW) -q -pl notification-service spring-boot:run

run-monitoring:
	$(MVNW) -q -pl monitoring-service spring-boot:run

run-all:
	@echo "Open four terminals and run: make run-order | make run-payment | make run-notification | make run-monitoring"
	@echo "Alternatively use docker targets after up-deps."

stop-all:
	@echo "Stop Java services with Ctrl+C in each terminal."

# Docker helpers for dependencies
docker-net:
	- docker network create $(DOCKER_NET)

up-deps: docker-net
	- docker run -d --name $(POSTGRES_CONT) --network $(DOCKER_NET) -e POSTGRES_PASSWORD=postgres -e POSTGRES_USER=postgres -e POSTGRES_DB=smart_delivery -p 5432:5432 postgres:15
	- docker run -d --name $(ZK_CONT) --network $(DOCKER_NET) -p 2181:2181 confluentinc/cp-zookeeper:7.5.0 zookeeper-server-start /etc/kafka/zookeeper.properties
	- docker run -d --name $(KAFKA_CONT) --network $(DOCKER_NET) -p 9092:9092 -e KAFKA_ZOOKEEPER_CONNECT=$(ZK_CONT):2181 -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 confluentinc/cp-kafka:7.5.0

DownLabels := $(POSTGRES_CONT) $(KAFKA_CONT) $(ZK_CONT)

down-deps:
	- docker stop $(DownLabels)
	- docker rm $(DownLabels)
	- docker network rm $(DOCKER_NET) || true

# Docker images for services
docker-build:
	docker build -t $(ORDER_IMG) ./order-service
	docker build -t $(PAYMENT_IMG) ./payment-service
	docker build -t $(NOTIF_IMG) ./notification-service
	docker build -t $(MON_IMG) ./monitoring-service

# Run containers; assumes Kafka/Postgres reachable on localhost
# For Docker Desktop (no host network), replace --network host with -p and host.docker.internal as needed.
docker-run:
	- docker run --rm -d --name order-service --network host -e SPRING_PROFILES_ACTIVE=docker -p 8080:8080 $(ORDER_IMG)
	- docker run --rm -d --name payment-service --network host -e SPRING_PROFILES_ACTIVE=docker -p 8081:8081 $(PAYMENT_IMG)
	- docker run --rm -d --name notification-service --network host -e SPRING_PROFILES_ACTIVE=docker -p 8082:8082 $(NOTIF_IMG)
	- docker run --rm -d --name monitoring-service --network host -e SPRING_PROFILES_ACTIVE=docker -p 8083:8083 $(MON_IMG)

docker-stop:
	- docker stop order-service payment-service notification-service monitoring-service

kafka-topics:
	- docker exec -it $(KAFKA_CONT) kafka-topics --bootstrap-server localhost:9092 --list

create-topics:
	- docker exec -it $(KAFKA_CONT) kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic order-events --partitions 3 --replication-factor 1
	- docker exec -it $(KAFKA_CONT) kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic payment-events --partitions 3 --replication-factor 1
	- docker exec -it $(KAFKA_CONT) kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic delivery-events --partitions 3 --replication-factor 1
	- docker exec -it $(KAFKA_CONT) kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic notification-events --partitions 3 --replication-factor 1

# Formatting/lint placeholder hooks (extend with your formatter of choice)
format fmt:
	@echo "No formatter configured; add Spotless/Checkstyle if desired."

lint: format verify
	@echo "Basic lint complete."
