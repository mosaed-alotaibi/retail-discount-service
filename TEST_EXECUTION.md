# Test Execution Guide

## Prerequisites

Before running tests, ensure you have:
- ✅ Java 11 or higher installed
- ✅ Maven 3.6+ installed
- ✅ Docker running (required for Testcontainers integration tests)

Check your setup:
```bash
java -version    # Should show Java 11+
mvn -version     # Should show Maven 3.6+
docker ps        # Should connect without error
```

---

## Quick Start

### Run All Tests
```bash
mvn clean test
```

**Expected output:**
```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.retail.discount.domain.model.MoneyTest
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.retail.discount.domain.model.CustomerTest
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.retail.discount.domain.model.BillTest
[INFO] Tests run: 18, Failures: 0, Errors: 0, Skipped: 0
...
[INFO] BUILD SUCCESS
```

### Generate Coverage Report
```bash
mvn clean test jacoco:report
```

Then open the report:
```bash
# On Mac/Linux
open target/site/jacoco/index.html

# On Windows
start target/site/jacoco/index.html

# Or manually navigate to:
target/site/jacoco/index.html
```

---

## Running Specific Test Categories

### 1. Run Only Domain Unit Tests (Fastest)
```bash
mvn test -Dtest="*Test" -DfailIfNoTests=false
```

**Why?** Domain tests are pure unit tests with no dependencies. They run in milliseconds.

### 2. Run Only Application Layer Tests (With Mocking)
```bash
mvn test -Dtest="*ServiceTest"
```

**Why?** These demonstrate Mockito

### 3. Run Only Integration Tests
```bash
mvn test -Dtest="*IntegrationTest"
```

**Why?** These test with real MongoDB via Testcontainers. Takes longer but tests the full stack.

### 4. Run Single Test Class
```bash
mvn test -Dtest=BillTest
mvn test -Dtest=BillServiceTest
mvn test -Dtest=BillControllerIntegrationTest
```

### 5. Run Single Test Method
```bash
mvn test -Dtest=BillTest#employeeShouldGet30PercentOffElectronics
```

---

## Understanding Test Output

### Successful Test Run
```
[INFO] Running com.retail.discount.domain.model.BillTest
[INFO] Tests run: 18, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.234 s
```

- **Tests run**: Total number of test methods executed
- **Failures**: Test assertions that failed (business logic errors)
- **Errors**: Test execution errors (technical failures)
- **Skipped**: Tests marked with @Disabled
- **Time elapsed**: How long the test class took

### Failed Test Example
```
[ERROR] Tests run: 18, Failures: 1, Errors: 0, Skipped: 0
[ERROR] employeeShouldGet30PercentOff  Time elapsed: 0.023 s  <<< FAILURE!
java.lang.AssertionError: 
Expecting:
  <Money{$700.00}>
to be equal to:
  <Money{$665.00}>
```

**Reading failures:**
- Test name that failed
- Expected vs actual values
- Stack trace showing where the assertion failed

---

## JaCoCo Coverage Reports

### Generate and View Coverage
```bash
# Generate coverage report
mvn clean test jacoco:report

# Open report in browser
open target/site/jacoco/index.html
```

### Understanding the Coverage Report

**Main Page Shows:**
```
Element          Missed Instructions  Cov.   Missed Branches  Cov.
───────────────────────────────────────────────────────────────────
Total            245 of 1,523         84%    28 of 156        82%
```

**Coverage Metrics:**
- **Instructions**: Bytecode instructions (most granular)
- **Branches**: if/else, switch cases, loops
- **Lines**: Source code lines
- **Methods**: Individual methods
- **Classes**: Individual classes

**Color Coding:**
- 🟢 **Green**: Fully covered (100%)
- 🟡 **Yellow**: Partially covered
- 🔴 **Red**: Not covered (0%)

### Drilling Down

Click on a package → Click on a class → See line-by-line coverage:

```java
// Green background = covered
public Money add(Money other) {
    return new Money(this.amount.add(other.amount));
}

// Red background = not covered
public Money subtract(Money other) {
    // This line never executed in tests
}

// Yellow = partially covered (branch not taken)
if (amount < 0) {  // One branch covered, one not
    throw new Exception();
}
```

---

## Coverage Thresholds

### Our Thresholds (Configured in pom.xml)
```xml
<limit>
    <counter>LINE</counter>
    <minimum>0.80</minimum>  <!-- 80% line coverage -->
</limit>
<limit>
    <counter>BRANCH</counter>
    <minimum>0.70</minimum>  <!-- 70% branch coverage -->
</limit>
```

### Verify Coverage Meets Thresholds
```bash
mvn clean verify
```

**If coverage is below threshold:**
```
[ERROR] Rule violated for package com.retail.discount: 
lines covered ratio is 0.75, but expected minimum is 0.80
[INFO] BUILD FAILURE
```

**Fix by:**
1. Check the coverage report to see what's not covered
2. Add tests for uncovered lines/branches
3. Run `mvn test jacoco:report` again

---

## Continuous Integration Setup

### GitHub Actions Example
```yaml
name: Test and Coverage

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v2
      
      - name: Set up JDK 11
        uses: actions/setup-java@v2
        with:
          java-version: '11'
          
      - name: Run tests with coverage
        run: mvn clean verify
        
      - name: Upload coverage report
        uses: actions/upload-artifact@v2
        with:
          name: coverage-report
          path: target/site/jacoco/
```

---

## Troubleshooting

### "Tests run: 0" or No tests found
**Problem**: Maven isn't finding your tests

**Solutions:**
1. Check test file names end with `Test.java` or `Tests.java`
2. Check test classes are in `src/test/java`
3. Check test methods have `@Test` annotation
4. Run with `-X` for debug output: `mvn test -X`

### "Container startup failed" in Integration Tests
**Problem**: Testcontainers can't start MongoDB container

**Solutions:**
1. Verify Docker is running: `docker ps`
2. Check Docker has internet access (needs to pull mongo image)
3. Check disk space: `df -h`
4. Try: `docker pull mongo:5.0` manually

### Tests Pass Locally but Fail in CI
**Problem**: Different environment

**Common causes:**
1. Time zone differences (dates)
2. Missing Docker in CI
3. Insufficient memory for containers

**Solutions:**
1. Use fixed dates in tests: `LocalDate.of(2024, 1, 1)`
2. Configure Docker in CI
3. Increase container memory limits

### "OutOfMemoryError" in Tests
**Problem**: Too many tests/containers running simultaneously

**Solution:**
```bash
# Increase Maven memory
export MAVEN_OPTS="-Xmx1024m"
mvn clean test
```

---

## Test Performance Tips

### Parallel Execution
```xml
<!-- Add to pom.xml surefire plugin -->
<configuration>
    <parallel>classes</parallel>
    <threadCount>4</threadCount>
</configuration>
```

**Caution**: Only parallelize if tests are truly independent!

### Skip Tests for Fast Builds
```bash
# During development, when you just want to build the JAR
mvn clean package -DskipTests

# Or skip compilation AND execution
mvn clean package -Dmaven.test.skip=true
```

**Warning**: Never skip tests before committing or deploying!

### Rerun Failed Tests Only
```bash
# After a failed run
mvn test -Dsurefire.rerunFailingTestsCount=2
```

---

## Interpreting Coverage Numbers

### What's Good Coverage?

**Our Targets:**
- Domain layer: **90-100%** (pure logic, easy to test)
- Application layer: **80-90%** (orchestration)
- Infrastructure layer: **70-80%** (harder to test, more setup)
- Overall: **80%+**

### What to Focus On

**High Priority (Must Cover):**
- Business logic calculations (Bill.calculateDiscount)
- Validation rules (Customer creation)
- Error handling paths (customer not found)
- Conditional branches (if/else, switch)

**Lower Priority:**
- Simple getters/setters
- toString() methods
- Equals/hashCode (if auto-generated)
- Configuration classes

### Coverage is NOT Everything

**100% coverage doesn't mean bug-free code!**

You can have:
- ✅ 100% coverage
- ❌ Tests that don't actually verify behavior
- ❌ Tests that mock everything
- ❌ Tests that catch exceptions but don't check the message

**Good tests verify BEHAVIOR, not just coverage.**

---

## Next Steps After Running Tests

### 1. Review Coverage Report
- Open `target/site/jacoco/index.html`
- Look for red/yellow areas
- Focus on business logic classes

### 2. Add Missing Tests
- Identify uncovered code paths
- Write tests for those paths
- Re-run coverage report

### 3. Refactor Based on Testability
If something is hard to test, it might be poorly designed:
- Too many dependencies?
- Too much logic in one method?
- Mixing concerns?

### 4. Document Test Strategy
Update README.md with:
- How to run tests
- Current coverage percentages
- What's tested and why

---

## Interview Demonstration

### Show Coverage Report
```bash
# Generate fresh report
mvn clean test jacoco:report

# Open in browser
open target/site/jacoco/index.html

# Point out:
- Overall coverage >80%
- Domain layer near 100%
- Good branch coverage
- Excluded config classes
```

### Run Specific Test Category
```bash
# Show pure domain tests (no mocking)
mvn test -Dtest=BillTest

# Show Mockito usage
mvn test -Dtest=BillServiceTest

# Show integration tests
mvn test -Dtest=CustomerRepositoryIntegrationTest
```

### Explain Test Pyramid
"I have many fast unit tests at the base (domain layer), fewer integration tests in the middle (repositories), and comprehensive API tests at the top. This follows the testing pyramid principle - lots of fast tests, fewer slow tests."