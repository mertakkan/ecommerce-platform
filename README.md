# EcommerceHub - Enterprise Microservices E-Commerce Platform

## Overview
A comprehensive microservices-based e-commerce platform built with Spring Boot and Spring Cloud, demonstrating enterprise-level Java development practices.

## Architecture
- **Microservices Architecture** with 12 independent services
- **Event-Driven Communication** using Apache Kafka
- **API Gateway** for request routing and cross-cutting concerns
- **Service Discovery** with Eureka
- **Distributed Configuration** with Spring Cloud Config
- **Containerized Deployment** with Docker and Kubernetes

## Technology Stack
- **Backend**: Java 17, Spring Boot 3.2, Spring Cloud 2023.x
- **Databases**: PostgreSQL, MongoDB, Redis
- **Message Broker**: Apache Kafka
- **Search Engine**: Elasticsearch
- **Monitoring**: Prometheus, Grafana
- **Documentation**: OpenAPI 3.0

## Services Overview

### Infrastructure Services
- **Config Server**: Centralized configuration management
- **Service Discovery**: Service registration and discovery
- **API Gateway**: Request routing, authentication, rate limiting

### Business Services
- **User Service**: User management and authentication
- **Product Service**: Product catalog management
- **Inventory Service**: Stock management and reservation
- **Cart Service**: Shopping cart functionality
- **Order Service**: Order processing and management
- **Payment Service**: Payment processing
- **Notification Service**: Email/SMS notifications
- **Review Service**: Product reviews and ratings
- **Search Service**: Full-text search capabilities