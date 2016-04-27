-- name: create-user!
-- Inserts a new user into the users table
INSERT INTO users (id, username, signature, nt_reply_url)
VALUES (:id, :username, :signature, :nt_reply_url);

-- name: get-signature
-- Gets signature of a particular user
SELECT signature
FROM users
WHERE id = :id;

-- name: save-signature!
-- Updates signature of a particular user
UPDATE users
SET signature = :signature
WHERE id = :id;

