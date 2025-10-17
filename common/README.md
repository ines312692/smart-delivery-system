# Common Module

This module contains shared code and artifacts used across the Smart Delivery System microservices. It centralizes DTOs, constants, and utility classes to keep services consistent and reduce duplication.

## Contents
- DTOs for cross-service communication (events, API responses, pagination, etc.)
- Constants such as Kafka topics, event types, and status values
- Utility helpers for date/time, JSON, strings, and validation

Refer to the repository root README for an expanded tree and overall architecture.

## Usage
Add this module as a dependency to other services within the multi-module Maven project. Typical imports include:
- com.example.common.dto.*
- com.example.common.constants.*
- com.example.common.util.*

## Build
From the repository root:
- mvn -q -DskipTests package

From this module directory only:
- mvn -q -DskipTests package

## Test
- mvn test

## Notes
- Keep contracts (DTOs and constants) backward compatible when possible, since multiple services may depend on them.
- When adding new event types or fields, update corresponding consumers/producers and documentation under docs/ if needed.

## See also
- Root README.md for project-wide setup and conventions
- common/HELP.md for Spring Boot and Maven references
