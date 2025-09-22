# Backend API Design Documentation

## Overview

This document explains the architectural decisions, design patterns, and implementation choices made for the Cooperative Voting System **Backend API**. The solution prioritizes **simplicity**, **maintainability**, and **adherence to established software engineering principles** for a robust REST API service.

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Design Principles](#design-principles)
- [Implementation Decisions](#implementation-decisions)
- [Error Handling Strategy](#error-handling-strategy)
- [Testing Approach](#testing-approach)
- [Performance Considerations](#performance-considerations)
- [Code Quality Measures](#code-quality-measures)

## Architecture Overview

### Hexagonal Architecture (Ports and Adapters)

**Decision**: Adopted hexagonal architecture for clear separation of concerns and testability.

**Reasoning**:
- **Domain-Centric**: Business logic is isolated in the domain layer, making it technology-agnostic
- **Testability**: Easy to unit test business logic without external dependencies
- **Flexibility**: Infrastructure components can be easily replaced without affecting business logic
- **Maintainability**: Clear boundaries between layers reduce coupling

```
├── domain/                 # Core business logic (no external dependencies)
│   ├── model/             # Rich domain entities with business rules
│   ├── repository/        # Repository interfaces (ports)
│   ├── service/          # Domain services
│   └── exception/        # Domain-specific exceptions
├── application/           # Application services and use cases
│   ├── usecase/          # Orchestrate domain operations
│   └── dto/              # Data transfer objects
└── infrastructure/       # External concerns (adapters)
    ├── persistence/      # Database adapters
    ├── web/             # REST controllers
    ├── external/        # External service integrations
    └── config/          # Configuration classes
```

**Benefits Realized**:
- Business logic is completely testable in isolation
- Database technology can be changed without affecting domain logic
- External services (CPF validation) are easily mockable
- Clear dependency direction (inward toward domain)

## Design Principles

### SOLID Principles Implementation

#### Single Responsibility Principle (SRP)
- **AgendaService**: Only handles agenda-related operations
- **VotingService**: Focuses solely on individual vote operations
- **BatchVotingService**: Dedicated to batch vote processing
- **GlobalExceptionHandler**: Centralized error handling

#### Open/Closed Principle (OCP)
- Repository interfaces allow for different implementations
- Strategy pattern could be easily added for different voting types
- Exception hierarchy allows adding new exception types without modifying handlers

#### Liskov Substitution Principle (LSP)
- Repository implementations are fully substitutable
- Domain entities maintain behavioral contracts

#### Interface Segregation Principle (ISP)
- Repository interfaces are focused and specific
- No "fat" interfaces that force unnecessary dependencies

#### Dependency Inversion Principle (DIP)
- Application services depend on repository abstractions, not implementations
- Domain layer has no dependencies on infrastructure
- Dependency injection through Spring Framework

### Domain-Driven Design (DDD)

**Rich Domain Model**: Entities contain business logic rather than being anemic data structures.

```java
// Example: Agenda entity encapsulates voting rules
public void openVotingSession(int durationInMinutes) {
    if (this.status == VotingSessionStatus.OPEN) {
        throw new IllegalStateException("Voting session is already open");
    }
    // Business logic here
}
```

**Ubiquitous Language**: Terms like "Agenda", "Vote", "Member", "Session" reflect the cooperative domain.

## Implementation Decisions

### 1. Simplicity Over Complexity

**Decision**: Chose simple, straightforward implementations over complex patterns.

**Examples**:
- **BatchVotingService**: Initially had complex chunking and parallel processing, simplified to single-transaction processing
- **Exception Handling**: Simple hierarchy with clear mappings to HTTP status codes
- **Caching**: Basic Spring Cache instead of complex distributed caching

**Reasoning**: Premature optimization leads to maintenance overhead. Simple solutions are easier to understand, debug, and modify.

### 2. Framework and Technology Choices

#### Spring Boot 3.2.6
**Why**: 
- Industry standard with excellent documentation
- Comprehensive auto-configuration reduces boilerplate
- Strong ecosystem and community support
- Built-in testing support

#### H2 Database
**Why**:
- Zero configuration for development and testing
- Easy to set up and run anywhere
- Sufficient for the problem scope
- Can be easily replaced with production database

#### Lombok
**Why**:
- Reduces boilerplate code significantly
- Improves readability
- Standard in Spring Boot applications
- Easy to remove if needed

### 3. Data Modeling Decisions

#### UUID for Primary Keys
**Why**:
- Avoids database dependency for ID generation
- Better for distributed systems
- No sequential patterns that could leak business information

#### Optimistic Design for Performance
**Indexes**: Strategic indexing on frequently queried columns
```java
@Table(name = "votes", indexes = {
    @Index(name = "idx_vote_agenda_id", columnList = "agenda_id"),
    @Index(name = "idx_vote_member_id", columnList = "member_id"),
    @Index(name = "idx_vote_agenda_member", columnList = "agenda_id, member_id")
})
```

#### Batch Operations
**Why**: Included batch voting for performance scenarios while keeping the implementation simple.

## Error Handling Strategy

### Custom Exception Hierarchy

**Decision**: Created domain-specific exceptions with global handling.

```java
BusinessException (base)
├── VotingException (voting-specific errors)
└── EntityNotFoundException (not found errors)
```

**Benefits**:
- Clear error semantics
- Proper HTTP status code mapping
- Consistent error response format
- Easy to extend for new error types

### Global Exception Handler

**Decision**: Centralized exception handling with @RestControllerAdvice.

**Benefits**:
- Consistent error responses across all endpoints
- Reduced code duplication
- Proper logging of errors
- Single place to modify error handling behavior

### Error Response Format

```json
{
  "code": "VOTING_ERROR",
  "message": "Member has already voted on this agenda",
  "timestamp": "2024-01-15T10:30:00",
  "details": {}
}
```

**Reasoning**: Structured format that's easy for clients to parse and handle programmatically.

## Testing Approach

### Test Strategy

**Unit Tests**: Focus on business logic and individual components
- Domain entities (business rule validation)
- Application services (orchestration logic)
- Exception handlers (error scenarios)

**Integration Tests**: Test component interactions
- Repository implementations
- REST controllers with mock services

**Test Organization**:
```
src/test/java/
├── domain/model/          # Domain entity tests
├── application/usecase/   # Service layer tests
└── infrastructure/web/    # Controller and exception handler tests
```

### Testing Principles

1. **AAA Pattern**: Arrange, Act, Assert for clear test structure
2. **Descriptive Names**: `shouldThrowExceptionWhenVotingSessionClosed()`
3. **Single Responsibility**: One assertion per test concept
4. **Mock External Dependencies**: Use Mockito for repository mocking

### Quality Measures

**Test Coverage**: Comprehensive coverage of business logic and error paths
**Readable Tests**: Clear test names and well-structured test code
**Fast Execution**: Unit tests run quickly without external dependencies

## Performance Considerations

### Database Optimization

**Connection Pooling**: HikariCP with optimized settings
```yaml
hikari:
  maximum-pool-size: 50
  minimum-idle: 10
  connection-timeout: 30000
```

**Batch Processing**: Hibernate batch inserts for bulk operations
```yaml
hibernate:
  jdbc:
    batch_size: 100
  order_inserts: true
```

**Strategic Indexing**: Indexes on frequently queried columns

### Caching Strategy

**Decision**: Simple Spring Cache for read-heavy operations

**What's Cached**:
- Agenda lookups
- Voting results
- Member information

**Cache Eviction**: Automatic eviction when data changes using `@CacheEvict`

### Batch Operations

**Decision**: Provided batch voting API for high-volume scenarios

**Implementation**: Simple, single-transaction approach that prioritizes data consistency over maximum throughput

**Reasoning**: Complexity of chunked/parallel processing doesn't justify the maintenance cost for the expected usage patterns

## Code Quality Measures

### Code Organization

**Package Structure**: Clear separation by architectural layer
**Naming Conventions**: Descriptive, intention-revealing names
**Single Responsibility**: Each class has a focused purpose

### Documentation

**JavaDoc**: Comprehensive documentation for public APIs
**README**: Clear setup and usage instructions
**Design Documentation**: This document explaining architectural decisions

### Logging Strategy

**Structured Logging**: Consistent format across the application
**Appropriate Levels**: 
- INFO for business events
- WARN for recoverable errors  
- ERROR for system errors
- DEBUG for detailed troubleshooting

**Log Management**: File rotation and retention policies configured

### Code Standards

**Consistent Style**: Lombok reduces boilerplate
**Validation**: Bean validation for input data
**Error Handling**: Comprehensive exception handling
**Testing**: High test coverage with meaningful tests

## Security Considerations

### Current State
**Note**: Security is intentionally simplified per requirements, but production considerations include:

### Production Security Recommendations
- **Authentication**: JWT or OAuth2 for API access
- **Authorization**: Role-based access control
- **Input Validation**: Comprehensive validation and sanitization
- **Rate Limiting**: Prevent abuse of voting endpoints
- **HTTPS**: All communications over encrypted channels
- **Audit Logging**: Track all voting activities

### Data Privacy
- **CPF Masking**: Personal identifiers are masked in logs
- **Minimal Data**: Only collect necessary information
- **Retention Policies**: Clear data lifecycle management

## Monitoring and Observability

### Application Metrics
**Spring Boot Actuator**: Health checks, metrics, and monitoring endpoints
**Custom Metrics**: Voting-specific metrics (if needed)

### Logging
**Structured Logs**: JSON format for log aggregation
**Correlation IDs**: Request tracing across services
**Error Tracking**: Comprehensive error logging with context

### Health Checks
**Database Connectivity**: Verify database health
**External Services**: Monitor CPF validation service
**Application Status**: Overall application health

## Future Considerations

### Scalability
- **Database**: Migration path to PostgreSQL/MySQL
- **Caching**: Redis for distributed caching
- **Load Balancing**: Horizontal scaling with multiple instances

### Features
- **Real-time Updates**: WebSocket for live voting results
- **Audit Trail**: Complete voting history and audit logs
- **Advanced Voting**: Ranked choice, weighted voting
- **Notifications**: Email/SMS notifications for voting sessions

### DevOps
- **Containerization**: Docker for consistent deployments
- **CI/CD**: Automated testing and deployment pipelines
- **Infrastructure as Code**: Terraform or similar tools

## Conclusion

This solution prioritizes **simplicity**, **maintainability**, and **solid engineering principles** over premature optimization. The architecture supports the core requirements while remaining flexible for future enhancements.

Key strengths:
- ✅ Clear separation of concerns
- ✅ Comprehensive error handling
- ✅ High test coverage
- ✅ Excellent documentation
- ✅ Production-ready logging and monitoring
- ✅ Simple, understandable codebase
- ✅ Follows established patterns and principles

The design balances current needs with future extensibility, ensuring the codebase remains maintainable as requirements evolve.