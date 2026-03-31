# Authsystem

A Spring Boot-based authentication and user management system with JWT authentication, rate limiting, and usage tracking.

## Features
- **User Registration & Login**: Register and authenticate users with hashed passwords.
- **JWT Authentication**: Secure endpoints using JSON Web Tokens.
- **Rate Limiting**: Prevent abuse by limiting requests per user/IP.
- **Usage Tracking**: Log API usage for analytics and monitoring.
- **Spring Security Integration**: Stateless security for REST APIs.
- **PostgreSQL Database**: Store user and usage data.

---

## Project Structure & File Explanations

### Main Application
- **AuthsystemApplication.java**: Entry point for the Spring Boot application.

### Configuration
- **SecurityConfig.java**: Configures Spring Security, disables sessions, sets up JWT filter, and password encoding.
- **RateLimitConfig.java**: Provides Bucket4j configuration for per-user/IP rate limiting.
- **RateLimitFilterConfig.java**: Registers and orders all servlet filters (JWT, rate limiting, usage tracking) in the filter chain.
- **JwtUtil.java**: Utility for generating, validating, and parsing JWT tokens.

### Filters
- **JwtAuthenticationFilter.java**: Checks JWT on protected endpoints, sets authentication context, and logs status codes.
- **RateLimitFilter.java**: Enforces rate limiting using Bucket4j, blocks requests exceeding the quota.
- **UsageTrackingFilter.java**: Logs each API call to the database for analytics.

### Controllers
- **AuthController.java**: Handles user registration, login, and token issuance. Returns JWT and user info on login.
- **UserController.java**: Exposes endpoints to fetch all users or a user by ID. Protected by JWT.

### Services
- **AuthService.java**: Business logic for registration and login, including password hashing and validation.
- **UserService.java**: Business logic for fetching user data.

### Entities
- **User.java**: JPA entity for user data (id, name, email, hashed password).
- **ApiUsage.java**: JPA entity for logging API usage (IP, endpoint, timestamp).

### DTOs
- **RegisterRequest.java**: Data transfer object for user registration.
- **LoginRequest.java**: Data transfer object for user login.

### Repositories
- **UserRepository.java**: Spring Data JPA repository for User entity.
- **ApiUsageRepository.java**: Spring Data JPA repository for ApiUsage entity.

### Resources
- **application.properties**: Spring Boot configuration (DB connection, JPA, etc).

---

## How to Use
1. **Register**: `POST /auth/register` with `{ "name": "...", "email": "...", "password": "..." }`
2. **Login**: `POST /auth/login` with `{ "email": "...", "password": "..." }` to receive JWT and user info.
3. **Access Users**: Use the JWT as a Bearer token in the `Authorization` header for `/users` endpoints.
4. **Rate Limiting**: After 50 requests per minute per IP, further requests receive HTTP 429.

---

## Why Each File Exists
- **SecurityConfig.java**: Centralizes all security settings and ensures stateless JWT authentication.
- **JwtUtil.java**: Encapsulates JWT logic, keeping controllers/services clean.
- **JwtAuthenticationFilter.java**: Ensures only authenticated requests reach protected endpoints and logs status codes for observability.
- **RateLimitConfig.java**: Encapsulates rate limiting logic, making it reusable and configurable.
- **RateLimitFilter.java**: Applies rate limiting to all requests, blocking excessive usage.
- **RateLimitFilterConfig.java**: Ensures correct filter order and registration, critical for security and rate limiting to work together.
- **UsageTrackingFilter.java**: Records every API call for monitoring and analytics.
- **AuthController.java**: Handles authentication-related endpoints, separating concerns from user management.
- **UserController.java**: Handles user data endpoints, protected by JWT.
- **AuthService.java**: Handles business logic for authentication, including password hashing.
- **UserService.java**: Handles business logic for user data retrieval.
- **User.java**: Represents users in the database.
- **ApiUsage.java**: Represents API usage logs in the database.
- **RegisterRequest.java / LoginRequest.java**: Cleanly separate input data for registration and login.
- **UserRepository.java / ApiUsageRepository.java**: Abstract database access for users and API usage.
- **application.properties**: Centralizes configuration for easy management.

---

## Logging
- All authentication attempts and protected endpoint accesses are logged with HTTP status codes, method, URI, and client IP for easy debugging and monitoring.

---

## Extending
- Add roles/authorities to User and enhance JWT claims for role-based access.
- Add more endpoints and business logic as needed.
- Integrate with frontend or other services using the JWT-based API.

---



