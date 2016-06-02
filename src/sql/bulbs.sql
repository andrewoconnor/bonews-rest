-- name: create-bulb!
-- Inserts a new bulb into the bulbs table
INSERT INTO bulbs (reply_id, user_id, vote)
VALUES (:reply_id, :user_id, :vote)
ON CONFLICT (reply_id, user_id)
DO NOTHING;