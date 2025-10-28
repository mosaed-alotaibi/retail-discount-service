# Retail Discount Service - Comprehensive Architecture Overview

## Executive Summary

The Retail Discount Service is a **Spring Boot 3.5.6** REST API built with **Java 21** that calculates retail discounts based on customer type and bill composition. The application follows **Hexagonal Architecture (Ports and Adapters)** principles, ensuring loose coupling between layers and excellent testability.

---

## 1. Overall Directory/Folder Organization

```
retail-discount-service/
├── src/
│   ├── main/
│   │   ├── java/io/mosaed/retaildiscountservice/
│   │   │   ├── domain/                    # Domain Layer (Business Logic)
│   │   │   │   ├── model/                 # Domain entities and value objects
│   │   │   │   └── exception/             # Domain-specific exceptions
│   │   │   ├── application/               # Application Layer (Use Cases)
│   │   │   │   ├── port/
│   │   │   │   │   ├── in/                # Input ports (use case interfaces)
│   │   │   │   │   └── out/               # Output ports (repository interfaces)
│   │   │   │   ├── service/               # Use case implementations
│   │   │   │   ├── mapper/                # DTO to domain object mapping
│   │   │   │   └── dto/                   # Data Transfer Objects
│   │   │   ├── infrastructure/            # Infrastructure Layer (Adapters)
│   │   │   │   ├── adapter/
│   │   │   │   │   ├── rest/              # REST API adapter
│   │   │   │   │   └── persistence/       # Data persistence adapter
│   │   │   │   ├── config/                # Spring configuration
│   │   │   │   └── security/              # Security configuration
│   │   │   └── RetailDiscountServiceApplication.java  # Entry point
│   │   └── resources/
│   │       ├── application.yml             # Default configuration
│   │       └── application-docker.yml      # Docker environment config
│   └── test/
│       └── java/                           # Unit and integration tests
├── Dockerfile                              # Multi-stage Docker build
├── docker-compose.yml                      # Complete stack orchestration
└── pom.xml                                 # Maven build configuration
```

---

## 2. Application Layering

The architecture follows **Hexagonal Architecture (Ports & Adapters)** with three distinct layers:

### Layer Structure

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION (REST)                      │
│  BillController - HTTP endpoints for bill calculation       │
└──────────────────────┬──────────────────────────────────────┘
                       │ implements
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                   APPLICATION LAYER                         │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐  │
│  │        Input Ports (Use Case Interfaces)            │  │
│  │  - CalculateBillUseCase (interface)                 │  │
│  └─────────────────────────────────────────────────────┘  │
│                       ▲                                     │
│                       │ implements                          │
│  ┌─────────────────────────────────────────────────────┐  │
│  │         Use Case Implementations                     │  │
│  │  - BillService (implements CalculateBillUseCase)    │  │
│  └─────────────────────────────────────────────────────┘  │
│                       │ uses                                │
│  ┌─────────────────────────────────────────────────────┐  │
│  │        Output Ports (Repository Interfaces)         │  │
│  │  - CustomerRepository (interface)                   │  │
│  │  - BillRepository (interface)                       │  │
│  └─────────────────────────────────────────────────────┘  │
│                       ▲                                     │
│  ┌─────────────────────────────────────────────────────┐  │
│  │              Data Mappers                            │  │
│  │  - BillMapper (DTO ↔ Domain)                        │  │
│  │  - PersistenceMapper (Document ↔ Domain)           │  │
│  └─────────────────────────────────────────────────────┘  │
└──────────────────────┬──────────────────────────────────────┘
                       │ delegates to
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                     DOMAIN LAYER                            │
│                  (Core Business Logic)                      │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐  │
│  │           Domain Entities (Aggregates)               │  │
│  │  - Bill (root aggregate)                            │  │
│  │  - Customer (domain entity)                         │  │
│  │  - BillItem (value object)                          │  │
│  │  - Money (value object)                             │  │
│  └─────────────────────────────────────────────────────┘  │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐  │
│  │           Enumerations                               │  │
│  │  - CustomerType (EMPLOYEE, AFFILIATE, etc.)        │  │
│  │  - ItemCategory (GROCERY, ELECTRONICS, etc.)       │  │
│  └─────────────────────────────────────────────────────┘  │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐  │
│  │           Domain Exceptions                          │  │
│  │  - DomainException (base)                           │  │
│  │  - CustomerNotFoundException                        │  │
│  │  - InvalidBillException                             │  │
│  └─────────────────────────────────────────────────────┘  │
└──────────────────────┬──────────────────────────────────────┘
                       │ uses
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                 INFRASTRUCTURE LAYER                        │
│             (Adapters & External Services)                 │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐  │
│  │      REST Adapter (Input Adapter)                    │  │
│  │  - BillController                                   │  │
│  │  - RestExceptionHandler                             │  │
│  │  - Authentication/Authorization                     │  │
│  └─────────────────────────────────────────────────────┘  │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐  │
│  │   MongoDB Persistence Adapter (Output Adapter)      │  │
│  │  - MongoBillRepository (implements port)            │  │
│  │  - MongoCustomerRepository (implements port)        │  │
│  │  - SpringBillRepository (Spring Data interface)     │  │
│  │  - SpringCustomerRepository (Spring Data interface) │  │
│  │  - BillDocument, CustomerDocument, BillItemDocument│  │
│  │  - PersistenceMapper (Document ↔ Domain)           │  │
│  └─────────────────────────────────────────────────────┘  │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐  │
│  │    Configuration & Infrastructure                    │  │
│  │  - ApplicationConfig (bean definitions)             │  │
│  │  - SecurityConfig (Spring Security)                 │  │
│  │  - DataInitializer (test data setup)                │  │
│  │  - CustomerUserDetailsService                       │  │
│  └─────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Key Characteristics

- **Hexagonal Architecture**: Application isolated from technology details
- **Dependency Inversion**: High-level modules don't depend on low-level modules
- **Port-Adapter Pattern**: Clear separation between what we do (ports) and how we do it (adapters)
- **Testability**: Each layer can be tested independently through mocks

---

## 3. Domain Models and Business Logic Organization

### 3.1 Core Domain Entities

#### **Bill (Root Aggregate)**
```
File: src/main/java/.../domain/model/Bill.java

Responsibilities:
- Create bills (factory method: Bill.create())
- Reconstruct bills from persistence (factory method: Bill.reconstitute())
- Calculate discounts with two tiers:
  1. Percentage discount based on customer type
  2. Bill-based discount (fixed $5 per $100)
- Encapsulate discount calculation logic
- Maintain immutability of items list

Key Methods:
- create(Customer, List<BillItem>)
- calculateDiscount() → DiscountBreakdown
- getTotalAmount() → Money
- getNetPayableAmount() → Money
- getPercentageDiscountEligibleAmount() → Money

Nested Class:
- DiscountBreakdown: Immutable data structure holding:
  - totalAmount, percentageDiscount, billBasedDiscount
  - totalDiscount, netPayable, percentageDiscountRate
```

#### **Customer (Entity)**
```
File: src/main/java/.../domain/model/Customer.java

Responsibilities:
- Maintain customer identity and type
- Calculate effective customer type based on tenure
- Determine applicable discount percentage
- Validate customer data

Key Logic:
- EMPLOYEE/AFFILIATE: Get their type discount (30%/10%)
- REGULAR customer for 2+ years: Become LONG_TERM_CUSTOMER (5%)
- REGULAR customer for <2 years: Get no discount (0%)

Key Methods:
- of(customerId, explicitType, registrationDate)
- getEffectiveType() → CustomerType
- getDiscountPercentage() → int
- getYearsAsCustomer() → int
```

#### **BillItem (Value Object)**
```
File: src/main/java/.../domain/model/BillItem.java

Responsibilities:
- Represent a line item in a bill
- Determine if item is eligible for percentage discount
- Calculate item total

Discount Eligibility:
- GROCERY: NOT eligible for percentage discount
- ELECTRONICS, CLOTHING, HOME_GOODS, OTHER: Eligible

Key Methods:
- of(name, category, unitPrice, quantity)
- getTotalPrice() → Money
- isEligibleForPercentageDiscount() → boolean
- getAmountEligibleForPercentageDiscount() → Money
```

#### **Money (Value Object)**
```
File: src/main/java/.../domain/model/Money.java

Responsibilities:
- Encapsulate monetary values with precision
- Ensure all currency operations maintain 2 decimal places
- Prevent negative amounts

Key Features:
- Precision: 2 decimal places with HALF_UP rounding
- Operations: add(), subtract(), multiply(), applyPercentageDiscount()
- Safe: No negative amounts allowed
- Immutable: All operations return new instances
```

### 3.2 Enumerations

#### **CustomerType**
```
EMPLOYEE(30%)           - Employee discount
AFFILIATE(10%)          - Affiliate discount
LONG_TERM_CUSTOMER(5%)  - Regular customers with 2+ years tenure
REGULAR(0%)             - New/regular customers
```

#### **ItemCategory**
```
GROCERY(false)          - Not eligible for percentage discount
ELECTRONICS(true)       - Eligible
CLOTHING(true)          - Eligible
HOME_GOODS(true)        - Eligible
OTHER(true)             - Eligible
```

### 3.3 Exception Hierarchy

```
DomainException (abstract, extends RuntimeException)
├── CustomerNotFoundException
│   └── Thrown when customer ID not found in repository
│   └── Contains: customerId for tracing
└── InvalidBillException
    └── Thrown when bill data violates domain rules
    └── Contains: wrapped validation errors
```

### 3.4 Discount Calculation Logic

The Bill entity implements a **two-tier discount system**:

```
STEP 1: Calculate Percentage Discount
├── Get customer's discount percentage
├── Identify items eligible for percentage discount (non-grocery)
├── Apply percentage to eligible items only
└── Result: percentageDiscount

STEP 2: Calculate Bill-Based Discount
├── Calculate amount AFTER percentage discount
├── For every full $100: apply $5 discount
├── Example: $785 → 7 hundreds → $35 discount
└── Result: billBasedDiscount

STEP 3: Calculate Net Payable
├── Total Amount - Percentage Discount - Bill-Based Discount
└── Result: netPayable

Example Calculation (Employee with $1,115 bill):
├── Items: $1000 electronics + $100 electronics + $15 grocery
├── Percentage eligible: $1100 (groceries excluded)
├── Percentage discount (30%): $330
├── After percentage: $1115 - $330 = $785
├── Bill-based discount: floor(785/100) * 5 = 7 * 5 = $35
├── Net Payable: $1115 - $330 - $35 = $750
└── Total Discount: $365 (33%)
```

---

## 4. API Endpoint Structure

### REST Controller: BillController

**Base Path**: `/api/v1/bills` (after server context path)

#### **1. Calculate Bill (Main Endpoint)**
```
POST /api/v1/bills/calculate

Authentication: HTTP Basic Auth required
Request Body:
{
  "customerId": "EMP001",
  "items": [
    {
      "name": "Laptop",
      "category": "ELECTRONICS",
      "unitPrice": 1000.00,
      "quantity": 1
    }
  ]
}

Response (200 OK):
{
  "billId": "550e8400-e29b-41d4-a716-446655440000",
  "customerId": "EMP001",
  "calculatedAt": "2024-01-15T10:30:00",
  "totalAmount": 1000.00,
  "percentageDiscount": 300.00,
  "percentageDiscountRate": 30,
  "billBasedDiscount": 35.00,
  "totalDiscount": 335.00,
  "netPayable": 665.00
}

Error Responses:
- 400 Bad Request: Validation errors (invalid items, missing fields)
- 404 Not Found: Customer not found
- 401 Unauthorized: Missing/invalid credentials
- 500 Internal Server Error: Unexpected errors
```

#### **2. Calculate My Bill (Authenticated Convenience Endpoint)**
```
POST /api/v1/bills/calculate/my-bill

Authentication: HTTP Basic Auth required
- Customer ID extracted from authenticated user
- No need to provide customerId in request

Request Body:
{
  "items": [
    {
      "name": "Laptop",
      "category": "ELECTRONICS",
      "unitPrice": 1000.00,
      "quantity": 1
    }
  ]
}

Response: Same as /calculate endpoint

Benefit: Uses authenticated user's ID automatically
```

#### **3. Health Check (Public Endpoint)**
```
GET /api/v1/bills/health

Authentication: None required (publicly accessible)

Response (200 OK):
"Retail Discount Service is running"

Purpose: Service availability verification
```

### Request Validation

All endpoints validate inputs using Jakarta Validation annotations:

```
Field Validations:
├── customerId: @NotBlank - Cannot be empty
├── items: @NotEmpty - At least one item required
├── items[]: @Valid - Each item must be valid
└── BillItemDto fields:
    ├── name: @NotBlank
    ├── category: @NotBlank (must be valid enum)
    ├── unitPrice: @NotNull, @DecimalMin("0.01")
    └── quantity: @Min(1)
```

### Response Structure

All error responses follow consistent format:

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Customer not found with ID: NONEXISTENT",
  "path": "/api/v1/bills/calculate",
  "fieldErrors": {
    "items": "Bill must contain at least one item",
    "name": "Item name is required"
  }
}
```

---

## 5. Dependency Flow Between Components

### Data Flow Through Layers

```
HTTP Request
    ↓
[REST Adapter - BillController]
    ├─ Validates HTTP request
    ├─ Extracts authentication info
    └─ Creates CalculateBillCommand DTO
    ↓
[Application Layer - BillService]
    ├─ Implements CalculateBillUseCase interface
    ├─ Calls CustomerRepository.findById() → Customer
    ├─ Maps DTO items → Domain BillItems via BillMapper
    ├─ Creates Bill aggregate: Bill.create()
    ├─ Calls bill.calculateDiscount() → DiscountBreakdown
    ├─ Persists bill via BillRepository.save()
    └─ Maps domain result → BillCalculationResponse DTO
    ↓
[Domain Layer]
    ├─ Bill.create() validates all inputs
    ├─ Encapsulates discount calculation logic
    └─ Returns immutable results
    ↓
[Infrastructure Adapter - MongoDB]
    ├─ MongoBillRepository receives domain Bill
    ├─ PersistenceMapper converts: Bill → BillDocument
    ├─ SpringBillRepository persists to MongoDB
    └─ Returns saved Bill (reconstructed from document)
    ↓
Response DTO serialized to JSON
    ↓
HTTP 200 Response with BillCalculationResponse
```

### Dependency Injection

**Framework**: Spring Framework (Constructor Injection)

```
BillController
├─ depends on: CalculateBillUseCase (interface)
│  └─ injected: BillService (implementation)
│     ├─ depends on: CustomerRepository (interface)
│     │  └─ injected: MongoCustomerRepository (adapter)
│     │     └─ uses: SpringCustomerRepository (Spring Data)
│     └─ depends on: BillRepository (interface)
│        └─ injected: MongoBillRepository (adapter)
│           └─ uses: SpringBillRepository (Spring Data)

Configuration: ApplicationConfig.java
├─ @Bean CalculateBillUseCase
│  └─ new BillService(customerRepository, billRepository)
└─ Repositories auto-discovered via @Repository annotations
```

### Port-Adapter Pattern

```
Application Layer (Port Definitions)
├─ CalculateBillUseCase (input port)
└─ BillRepository, CustomerRepository (output ports)
    ↑
    │ implemented by
    │
Infrastructure Layer (Adapters)
├─ BillService (implements CalculateBillUseCase)
├─ MongoBillRepository (implements BillRepository)
└─ MongoCustomerRepository (implements CustomerRepository)
    ↑
    │ delegates to
    │
Spring Data Layer
├─ SpringBillRepository (MongoRepository)
└─ SpringCustomerRepository (MongoRepository)
```

### No Coupling to Framework Details

```
Application Layer NEVER knows about:
✗ @Repository, @Service annotations
✗ MongoRepository, Spring Data
✗ MongoDB documents or collections
✗ HTTP requests or responses
✗ Spring Security details

Why? Because the application depends on:
✓ Port interfaces (BillRepository, CustomerRepository)
✓ Domain objects (Bill, Customer, BillItem)
✓ DTOs for serialization (not dependency)
```

---

## 6. Configuration and Infrastructure Setup

### 6.1 Spring Configuration

#### **ApplicationConfig.java**
```
@Configuration
- Defines beans for application layer
- Creates CalculateBillUseCase bean with dependencies
- Pure bean definitions, no XML or annotations on domain classes
```

#### **SecurityConfig.java**
```
@Configuration
@EnableWebSecurity
- HTTP Basic Authentication enabled
- CSRF disabled (stateless API)
- Stateless session creation
- BCryptPasswordEncoder for password hashing

Security Rules:
├─ /bills/health: permitAll (public)
├─ /bills/**: authenticated (requires HTTP Basic Auth)
└─ Everything else: authenticated

Authentication Flow:
├─ Client sends: Authorization: Basic base64(username:password)
├─ Spring Security decodes and validates
├─ CustomerUserDetailsService loads user details
└─ Bill controller extracts customerId from authentication principal
```

#### **DataInitializer.java**
```
@Configuration
- CommandLineRunner bean
- Runs on application startup (unless test profile)
- Creates test customers:
  ├─ EMP001: Employee (30% discount)
  ├─ AFF001: Affiliate (10% discount)
  ├─ CUST001: Regular → Long-term after 3 years (5% discount)
  └─ CUST002: Regular, new (0% discount)
- Default password: "password" (BCrypt hashed)
```

### 6.2 MongoDB Configuration

#### **application.yml**
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/retail_discount
      database: retail_discount
      auto-index-creation: true
  jackson:
    serialization:
      write-dates-as-timestamps: false
    date-format: yyyy-MM-dd'T'HH:mm:ss
    time-zone: UTC

server:
  port: 8080
  servlet:
    context-path: /api/v1

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

#### **MongoDB Indexes**

Automatically created via `@Indexed` annotations:

```
CustomerDocument:
├─ @Id customerId (primary key)
├─ @Indexed customerType (for filtering)
└─ @Indexed registrationDate (for tenure filtering)

BillDocument:
├─ @Id billId (primary key)
├─ @Indexed customerId (foreign key)
├─ @Indexed createdAt (for time-range queries)
└─ @CompoundIndex(customer_id, created_at desc)
   (for efficient queries on customer's bills by date)
```

### 6.3 Containerization

#### **Dockerfile (Multi-Stage Build)**

**Stage 1: Build**
```dockerfile
FROM eclipse-temurin:21-jdk-alpine
- Base image with Java 21 JDK
- Copy source code and Maven wrapper
- Run: mvn clean package -DskipTests
- Extract JAR layers for optimal caching
```

**Stage 2: Runtime**
```dockerfile
FROM eclipse-temurin:21-jre-alpine
- Base image with Java 21 JRE (smaller)
- Create non-root user 'spring' for security
- Copy layers from build stage
- Expose port 8080
- Health check: wget to /actuator/health
- JVM tuning for containers:
  ├─ -XX:+UseContainerSupport
  ├─ -XX:MaxRAMPercentage=75.0
  ├─ -XX:+UseG1GC
  └─ -Djava.security.egd=file:/dev/./urandom
```

Benefits:
- Smaller final image (JRE instead of JDK)
- Faster builds (cached layers)
- Non-root user (security)
- Health checks enabled

#### **docker-compose.yml**

**Services**:
```yaml
mongodb:
  - Image: mongo:7.0-jammy
  - Port: 27017
  - Credentials: admin/admin123
  - Database: retail_discount
  - Health check: mongosh ping command
  - Volumes: MongoDB data persistence

app:
  - Build: Dockerfile (custom image)
  - Port: 8080 (mapped)
  - Environment: Spring profiles, MongoDB URI, Java options
  - Depends on: MongoDB (condition: service_healthy)
  - Health check: wget to /actuator/health

mongo-express:
  - Image: mongo-express:1.0-20-alpine3.19
  - Port: 8081 (web UI)
  - Purpose: Visual MongoDB management
  - Credentials: admin/admin
```

**Network**:
```yaml
retail-network (bridge)
- All services connected
- Service-to-service communication by name
```

**Volumes**:
```yaml
mongodb_data: Persistent MongoDB data
mongodb_config: MongoDB configuration
logs: Application logs (optional)
```

**Startup Sequence**:
1. MongoDB starts first
2. MongoDB health check passes
3. App starts (after MongoDB healthy)
4. App health check begins
5. Mongo Express starts (after MongoDB healthy)

### 6.4 Maven Configuration

#### **Key Plugins**:

```xml
1. Maven Compiler Plugin
   └─ Java 21, -parameters flag for reflection

2. Spring Boot Maven Plugin
   └─ package → executable JAR with embedded Tomcat

3. Maven Surefire Plugin
   └─ Unit tests: **/*Test.java, **/*Tests.java
   └─ Excludes: *IT.java (integration tests)

4. Maven Failsafe Plugin
   └─ Integration tests: **/*IntegrationTest.java, **/*IT.java
   └─ Runs after unit tests

5. JaCoCo Plugin
   └─ Code coverage: Unit + Integration tests
   └─ Coverage report: target/site/jacoco/
   └─ Minimum coverage: 80% per package

6. SonarQube Scanner Plugin
   └─ Static code analysis integration
   └─ Exclusions: config/, DTOs, documents
```

### 6.5 Build Profiles

```xml
dev (default)
├─ Property: spring.profiles.active=dev
└─ Load: application.yml

docker
├─ Property: spring.profiles.active=docker
└─ Load: application-docker.yml
└─ MongoDB URI: mongodb://admin:admin123@mongodb:27017/...

prod
├─ Property: spring.profiles.active=prod
└─ Load: application-prod.yml (not in repo - environment-specific)
```

---

## 7. Testing Strategy

### Test Organization

```
src/test/java/
├── Unit Tests (run by Surefire)
│   ├── domain/model/
│   │   ├── BillTest.java - Bill aggregate logic
│   │   ├── CustomerTest.java - Customer business rules
│   │   └── MoneyTest.java - Money value object operations
│   ├── application/
│   │   ├── service/BillServiceTest.java - Use case with mocks
│   │   └── mapper/BillMapperTest.java - DTO/domain mapping
│   └── ...
│
└── Integration Tests (run by Failsafe)
    ├── infrastructure/adapter/
    │   ├── rest/controller/BillControllerIntegrationTest.java
    │   │   └─ Full Spring context, MockMvc, HTTP testing
    │   └── persistence/mongodb/repository/
    │       └─ CustomerRepositoryIntegrationTest.java
    │           └─ Testcontainers MongoDB
    └── RetailDiscountServiceApplicationTests.java
        └─ Application context loading test
```

### Test Coverage

**Unit Testing Approach**:
- Domain models tested with real objects
- Services tested with mocked repositories
- Mappers tested with real domain/DTO objects
- Focus: Business logic, edge cases

**Integration Testing Approach**:
- Full Spring Boot context loaded
- Real database (Testcontainers MongoDB)
- MockMvc for HTTP testing
- Security testing (authentication/authorization)

**Coverage Requirements**:
- Minimum: 80% line coverage per package
- Excludes: config/, DTOs, documents

### Example: BillServiceTest

```java
@ExtendWith(MockitoExtension.class)
class BillServiceTest {
  @Mock CustomerRepository customerRepository;
  @Mock BillRepository billRepository;
  
  BillService billService;
  
  @BeforeEach
  void setup() {
    billService = new BillService(
      customerRepository,
      billRepository
    );
  }
  
  @Test
  void shouldCalculateBillForExistingCustomer() {
    // Given: Valid command
    // When: Execute use case
    // Then: Verify result and verify mock interactions
  }
}
```

---

## 8. Key Architectural Decisions

### 1. Hexagonal Architecture
**Why**: Isolate business logic from framework details
- Domain layer doesn't depend on any framework
- Easy to switch persistence (MongoDB → PostgreSQL)
- Highly testable

### 2. Value Objects for Money
**Why**: Prevent monetary calculation errors
- Encapsulate precision rules (2 decimal places)
- Prevent negative amounts
- Type safety (Money vs String)

### 3. Aggregate Pattern for Bill
**Why**: Encapsulate all bill-related logic
- Discount calculation rules live in Bill
- Consistency: Bill always has valid state
- Prevent partial updates (atomicity)

### 4. Port-Adapter Repositories
**Why**: Separate domain repositories from Spring Data
```
Domain Port: BillRepository (uses Bill objects)
Spring Adapter: MongoBillRepository
Spring Data: SpringBillRepository (uses BillDocument)
```

### 5. DTO/Domain Mapping
**Why**: API contracts independent of domain model
- REST API can change without affecting domain
- Type safety in API layer
- Clear transformation logic in mappers

### 6. Spring Security with HTTP Basic
**Why**: Simple stateless authentication for API
- No session state (12-factor app principles)
- Easy to scale (no sticky sessions needed)
- Authentication via Authorization header

### 7. Two-Tier Discount System
**Why**: Real-world business rules requirement
- Percentage discount: Customer type benefit
- Bill-based discount: Purchase volume incentive
- Combined effect maximizes customer benefit

### 8. Multi-Stage Docker Build
**Why**: Optimize image size and build time
- Build stage: Large JDK only needed for compilation
- Runtime stage: Small JRE for execution
- Layer caching: Faster rebuilds

---

## 9. Data Flow Examples

### Complete Request-Response Flow

#### Example 1: Successful Bill Calculation

```
HTTP Request:
POST /api/v1/bills/calculate
Authorization: Basic RU1QMDAxOnBhc3N3b3Jk
{
  "customerId": "EMP001",
  "items": [
    {"name": "Laptop", "category": "ELECTRONICS", "unitPrice": 1000, "quantity": 1}
  ]
}

↓ BillController.calculateBill()
├─ Parse JSON → CalculateBillCommand
├─ Validate command (jakarta.validation)
├─ Call calculateBillUseCase.execute(command)

↓ BillService.execute()
├─ customerRepository.findById("EMP001")
├─ Create Bill aggregate
├─ bill.calculateDiscount()
├─ billRepository.save(bill)
├─ BillMapper.toResponse(bill)

↓ Domain Logic (Bill.calculateDiscount)
├─ Step 1: percentageDiscount = $1000 * 30% = $300
├─ Step 2: afterDiscount = $1000 - $300 = $700
├─ Step 3: billBasedDiscount = floor(700/100) * 5 = $35
├─ Step 4: netPayable = $1000 - $300 - $35 = $665

↓ Persistence (MongoBillRepository)
├─ Convert Bill → BillDocument
├─ springRepository.save(document)
├─ Retrieve customer (reconstruct Bill)
├─ Return domain Bill object

↓ Response Mapping
├─ BillMapper.toResponse(bill)
├─ Return BillCalculationResponse DTO

HTTP Response (200 OK):
{
  "billId": "550e8400...",
  "customerId": "EMP001",
  "calculatedAt": "2024-01-15T10:30:00",
  "totalAmount": 1000.00,
  "percentageDiscount": 300.00,
  "percentageDiscountRate": 30,
  "billBasedDiscount": 35.00,
  "totalDiscount": 335.00,
  "netPayable": 665.00
}
```

#### Example 2: Validation Error

```
HTTP Request:
POST /api/v1/bills/calculate
{
  "customerId": "EMP001",
  "items": [
    {"name": "", "category": "ELECTRONICS", "unitPrice": -100, "quantity": 0}
  ]
}

↓ Spring Validation (Jakarta Validation)
├─ @NotBlank name → ERROR: "Item name is required"
├─ @DecimalMin unitPrice → ERROR: "Unit price must be > 0"
├─ @Min quantity → ERROR: "Quantity must be at least 1"

↓ RestExceptionHandler.handleValidationErrors()
├─ Extract field errors
├─ Build ErrorResponse with field details

HTTP Response (400 Bad Request):
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed: name - Item name is required, unitPrice - Unit price must be > 0, quantity - Quantity must be at least 1",
  "path": "/api/v1/bills/calculate",
  "fieldErrors": {
    "name": "Item name is required",
    "unitPrice": "Unit price must be > 0",
    "quantity": "Quantity must be at least 1"
  }
}
```

#### Example 3: Customer Not Found

```
HTTP Request:
POST /api/v1/bills/calculate
{
  "customerId": "NONEXISTENT",
  "items": [...]
}

↓ BillService.execute()
├─ customerRepository.findById("NONEXISTENT")
├─ Optional.empty()
├─ orElseThrow() → CustomerNotFoundException

↓ RestExceptionHandler.handleCustomerNotFound()
├─ Build ErrorResponse with HTTP 404

HTTP Response (404 Not Found):
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Customer not found with ID: NONEXISTENT",
  "path": "/api/v1/bills/calculate"
}
```

---

## 10. Technology Stack Summary

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **Runtime** | Java 21 | Modern language features, better performance |
| **Framework** | Spring Boot 3.5.6 | REST API, dependency injection, auto-configuration |
| **Database** | MongoDB 7.0 | NoSQL document storage, flexible schema |
| **ORM/Data** | Spring Data MongoDB | Repository pattern, auto-implemented queries |
| **Security** | Spring Security 6 | Authentication, authorization, password hashing |
| **Validation** | Jakarta Validation | Input validation annotations |
| **JSON** | Jackson | JSON serialization/deserialization |
| **Logging** | SLF4J + Logback | Structured logging |
| **Testing** | JUnit 5, Mockito, Testcontainers | Unit and integration testing |
| **Code Coverage** | JaCoCo | Test coverage analysis |
| **Quality** | SonarQube | Static code analysis |
| **Build** | Maven | Project build and dependency management |
| **Containerization** | Docker, Docker Compose | Application packaging and orchestration |

---

## 11. Summary

The Retail Discount Service exemplifies modern Java application architecture with:

1. **Clean Code**: Hexagonal architecture isolates business logic
2. **Type Safety**: Value objects and domain entities prevent errors
3. **Testability**: Each layer independently testable
4. **Scalability**: Stateless API, containerized deployment
5. **Maintainability**: Clear separation of concerns, minimal coupling
6. **Production-Ready**: Comprehensive error handling, health checks, logging
7. **Security**: HTTP Basic Auth, password hashing, input validation
8. **Monitoring**: Actuator endpoints, health checks, structured logging

This architecture ensures the system remains flexible and maintainable as business requirements evolve.

