# Testing Strategy Guide

## Overview

This project demonstrates a comprehensive testing strategy following the testing pyramid principle with tests at multiple layers using appropriate testing techniques for each layer.

## Testing Pyramid

```
           /\
          /  \  End-to-End (Few)
         /____\
        /      \  Integration Tests (Some)
       /________\
      /          \  Unit Tests (Many)
     /____________\
```

## Test Categories

### 1. Domain Layer Unit Tests (Pure)
**Location**: `src/test/java/com/retail/discount/domain/model/`

**Strategy**: Pure unit tests with NO mocking, NO frameworks

**Tests**:
- `MoneyTest.java` - Value object calculations
- `CustomerTest.java` - Customer type and discount logic
- `BillTest.java` - Core discount calculation business logic

**Why no mocking**: Domain objects have no dependencies. We test them directly with real instances.

**Benefits**:
- Run in milliseconds
- No framework overhead
- Tests the actual business logic
- Easy to understand and maintain

**Example**:
```java
@Test
void employeeShouldGet30PercentOff() {
    Customer employee = Customer.of("EMP001", CustomerType.EMPLOYEE, LocalDate.now());
    List<BillItem> items = List.of(createElectronicsItem(1000.00));
    
    Bill bill = Bill.create(employee, items);
    
    assertThat(bill.getNetPayableAmount()).isEqualTo(Money.of(665.00));
}
```

### 2. Application Layer Unit Tests (Mocked)
**Location**: `src/test/java/com/retail/discount/application/service/`

**Strategy**: Unit tests using Mockito to mock repository interfaces

**Tests**:
- `BillServiceTest.java` - Use case orchestration logic

**What we mock**: Repository interfaces (output ports)
**What we DON'T mock**: Domain objects (they're part of what we're testing)

**Benefits**:
- Tests orchestration logic in isolation
- No database needed
- Fast execution
- Demonstrates proper Mockito usage

**Example**:
```java
@Test
void shouldCalculateBillForExistingCustomer() {
    // Mock repository behavior
    when(customerRepository.findById("EMP001"))
        .thenReturn(Optional.of(testCustomer));
    
    // Execute use case
    BillCalculationResponse response = billService.execute(command);
    
    // Verify interactions
    verify(customerRepository).findById("EMP001");
    verify(billRepository).save(any());
}
```

### 3. Repository Integration Tests
**Location**: `src/test/java/com/retail/discount/infrastructure/adapter/persistence/`

**Strategy**: Integration tests with real MongoDB using Testcontainers

**Tests**:
- `CustomerRepositoryIntegrationTest.java` - MongoDB operations

**Technology**: Testcontainers spins up real MongoDB in Docker

**Benefits**:
- Tests actual database operations
- Verifies document mapping
- Catches MongoDB-specific issues
- Fresh database for each test run

**Example**:
```java
@Container
static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:5.0");

@Test
void shouldSaveAndRetrieveCustomer() {
    Customer customer = Customer.of("TEST001", CustomerType.EMPLOYEE, LocalDate.now());
    
    customerRepository.save(customer);
    
    Optional<Customer> retrieved = customerRepository.findById("TEST001");
    assertThat(retrieved).isPresent();
}
```

### 4. API Integration Tests
**Location**: `src/test/java/com/retail/discount/infrastructure/adapter/rest/controller/`

**Strategy**: Full Spring Boot integration tests with security

**Tests**:
- `BillControllerIntegrationTest.java` - HTTP API with authentication

**Technology**: MockMvc for HTTP testing, Spring Security Test for auth

**Benefits**:
- Tests complete HTTP request/response cycle
- Verifies JSON serialization
- Tests Spring Security integration
- Validates error handling and status codes

**Example**:
```java
@Test
void shouldCalculateBillWithAuthentication() throws Exception {
    mockMvc.perform(post("/bills/calculate")
            .with(httpBasic("EMP001", "password"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.netPayable").value(665.00));
}
```

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run Tests with Coverage Report
```bash
mvn clean test jacoco:report
```

Coverage report will be generated at: `target/site/jacoco/index.html`

### Run Specific Test Class
```bash
mvn test -Dtest=BillTest
```

### Run Tests with Coverage Verification
```bash
mvn clean verify
```

This will fail the build if coverage is below thresholds:
- Line coverage < 80%
- Branch coverage < 70%

### Skip Tests (for fast builds)
```bash
mvn clean install -DskipTests
```

## Coverage Goals

**Target Coverage**:
- **Line Coverage**: 80%+
- **Branch Coverage**: 70%+

**What's Excluded from Coverage**:
- Configuration classes (`**/config/**`)
- Main application class
- DTOs (simple data holders)
- Document classes (MongoDB models)

## Test Execution Time

**Expected execution times**:
- Domain unit tests: < 100ms
- Application unit tests: < 200ms
- Repository integration tests: ~2-3 seconds (Docker startup)
- API integration tests: ~3-5 seconds (Full Spring context)

**Total test suite**: ~5-10 seconds

## Continuous Integration

Tests are designed to run in CI/CD pipelines:
- No manual setup required
- Testcontainers handles MongoDB automatically
- All dependencies in pom.xml
- Deterministic (no flaky tests)

## Best Practices Demonstrated

1. **Testing Pyramid**: Many fast unit tests, fewer slow integration tests
2. **Right Tool for Right Layer**: Pure unit tests for domain, mocks for application, real DB for repositories
3. **Clear Test Names**: `@DisplayName` annotations explain what's being tested
4. **Given-When-Then**: Tests follow clear structure
5. **AssertJ**: Fluent assertions for readability
6. **Test Organization**: `@Nested` classes group related scenarios
7. **No Test Pollution**: Each test is independent
8. **Realistic Data**: Tests use meaningful business scenarios

## Interview Talking Points

**On Testing Strategy**:
"I followed the testing pyramid with lots of fast unit tests at the base, some integration tests in the middle, and comprehensive API tests at the top. Each layer uses the appropriate testing strategy - pure unit tests for domain logic, Mockito for application orchestration, Testcontainers for database integration, and Spring Boot Test for the API layer."

**On Mockito Usage**:
"I used Mockito to mock the repository interfaces in the application layer tests. This lets me test the orchestration logic without touching the database. I mock the output ports but never the domain objects, because domain logic is what I'm testing. The mocks use when-thenReturn for behavior definition and verify for interaction checking."

**On Code Coverage**:
"I achieved over 80% line coverage and 70% branch coverage as measured by JaCoCo. The domain layer, which contains all business logic, has close to 100% coverage because it's pure Java with no dependencies - very easy to test thoroughly. The coverage reports are generated automatically by Maven and can be viewed in HTML format."

**On Integration Testing**:
"For integration tests, I used Testcontainers to spin up a real MongoDB instance in Docker. This gives me confidence that the MongoDB operations, document mapping, and Spring Data configuration all work correctly together. It's much better than using an in-memory fake database because it catches real MongoDB-specific issues."

## Viewing Coverage Reports

After running `mvn test jacoco:report`, open:
```
target/site/jacoco/index.html
```

The report shows:
- Overall coverage percentages
- Coverage by package
- Coverage by class
- Line-by-line coverage (green = covered, red = not covered)
- Branch coverage for conditionals
- Missed complexity

## Test Data Strategy

**Domain Tests**: Create objects directly with factory methods
**Application Tests**: Use Mockito to return predefined test data  
**Integration Tests**: Save real data to Testcontainer database
**API Tests**: Use actual data saved via repository in `@BeforeEach`

## Troubleshooting

**If tests fail with "Container not found"**:
- Ensure Docker is running
- Testcontainers requires Docker to be accessible

**If coverage is below threshold**:
- Run `mvn jacoco:report` to see which lines are missing
- Focus on testing conditional branches and error paths

**If tests are slow**:
- Unit tests should be fast (< 1 second total)
- Integration tests take longer due to Docker startup
- Consider running unit tests separately: `mvn test -Dtest=*Test` (excludes integration tests)