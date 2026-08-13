-- Sample test data for authentication system
-- Passwords are BCrypt hashed:
-- password123 -> $2a$10$uTptznMmlJMHZojzojE7p.gmitlNMAq1c06k.G62heVGeVyTCB3Tu
-- test123 -> $2a$10$Pog7H7J9mMbAPGLEV6HceOvfMfxqIr0hn2VcGutDGeJNvE.47OVJq
-- admin@123 -> $2a$10$MgoMQCeEAEJX8CZd2gbt5.Uo/dylIcs.sho0jKgZlVbFaLc123Zf2

-- Insert test users (only if not already present)
INSERT OR IGNORE INTO users (email, fullname, password, created_at, updated_at)
VALUES 
  ('john@example.com', 'John Doe', '$2a$10$uTptznMmlJMHZojzojE7p.gmitlNMAq1c06k.G62heVGeVyTCB3Tu', datetime('now'), datetime('now')),
  ('jane@example.com', 'Jane Smith', '$2a$10$Pog7H7J9mMbAPGLEV6HceOvfMfxqIr0hn2VcGutDGeJNvE.47OVJq', datetime('now'), datetime('now')),
  ('admin@example.com', 'Admin User', '$2a$10$MgoMQCeEAEJX8CZd2gbt5.Uo/dylIcs.sho0jKgZlVbFaLc123Zf2', datetime('now'), datetime('now'));
