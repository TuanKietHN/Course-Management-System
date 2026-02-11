# Flyway Migration Analysis

## 1. Overview
The current Flyway migrations define a comprehensive database schema covering:
- **Core/Auth:** Users, Roles, Permissions (RBAC), Tokens.
- **Academic:** Semesters, Subjects, Courses, Enrollments, Teachers.
- **LMS:** Lessons, Sections, Contents, Progress.
- **Assessment:** Assignments, Quizzes, Questions, Attempts.
- **Attendance:** Sessions, Records, QR Codes.

## 2. Consistency with Architecture & Principles

### 2.1. Pros (Good Practices)
- **Modular Structure:** The migrations are reasonably split by feature (Auth, Academic, Lesson, Assessment, Attendance). This mirrors the modular monolith code structure.
- **Naming Conventions:** Consistent naming (plural table names, snake_case columns).
- **Audit Columns:** `created_at` and `updated_at` are consistently used (with triggers in V2).
- **Constraints:** Good use of UNIQUE, FOREIGN KEY, and NOT NULL constraints to ensure data integrity.
- **RBAC:** V2 migration implements a solid Role-Based Access Control system with `roles`, `permissions`, `user_roles`, and `role_permissions`.

### 2.2. Cons & Discrepancies (Risks)
- **Duplicate Data Seeding:** Both Flyway (V1, V2) and `DataSeeder.java` attempt to seed initial data (Admin, Teacher, Student). This causes conflicts or redundant checks.
  - *Recommendation:* Remove data seeding from `DataSeeder.java` or Flyway (preferably keep structural reference data in Flyway and test/demo data in `DataSeeder`).
- **`role` Column in `users` table:**
  - V1 creates a `role` column (VARCHAR) in `users`.
  - V2 introduces a full RBAC system (`user_roles` table) and attempts to migrate data.
  - **Conflict:** The Java code now uses `Set<RoleType>` mapped via `@ElementCollection` to `user_roles`. The `role` column in `users` table is now **obsolete** and potentially confusing.
- **Teacher Table:**
  - V3 (implied) seems to have a `teachers` table, but V1/V2 links courses directly to `users` (teacher_id).
  - *Note:* Need to verify if `teachers` table actually exists in migrations (V3 content was not fully read but inferred). If V1 `courses` references `users` directly as `teacher_id`, it might violate the domain separation if "Teacher" has specific attributes (degree, department) not in User.

## 3. Alignment with Recent Code Refactoring
- **Multi-Role Support:**
  - The V2 migration **supports** multi-role via `user_roles` (Many-to-Many). This aligns perfectly with the recent Java refactoring (`Set<RoleType>`).
- **Enum vs DB:**
  - Java Enum `RoleType` (ADMIN, TEACHER, STUDENT) matches the initial roles inserted in V2.

## 4. Conclusion
The database schema is generally robust and supports the application's direction. However, the **hybrid state** of having both a legacy `role` column in `users` and a new `user_roles` table needs cleanup to avoid confusion. The data seeding strategy also needs to be unified.
