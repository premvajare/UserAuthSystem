# Authsystem - Enterprise JWT Authentication & Rate Limiting API

A production-ready Spring Boot application demonstrating enterprise-grade authentication, JWT-based authorization, distributed rate limiting, and comprehensive API analytics.

## 🎯 Features

- ✅ **User Authentication**: Secure registration and login with BCrypt password hashing
- ✅ **JWT Authorization**: Token-based access control with 24-hour expiration
- ✅ **Rate Limiting**: Per-IP request throttling (50 requests/minute) using Bucket4j
- ✅ **API Analytics**: Real-time usage tracking, endpoint statistics, and top requester identification
- ✅ **Input Validation**: Comprehensive DTO validation with detailed error messages
- ✅ **Global Exception Handling**: Unified error response format with proper HTTP status codes
- ✅ **Security**: Spring Security, CSRF disabled (stateless API), BCrypt password encoding
- ✅ **Swagger/OpenAPI**: Interactive API documentation and testing interface
- ✅ **Logging**: Request/response logging with status codes for debugging

---

## 📁 Project Structure

```
src/main/java/com/app/authsystem/
├── AuthsystemApplication.java          # Spring Boot entry point
├── config/                             # Configuration classes
│   ├── JwtUtil.java                    # JWT token generation & validation
│   ├── RateLimitConfig.java            # Bucket4j rate limiting configuration
│   ├── RateLimitFilterConfig.java      # Filter registration & execution order
│   └── SecurityConfig.java             # Spring Security & HTTP authorization
├── controller/                         # REST API endpoints
│   ├── AuthController.java             # /auth/* - Registration, login, token
│   ├── AnalyticsController.java        # /analytics/* - Usage statistics
│   └── UserController.java             # /users/* - User retrieval (JWT required)
├── dto/                                # Data Transfer Objects
│   ├── LoginRequest.java               # Login payload with validation
│   ├── RegisterRequest.java            # Registration payload with validation
│   └── ApiUsageView.java               # Analytics response projection
├── entity/                             # JPA entities
│   ├── User.java                       # User table mapping
│   └── ApiUsage.java                   # API usage tracking table
├── exception/                          # Exception handling
│   ├── GlobalExceptionHandler.java     # @ControllerAdvice for all exceptions
│   ├── ApiErrorResponse.java           # Standardized error response DTO
│   ├── ResourceNotFoundException.java  # 404 Not Found
│   ├── DuplicateResourceException.java # 409 Conflict
│   └── UnauthorizedException.java      # 401 Unauthorized
├── filter/                             # Servlet filters
│   ├── JwtAuthenticationFilter.java    # JWT validation & Spring Security context
│   ├── RateLimitFilter.java            # Per-IP rate limiting enforcement
│   └── UsageTrackingFilter.java        # API request logging to database
├── repository/                         # Data access layer
│   ├── UserRepository.java             # User CRUD & custom queries
│   └── ApiUsageRepository.java         # Usage statistics queries
└── service/                            # Business logic layer
    ├── AuthService.java                # Registration & login logic
    ├── UserService.java                # User retrieval logic
    └── AnalyticsService.java           # Usage statistics aggregation
```

---

## 📋 File-by-File Explanation

### Why Each File Exists & What It Does

#### **Configuration Layer (config/)**

- **SecurityConfig.java**: 
  - Centralizes Spring Security configuration for all HTTP endpoints
  - Permits `/auth/**` for public registration/login
  - Requires JWT for `/users/**` and `/analytics/**`
  - Permits Swagger UI endpoints for API documentation
  - Disables CSRF (not needed for stateless APIs)
  - Uses BCrypt for password encoding

- **JwtUtil.java**: 
  - Encapsulates all JWT operations (generation, validation, claim extraction)
  - Uses HMAC-SHA256 algorithm for token signing
  - 24-hour token expiration TTL
  - Keeps controllers clean by handling token complexity

- **RateLimitConfig.java**: 
  - Uses Bucket4j token bucket algorithm
  - Per-IP rate limiting (50 tokens/minute)
  - Lazy bucket creation for new IPs
  - Stateless design suitable for distributed systems

- **RateLimitFilterConfig.java**: 
  - Registers three filters in correct execution order:
    1. JwtAuthenticationFilter (order=0) - Extract & validate JWT
    2. RateLimitFilter (order=1) - Enforce rate limits
    3. UsageTrackingFilter (order=2) - Log usage
  - Ensures security & rate limiting work together

#### **Filter Layer (filter/)**

- **JwtAuthenticationFilter.java**: 
  - Extracts JWT from `Authorization: Bearer <token>` header
  - Validates token signature and expiration
  - Sets Spring SecurityContext for downstream authorization
  - Logs request details: method, URI, IP, response status code
  - Skips validation for Swagger endpoints (public access)

- **RateLimitFilter.java**: 
  - Checks if request IP has available tokens in bucket
  - Extracts real client IP from `X-Forwarded-For` header (proxy support)
  - Fallback to `RemoteAddr` if behind NAT
  - Returns HTTP 429 when limit exceeded
  - Runs AFTER JWT validation to avoid wasting tokens on auth failures

- **UsageTrackingFilter.java**: 
  - Records every API request to database for analytics
  - Stores: client IP, endpoint path, timestamp
  - Runs last in filter chain so only valid requests are tracked

#### **Controller Layer (controller/)**

- **AuthController.java**: 
  - POST `/auth/register` - New user registration with validation
  - POST `/auth/login` - Authentication returning JWT + user info
  - POST `/auth/token` - Get JWT token only
  - @Valid annotations trigger input validation
  - Returns 409 if email already registered
  - Returns 401 if credentials invalid

- **UserController.java**: 
  - GET `/users` - List all users (JWT required)
  - GET `/users/{id}` - Get user by ID (JWT required, path param validation)
  - @Validated enables path parameter constraints
  - Returns 404 if user not found

- **AnalyticsController.java**: 
  - GET `/analytics/summary` - Total calls, top endpoints, top IPs (JWT required)
  - GET `/analytics/recent-usage` - Last 100 requests with timestamps (JWT required)
  - Provides insights into system usage patterns

#### **Service Layer (service/)**

- **AuthService.java**: 
  - Business logic for user registration with duplicate checking
  - Login validation with BCrypt password matching
  - Throws custom exceptions for proper HTTP response codes
  - Password never returned in responses

- **UserService.java**: 
  - User data retrieval by ID
  - Throws ResourceNotFoundException if not found (returns 404)

- **AnalyticsService.java**: 
  - Aggregates API usage statistics
  - Queries top endpoints and top requesters
  - Converts JPA entities to view DTOs

#### **Data Layer (repository/ & entity/)**

- **User.java**: 
  - JPA entity mapped to `users` table
  - Fields: id (PK), name, email (unique), password (BCrypt hash)
  - Email used as login identifier

- **ApiUsage.java**: 
  - JPA entity mapped to `api_usage` table
  - Fields: id (PK), identifier (IP), endpoint, timestamp
  - Used for analytics and audit trails

- **UserRepository.java**: 
  - Custom query: `findByEmail()` for login lookup
  - Leverages Spring Data JPA auto-implementation

- **ApiUsageRepository.java**: 
  - `findTop100ByOrderByTimestampDesc()` - Recent usage
  - `countByEndpoint()` - Top endpoints
  - `countByIdentifier()` - Top requesters

#### **DTO Layer (dto/)**

- **RegisterRequest.java**: 
  - Input validation: name (required), email (required, valid), password (required, min 6 chars)
  - Constraints prevent invalid data from reaching service layer

- **LoginRequest.java**: 
  - Input validation: email (required, valid), password (required)
  - Ensures only well-formed credentials are processed

- **ApiUsageView.java**: 
  - Read-only projection for analytics responses
  - Maps JPA entity to clean API response

#### **Exception Handling (exception/)**

- **GlobalExceptionHandler.java**: 
  - Central @ControllerAdvice for all exceptions
  - Maps different exception types to proper HTTP status codes
  - Returns unified error response format
  - Includes validation error details for 400 responses

- **ApiErrorResponse.java**: 
  - Standard error response with: timestamp, status, error name, message, path
  - Optional: validationErrors map for field-level details
  - Ensures predictable error handling for clients

- **Custom Exceptions**: 
  - ResourceNotFoundException → 404
  - DuplicateResourceException → 409 (Conflict)
  - UnauthorizedException → 401

---

## 🚀 Getting Started

### Prerequisites
```
✓ Java 17 or higher
✓ Maven 3.6+
✓ PostgreSQL 12+ (or H2 for development)
```

### Clone & Build
```bash
git clone <repo>
cd Authsystem
mvn clean install
```

### Run Application
```bash
mvn spring-boot:run
```
Application starts on `http://localhost:8080`

### Access Swagger UI
```
http://localhost:8080/swagger-ui/index.html
```

---

## 📚 API Endpoints

### Authentication (Public - No JWT Required)
```
POST /auth/register
  Body: { "name": "John Doe", "email": "john@example.com", "password": "securePass123" }
  Response: 200 OK - "User registered successfully!"

POST /auth/login
  Body: { "email": "john@example.com", "password": "securePass123" }
  Response: 200 OK - { "name": "John Doe", "email": "john@example.com", "token": "eyJ..." }

POST /auth/token
  Body: { "email": "john@example.com", "password": "securePass123" }
  Response: 200 OK - "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Users (Protected - JWT Required)
```
GET /users
  Header: Authorization: Bearer <token>
  Response: 200 OK - [{ "id": 1, "name": "John Doe", "email": "john@example.com" }, ...]

GET /users/{id}
  Header: Authorization: Bearer <token>
  Response: 200 OK - { "id": 1, "name": "John Doe", "email": "john@example.com" }
  Response: 404 Not Found - if user doesn't exist
```

### Analytics (Protected - JWT Required)
```
GET /analytics/summary
  Header: Authorization: Bearer <token>
  Response: 200 OK
  {
    "total_calls": 150,
    "top_endpoints": [
      { "endpoint": "/users", "count": 45 },
      { "endpoint": "/analytics/summary", "count": 30 }
    ],
    "top_identifiers": [
      { "identifier": "192.168.1.1", "count": 100 },
      { "identifier": "192.168.1.2", "count": 50 }
    ]
  }

GET /analytics/recent-usage
  Header: Authorization: Bearer <token>
  Response: 200 OK
  [
    { "identifier": "192.168.1.1", "endpoint": "/users", "timestamp": "2026-04-01T12:34:56" },
    ...
  ]
```

---

## 🔒 Authentication Flow Example

### Step 1: Register
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "SecurePassword123"
  }'
```
Response:
```
User registered successfully!
```

### Step 2: Login
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePassword123"
  }'
```
Response:
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIiwiaWF0IjoxNzExOTc3NjAwLCJleHAiOjE3MTE5NzcyMDB9.xxx"
}
```

### Step 3: Use JWT Token
```bash
curl -X GET http://localhost:8080/users/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...."
```
Response:
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com"
}
```

---

## ⚡ Rate Limiting Examples

### Limit: 50 requests/minute per IP

```bash
# Requests 1-50: HTTP 200 OK
for i in {1..50}; do
  curl -X POST http://localhost:8080/auth/register \
    -H "Content-Type: application/json" \
    -d '{"name":"Test","email":"test'$i'@example.com","password":"password123"}'
done

# Request 51: HTTP 429 Too Many Requests
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","email":"test51@example.com","password":"password123"}'
```
Response (429):
```
Too Many Requests - Rate limit exceeded
```

---

## ❌ Error Response Examples

### Validation Error (400)
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"invalid","password":"short"}'
```
Response:
```json
{
  "timestamp": "2026-04-01T12:34:56.789123",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/auth/register",
  "validationErrors": {
    "name": "name is required",
    "email": "email must be valid",
    "password": "password must be at least 6 characters"
  }
}
```

### Duplicate Email (409)
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Jane","email":"john@example.com","password":"password123"}'
```
Response:
```json
{
  "timestamp": "2026-04-01T12:34:56.789123",
  "status": 409,
  "error": "Conflict",
  "message": "User already exists with email: john@example.com",
  "path": "/auth/register"
}
```

### Missing JWT Token (401)
```bash
curl -X GET http://localhost:8080/users
```
Response:
```json
{
  "timestamp": "2026-04-01T12:34:56.789123",
  "status": 401,
  "error": "Unauthorized",
  "message": "Missing JWT token",
  "path": "/users"
}
```

### User Not Found (404)
```bash
curl -X GET http://localhost:8080/users/999 \
  -H "Authorization: Bearer <token>"
```
Response:
```json
{
  "timestamp": "2026-04-01T12:34:56.789123",
  "status": 404,
  "error": "Not Found",
  "message": "User not found with id: 999",
  "path": "/users/999"
}
```

---

## 🛡️ Security Features

| Feature | Implementation |
|---------|-----------------|
| Password Hashing | BCrypt with automatic salt |
| JWT Signing | HMAC-SHA256 algorithm |
| Token Expiration | 24 hours |
| Session Management | Stateless (no server-side sessions) |
| CSRF Protection | Disabled (stateless API) |
| SQL Injection | Prevented (JPA parameterized queries) |
| Rate Limiting | Token bucket algorithm per IP |
| Input Validation | Jakarta Bean Validation |
| Error Handling | Global with no stack traces exposed |

---

## 🔧 Configuration

### JWT Settings (JwtUtil.java)
```java
private static final String SECRET_KEY = "your-secret-key-here-min-256-bits";
private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000; // 24 hours
private static final String ALGORITHM = "HS256";
```

### Rate Limit Settings (RateLimitConfig.java)
```java
Bandwidth limit = Bandwidth.classic(50, Refill.greedy(50, Duration.ofMinutes(1)));
// 50 requests per minute per IP
```

### Database (application.properties)
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/authsystem
spring.datasource.username=postgres
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update
```

---

## 📊 Database Schema

### Users Table
```sql
CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL
);
```

### API Usage Table
```sql
CREATE TABLE api_usage (
  id BIGSERIAL PRIMARY KEY,
  identifier VARCHAR(255),         -- IP address or user ID
  endpoint VARCHAR(255),
  timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Recommended indexes for performance
CREATE INDEX idx_api_usage_timestamp ON api_usage(timestamp DESC);
CREATE INDEX idx_users_email ON users(email);
```

---

## 🧪 Testing the System

### Test Script (Bash)
```bash
#!/bin/bash

# Register
echo "1. Registering user..."
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@example.com","password":"password123"}'

# Login
echo -e "\n2. Logging in..."
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}' | jq -r '.token')

echo "JWT Token: $TOKEN"

# Get all users
echo -e "\n3. Getting all users..."
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/users

# Get analytics
echo -e "\n4. Getting analytics summary..."
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/analytics/summary

# Test rate limiting
echo -e "\n5. Testing rate limiting (50 requests)..."
for i in {1..51}; do
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/auth/register \
    -H "Content-Type: application/json" \
    -d '{"name":"Test","email":"test'$i'@example.com","password":"password123"}')
  if [ $STATUS -eq 429 ]; then
    echo "Request $i: Rate limited (429)"
    break
  else
    echo "Request $i: OK ($STATUS)"
  fi
done
```

---

## 🚀 Production Readiness Checklist

- ✅ JWT authentication with proper expiration
- ✅ Rate limiting with token bucket algorithm
- ✅ Password hashing with BCrypt
- ✅ Global exception handling with proper status codes
- ✅ Input validation on all DTOs
- ✅ Logging of all requests with status codes
- ✅ Swagger documentation
- ✅ CORS configuration (if needed)
- ✅ SQL injection prevention (JPA)
- ✅ Session management (stateless)
- ⚠️ **TODO**: Add HTTPS/TLS in deployment
- ⚠️ **TODO**: Implement refresh tokens
- ⚠️ **TODO**: Add distributed rate limiting (Redis)
- ⚠️ **TODO**: Implement email verification
- ⚠️ **TODO**: Add audit logging

---

## 📈 Future Enhancements

- [ ] Refresh token mechanism
- [ ] OAuth2/OpenID Connect integration
- [ ] Role-based access control (ADMIN, USER)
- [ ] Email verification on registration
- [ ] Password reset flow
- [ ] Two-factor authentication
- [ ] Redis-backed distributed rate limiting
- [ ] ELK stack integration for logging
- [ ] User profile update endpoint
- [ ] Admin dashboard
- [ ] Caching layer (Redis)
- [ ] Horizontal scaling support

---

## 📝 License

This project is provided as-is for educational and commercial use.

---

## 👨‍💼 Support

For issues or questions, refer to the code comments and file structure explanations above.



