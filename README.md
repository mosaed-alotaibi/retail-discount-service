# Retail Discount Service

A production-ready REST API for calculating retail discounts based on customer type, built with Spring Boot following DDD, Hexagonal Architecture, and RESTful best practices.

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-7.0-green.svg)](https://www.mongodb.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## 📋 Table of Contents

- [Business Requirements](#-business-requirements)
- [Architecture](#-architecture)
- [Technologies](#-technologies)
- [Prerequisites](#-prerequisites)
- [Quick Start](#-quick-start)
- [Running the Application](#-running-the-application)
- [Running Tests](#-running-tests)
- [Code Coverage](#-code-coverage)
- [API Documentation](#-api-documentation)
- [API Examples](#-api-examples)
- [Docker](#-docker)
- [Project Structure](#-project-structure)

---

## 🎯 Business Requirements

The service implements a two-tier discount system for a retail store:

### Discount Rules

1. **Percentage-Based Discounts** (Only ONE applies per bill):
   - **Employee**: 30% discount
   - **Affiliate**: 10% discount
   - **Long-term Customer** (>2 years): 5% discount
   - **Regular Customer**: 0% discount

2. **Bill-Based Discount**:
   - $5 discount for every $100 on the bill
   - Applies AFTER percentage discount

3. **Special Rules**:
   - Percentage discounts do NOT apply to groceries
   - Bill-based discount applies to the total after percentage discount
   - Only ONE percentage discount applies per bill

### Example Calculation

```
Bill: $1,115 ($1,100 electronics + $15 groceries)
Customer: Employee (30% discount)

Step 1: Percentage Discount
- Eligible amount: $1,100 (excluding groceries)
- Discount: $1,100 × 30% = $330

Step 2: Bill-Based Discount
- Amount after percentage: $1,115 - $330 = $785
- Bill-based: floor($785/$100) × $5 = $35

Final Calculation:
- Total: $1,115
- Total Discount: $330 + $35 = $365
- Net Payable: $750
```

---

## 🏗️ Architecture

This project follows **Clean Architecture** principles with Hexagonal Architecture (Ports & Adapters) and Domain-Driven Design (DDD).

See [ARCHITECTURE_OVERVIEW.md](ARCHITECTURE_OVERVIEW.md) and [CLASS_DIAGRAM.md](CLASS_DIAGRAM.md) for detailed architecture documentation.

---

## 🛠️ Technologies

- **Java 21** - Latest LTS
- **Spring Boot 3.3.0** - Application framework
- **Spring Security** - HTTP Basic Authentication  
- **Spring Data MongoDB** - Data persistence
- **MongoDB 7.0** - NoSQL database
- **Maven** - Build tool
- **JUnit 5 + Mockito** - Testing
- **Testcontainers** - Integration testing
- **JaCoCo** - Code coverage
- **SpringDoc OpenAPI** - API documentation
- **Docker & Docker Compose** - Containerization

---

## ✅ Prerequisites

| Tool | Version | Required |
|------|---------|----------|
| Java JDK | 21+ | ✅ Yes |
| Maven | 3.9+ | ✅ Yes |
| Docker | 24+ | ⚠️ Optional |

```bash
# Verify installation
java -version   # Should show Java 21
mvn -version    # Should show Maven 3.9+
```

---

## 🚀 Quick Start

```bash
# 1. Clone repository
git clone <repository-url>
cd retail-discount-service

# 2. Build project
mvn clean install

# 3. Run with Docker Compose (Recommended)
docker-compose up -d

# 4. Access application
# API: http://localhost:8080/api/v1/bills
# Swagger: http://localhost:8080/api/v1/swagger-ui.html
```

---

## 🏃 Running the Application

### Option 1: Docker Compose (Recommended)

```bash
docker-compose up -d
```

### Option 2: Maven

```bash
mvn spring-boot:run
```

### Option 3: JAR

```bash
mvn clean package -DskipTests
java -jar target/retail-discount-service.jar
```

### Verify Running

```bash
curl http://localhost:8080/api/v1/actuator/health
```

---

## 🧪 Running Tests

### All Tests

```bash
mvn clean test verify
```

### Unit Tests Only

```bash
mvn test
```

### Integration Tests Only

```bash
mvn verify -Dskip.surefire.tests=true
```

### Specific Test

```bash
mvn test -Dtest=BillServiceTest
```

---

## 📊 Code Coverage

### Generate Report

```bash
# Generate coverage report
mvn clean verify

# View report
open target/site/jacoco/index.html
```

### Requirements

- **Minimum**: 80% line coverage
- **Exclusions**: Config, DTOs, Documents

---

## 📚 API Documentation

### Swagger UI

Interactive API documentation:

```
http://localhost:8080/api/v1/swagger-ui.html
```

### OpenAPI Spec

```
http://localhost:8080/api/v1/v3/api-docs
```

---

## 🌐 API Examples

### Create Bill

```bash
curl -X POST http://localhost:8080/api/v1/bills \
  -u EMP001:password \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "EMP001",
    "items": [
      {
        "name": "Laptop",
        "category": "ELECTRONICS",
        "unitPrice": 1000.00,
        "quantity": 1
      }
    ]
  }'
```

### Get Bill

```bash
curl -X GET http://localhost:8080/api/v1/bills/{billId} \
  -u EMP001:password
```

### List Bills

```bash
curl -X GET http://localhost:8080/api/v1/bills?customerId=EMP001 \
  -u EMP001:password
```

### Test Users

| Customer ID | Type | Discount | Password |
|-------------|------|----------|----------|
| EMP001 | Employee | 30% | password |
| AFF001 | Affiliate | 10% | password |
| CUST001 | Regular | 0% | password |

---

## 🐳 Docker

### Commands

```bash
# Start services
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down

# Rebuild
docker-compose up -d --build
```

### Services

- **MongoDB**: Port 27017
- **Application**: Port 8080
- **Mongo Express**: Port 8081 (admin/password)

---

## 📁 Project Structure

```
src/
├── main/java/
│   ├── domain/              # Pure business logic (no framework dependencies)
│   │   ├── model/           # Entities, Value Objects, Aggregates
│   │   ├── event/           # Domain Events
│   │   └── exception/       # Domain Exceptions
│   ├── application/         # Use cases, DTOs, Ports
│   │   ├── service/         # Use case implementations
│   │   ├── port/in/         # Inbound ports (use cases)
│   │   ├── port/out/        # Outbound ports (repositories)
│   │   └── dto/             # Data Transfer Objects
│   └── infrastructure/      # Framework & technical concerns
│       ├── adapter/rest/    # REST controllers
│       ├── adapter/persistence/  # Database adapters
│       └── config/          # Spring configuration
└── test/                    # Mirror structure with tests
```

---

## 🧪 Testing Strategy

### Unit Tests

- **Domain**: Pure business logic tests
- **Application**: Service tests with Mockito
- **Coverage**: 80%+ required

### Integration Tests

- **API Tests**: Full Spring context
- **Testcontainers**: Real MongoDB instance
- **Security**: Authentication testing

---

## 🔒 Security

- HTTP Basic Authentication
- BCrypt password encoding
- Stateless sessions
- Secured endpoints (except actuator/health)

---

## 📝 Maven Commands

```bash
# Build project
mvn clean install

# Run tests
mvn test

# Generate coverage
mvn verify

# Run application
mvn spring-boot:run

# Static analysis
mvn sonar:sonar
```

---

## 👤 Author

**Mosaed Alotaibi**

---

## 📄 License

MIT License - see LICENSE file

---

**Made with ❤️ using Spring Boot, DDD, and Clean Architecture**
