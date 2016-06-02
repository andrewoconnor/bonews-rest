-- name: create-thread!
-- Inserts a new thread into the threads table
INSERT INTO threads (id, subforum_id, user_id)
VALUES (:id, :subforum_id, :user_id)
ON CONFLICT (id)
DO NOTHING;