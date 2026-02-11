# System vs Flyway Comparison

## 1. Entity vs Table Mapping

| Java Entity | Database Table | Status | Notes |
| :--- | :--- | :--- | :--- |
| `User` | `users` | ⚠️ Mismatch | Java `User` has `Set<RoleType> roles`, DB `users` has obsolete `role` column. DB `user_roles` exists in V2 but Java might be using `@ElementCollection` which usually maps to a separate table managed by JPA, or we need to ensure `@CollectionTable` points to `user_roles` correctly. |
| `Role` (Enum) | `roles` | ✅ Aligned | Java Enum values match `roles` table entries in V2. |
| `Semester` | `semesters` | ✅ Aligned | Fields match (code, name, start_date, end_date, active). |
| `Subject` | `subjects` | ✅ Aligned | Fields match (code, name, credit, description). |
| `Course` | `courses` | ✅ Aligned | references `users` as teacher. |

## 2. Key Discrepancies

### 2.1. The "Role" Conflict
- **DB (V1):** `users.role` (VARCHAR) -> Single role per user.
- **DB (V2):** `user_roles` (Many-to-Many) -> Multi-role support.
- **Java:** `User.roles` (`Set<RoleType>`).
- **Issue:** The `users.role` column is deprecated but still exists and has NOT NULL constraint in V1. Code might fail if it tries to insert null here, or if JPA tries to read it.

### 2.2. Data Seeding Strategy
- **Flyway:** Inserts initial roles (ADMIN, TEACHER, STUDENT) and initial users (admin, teacher, student).
- **Java DataSeeder:** Also tries to insert the same users.
- **Risk:** `DataSeeder` checks `existsByEmail`, so it might skip if Flyway already inserted them. However, if we change logic in one place (e.g., password hashing algorithm), they might drift apart.

### 2.3. JPA Mapping Detail
- In `UserEntity.java`:
  ```java
  @ElementCollection(targetClass = RoleType.class)
  @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
  @Enumerated(EnumType.STRING)
  @Column(name = "role_id") // <--- This might be wrong!
  private Set<RoleType> roles;
  ```
  - **Flyway V2 `user_roles` definition:**
    ```sql
    CREATE TABLE user_roles (
        user_id BIGINT NOT NULL,
        role_id BIGINT NOT NULL, -- This expects a Long ID reference to 'roles' table
        ...
    );
    ```
  - **Mismatch:** JPA `@ElementCollection` with `@Enumerated(EnumType.STRING)` tries to save the **String value** (e.g., "ADMIN") into the column. But Flyway created a table expecting a **Foreign Key ID** (Long) to the `roles` table.
  - **CRITICAL ERROR:** The application will fail to start or save users because JPA tries to insert a String ("ADMIN") into a BigInt column (`role_id`), OR it tries to insert into a column named `role` but the table has `role_id`.

## 3. Summary
The Java code (using Enums and ElementCollection) assumes a simple value mapping, while the Database (V2 Migration) implements a full relational RBAC with a separate `roles` table. **They are currently incompatible.**
