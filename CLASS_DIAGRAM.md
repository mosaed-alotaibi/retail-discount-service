# UML Class Diagram - Retail Discount Service

## High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         INFRASTRUCTURE LAYER                             │
│  ┌────────────────┐      ┌─────────────────────────────────────┐       │
│  │ BillController │      │  MongoBillRepository                │       │
│  │  (REST API)    │──────│  (MongoDB Adapter)                  │       │
│  └────────────────┘      └─────────────────────────────────────┘       │
└────────────┬────────────────────────────────────────┬───────────────────┘
             │ uses                                    │ implements
             ▼                                         ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         APPLICATION LAYER                                │
│  ┌──────────────────────┐    ┌────────────────────────────────┐        │
│  │ CalculateBillUseCase │◄───│ BillService                    │        │
│  │ GetBillUseCase       │    │  (Orchestrates use cases)      │        │
│  │ ListBillsUseCase     │    └────────────────────────────────┘        │
│  │  (Port Interfaces)   │                  │                            │
│  └──────────────────────┘                  │ uses                       │
│                                             ▼                            │
│  ┌──────────────────────┐    ┌────────────────────────────────┐        │
│  │ BillRepository       │    │ CustomerRepository             │        │
│  │  (Port Interface)    │    │  (Port Interface)              │        │
│  └──────────────────────┘    └────────────────────────────────┘        │
└────────────────────────────────────────────┬───────────────────────────┘
                                              │ depends on
                                              ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                          DOMAIN LAYER                                    │
│  ┌─────────────┐     ┌──────────────┐     ┌─────────────┐             │
│  │    Bill     │────►│   Customer   │     │  BillItem   │             │
│  │ (Aggregate) │     │   (Entity)   │     │  (Value Obj)│             │
│  └─────────────┘     └──────────────┘     └─────────────┘             │
│         │                    │                     │                     │
│         │                    │                     │                     │
│         ▼                    ▼                     ▼                     │
│  ┌─────────────┐     ┌──────────────┐     ┌─────────────┐             │
│  │   Money     │     │ CustomerType │     │ItemCategory │             │
│  │ (Value Obj) │     │    (Enum)    │     │   (Enum)    │             │
│  └─────────────┘     └──────────────┘     └─────────────┘             │
│                                                                           │
│  ┌────────────────────────────────────────────────────────┐            │
│  │                  Domain Events                          │            │
│  │  - BillCreated                                          │            │
│  │  - BillCalculated                                       │            │
│  └────────────────────────────────────────────────────────┘            │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Detailed Class Diagrams

### Domain Layer

#### Bill (Aggregate Root)

```
┌──────────────────────────────────────────────────────┐
│                      «aggregate»                      │
│                         Bill                          │
├──────────────────────────────────────────────────────┤
│ - billId: String                                      │
│ - customer: Customer                                  │
│ - items: List<BillItem>                              │
│ - createdAt: LocalDateTime                           │
│ - cachedDiscount: DiscountBreakdown                  │
│ - domainEvents: List<DomainEvent>                    │
├──────────────────────────────────────────────────────┤
│ + create(Customer, List<BillItem>): Bill             │
│ + reconstitute(...): Bill                            │
│ + calculateDiscount(): DiscountBreakdown             │
│ + getTotalAmount(): Money                            │
│ + getPercentageDiscountEligibleAmount(): Money       │
│ + pullDomainEvents(): List<DomainEvent>              │
│ + getBillId(): String                                │
│ + getCustomer(): Customer                            │
│ + getItems(): List<BillItem>                         │
│ + getCreatedAt(): LocalDateTime                      │
└──────────────────────────────────────────────────────┘
          │
          │ contains
          ▼
┌──────────────────────────────────────────────────────┐
│                «inner class»                          │
│                 DiscountBreakdown                     │
├──────────────────────────────────────────────────────┤
│ - totalAmount: Money                                  │
│ - percentageDiscount: Money                           │
│ - billBasedDiscount: Money                           │
│ - totalDiscount: Money                               │
│ - netPayable: Money                                  │
│ - percentageDiscountRate: int                        │
├──────────────────────────────────────────────────────┤
│ + getTotalAmount(): Money                            │
│ + getPercentageDiscount(): Money                     │
│ + getBillBasedDiscount(): Money                      │
│ + getTotalDiscount(): Money                          │
│ + getNetPayable(): Money                             │
│ + getPercentageDiscountRate(): int                   │
└──────────────────────────────────────────────────────┘
```

#### Customer (Entity)

```
┌──────────────────────────────────────────────────────┐
│                      «entity»                         │
│                      Customer                         │
├──────────────────────────────────────────────────────┤
│ - customerId: String                                  │
│ - explicitType: CustomerType                          │
│ - registrationDate: LocalDate                         │
├──────────────────────────────────────────────────────┤
│ + of(String, CustomerType, LocalDate): Customer       │
│ + getEffectiveType(): CustomerType                    │
│ + getDiscountPercentage(): int                        │
│ + getYearsAsCustomer(): int                           │
│ + getCustomerId(): String                             │
│ + getExplicitType(): CustomerType                     │
│ + getRegistrationDate(): LocalDate                    │
└──────────────────────────────────────────────────────┘
```

#### Money (Value Object)

```
┌──────────────────────────────────────────────────────┐
│                  «value object»                       │
│                       Money                           │
├──────────────────────────────────────────────────────┤
│ - amount: BigDecimal                                  │
│ - SCALE: int = 2                                      │
│ - ROUNDING_MODE: RoundingMode = HALF_UP               │
├──────────────────────────────────────────────────────┤
│ + of(BigDecimal): Money                               │
│ + of(double): Money                                   │
│ + zero(): Money                                       │
│ + add(Money): Money                                   │
│ + subtract(Money): Money                              │
│ + multiply(BigDecimal): Money                         │
│ + applyPercentageDiscount(int): Money                 │
│ + divideAndFloor(int): int                            │
│ + isGreaterThanOrEqual(Money): boolean                │
│ + isZero(): boolean                                   │
│ + getAmount(): BigDecimal                             │
└──────────────────────────────────────────────────────┘
```

#### BillItem (Value Object)

```
┌──────────────────────────────────────────────────────┐
│                  «value object»                       │
│                      BillItem                         │
├──────────────────────────────────────────────────────┤
│ - name: String                                        │
│ - category: ItemCategory                              │
│ - unitPrice: Money                                    │
│ - quantity: int                                       │
├──────────────────────────────────────────────────────┤
│ + of(String, ItemCategory, Money, int): BillItem     │
│ + getTotalPrice(): Money                              │
│ + isEligibleForPercentageDiscount(): boolean          │
│ + getAmountEligibleForPercentageDiscount(): Money     │
│ + getName(): String                                   │
│ + getCategory(): ItemCategory                         │
│ + getUnitPrice(): Money                               │
│ + getQuantity(): int                                  │
└──────────────────────────────────────────────────────┘
```

#### Enumerations

```
┌──────────────────────────────────────────────────────┐
│                     «enumeration»                     │
│                    CustomerType                       │
├──────────────────────────────────────────────────────┤
│ EMPLOYEE(30)                                          │
│ AFFILIATE(10)                                         │
│ LONG_TERM_CUSTOMER(5)                                 │
│ REGULAR(0)                                            │
├──────────────────────────────────────────────────────┤
│ - discountPercentage: int                             │
├──────────────────────────────────────────────────────┤
│ + getDiscountPercentage(): int                        │
└──────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────┐
│                     «enumeration»                     │
│                    ItemCategory                       │
├──────────────────────────────────────────────────────┤
│ GROCERY(false)                                        │
│ ELECTRONICS(true)                                     │
│ CLOTHING(true)                                        │
│ FURNITURE(true)                                       │
│ OTHER(true)                                           │
├──────────────────────────────────────────────────────┤
│ - eligibleForPercentageDiscount: boolean              │
├──────────────────────────────────────────────────────┤
│ + isEligibleForPercentageDiscount(): boolean          │
└──────────────────────────────────────────────────────┘
```

---

### Application Layer

#### Use Case Ports (Interfaces)

```
┌──────────────────────────────────────────────────────┐
│                     «interface»                       │
│                CalculateBillUseCase                   │
├──────────────────────────────────────────────────────┤
│ + execute(CalculateBillCommand): BillCalculationResponse │
└──────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────┐
│                     «interface»                       │
│                   GetBillUseCase                      │
├──────────────────────────────────────────────────────┤
│ + execute(String billId): Optional<BillCalculationResponse> │
└──────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────┐
│                     «interface»                       │
│                  ListBillsUseCase                     │
├──────────────────────────────────────────────────────┤
│ + findByCustomerId(String): List<BillCalculationResponse> │
│ + findByCustomerIdAndDateRange(...): List<BillCalculationResponse> │
│ + findRecentBills(int): List<BillCalculationResponse> │
└──────────────────────────────────────────────────────┘
```

#### BillService

```
┌──────────────────────────────────────────────────────┐
│                     «service»                         │
│                     BillService                       │
│                                                        │
│          implements CalculateBillUseCase              │
│          implements GetBillUseCase                    │
│          implements ListBillsUseCase                  │
├──────────────────────────────────────────────────────┤
│ - customerRepository: CustomerRepository              │
│ - billRepository: BillRepository                      │
├──────────────────────────────────────────────────────┤
│ + execute(CalculateBillCommand): BillCalculationResponse │
│ + execute(String): Optional<BillCalculationResponse>  │
│ + findByCustomerId(String): List<...>                │
│ + findByCustomerIdAndDateRange(...): List<...>       │
│ + findRecentBills(int): List<...>                    │
└──────────────────────────────────────────────────────┘
```

#### Repository Ports (Interfaces)

```
┌──────────────────────────────────────────────────────┐
│                     «interface»                       │
│                   BillRepository                      │
├──────────────────────────────────────────────────────┤
│ + save(Bill): Bill                                    │
│ + findById(String): Optional<Bill>                    │
│ + findByCustomerId(String): List<Bill>               │
│ + findByCustomerIdAndDateRange(...): List<Bill>      │
│ + findRecentBills(int): List<Bill>                   │
│ + deleteById(String): boolean                         │
│ + existsById(String): boolean                         │
└──────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────┐
│                     «interface»                       │
│                 CustomerRepository                    │
├──────────────────────────────────────────────────────┤
│ + findById(String): Optional<Customer>                │
│ + save(Customer): Customer                            │
│ + existsById(String): boolean                         │
└──────────────────────────────────────────────────────┘
```

---

### Infrastructure Layer

#### BillController (REST API)

```
┌──────────────────────────────────────────────────────┐
│                   «@RestController»                   │
│                    BillController                     │
├──────────────────────────────────────────────────────┤
│ - calculateBillUseCase: CalculateBillUseCase          │
│ - getBillUseCase: GetBillUseCase                      │
│ - listBillsUseCase: ListBillsUseCase                  │
├──────────────────────────────────────────────────────┤
│ + createBill(CreateBillRequest): ResponseEntity<...>  │
│   @PostMapping("/bills")                              │
│   @ResponseStatus(201 CREATED)                        │
│                                                        │
│ + getBill(String id): ResponseEntity<...>             │
│   @GetMapping("/bills/{id}")                          │
│                                                        │
│ + listBills(...): ResponseEntity<List<...>>           │
│   @GetMapping("/bills")                               │
└──────────────────────────────────────────────────────┘
```

#### MongoBillRepository (MongoDB Adapter)

```
┌──────────────────────────────────────────────────────┐
│                  «@Repository»                        │
│                 MongoBillRepository                   │
│                                                        │
│              implements BillRepository                │
├──────────────────────────────────────────────────────┤
│ - springRepository: SpringBillRepository              │
│ - customerRepository: CustomerRepository              │
├──────────────────────────────────────────────────────┤
│ + save(Bill): Bill                                    │
│ + findById(String): Optional<Bill>                    │
│ + findByCustomerId(String): List<Bill>               │
│ + findByCustomerIdAndDateRange(...): List<Bill>      │
│ + findRecentBills(int): List<Bill>                   │
│ + deleteById(String): boolean                         │
│ + existsById(String): boolean                         │
└──────────────────────────────────────────────────────┘
```

---

## Class Relationships

### Aggregation and Composition

```
Bill ◆──────────► Customer     (composition - Bill owns Customer reference)
Bill ◆──────────► BillItem*    (composition - Bill owns BillItems)
BillItem ◆──────► Money        (composition - BillItem owns Money)
BillItem ────────► ItemCategory (association - references enum)
Customer ────────► CustomerType (association - references enum)
```

### Dependencies

```
BillController ────────► CalculateBillUseCase
BillController ────────► GetBillUseCase
BillController ────────► ListBillsUseCase

BillService ───────────► BillRepository
BillService ───────────► CustomerRepository

MongoBillRepository ───► BillRepository (implements)
MongoCustomerRepository ► CustomerRepository (implements)
```

### Inheritance

```
BillService ───|▷ CalculateBillUseCase
BillService ───|▷ GetBillUseCase
BillService ───|▷ ListBillsUseCase
```

---

## Key Design Patterns

1. **Aggregate Pattern**: Bill is the aggregate root that ensures consistency
2. **Value Object Pattern**: Money, BillItem are immutable value objects
3. **Repository Pattern**: BillRepository, CustomerRepository abstract persistence
4. **Factory Method**: Bill.create() vs Bill.reconstitute()
5. **Strategy Pattern**: CustomerType determines discount calculation
6. **Adapter Pattern**: MongoBillRepository adapts domain to MongoDB
7. **Dependency Inversion**: Controllers depend on use case interfaces, not implementations

---

## Diagram Legend

```
┌────────┐
│ Class  │  Regular class
└────────┘

«interface»   Interface
«aggregate»   DDD Aggregate
«entity»      DDD Entity
«value object» DDD Value Object
«enumeration» Enum
«@Controller» Spring annotation

◆────► Composition (strong ownership)
────►  Association
───|▷  Implementation/Inheritance
- - -> Dependency
```

---

## Notes

1. **Domain Layer** has ZERO dependencies on frameworks (Spring, MongoDB, etc.)
2. **Dependency flow**: Infrastructure → Application → Domain (never reverse)
3. **Port/Adapter**: Ports are interfaces in application layer, adapters are implementations in infrastructure
4. **Value Objects** are immutable and have value-based equality
5. **Aggregates** enforce business invariants and consistency boundaries

---

**This diagram shows the core domain model and its relationships following DDD and Clean Architecture principles.**
