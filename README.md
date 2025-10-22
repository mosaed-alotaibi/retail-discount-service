# 🛒 Retail Discount Service

> **A production-ready REST API for calculating retail discounts based on customer type and purchase history**

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-7.0-green.svg)](https://www.mongodb.com/)
[![Docker](https://img.shields.io/badge/Docker-ready-blue.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📋 Table of Contents

- [Quick Commands (TL;DR)](#-quick-commands-tldr)
- [Overview](#overview)
- [Business Rules](#business-rules)
- [Architecture](#architecture)
- [Technologies](#technologies)
- [Quick Start](#quick-start)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Code Quality](#code-quality)
- [Deployment](#deployment)
- [Project Structure](#project-structure)
- [Design Decisions](#design-decisions)

---

## ⚡ Quick Commands (TL;DR)

**Run the Application:**
```bash
docker-compose up -d
# Application available at: http://localhost:8080
```

**Run Tests:**
```bash
./mvnw test              # Unit tests
./mvnw verify            # All tests (unit + integration)
```

**Generate Coverage Report:**
```bash
./mvnw clean verify jacoco:report
open target/site/jacoco/index.html
```

**For detailed instructions, see sections below ⬇️**

---

## 🎯 Overview

The Retail Discount Service is a RESTful API that calculates discounts for retail purchases based on:
- **Customer type** (Employee, Affiliate, Long-time customer)
- **Purchase amount** (Bill-based discounts)
- **Item categories** (Grocery exemptions)

Built using modern software engineering practices including **Hexagonal Architecture**, **Domain-Driven Design (DDD)**, and comprehensive testing strategies.

### Key Features

✅ **Multiple discount types** - Percentage-based and bill-based discounts  
✅ **Category-aware** - Groceries excluded from percentage discounts  
✅ **Secure API** - Spring Security with Basic Authentication  
✅ **MongoDB persistence** - Customer and bill history storage  
✅ **Comprehensive testing** - 80%+ code coverage with unit & integration tests  
✅ **Docker-ready** - One-command deployment with docker-compose  
✅ **Code quality** - SonarQube integration, static analysis  
✅ **Observability** - Health checks, metrics via Spring Actuator

---

## 💰 Business Rules

### 1. Percentage Discounts (Mutually Exclusive)

**Priority order** (highest to lowest):
1. **Employee**: 30% off
2. **Affiliate**: 10% off
3. **Customer (2+ years)**: 5% off
4. **Regular customer**: 0% off

**Important**: Only ONE percentage discount applies per bill (the highest one).

### 2. Bill-Based Discount

**Rule**: For every $100 on the bill → $5 discount

**Application order**:
1. Apply percentage discount first (if eligible)
2. Calculate subtotal after percentage discount
3. Apply bill-based discount on the subtotal

### 3. Category Exemptions

**Groceries are excluded** from percentage discounts but **included** in bill-based discounts.

### Example Calculation

```
Original Bill:
- Laptop (Electronics): $1,000
- Groceries: $200
Total: $1,200

Customer: Employee (30% discount, 2+ years)

Step 1: Percentage Discount (30% on non-grocery)
- Laptop: $1,000 × 0.30 = $300 discount
- Groceries: $0 (exempt)
Subtotal after percentage: $1,200 - $300 = $900

Step 2: Bill-Based Discount
- $900 ÷ $100 = 9 × $5 = $45 discount

Final Amount: $900 - $45 = $855
Total Savings: $345 (28.75%)
```

---

## 🏗️ Architecture

This project follows **Hexagonal Architecture (Ports & Adapters)** combined with **Domain-Driven Design** principles.

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     PRESENTATION LAYER                      │
│  ┌──────────────┐  ┌─────────────┐  ┌──────────────────┐    │
│  │ REST         │  │ Exception   │  │ Security         │    │
│  │ Controllers  │  │ Handlers    │  │ Configuration    │    │
│  └──────────────┘  └─────────────┘  └──────────────────┘    │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                    APPLICATION LAYER                        │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              CalculateBillUseCase (Port In)          │   │
│  └──────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │                   BillService                        │   │
│  │         (Orchestrates business operations)           │   │
│  └──────────────────────────────────────────────────────┘   │
│  ┌───────────────────┐  ┌───────────────────────────────┐   │
│  │ CustomerRepository│  │ BillRepository (Ports Out)    │   │
│  │      (Port Out)   │  │                               │   │
│  └───────────────────┘  └───────────────────────────────┘   │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                      DOMAIN LAYER                           │
│  ┌──────────┐  ┌──────────┐  ┌──────────────────────────┐   │
│  │  Money   │  │ Customer │  │         Bill             │   │
│  │  (Value  │  │  (Value  │  │     (Aggregate Root)     │   │
│  │  Object) │  │  Object) │  │                          │   │
│  └──────────┘  └──────────┘  └──────────────────────────┘   │
│  ┌──────────┐  ┌──────────┐  ┌──────────────────────────┐   │
│  │ BillItem │  │ Customer │  │    ItemCategory          │   │
│  │  (Value  │  │   Type   │  │        (Enum)            │   │
│  │  Object) │  │  (Enum)  │  │                          │   │
│  └──────────┘  └──────────┘  └──────────────────────────┘   │
│                                                             │
│         ★ Pure Business Logic - No Framework Dependencies   │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                  INFRASTRUCTURE LAYER                        │
│  ┌───────────────────┐  ┌───────────────────────────────┐   │
│  │ MongoDB           │  │ MongoDB                       │   │
│  │ CustomerRepo      │  │ BillRepo (Implementations)    │   │
│  │ Implementation    │  │                               │   │
│  └───────────────────┘  └───────────────────────────────┘   │
│  ┌────────────────────────────────────────────────────────┐ │
│  │          Document Models & Mappers                     │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### Why Hexagonal Architecture?

**Benefits**:
- ✅ **Testability**: Domain logic testable without databases or frameworks
- ✅ **Flexibility**: Easy to swap MongoDB for PostgreSQL or any other database
- ✅ **Clarity**: Clear separation of concerns
- ✅ **Maintainability**: Changes in infrastructure don't affect business logic

**Core Principle**: Dependencies point **inward**. Domain knows nothing about Spring, MongoDB, or HTTP.

---

## 🛠️ Technologies

### Core Stack
- **Java 21** - 2023 LTS release with virtual threads and pattern matching
- **Spring Boot 3.5.6** - Framework for building production-ready applications
- **Spring Framework 6.x** - Core dependency injection and web support
- **MongoDB 7.0** - NoSQL database for flexible document storage

### Architecture & Design
- **Hexagonal Architecture** (Ports & Adapters)
- **Domain-Driven Design** (DDD)

### Security & Validation
- **Spring Security 6.x** - Authentication and authorization
- **Jakarta Validation 3.x** - Bean validation (JSR-380)

### Testing
- **JUnit 5** - Unit testing framework
- **Mockito** - Mocking framework
- **AssertJ** - Fluent assertions
- **Testcontainers** - Integration testing with real MongoDB
- **Spring Security Test** - Security testing utilities

### Code Quality & DevOps
- **JaCoCo** - Code coverage analysis (80%+ coverage)
- **Maven** - Build automation and dependency management
- **Docker & Docker Compose** - Containerization
- **SonarQube** - Static code analysis (optional)
- **Spring Boot Actuator** - Production-ready monitoring

---

## 🚀 Quick Start

### Prerequisites

- **Java 21** or higher ([Download](https://adoptium.net/))
- **Docker & Docker Compose** ([Download](https://www.docker.com/))
- **Maven 3.8+** (or use included Maven wrapper)

### Option 1: Docker Compose (Recommended)

**Start everything with one command:**

```bash
# Clone the repository
git clone https://github.com/mosaed-alotaibi/retail-discount-service.git
cd retail-discount-service

# Start all services (app + MongoDB)
docker-compose up -d

# Check application status
curl http://localhost:8080/actuator/health

# View logs
docker-compose logs -f app
```

The application will be available at `http://localhost:8080`

### Option 2: Local Development

**1. Start MongoDB:**
```bash
docker run -d \
  -p 27017:27017 \
  --name mongodb \
  -e MONGO_INITDB_ROOT_USERNAME=admin \
  -e MONGO_INITDB_ROOT_PASSWORD=admin123 \
  mongo:7.0-jammy
```

**2. Run the application:**
```bash
# Using Maven wrapper (recommended)
./mvnw spring-boot:run

# Or using Maven
mvn spring-boot:run

# Or build and run JAR
./mvnw clean package
java -jar target/retail-discount-service.jar
```

### Option 3: IDE (IntelliJ IDEA / Eclipse)

1. Import project as Maven project
2. Ensure JDK 21 is configured
3. Start MongoDB (via Docker or local installation)
4. Run `RetailDiscountApplication.java`

---

## 📡 API Documentation

### Base URL
```
http://localhost:8080/api/v1
```

### Authentication
All endpoints except health checks require **HTTP Basic Authentication**:
- **Username**: `user`
- **Password**: `password`

### Endpoints

#### 1. Calculate Bill Discount

**POST** `/api/v1/bills/calculate`

Calculate the final amount after applying all discounts.

**Request Body:**
```json
{
  "customerId": "CUST001",
  "customerType": "EMPLOYEE",
  "items": [
    {
      "name": "Laptop",
      "category": "ELECTRONICS",
      "price": 1000.00
    },
    {
      "name": "Milk",
      "category": "GROCERY",
      "price": 50.00
    }
  ]
}
```

**Response:** (200 OK)
```json
{
  "billId": "66a3f9b8c1e4f2a7b8c9d0e1",
  "customerId": "CUST001",
  "customerType": "EMPLOYEE",
  "originalAmount": 1050.00,
  "percentageDiscount": 300.00,
  "billBasedDiscount": 35.00,
  "totalDiscount": 335.00,
  "finalAmount": 715.00,
  "discountPercentage": 31.90,
  "calculatedAt": "2025-10-22T10:30:00Z"
}
```

**Customer Types:**
- `EMPLOYEE` - 30% discount
- `AFFILIATE` - 10% discount
- `CUSTOMER_OVER_2_YEARS` - 5% discount
- `REGULAR` - 0% discount

**Item Categories:**
- `ELECTRONICS` - Eligible for percentage discount
- `CLOTHING` - Eligible for percentage discount
- `GROCERY` - NOT eligible for percentage discount
- `OTHER` - Eligible for percentage discount

**Error Response:** (400 Bad Request)
```json
{
  "timestamp": "2025-10-22T10:30:00Z",
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid request parameters",
  "validationErrors": {
    "items": "Items list cannot be empty",
    "customerType": "Invalid customer type"
  }
}
```

#### 2. Health Check

**GET** `/actuator/health`

Check application health status (no authentication required).

**Response:** (200 OK)
```json
{
  "status": "UP",
  "components": {
    "mongo": {
      "status": "UP"
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

### Testing the API

**Using cURL:**
```bash
# Calculate discount for an employee
curl -X POST http://localhost:8080/api/v1/bills/calculate \
  -H "Content-Type: application/json" \
  -u user:password \
  -d '{
    "customerId": "CUST001",
    "customerType": "EMPLOYEE",
    "items": [
      {
        "name": "Laptop",
        "category": "ELECTRONICS",
        "price": 1000
      }
    ]
  }'

# Health check (no auth needed)
curl http://localhost:8080/actuator/health
```

**Using Postman:**
1. Import the API collection from `docs/postman/`
2. Set authorization: Basic Auth (user/password)
3. Send requests

---

## 🧪 Testing

### Run All Tests
```bash
# Run unit tests
./mvnw test

# Run integration tests (requires Docker)
./mvnw verify

# Run with coverage report
./mvnw clean verify jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Test Coverage

Achieved **80%+ code coverage** across all layers:

| Layer            | Coverage | Strategy                              |
|------------------|----------|---------------------------------------|
| Domain Layer     | ~95%     | Pure unit tests, no mocks             |
| Application Layer| ~85%     | Unit tests with mocked repositories   |
| Infrastructure   | ~75%     | Integration tests with Testcontainers |
| API Layer        | ~80%     | API tests with security               |

**Coverage Report Location**: `target/site/jacoco/index.html`

### Testing Strategy

```
         ╱╲
        ╱  ╲
       ╱ E2E ╲           ← Few end-to-end tests (API layer)
      ╱────────╲
     ╱          ╲
    ╱ Integration╲       ← Some integration tests (with real DB)
   ╱──────────────╲
  ╱                ╲
 ╱   Unit Tests     ╲    ← Many unit tests (domain + application)
╱────────────────────╲
```

**Test Types**:
- **Unit Tests**: Domain logic, application services (fast, no external dependencies)
- **Integration Tests**: Repository layer (Testcontainers with real MongoDB)
- **API Tests**: Controller layer (security, validation, error handling)

---

## 📊 Code Quality

### SonarQube Analysis

**Run SonarQube locally:**

```bash
# Start SonarQube
docker run -d -p 9000:9000 sonarqube:latest

# Run analysis
./mvnw clean verify sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=admin \
  -Dsonar.password=admin

# View report at http://localhost:9000
```

**Code Quality Metrics**:
- ✅ **0 Bugs**
- ✅ **0 Vulnerabilities**
- ✅ **0 Code Smells** (or minimal technical debt)
- ✅ **80%+ Test Coverage**
- ✅ **A Rating** in Maintainability

### Static Analysis

```bash
# Run Maven checkstyle
./mvnw checkstyle:check

# Run SpotBugs
./mvnw spotbugs:check

# Run all quality checks
./mvnw clean verify
```

---

## 🐳 Deployment

### Docker

**Build Docker image:**
```bash
docker build -t retail-discount:latest .
```

**Run container:**
```bash
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e SPRING_DATA_MONGODB_URI=mongodb://mongodb:27017/retail_discount \
  --name retail-discount-app \
  retail-discount:latest
```

### Docker Compose (Production)

**docker-compose.prod.yml:**
```yaml
version: '3.8'
services:
  app:
    image: retail-discount:latest
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATA_MONGODB_URI: mongodb://admin:password@mongodb:27017/retail_discount
    depends_on:
      - mongodb
  
  mongodb:
    image: mongo:7.0-jammy
    volumes:
      - mongodb_data:/data/db
    environment:
      MONGO_INITDB_ROOT_USERNAME: admin
      MONGO_INITDB_ROOT_PASSWORD: password

volumes:
  mongodb_data:
```

**Deploy:**
```bash
docker-compose -f docker-compose.prod.yml up -d
```

---

## 📁 Project Structure

```
retail-discount-service/
├── src/
│   ├── main/
│   │   ├── /io/mosaed/retaildiscountservice
│   │   │   ├── RetailDiscountApplication.java
│   │   │   │
│   │   │   ├── domain/                    # Domain Layer (Business Logic)
│   │   │   │   ├── model/
│   │   │   │   │   ├── Money.java         # Value Object
│   │   │   │   │   ├── Customer.java      # Value Object
│   │   │   │   │   ├── BillItem.java      # Value Object
│   │   │   │   │   ├── Bill.java          # Aggregate Root
│   │   │   │   │   ├── CustomerType.java  # Enum
│   │   │   │   │   └── ItemCategory.java  # Enum
│   │   │   │   └── exception/
│   │   │   │
│   │   │   ├── application/               # Application Layer (Use Cases)
│   │   │   │   ├── port/
│   │   │   │   │   ├── in/
│   │   │   │   │   │   └── CalculateBillUseCase.java
│   │   │   │   │   └── out/
│   │   │   │   │       ├── CustomerRepository.java
│   │   │   │   │       └── BillRepository.java
│   │   │   │   ├── service/
│   │   │   │   │   └── BillService.java
│   │   │   │   └── mapper/
│   │   │   │
│   │   │   └── adapter/                   # Infrastructure Layer
│   │   │       ├── in/web/                # Inbound Adapters
│   │   │       │   ├── controller/
│   │   │       │   ├── dto/
│   │   │       │   ├── mapper/
│   │   │       │   ├── exception/
│   │   │       │   └── config/
│   │   │       └── out/persistence/       # Outbound Adapters
│   │   │           ├── mongo/
│   │   │           ├── document/
│   │   │           └── mapper/
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-docker.yml
│   │       └── application-prod.yml
│   │
│   └── test/                              # Test Mirror Structure
│       └── java/io/mosaed/retaildiscountservice
│           ├── domain/
│           ├── application/
│           └── adapter/
│
├── docker/
│   ├── Dockerfile
│   ├── docker-compose.yml
│   └── docker-compose.prod.yml
│
├── docs/
│   ├── architecture/
│   ├── api/
│   └── postman/
│
├── pom.xml
└── README.md
```

---

## 🎨 Design Decisions

### 1. Why Hexagonal Architecture?

**Decision**: Separate domain logic from infrastructure concerns.

**Rationale**:
- Domain layer has NO framework dependencies
- Easy to test business logic without databases
- Flexibility to change databases, APIs, or frameworks
- Clear boundaries and responsibilities

### 2. Why DDD Value Objects?

**Decision**: `Money`, `Customer`, `BillItem` are immutable value objects.

**Rationale**:
- Encapsulate business rules (e.g., Money prevents negative amounts)
- Type safety (can't accidentally add Money to String)
- Immutability prevents bugs
- Equality based on values, not identity

### 3. Why BigDecimal for Money?

**Decision**: Use `BigDecimal` instead of `double` for financial calculations.

**Problem with double**:
```java
double wrong = 0.1 + 0.2;  // Result: 0.30000000000000004 ❌
```

**Solution with BigDecimal**:
```java
BigDecimal right = new BigDecimal("0.1")
    .add(new BigDecimal("0.2"));  // Result: 0.3 ✅
```

**Rationale**: Financial calculations require precision. Floating-point errors are unacceptable.

### 4. Why Separate Document Models?

**Decision**: Domain models (`Bill`) separate from MongoDB documents (`BillDocument`).

**Rationale**:
- Domain purity (no `@Document` annotations in domain)
- Optimize database schema independently
- Domain models testable without MongoDB
- Flexibility to change persistence technology

### 5. Why Only One Percentage Discount?

**Business Rule**: Customer gets the HIGHEST percentage discount only.

**Rationale**:
- Prevents discount stacking abuse
- Simpler customer experience
- Business profitability
- Industry standard practice

### 6. Why Test Different Layers Differently?

**Decision**: Different testing strategies per layer.

**Rationale**:
- **Domain**: Pure unit tests (fast, no mocks)
- **Application**: Unit tests with mocked ports (test orchestration)
- **Infrastructure**: Integration tests with Testcontainers (verify real DB operations)
- **API**: Security and validation tests (verify HTTP layer)

Each layer has different concerns requiring different testing approaches.

---

## 🤝 Contributing

This is a technical project made for chilling. For production enhancements, consider:

- **JWT Authentication** instead of Basic Auth
- **Audit Logging** for all discount calculations
- **Rate Limiting** to prevent API abuse
- **Caching** (Redis) for frequent customer lookups
- **API Versioning** (`/api/v2`) for breaking changes
- **Pagination** for bill listing endpoints
- **Distributed Tracing** (OpenTelemetry/Zipkin)
- **Kubernetes Deployment** manifests

---

## 📄 License

This project is created for practice purposes and to be licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

## 👤 Author

**Your Name**
- 📧 Email: [masaadbal@gmail.com](mailto:masaadbal@gmail.com)
- 💼 LinkedIn: [Mosaed Alotaibi](https://www.linkedin.com/in/mosaedalotaibi/)
- 🐙 GitHub: [mosaed-alotaibi](https://github.com/mosaed-alotaibi)

---

## 🙏 Acknowledgments

Built with:
- **Spring Boot** ecosystem
- **Domain-Driven Design** principles by Eric Evans
- **Hexagonal Architecture** pattern by Alistair Cockburn

---

## 📚 Additional Documentation

- [Architecture Deep Dive](docs/ARCHITECTURE.md)
- [Testing Strategy](docs/TESTING.md)
- [API Reference](docs/API.md)
- [Deployment Guide](docs/DEPLOYMENT.md)

---

**Made with ❤️ for D**