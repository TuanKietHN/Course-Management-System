-- ============================================
-- V7__drop_legacy_role_column.sql
-- Drop the legacy 'role' column from 'users' table
-- which causes NOT NULL constraint violations
-- ============================================

ALTER TABLE users DROP COLUMN role;
