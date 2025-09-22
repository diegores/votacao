# Cooperative Voting System - Backend API

A comprehensive REST API for cooperative voting assemblies built with Java 17 and Spring Boot, following hexagonal architecture principles and clean code practices.

## 🏗️ Architecture

This project implements **Hexagonal Architecture (Ports and Adapters)** with clear separation of concerns:

```
src/main/java/com/example/votacao/
├── domain/                 # Business Domain (Core)
│   ├── model/             # Domain Entities & Value Objects
│   ├── repository/        # Repository Interfaces (Ports)
│   └── service/          # Domain Services
├── application/           # Application Layer
│   ├── usecase/          # Use Cases (Application Services)
│   └── dto/              # Data Transfer Objects
└── infrastructure/       # Infrastructure Layer
    ├── persistence/      # Database Adapters
    ├── web/             # REST Controllers
    └── config/          # Configuration
```

## 🎯 Features

### Core Functionality
- ✅ **Agenda Management**: Create and manage voting agendas
- ✅ **Voting Sessions**: Open time-limited voting sessions (default: 1 minute)
- ✅ **Member Voting**: Members vote Yes/No with unique ID validation
- ✅ **Vote Counting**: Real-time vote tallying and results
- ✅ **Persistence**: H2 database with JPA for data persistence
- ✅ **CPF Validation**: External service integration for Brazilian CPF validation and voting eligibility

### Technical Features
- ✅ **Java 17**: Modern Java features and performance improvements
- ✅ **Spring Boot 3.2.6**: Modern Spring framework
- ✅ **Hexagonal Architecture**: Clean separation of concerns
- ✅ **SOLID Principles**: Maintainable and extensible code
- ✅ **Comprehensive REST API**: Complete OpenAPI/Swagger documentation
- ✅ **Automatic Session Closure**: Scheduled task to close expired sessions
- ✅ **Comprehensive Testing**: Unit and integration tests
- ✅ **Data Validation**: Input validation and error handling
- ✅ **Global Exception Handling**: Centralized error management
- ✅ **Structured Logging**: Production-ready logging configuration
- ✅ **Performance Monitoring**: Spring Boot Actuator integration
- ✅ **Batch Processing**: High-performance operations for large-scale voting

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+

### Setup

1. **Clone and navigate to project**:
```bash
git clone <repository-url>
cd votacao
```

2. **Build and run the application**:
```bash
mvn clean install
mvn spring-boot:run
```

The API will start on `http://localhost:8080`

### Access Points
- **API Documentation (Swagger UI)**: http://localhost:8080/swagger-ui.html
- **OpenAPI Specification**: http://localhost:8080/v3/api-docs
- **H2 Database Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:votacao;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`
  - Username: `sa`
  - Password: (empty)
- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics

## 📚 API Documentation

### Agendas
- `POST /api/agendas` - Create new agenda
- `GET /api/agendas` - List all agendas
- `GET /api/agendas/{id}` - Get specific agenda
- `POST /api/agendas/{id}/voting-session` - Open voting session
- `GET /api/agendas/{id}/result` - Get voting results
- `GET /api/agendas/voting-sessions/open` - List open voting sessions

### Members
- `POST /api/members` - Register new member
- `GET /api/members` - List all members
- `GET /api/members/{id}` - Get member by ID
- `GET /api/members/cpf/{cpf}` - Get member by CPF

### Voting
- `POST /api/agendas/{id}/votes` - Submit individual vote

### Batch Voting (Performance Feature)
- `POST /api/batch-voting/votes` - Submit multiple votes in batch (up to 10,000)

### CPF Validation (Bonus Feature)
- `GET /api/cpf/validate/{cpf}` - Validate CPF and check voting eligibility
- `GET /api/cpf-generator/generate` - Generate valid CPF for testing
- `GET /api/cpf-generator/samples` - Get sample valid CPFs

## 🧪 Testing

### Run Tests
```bash
mvn test
```

### Test Coverage
- Domain entities and business logic
- Application services and use cases
- REST API controllers
- Repository implementations
- Integration tests for complete workflows

## 🗄️ Database Schema

### Tables
- **agendas**: Voting agenda information
- **members**: Cooperative member details
- **votes**: Individual vote records

### Key Relationships
- Agenda (1) → (N) Votes
- Each vote links to a Member via UUID
- Unique constraint: one vote per member per agenda

## 🔧 Configuration

### Application Properties
Located in `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:votacao;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  h2:
    console:
      enabled: true

server:
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

## 🏛️ Design Patterns & Principles

### SOLID Principles
- **S**ingle Responsibility: Each class has one reason to change
- **O**pen/Closed: Open for extension, closed for modification
- **L**iskov Substitution: Subtypes must be substitutable for base types
- **I**nterface Segregation: Clients depend only on interfaces they use
- **D**ependency Inversion: Depend on abstractions, not concretions

### Design Patterns Used
- **Repository Pattern**: Data access abstraction
- **Adapter Pattern**: Infrastructure adapters for external concerns
- **Service Layer Pattern**: Application service coordination
- **DTO Pattern**: Data transfer between layers
- **Builder Pattern**: Entity construction (via Lombok)

## 🚀 Performance Features (Bonus Task 2)

### High-Volume Optimization
The system is optimized to handle **hundreds of thousands of votes** with the following performance enhancements:

#### Database Optimizations
- **Strategic Indexing**: Optimized indexes on frequently queried columns
- **HikariCP Connection Pooling**: 50 connections with optimized settings
- **Hibernate Batch Processing**: 100 records per batch with optimized inserts

#### Batch Operations
- **Batch Voting API**: Process up to 10,000 votes in a single request
- **Chunked Processing**: 1,000 votes per chunk for optimal memory usage
- **Parallel Processing**: Concurrent vote validation and saving

#### Caching Layer
- **Spring Cache**: Cached agendas, voting results, and member data
- **Automatic Cache Eviction**: Intelligent cache invalidation on updates
- **Performance Boost**: Significantly reduced database queries

#### Monitoring & Metrics
- **Spring Boot Actuator**: Health checks, metrics, and performance monitoring
- **Custom Metrics**: Vote submission timers, batch processing counters
- **Real-time Monitoring**: Prometheus-compatible metrics endpoint

### Performance Test Suite
- **JMeter Test Plans**: Comprehensive load testing scenarios
- **Python Performance Scripts**: Automated high-volume testing
- **Benchmarking Tools**: Performance measurement and reporting

### Performance Targets
- **Individual Votes**: 1,000+ votes/second
- **Batch Processing**: 10,000+ votes/second
- **Response Time**: < 100ms (95th percentile)
- **Concurrent Users**: 1,000+ simultaneous voters

📊 **See [PERFORMANCE.md](PERFORMANCE.md) for detailed performance testing guide**

## 🔄 Workflow Example

1. **Register Members**: Add cooperative members to the system
2. **Create Agenda**: Define voting topic and description
3. **Open Voting Session**: Start timed voting (1+ minutes)
4. **Members Vote**: Each member casts one Yes/No vote (individual or batch)
5. **Automatic Closure**: Session closes when time expires
6. **View Results**: See vote counts and final outcome

## 🔍 CPF Validation (Bonus Feature)

The system includes a comprehensive CPF validation service that simulates integration with an external validation system:

### Features
- **Real CPF Validation**: Validates CPF format and checksum using Brazilian standards
- **Random Eligibility Check**: Simulates external service that randomly returns ABLE_TO_VOTE or UNABLE_TO_VOTE
- **Proper Error Handling**: Returns HTTP 404 for invalid CPFs or ineligible voters
- **Integration**: Automatically validates CPFs when creating new members

### API Endpoints
```bash
# Validate a CPF
GET /api/cpf/validate/11144477735
# Response examples:
{"status": "ABLE_TO_VOTE"}          # HTTP 200
{"status": "UNABLE_TO_VOTE"}        # HTTP 404

# Generate valid CPF for testing
GET /api/cpf-generator/generate
# Response: {"cpf": "11144477735", "formatted": "111.444.777-35"}

# Get sample valid CPFs
GET /api/cpf-generator/samples
# Response: {"cpfs": ["11144477735", "22255588820", ...]}
```

### How It Works
1. **Format Validation**: Checks if CPF has 11 digits and valid format
2. **Checksum Validation**: Verifies CPF checksum using Brazilian algorithm
3. **Random Eligibility**: Simulates external API with 50% success rate
4. **Error Responses**: Returns appropriate HTTP status codes
5. **Logging**: Includes privacy-conscious logging with masked CPFs

### Example Usage
```bash
# Test with a valid CPF
curl "http://localhost:8080/api/cpf/validate/11144477735"

# Test with invalid CPF (will always return 404)
curl "http://localhost:8080/api/cpf/validate/12345"

# Generate a new valid CPF for testing
curl "http://localhost:8080/api/cpf-generator/generate"
```

### Integration Points
- **Member Registration**: CPF is validated before allowing member creation
- **Privacy**: CPF numbers are masked in logs for privacy protection
- **Error Handling**: Proper exception handling with meaningful error messages

## 🛡️ Security Considerations

> **Note**: As specified in requirements, security is abstracted for exercise purposes. In production, implement:
> - Authentication/Authorization
> - Rate limiting
> - Input sanitization
> - CSRF protection
> - HTTPS enforcement

## 🚧 Future Enhancements

- Member authentication system
- Vote delegation capabilities
- Multiple voting types (ranked choice, etc.)
- Audit trail and vote history
- Email notifications for voting sessions
- GraphQL API support
- Event-driven architecture with message queues
- Database migration to PostgreSQL for production

## 📦 Dependencies

- **Spring Boot 3.2.6**: Core framework
- **Spring Data JPA**: Database abstraction
- **H2 Database**: In-memory database for development
- **Lombok**: Boilerplate code reduction
- **SpringDoc OpenAPI**: API documentation generation
- **Spring Boot Actuator**: Health checks and monitoring
- **Hibernate Validator**: Request validation
- **HikariCP**: Connection pooling
- **JUnit 5 & Mockito**: Testing framework

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## 📄 License

This project is for educational/evaluation purposes as part of a technical challenge.

---

Built with ❤️ using Java 17 and Spring Boot