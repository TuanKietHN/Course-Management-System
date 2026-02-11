-- ============================================
-- V8__add_avatar_column.sql
-- Add 'avatar' column to 'users' table
-- ============================================

ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar VARCHAR(255);
