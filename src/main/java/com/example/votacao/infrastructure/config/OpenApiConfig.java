package com.example.votacao.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for the Cooperative Voting System API.
 * 
 * This configuration provides comprehensive API documentation with:
 * - Detailed API information and contact details
 * - Server configuration for different environments
 * - Organized API tags for better navigation
 * - Complete endpoint documentation
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cooperative Voting System API")
                        .version("1.0.0")
                        .description("""
                                # Cooperative Voting System API
                                
                                A comprehensive REST API for managing cooperative voting sessions with the following features:
                                
                                ## 🏗️ Architecture
                                - **Hexagonal Architecture** with clean separation of concerns
                                - **SOLID Principles** implementation
                                - **Domain-Driven Design** with rich domain entities
                                - **Spring Boot 3.2.6** with Java 17
                                
                                ## 🎯 Core Features
                                - **Agenda Management**: Create and manage voting topics
                                - **Voting Sessions**: Time-limited voting with automatic closure
                                - **Member Management**: Cooperative member registration with CPF validation
                                - **Vote Processing**: Individual and batch vote submission
                                - **Real-time Results**: Live vote counting and result calculation
                                
                                ## 🚀 Performance Features
                                - **Batch Operations**: Process up to 10,000 votes in a single request
                                - **Database Optimization**: Strategic indexing and connection pooling
                                - **Caching**: Spring Cache for improved performance
                                - **Monitoring**: Comprehensive metrics and health checks
                                
                                ## 🔍 Bonus Features
                                - **CPF Validation**: Brazilian CPF validation with eligibility checking
                                - **Error Handling**: Comprehensive error responses with proper HTTP status codes
                                - **Security**: Input validation and business rule enforcement
                                
                                ## 📊 Monitoring
                                - Health checks available at `/actuator/health`
                                - Metrics available at `/actuator/metrics`
                                - Performance monitoring with Spring Boot Actuator
                                
                                ## 🗄️ Database
                                - H2 in-memory database for easy setup and testing
                                - JPA/Hibernate with optimized queries
                                - Database console available at `/h2-console`
                                """)
                        .contact(new Contact()
                                .name("Cooperative Voting System")
                                .email("support@votacao.example.com")
                                .url("https://github.com/example/votacao"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development server"),
                        new Server()
                                .url("https://api.votacao.example.com")
                                .description("Production server")))
                .tags(List.of(
                        new Tag().name("Agendas").description("Voting agenda management operations"),
                        new Tag().name("Members").description("Cooperative member management operations"),
                        new Tag().name("Voting").description("Individual vote submission operations"),
                        new Tag().name("Batch Voting").description("High-performance batch voting operations"),
                        new Tag().name("CPF Validation").description("Brazilian CPF validation and eligibility checking"),
                        new Tag().name("CPF Generator").description("Utility APIs for generating valid CPFs for testing"),
                        new Tag().name("Monitoring").description("Health checks and system monitoring")));
    }
}