-- password for admin: password
INSERT INTO users (id, username, email, password_hash, role, avatar_image_id, created_at, updated_at) VALUES
('1', 'admin', 'amdin2@triplify.com', '$2a$10$RNCuj5SA1lJFdju6G6F1j.fWqPCRfLm2FRPslvjruiACyGxllc6G.', 'configuration manager', null, '2026-03-30T14:19:58.617522100Z', '2026-03-30T14:19:58.617522100Z'),
('2', 'admin2', 'admin@triplify.com', '$2a$10$V5BwTIAU4zy6VG2eRhzqOe.95eLOE.kL84zqDpQmcIar2QrWIs/0e', 'configuration manager', null, '2026-03-30T14:20:05.917682100Z', '2026-03-30T14:20:05.917682100Z');
