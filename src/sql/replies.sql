-- name: create-reply!
-- Inserts a new user into the users table
INSERT INTO replies (id, thread_id, user_id, parent_id, title, message)
VALUES (:id, :thread_id, :user_id, :parent_id, :title, :message);