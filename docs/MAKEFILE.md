Makefile Guide

Overview
- This project includes a Makefile at the repository root to streamline common developer workflows.
- It wraps Maven Wrapper, Docker, and convenience commands for Kafka/Postgres.

Prerequisites
- Git Bash, WSL, or any POSIX-like shell for Windows users. On Linux/macOS, use your default shell.
- Java 17+, Docker (optional but recommended), and internet connectivity.

Key Targets
- init: Prints versions of Java, Maven (wrapper), and Docker.
- build: mvn clean install with tests skipped for speed.
- test: Run all tests across modules.
- verify: Full Maven verify lifecycle.
- package: Build artifacts without running tests.
- run-order|run-payment|run-notification|run-monitoring: Run each service locally via Spring Boot.
- docker-net: Create a local Docker network used by dependency containers.
- up-deps: Start PostgreSQL, ZooKeeper, and Kafka in Docker.
- down-deps: Stop and remove the dependency containers and network.
- docker-build: Build Docker images for all services.
- docker-run: Start all services as Docker containers (assumes dependencies already running and reachable on localhost).
- docker-stop: Stop all service containers.
- kafka-topics: List topics on the running Kafka container.
- create-topics: Create default project topics.

Usage Examples
- make init
- make build
- make up-deps
- make run-order   # in one terminal
- make run-payment # in another terminal
- make docker-build && make docker-run
- make kafka-topics
- make down-deps

Notes
- The Makefile auto-detects Windows and uses mvnw.cmd instead of ./mvnw.
- On Docker Desktop (Windows/macOS), host networking is not supported. Replace --network host in docker-run with explicit -p mappings and ensure services use host.docker.internal for Kafka/Postgres if needed.
- These commands are convenience helpers; for deeper detail, see docs/RUNBOOK.md and docs/KAFKA.md.
