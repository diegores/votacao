# Performance Testing Guide

## Overview

This document provides comprehensive guidance for performance testing the cooperative voting system, designed to handle hundreds of thousands of votes efficiently.

## Performance Optimizations Implemented

### 1. Database Optimizations

#### Indexing Strategy
- **Agenda indexes**: status, session times, creation date
- **Vote indexes**: agenda_id, member_id, vote_type, voted_at, composite agenda_member
- **Member indexes**: CPF (unique), name

#### Connection Pooling (HikariCP)
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

#### Hibernate Optimizations
```yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 100
        order_inserts: true
        order_updates: true
        batch_versioned_data: true
```

### 2. Caching Layer

- **Spring Cache** with concurrent map cache manager
- Cached operations:
  - Agenda lookups (`@Cacheable`)
  - Voting results (`@Cacheable`)
  - Cache eviction on updates (`@CacheEvict`)

### 3. Batch Operations

#### Batch Voting API
- Endpoint: `POST /api/batch-voting/votes`
- Maximum batch size: 10,000 votes
- Chunked processing: 1,000 votes per chunk
- Optimized duplicate detection

#### Performance Features
- Bulk vote validation
- Batch database saves
- Optimized duplicate checking
- Parallel processing capabilities

### 4. Monitoring & Metrics

#### Spring Boot Actuator Endpoints
- `/actuator/health` - Application health
- `/actuator/metrics` - Performance metrics
- `/actuator/prometheus` - Prometheus metrics
- `/actuator/httptrace` - HTTP request tracing

#### Custom Metrics
- Vote submission timers
- Batch processing timers
- Vote count counters
- Session management counters

## Performance Test Scenarios

### 1. JMeter Test Plan

Location: `performance-tests/jmeter/voting-performance-test.jmx`

#### Test Scenarios
1. **Setup**: Create agenda and open voting session
2. **High Volume Individual Votes**: 100,000 individual votes (1,000 threads × 10 loops)
3. **Batch Voting**: 100 concurrent batch submissions (1,000 votes each)

#### Key Metrics Measured
- Response times (50th, 90th, 95th, 99th percentiles)
- Throughput (requests/second)
- Error rates
- Resource utilization

### 2. Python Performance Test Script

Location: `performance-tests/scripts/generate-test-data.py`

#### Features
- Automated test data generation
- Individual and batch voting tests
- Concurrent execution with ThreadPoolExecutor
- Performance metrics collection
- Results export to JSON

#### Usage Examples

```bash
# Run both individual and batch tests with 50K votes
python3 performance-tests/scripts/generate-test-data.py \
  --test-type both \
  --member-count 50000 \
  --batch-size 1000

# Run only batch tests with custom batch size
python3 performance-tests/scripts/generate-test-data.py \
  --test-type batch \
  --member-count 100000 \
  --batch-size 5000
```

## Performance Benchmarks

### Expected Performance Targets

#### Individual Vote Submission
- **Target**: 1,000+ votes/second
- **Response Time**: < 50ms (95th percentile)
- **Concurrent Users**: 1,000+

#### Batch Vote Processing
- **Target**: 10,000+ votes/second
- **Batch Size**: 1,000-10,000 votes
- **Processing Time**: < 2 seconds per 10K batch

#### Database Performance
- **Connection Pool**: 50 connections
- **Query Response**: < 10ms for indexed queries
- **Batch Inserts**: 100 records per batch

### System Resource Requirements

#### Memory
- **Minimum**: 2GB RAM
- **Recommended**: 4GB+ RAM for high loads
- **JVM Heap**: -Xmx2g -Xms1g

#### CPU
- **Minimum**: 2 cores
- **Recommended**: 4+ cores for concurrent processing

#### Database
- **File-based H2**: Suitable for testing up to 1M records
- **Production**: Consider PostgreSQL/MySQL for larger datasets

## Running Performance Tests

### Prerequisites

1. **Java 17+** installed
2. **Python 3.8+** for test scripts
3. **Apache JMeter** for GUI-based testing
4. **Application running** on localhost:8080

### Step 1: Start the Application

```bash
mvn spring-boot:run
```

### Step 2: Verify Application Health

```bash
curl http://localhost:8080/actuator/health
```

### Step 3: Run Performance Tests

#### Option A: Python Script (Recommended)
```bash
cd performance-tests/scripts
python3 generate-test-data.py --test-type both --member-count 10000
```

#### Option B: JMeter GUI
```bash
jmeter -t performance-tests/jmeter/voting-performance-test.jmx
```

#### Option C: JMeter Command Line
```bash
jmeter -n -t performance-tests/jmeter/voting-performance-test.jmx \
       -l performance-tests/results/test-results.jtl \
       -e -o performance-tests/results/html-report
```

### Step 4: Monitor Performance

#### Real-time Metrics
```bash
# Application metrics
curl http://localhost:8080/actuator/metrics

# Specific vote metrics
curl http://localhost:8080/actuator/metrics/vote.submission
curl http://localhost:8080/actuator/metrics/vote.batch.processing
```

#### System Monitoring
```bash
# Memory usage
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Thread usage
curl http://localhost:8080/actuator/metrics/jvm.threads.live

# Database connections
curl http://localhost:8080/actuator/metrics/hikaricp.connections.active
```

## Performance Tuning Tips

### 1. Database Tuning

```yaml
# Increase batch size for better throughput
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 200  # Increase from 100
```

### 2. Connection Pool Tuning

```yaml
# Adjust based on load
spring:
  datasource:
    hikari:
      maximum-pool-size: 100  # Increase for high concurrency
      minimum-idle: 20
```

### 3. Cache Configuration

```java
// Consider Redis for distributed caching
@Configuration
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        RedisCacheManager.Builder builder = RedisCacheManager
            .RedisCacheManagerBuilder
            .fromConnectionFactory(redisConnectionFactory());
        return builder.build();
    }
}
```

### 4. JVM Tuning

```bash
# Production JVM settings
java -Xmx4g -Xms2g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:+UnlockExperimentalVMOptions \
     -XX:+UseZGC \
     -jar votacao.jar
```

## Troubleshooting Performance Issues

### Common Issues and Solutions

#### 1. High Response Times
- **Symptom**: Response times > 500ms
- **Solutions**:
  - Check database indexes
  - Increase connection pool size
  - Enable query caching
  - Review N+1 query problems

#### 2. Memory Issues
- **Symptom**: OutOfMemoryError
- **Solutions**:
  - Increase heap size
  - Check for memory leaks
  - Reduce batch sizes
  - Optimize entity fetching strategies

#### 3. Database Connection Issues
- **Symptom**: Connection timeout errors
- **Solutions**:
  - Increase connection pool size
  - Adjust connection timeout settings
  - Monitor connection usage
  - Check for connection leaks

#### 4. Low Throughput
- **Symptom**: < 100 votes/second
- **Solutions**:
  - Use batch operations
  - Increase thread pool sizes
  - Optimize database queries
  - Enable parallel processing

### Performance Monitoring Dashboard

#### Key Metrics to Monitor

1. **Request Rate**: requests/second
2. **Response Time**: 95th percentile latency
3. **Error Rate**: % of failed requests
4. **Database Performance**: query execution time
5. **Memory Usage**: heap utilization
6. **CPU Usage**: system load
7. **Database Connections**: active/idle connections

#### Alerting Thresholds

- Response time > 1 second (95th percentile)
- Error rate > 1%
- Memory usage > 80%
- CPU usage > 80%
- Database connections > 80% of pool

## Load Testing Strategy

### Gradual Load Increase

1. **Baseline**: 10 concurrent users
2. **Ramp-up**: Increase by 50 users every 30 seconds
3. **Peak Load**: 1,000 concurrent users
4. **Sustained Load**: Maintain peak for 10 minutes
5. **Ramp-down**: Gradually decrease load

### Test Data Management

- **Members**: Pre-create 100,000+ test members
- **Agendas**: Create multiple agendas for parallel testing
- **Vote Distribution**: 50/50 YES/NO votes for realistic scenarios
- **Cleanup**: Automated test data cleanup after tests

## Results Analysis

### Performance Report Template

```json
{
  "test_configuration": {
    "test_type": "batch",
    "member_count": 100000,
    "batch_size": 1000,
    "concurrent_batches": 100
  },
  "results": {
    "total_time": 120.5,
    "successful_votes": 99850,
    "failed_votes": 150,
    "votes_per_second": 828.5,
    "average_response_time": 1.2,
    "95th_percentile_response_time": 2.1
  },
  "resource_usage": {
    "max_memory_mb": 1024,
    "max_cpu_percent": 75,
    "active_db_connections": 45
  }
}
```

### Success Criteria

- ✅ **Throughput**: > 1,000 votes/second
- ✅ **Response Time**: < 100ms (95th percentile)
- ✅ **Success Rate**: > 99%
- ✅ **Resource Usage**: < 80% of available resources
- ✅ **Scalability**: Linear performance improvement with resources

## Conclusion

The voting system has been optimized for high-performance scenarios with comprehensive testing capabilities. The combination of database optimizations, caching, batch operations, and monitoring provides a robust foundation for handling hundreds of thousands of votes efficiently.

For production deployments, consider:
- PostgreSQL or MySQL instead of H2
- Redis for distributed caching
- Load balancing across multiple instances
- Container orchestration (Kubernetes)
- Advanced monitoring (Prometheus + Grafana)