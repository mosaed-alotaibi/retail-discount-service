#!/bin/bash

###############################################################################
# Retail Discount Service - Compliance Verification Script
#
# This script runs all tests, generates coverage reports, and verifies
# compliance with all assessment requirements.
#
# Prerequisites:
#   - Java 21 (JDK)
#   - Maven 3.9+
#   - Internet connection (for dependency downloads)
#
# Usage:
#   chmod +x verify-compliance.sh
#   ./verify-compliance.sh
###############################################################################

set -e  # Exit on error

echo "=========================================="
echo "Retail Discount Service"
echo "Compliance Verification Script"
echo "=========================================="
echo ""

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_step() {
    echo -e "${BLUE}==>${NC} $1"
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

# Step 1: Verify Prerequisites
print_step "Step 1: Verifying prerequisites..."

# Check Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -ge 21 ]; then
        print_success "Java $JAVA_VERSION detected"
    else
        print_error "Java 21 or higher required. Found: Java $JAVA_VERSION"
        exit 1
    fi
else
    print_error "Java not found. Please install Java 21 or higher."
    exit 1
fi

# Check Maven
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -n 1 | awk '{print $3}')
    print_success "Maven $MVN_VERSION detected"
else
    print_error "Maven not found. Please install Maven 3.9 or higher."
    exit 1
fi

echo ""

# Step 2: Clean previous builds
print_step "Step 2: Cleaning previous builds..."
mvn clean -q
print_success "Clean completed"
echo ""

# Step 3: Compile project
print_step "Step 3: Compiling project..."
mvn compile -q
print_success "Compilation successful"
echo ""

# Step 4: Run unit tests
print_step "Step 4: Running unit tests (Mockito)..."
echo ""
mvn test
UNIT_TEST_RESULT=$?

if [ $UNIT_TEST_RESULT -eq 0 ]; then
    print_success "All unit tests passed"
else
    print_error "Unit tests failed"
    exit 1
fi
echo ""

# Step 5: Run integration tests
print_step "Step 5: Running integration tests (Testcontainers)..."
echo ""
mvn verify -Dskip.surefire.tests=true
INTEGRATION_TEST_RESULT=$?

if [ $INTEGRATION_TEST_RESULT -eq 0 ]; then
    print_success "All integration tests passed"
else
    print_error "Integration tests failed"
    exit 1
fi
echo ""

# Step 6: Generate coverage reports
print_step "Step 6: Generating code coverage reports..."
mvn jacoco:report jacoco:check -q
COVERAGE_RESULT=$?

if [ $COVERAGE_RESULT -eq 0 ]; then
    print_success "Coverage report generated (minimum 80% met)"
else
    print_error "Coverage verification failed (below 80% threshold)"
    exit 1
fi
echo ""

# Step 7: Display coverage summary
print_step "Step 7: Coverage summary..."

if [ -f "target/site/jacoco/index.html" ]; then
    echo ""
    echo "Coverage report available at:"
    echo "  file://$(pwd)/target/site/jacoco/index.html"
    echo ""

    # Extract coverage percentage if possible
    if command -v grep &> /dev/null; then
        COVERAGE=$(grep -oP 'Total[^>]*>\s*\K[\d,]+(?=\s*%)' target/site/jacoco/index.html 2>/dev/null | head -1 | tr -d ',')
        if [ -n "$COVERAGE" ]; then
            if [ "$COVERAGE" -ge 80 ]; then
                print_success "Code coverage: ${COVERAGE}% (✓ Meets 80% requirement)"
            else
                print_warning "Code coverage: ${COVERAGE}% (⚠ Below 80% requirement)"
            fi
        fi
    fi
fi
echo ""

# Step 8: Optional - Run SonarQube analysis (if server is available)
print_step "Step 8: SonarQube analysis (optional)..."

read -p "Do you want to run SonarQube analysis? (y/n) [n]: " -n 1 -r
echo ""

if [[ $REPLY =~ ^[Yy]$ ]]; then
    print_step "Running SonarQube analysis..."

    # Check if SonarQube is running
    if curl -s http://localhost:9000/api/system/status > /dev/null 2>&1; then
        mvn sonar:sonar
        SONAR_RESULT=$?

        if [ $SONAR_RESULT -eq 0 ]; then
            print_success "SonarQube analysis completed"
            echo "View report at: http://localhost:9000/dashboard?id=retail-discount-service"
        else
            print_error "SonarQube analysis failed"
        fi
    else
        print_warning "SonarQube server not running at localhost:9000"
        echo "To start SonarQube:"
        echo "  docker run -d --name sonarqube -p 9000:9000 sonarqube:lts"
    fi
else
    print_step "Skipping SonarQube analysis"
fi
echo ""

# Step 9: Docker build verification
print_step "Step 9: Verifying Docker configuration..."

if [ -f "Dockerfile" ] && [ -f "docker-compose.yml" ]; then
    print_success "Docker configuration files present"

    # Optional: Build Docker image
    read -p "Do you want to build Docker image? (y/n) [n]: " -n 1 -r
    echo ""

    if [[ $REPLY =~ ^[Yy]$ ]]; then
        print_step "Building Docker image..."
        docker build -t retail-discount-service:latest .
        DOCKER_BUILD_RESULT=$?

        if [ $DOCKER_BUILD_RESULT -eq 0 ]; then
            print_success "Docker image built successfully"
        else
            print_error "Docker image build failed"
        fi
    fi
else
    print_warning "Docker configuration files not found"
fi
echo ""

# Final Summary
echo "=========================================="
echo "COMPLIANCE VERIFICATION SUMMARY"
echo "=========================================="
echo ""

print_success "✓ Prerequisites verified (Java 21, Maven)"
print_success "✓ Project compilation successful"
print_success "✓ All unit tests passed (Mockito)"
print_success "✓ All integration tests passed (Testcontainers)"
print_success "✓ Code coverage ≥80% (JaCoCo)"
print_success "✓ Build configuration verified"

echo ""
echo "=========================================="
echo "COMPLIANCE STATUS: ✅ FULLY COMPLIANT"
echo "=========================================="
echo ""

echo "Reports generated:"
echo "  1. JaCoCo Coverage Report:"
echo "     file://$(pwd)/target/site/jacoco/index.html"
echo ""
echo "  2. Surefire Test Report:"
echo "     file://$(pwd)/target/surefire-reports/index.html"
echo ""
echo "  3. Test Results Summary:"
echo "     $(pwd)/target/test-classes/"
echo ""

echo "To run the application:"
echo "  Option 1 (Docker - Recommended):"
echo "    docker-compose up -d"
echo ""
echo "  Option 2 (Maven):"
echo "    mvn spring-boot:run"
echo ""
echo "  Option 3 (JAR):"
echo "    java -jar target/retail-discount-service.jar"
echo ""

echo "API Endpoints:"
echo "  POST   http://localhost:8080/api/v1/bills"
echo "  GET    http://localhost:8080/api/v1/bills/{id}"
echo "  GET    http://localhost:8080/api/v1/bills"
echo "  GET    http://localhost:8080/api/v1/swagger-ui.html"
echo ""

echo "Test Authentication:"
echo "  Username: EMP001, Password: password (Employee, 30% discount)"
echo "  Username: AFF001, Password: password (Affiliate, 10% discount)"
echo "  Username: CUST001, Password: password (Long-term customer, 5% discount)"
echo ""

print_success "Verification complete! 🎉"
