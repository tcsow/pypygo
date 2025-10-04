# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**pypygo** is a Spring Boot 3.5.6 inventory management application built with Java 17, featuring a production-ready infrastructure with MySQL database and Redis clustering with Sentinel high-availability configuration.

## Development Commands

### Build and Run
```bash
# Build application
./mvnw clean compile

# Run tests
./mvnw test

# Package application
./mvnw package

# Run application (listens on port 8091)
./mvnw spring-boot:run
```

### Infrastructure Management
```bash
# Start all services (MySQL + Redis cluster)
cd setup && docker-compose up -d

# Stop services
cd setup && docker-compose down

# Reset with fresh data
cd setup && docker-compose down -v && docker-compose up -d

# View service logs
cd setup && docker-compose logs -f [service-name]
```

## Architecture Overview

### Application Configuration
- **Port**: 8091 (configured to avoid conflicts)
- **Database**: MySQL 8.0 on port 3307 (containerized)
- **Cache**: Redis cluster with master-slave-sentinel architecture
- **JPA**: Schema managed via SQL scripts (`ddl-auto: none`)

### Database Schema (Inventory Management)
Key tables in `/setup/mysql/`:
- **products**: Product catalog with string IDs (e.g., 'prod-001')
- **inventories**: Stock levels with reserved quantities and minimum thresholds
- **inventory_transactions**: Complete audit trail with transaction types (STOCK_IN, STOCK_OUT, RESERVE, RELEASE, CONFIRM, ADJUSTMENT)

### Redis Infrastructure (High Availability)
- **Master**: redis-master (172.20.0.10:6379)
- **Slaves**: redis-slave-1 (172.20.0.11:6380), redis-slave-2 (172.20.0.12:6381)
- **Sentinels**: 3 instances (ports 26379, 26380, 26381) with quorum=2
- **Authentication**: Password-protected (redis_password_123)

## Key Configuration Files

- `application.yml`: Database connection to localhost:3307, JPA settings, internationalization
- `setup/.env`: Database credentials and Redis password
- `setup/docker-compose.yml`: Complete infrastructure definition with health checks
- `setup/mysql/01-init.sql`: Database schema with foreign key constraints
- `setup/mysql/02-data.sql`: Sample inventory data for development

## Development Workflow

1. **Infrastructure First**: Always start Docker services before running the application
2. **Database Schema**: Managed via SQL scripts, not JPA auto-generation
3. **Port Configuration**: Application uses 8091, database 3307 to avoid local conflicts
4. **ARM64 Optimized**: Docker setup configured for Apple Silicon

## Infrastructure Dependencies

The application requires the Docker infrastructure to be running:
- MySQL database with inventory schema and sample data
- Redis master-slave cluster with Sentinel failover
- Custom network (172.20.0.0/16) with static IP assignments
- Persistent volumes for data storage

## Testing and Validation

After starting services, verify connectivity:
- Application: http://localhost:8091
- MySQL: localhost:3307 (root/password)
- Redis Master: localhost:6379
- Health checks ensure proper service startup ordering
