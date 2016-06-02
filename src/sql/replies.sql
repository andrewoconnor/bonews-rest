-- name: create-reply!
-- Inserts a new reply into the replies table
INSERT INTO replies (id, thread_id, user_id, parent_id, title, message, post_time)
VALUES (:id, :thread_id, :user_id, :parent_id, :title, :message, :post_time)
ON CONFLICT (id)
DO UPDATE SET
  title = EXCLUDED.title,
  message = EXCLUDED.message;

-- name: get-replies
-- Gets replies of a particular user with limit and offset
SELECT *
FROM replies
WHERE user_id = :user_id
ORDER BY created_at DESC
OFFSET :offset
LIMIT :limit;