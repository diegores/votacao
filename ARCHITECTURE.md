# Architecture Documentation

## Hexagonal Architecture Implementation

This project implements **Hexagonal Architecture** (also known as **Ports and Adapters**) to achieve clean separation of concerns and maintainable code structure.

```
┌─────────────────────────────────────────────────────────────┐
│                    INFRASTRUCTURE LAYER                     │
├─────────────────────────────────────────────────────────────┤
│  Web Controllers  │  JPA Repositories  │  Configuration    │
│  - AgendaController│  - AgendaRepositoryImpl│  - SchedulingConfig│
│  - VotingController│  - MemberRepositoryImpl│  - DataSourceConfig│
│  - MemberController│  - VoteRepositoryImpl  │                 │
│  - ExceptionHandler│                        │                 │
└─────────────────────────────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                    APPLICATION LAYER                        │
├─────────────────────────────────────────────────────────────┤
│  Use Cases / Services      │  DTOs                          │
│  - AgendaService          │  - CreateAgendaRequest          │
│  - VotingService          │  - OpenVotingSessionRequest     │
│  - MemberService          │  - CreateVoteRequest            │
│                           │  - AgendaResponse               │
│                           │  - VotingResultResponse         │
└─────────────────────────────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                      DOMAIN LAYER                          │
├─────────────────────────────────────────────────────────────┤
│  Entities                  │  Repository Interfaces         │
│  - Agenda                  │  - AgendaRepository            │
│  - Vote                    │  - MemberRepository            │
│  - Member                  │  - VoteRepository              │
│  - VotingResult           │                                │
│                           │                                │
│  Value Objects            │  Domain Services               │
│  - VoteType               │  - Business Logic in Entities  │
│  - VotingSessionStatus    │                                │
└─────────────────────────────────────────────────────────────┘
```

## Layer Responsibilities

### 🎯 Domain Layer (Core Business Logic)
**Location**: `src/main/java/com/example/votacao/domain/`

- **Entities**: Core business objects with behavior
  - `Agenda`: Manages voting sessions and vote collection
  - `Vote`: Represents individual member votes
  - `Member`: Cooperative member information
  - `VotingResult`: Calculated voting outcomes

- **Value Objects**: Immutable objects representing concepts
  - `VoteType`: YES/NO enumeration
  - `VotingSessionStatus`: OPEN/CLOSED enumeration

- **Repository Interfaces**: Define data access contracts
  - Pure interfaces with no implementation details
  - Define business-focused query methods

**Key Principles**:
- ✅ No dependencies on outer layers
- ✅ Rich domain model with business logic
- ✅ Entities encapsulate business rules
- ✅ Value objects ensure type safety

### 🔧 Application Layer (Use Cases)
**Location**: `src/main/java/com/example/votacao/application/`

- **Services**: Orchestrate domain objects and use cases
  - `AgendaService`: Agenda lifecycle management
  - `VotingService`: Vote processing logic
  - `MemberService`: Member management

- **DTOs**: Data transfer between layers
  - Request DTOs: Input validation and mapping
  - Response DTOs: Output formatting

**Key Principles**:
- ✅ Coordinates domain objects
- ✅ Implements use cases
- ✅ Handles transactions
- ✅ Maps between DTOs and domain objects

### 🏗️ Infrastructure Layer (External Concerns)
**Location**: `src/main/java/com/example/votacao/infrastructure/`

- **Web Controllers**: REST API endpoints
  - Handle HTTP requests/responses
  - Input validation and error handling
  - OpenAPI documentation

- **Persistence Adapters**: Database implementation
  - JPA repository implementations
  - Database entity mapping
  - Query implementations

- **Configuration**: Framework setup
  - Spring Boot configuration
  - Scheduling configuration
  - Database configuration

**Key Principles**:
- ✅ Implements infrastructure concerns
- ✅ Adapts external frameworks to domain needs
- ✅ Handles technical cross-cutting concerns

## SOLID Principles Implementation

### Single Responsibility Principle (SRP)
- Each class has one reason to change
- **Example**: `AgendaService` only handles agenda use cases
- **Example**: `VotingService` only handles voting logic

### Open/Closed Principle (OCP)
- Open for extension, closed for modification
- **Example**: Repository interfaces allow different implementations
- **Example**: New voting types can be added without changing existing code

### Liskov Substitution Principle (LSP)
- Subtypes must be substitutable for base types
- **Example**: Any `AgendaRepository` implementation works with `AgendaService`

### Interface Segregation Principle (ISP)
- Clients depend only on interfaces they use
- **Example**: Separate repository interfaces for each aggregate
- **Example**: Focused DTOs for specific operations

### Dependency Inversion Principle (DIP)
- Depend on abstractions, not concretions
- **Example**: Services depend on repository interfaces, not implementations
- **Example**: Domain layer has no dependencies on infrastructure

## Design Patterns

### Repository Pattern
```java
// Domain interface (port)
public interface AgendaRepository {
    Agenda save(Agenda agenda);
    Optional<Agenda> findById(UUID id);
}

// Infrastructure implementation (adapter)
@Component
public class AgendaRepositoryImpl implements AgendaRepository {
    // JPA implementation
}
```

### Adapter Pattern
```java
// Domain repository interface
AgendaRepository → AgendaRepositoryImpl → JpaAgendaRepository
```

### Service Layer Pattern
```java
@Service
@Transactional
public class AgendaService {
    // Orchestrates domain objects
    // Implements use cases
}
```

## Data Flow

### 1. Create Agenda Flow
```
HTTP Request → AgendaController → AgendaService → Agenda (Domain) → AgendaRepository → Database
```

### 2. Vote Submission Flow
```
HTTP Request → VotingController → VotingService → Agenda.addVote() → VoteRepository → Database
```

### 3. Get Results Flow
```
HTTP Request → AgendaController → AgendaService → Agenda.getVotingResult() → VotingResult
```

## Benefits of This Architecture

### ✅ Testability
- Easy to unit test domain logic
- Mock repository interfaces for testing
- Clear separation enables focused tests

### ✅ Maintainability
- Changes to infrastructure don't affect business logic
- Clear boundaries between concerns
- Easy to understand and modify

### ✅ Flexibility
- Can swap implementations (e.g., MySQL instead of H2)
- Can add new interfaces (e.g., GraphQL alongside REST)
- Business logic is framework-independent

### ✅ Scalability
- Clear separation allows independent scaling
- Can extract services to microservices later
- Database and API can be optimized separately

## Technology Mapping

### Domain Layer
- Pure Java POJOs
- JPA annotations for persistence mapping
- Lombok for boilerplate reduction

### Application Layer
- Spring `@Service` annotations
- Spring `@Transactional` for transaction management
- Jakarta Validation for input validation

### Infrastructure Layer
- Spring Web for REST controllers
- Spring Data JPA for repositories
- H2 Database for persistence
- SpringDoc for API documentation

## Future Considerations

### Microservices Evolution
This architecture supports future microservices extraction:
- Each bounded context can become a service
- Domain interfaces become service contracts
- Repository implementations become HTTP clients

### Event-Driven Architecture
Can be enhanced with domain events:
- `VoteSubmitted` events
- `VotingSessionClosed` events
- Event sourcing for audit trails

### CQRS (Command Query Responsibility Segregation)
Can be extended with read/write separation:
- Command handlers for state changes
- Query handlers for data retrieval
- Separate read models for optimization