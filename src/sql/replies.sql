-- name: create-reply!
-- Inserts a new user into the users table
INSERT INTO replies (id, thread_id, user_id, parent_id, title, message)
VALUES (:id, :thread_id, :user_id, :parent_id, :title, :message);

-- name: get-replies
-- Gets replies of a particular user with limit and offset
SELECT *
FROM replies
WHERE user_id = :user_id
ORDER BY created_at DESC
OFFSET :offset
LIMIT :limit;