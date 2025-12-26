-- V006 - Seed initial data
-- Create demo tenant
INSERT INTO tenants (id, name, subdomain, status, subscription_plan, max_users, max_projects)
VALUES ('550e8400-e29b-41d4-a716-446655440001', 'Demo Company', 'demo', 'ACTIVE', 'PRO', 25, 15);

-- Create super admin user
INSERT INTO users (id, tenant_id, email, password_hash, full_name, role, is_active)
VALUES ('550e8400-e29b-41d4-a716-446655440010', NULL, 'superadmin@system.com', '$2b$12$UbCtt5o9ExqjKnduByeU1O7ag8WuBPYrlmyJXThnFmVdnbpIT2XqG', 'Super Admin', 'SUPER_ADMIN', true);

-- Create demo tenant admin
INSERT INTO users (id, tenant_id, email, password_hash, full_name, role, is_active)
VALUES ('550e8400-e29b-41d4-a716-446655440011', '550e8400-e29b-41d4-a716-446655440001', 'admin@demo.com', '$2b$12$Ss5jhgeKlvr0M38f0tDPwe/eZ7WejpT7w0W/DgjEMkvyMlMxzAvze', 'Demo Admin', 'TENANT_ADMIN', true);

-- Create demo tenant users
INSERT INTO users (id, tenant_id, email, password_hash, full_name, role, is_active)
VALUES ('550e8400-e29b-41d4-a716-446655440012', '550e8400-e29b-41d4-a716-446655440001', 'user1@demo.com', '$2b$12$CMamegIKakLsOZJmPrh12umofNwuEJjSXg.EtJywxjjLsOH6guQmq', 'Demo User 1', 'USER', true);

INSERT INTO users (id, tenant_id, email, password_hash, full_name, role, is_active)
VALUES ('550e8400-e29b-41d4-a716-446655440013', '550e8400-e29b-41d4-a716-446655440001', 'user2@demo.com', '$2b$12$CMamegIKakLsOZJmPrh12umofNwuEJjSXg.EtJywxjjLsOH6guQmq', 'Demo User 2', 'USER', true);

-- Create sample projects
INSERT INTO projects (id, tenant_id, name, description, status, created_by)
VALUES ('550e8400-e29b-41d4-a716-446655440020', '550e8400-e29b-41d4-a716-446655440001', 'Website Redesign', 'Complete website redesign project', 'ACTIVE', '550e8400-e29b-41d4-a716-446655440011');

INSERT INTO projects (id, tenant_id, name, description, status, created_by)
VALUES ('550e8400-e29b-41d4-a716-446655440021', '550e8400-e29b-41d4-a716-446655440001', 'Mobile App', 'Mobile application development', 'ACTIVE', '550e8400-e29b-41d4-a716-446655440011');

-- Create sample tasks
INSERT INTO tasks (id, project_id, tenant_id, title, description, status, priority, assigned_to, due_date)
VALUES ('550e8400-e29b-41d4-a716-446655440030', '550e8400-e29b-41d4-a716-446655440020', '550e8400-e29b-41d4-a716-446655440001', 'Design Homepage', 'Create high-fidelity mockups', 'IN_PROGRESS', 'HIGH', '550e8400-e29b-41d4-a716-446655440012', '2025-01-15');

INSERT INTO tasks (id, project_id, tenant_id, title, description, status, priority, assigned_to, due_date)
VALUES ('550e8400-e29b-41d4-a716-446655440031', '550e8400-e29b-41d4-a716-446655440020', '550e8400-e29b-41d4-a716-446655440001', 'Implement Backend API', 'Build REST APIs', 'TODO', 'HIGH', '550e8400-e29b-41d4-a716-446655440013', '2025-01-20');

INSERT INTO tasks (id, project_id, tenant_id, title, description, status, priority, assigned_to, due_date)
VALUES ('550e8400-e29b-41d4-a716-446655440032', '550e8400-e29b-41d4-a716-446655440021', '550e8400-e29b-41d4-a716-446655440001', 'Setup Project Structure', 'Initialize React project', 'COMPLETED', 'MEDIUM', '550e8400-e29b-41d4-a716-446655440012', '2025-01-10');
