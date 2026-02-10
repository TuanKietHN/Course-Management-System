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

## Coding Standards
- **API Versioning**: All APIs must be versioned (e.g., `/api/v1/auth/...`).
- **Null Safety**: Implement explicit null checks and handling.
- **Documentation**:
  - **Swagger/OpenAPI**: Required (`springdoc-openapi`).
  - **Localization**: API descriptions and Tags in Swagger must be in **Vietnamese**.
- **Libraries**:
  - Use `Lombok` to reduce boilerplate.
  - Use `MapStruct` for object mapping.

## E-Learning Standards
- **Compliance**: System should be designed with SCORM and xAPI (Tin Can API) standards in mind.
