# Database Migration & Update Plan

## Objective
Align the Java codebase and Database Schema to resolve the conflict between Simple Enum Mapping (Java) and Relational RBAC (Database).

**Decision:** We will adopt the **Relational RBAC** approach defined in Flyway V2, as it is more robust and extensible. This means we need to update the Java `User` entity to map to `Role` entities, not just Enums.

## Phase 1: Fix Java Domain Model
1.  **Create `Role` Entity:**
    - Create `vn.com.nws.cms.modules.auth.infrastructure.persistence.entity.RoleEntity`.
    - Map to `roles` table.
2.  **Update `UserEntity`:**
    - Change `Set<RoleType> roles` to `Set<RoleEntity> roles`.
    - Use `@ManyToMany` annotation instead of `@ElementCollection`.
    - Map to `user_roles` join table.
3.  **Update `User` Domain Model:**
    - It can keep `Set<RoleType>` if we map `RoleEntity` -> `RoleType` in the Mapper.
    - OR change it to hold `Set<String>` or `Set<Role>` domain object. *Recommendation: Keep `Set<RoleType>` in Domain for simplicity if roles are static, but since DB has dynamic roles table, `Set<String>` or a `Role` domain object is better.*

## Phase 2: Update Data Seeder
1.  **Stop creating Users blindly:** `DataSeeder` should check if Roles exist first.
2.  **Fetch Roles from DB:** Instead of `Set.of(RoleType.ADMIN)`, fetch the Role entity "ROLE_ADMIN" from repositories and assign it.
3.  **Remove Duplicate Logic:** Rely on Flyway for structural data (Roles, Permissions). Use Seeder only for Demo Data (Users, Courses).

## Phase 3: Cleanup Database (Optional/Later)
1.  **Drop Obsolete Column:** Create V7 migration to drop `role` column from `users` table to remove confusion.

## Execution Steps (Immediate Actions)
1.  **Refactor `UserEntity`**: Change from `@ElementCollection` (Enum) to `@ManyToMany` (Entity).
2.  **Create `RoleEntity`**: To map to the existing `roles` table.
3.  **Update `DataSeeder`**: To fetch roles via Repository instead of creating sets of Enums.
4.  **Update `UserService/AuthService`**: To handle Role Entities during registration/update.
