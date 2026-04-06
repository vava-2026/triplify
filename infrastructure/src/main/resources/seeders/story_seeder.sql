-- Story seeder: creates a pro-user, sample trips, and sample stories.
-- Depends on: user_seeder.sql, category_seeder.sql

-- Pro-user for story authoring
INSERT INTO users (id, username, email, password_hash, role, avatar_image_id, created_at, updated_at) VALUES
('f0a1b2c3-d4e5-6789-abcd-012345678901', 'prouser1', 'prouser1@triplify.com', '$2a$10$vZQczlTJCqrbp/J86XegJu8nGpHBxLVisj.zpL7dnHYqpFwPJHHs.', 'pro-user', null, '2026-04-01T10:00:00.000000000Z', '2026-04-01T10:00:00.000000000Z');

-- Sample trips for the pro-user (category_id references category_seeder.sql entries)
INSERT INTO trips (id, user_id, category_id, cover_image_id, title, description, status, started_at, ended_at, created_at, updated_at) VALUES
('a0b1c2d3-e4f5-6789-0abc-def012345678', 'f0a1b2c3-d4e5-6789-abcd-012345678901', 'f6a7b8c9-d0e1-2345-fabc-34567890abcd', null, 'Hiking in the Alps', 'A memorable weekend hike through the Austrian Alps.', 'visited', '2026-02-15T07:00:00.000000000Z', '2026-02-17T18:00:00.000000000Z', '2026-02-10T09:00:00.000000000Z', '2026-02-17T20:00:00.000000000Z'),
('b1c2d3e4-f5a6-7890-1bcd-ef0123456789', 'f0a1b2c3-d4e5-6789-abcd-012345678901', 'c3d4e5f6-a7b8-9012-cdef-012345678912', null, 'Backpacking Southeast Asia', 'Three weeks across Thailand, Vietnam, and Cambodia.', 'visited', '2025-11-01T00:00:00.000000000Z', '2025-11-22T00:00:00.000000000Z', '2025-10-20T08:00:00.000000000Z', '2025-11-22T10:00:00.000000000Z');

-- Sample stories linked to the trips above
INSERT INTO stories (id, user_id, trip_id, trip_route_id, trip_place_id, emotion_id, title, description, story_time, created_at) VALUES
('e1f2a3b4-c5d6-7890-ef12-abcdef012345', 'f0a1b2c3-d4e5-6789-abcd-012345678901', 'a0b1c2d3-e4f5-6789-0abc-def012345678', null, null, null, 'First steps on the trail', 'The morning air was crisp as we set off from the valley floor. Snow-capped peaks reflected the first light of dawn and I already knew this would be a trip to remember.', '2026-02-15T08:30:00.000000000Z', '2026-02-15T21:00:00.000000000Z'),
('f2a3b4c5-d6e7-8901-fa23-bcdef0123456', 'f0a1b2c3-d4e5-6789-abcd-012345678901', 'a0b1c2d3-e4f5-6789-0abc-def012345678', null, null, null, 'Summit reached at last', 'After six hours of steady climbing we broke through the cloud layer and were rewarded with a 360° panorama stretching all the way to the Swiss border. Worth every sore muscle.', '2026-02-16T13:00:00.000000000Z', '2026-02-16T20:00:00.000000000Z'),
('a3b4c5d6-e7f8-9012-ab34-cdef01234567', 'f0a1b2c3-d4e5-6789-abcd-012345678901', 'b1c2d3e4-f5a6-7890-1bcd-ef0123456789', null, null, null, 'Floating markets of Bangkok', 'Woke up at 5 AM to catch the floating market before the crowds arrived. The colours, smells, and energy of the vendors were unlike anything I have experienced at home.', '2025-11-03T05:30:00.000000000Z', '2025-11-03T19:00:00.000000000Z'),
('b4c5d6e7-f8a9-0123-bc45-def012345678', 'f0a1b2c3-d4e5-6789-abcd-012345678901', 'b1c2d3e4-f5a6-7890-1bcd-ef0123456789', null, null, null, 'Sunset over Angkor Wat', 'Standing in front of the temple as the sun dipped below the horizon, casting long amber shadows across the ancient stones. A moment of complete silence in a normally busy place.', '2025-11-18T17:45:00.000000000Z', '2025-11-18T22:00:00.000000000Z');
