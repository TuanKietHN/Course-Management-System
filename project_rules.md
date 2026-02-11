# Project Rules & Standards

## Architecture
- **Type**: Modular Monolith.
- **Pattern**: **Strict Clean Architecture**.
  - **Domain Layer**: Must be pure POJOs. **NO** Framework annotations (e.g., JPA `@Entity`, `@Table`) allowed in Domain Models.
  - **Persistence Layer**: Located in `infrastructure/persistence`. Contains JPA Entities (`@Entity`) and Repositories.
  - **Mapping**: Use **MapStruct** to map between Domain Models and Persistence Entities.
  - **Database Agnostic**: The Core/Domain should not know about the underlying database.

## Technology Stack
- **Backend**:
  - Java 21
  - Spring Boot 4.x
  - Build Tool: Maven
- **Frontend**:
  - Vue.js 3 (Composition API)
  - TypeScript
  - Vite
  - Pinia (State Management)
  - Monorepo structure (located in `frontend/` directory).
- **Database**:
  - Primary: PostgreSQL (managed via Flyway).
  - Cache/Session: Redis.

## Security & Authentication
- **Protocol**: OAuth2 Resource Server (JWT).
- **Token Management**:
  - **Access Token**: Stateless JWT.
  - **Refresh Token**: Opaque UUID, stored in **Redis**.
  - **Rotation**: Refresh Token Rotation is mandatory (detect reuse and invalidate).
  - **Password Reset**: Short-lived tokens stored in Redis (TTL ~15 mins).
- **Authorization**: Role-based access control (RBAC).

## Controller Layer Standards
- **Base Path**: `/api`
- **Architecture**: Controllers must follow Clean Architecture principles.
- **Naming Conventions**:
  - Controller classes: `{ModuleName}Controller`.
- **Input Parameters**:
  - **Accept DTOs for request bodies**: Use DTOs to validate and map request payloads.
  - **Use standardized pagination DTOs for list endpoints (optimized structure).**
  - **DO NOT use Pageable directly in Controllers. Use pagination DTOs instead.**
  - **Path Variables**: For resource identification (e.g., `/api/users/{id}`).
  - **Query Parameters**: For filtering, pagination, etc. (e.g., `/api/users?role=student`).
- **Validate Input Parameters**: Use Bean Validation annotations (`@Valid`,`@NotNull`, etc.) on input DTOs.
- **Request/Response**:
  - **Request Body**: JSON.
  - **Response**: return DTOs (never return Domain Models or Entities directly).
  - **All list APIs MUST return a standardized pagination response format.**
  - **Business Logic**: NO business logic in Controllers. Delegate all logic to Service layer.
  - **Security**: Use annotations only for security (@PreAuthorize, @Secured, etc.).
  - **Page Response Format**: All paginated responses must follow a unified standard structure.
  - **Response Body**: JSON.
  - **Error Responses**: Follow a consistent format (e.g., `{status: 400, message: "Invalid input"}`).

## Service Layer Standards
- **Business Logic**: ALL business logic must be implemented in ServiceImpl classes. Service layer should only contain business logic, not data access or other concerns.
- **Service Interface**: Define contracts in Service interfaces. ServiceImpl classes must implement these interfaces.
- **Separation of Concerns**: Services orchestrate use cases and delegate to Repositories.
  
## Repository Layer Standards
- **Query Language**: Write ALL queries using HQL (Hibernate Query Language).
- **Optimization**: Queries must be optimized for performance (avoid N+1 problems, use joins efficiently).
- **Clean Architecture Compliance**: Repositories are part of the infrastructure layer and must not leak into the domain.
- **Custom Queries**: Prefer HQL over native SQL for database agnosticism.
  
## Coding Standards
- **API Versioning**: All APIs must be versioned (e.g., `/api/v1/auth/...`).
- **Null Safety**: Implement explicit null checks and handling.
- **Documentation**:
  - **Swagger/OpenAPI**: Required (`springdoc-openapi`).
  - **Localization**: API descriptions and Tags in Swagger must be in **Vietnamese**.
- **Libraries**:
  - Use `Lombok` to reduce boilerplate.
  - Use `MapStruct` for object mapping.
- **Enums**: avoid typo; easy refactor
## E-Learning Standards
- **Compliance**: System should be designed with SCORM and xAPI (Tin Can API) standards in mind.
