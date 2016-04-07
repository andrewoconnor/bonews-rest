CREATE DOMAIN dom_username TEXT CHECK (
    LENGTH(VALUE) > 1    AND
    LENGTH(VALUE) < 200
);
--;;

CREATE DOMAIN dom_signature TEXT CHECK (
  LENGTH(VALUE) < 2000
);
--;;

CREATE TABLE users (
  id               INT UNIQUE NOT NULL,
  username         dom_username UNIQUE NOT NULL,
  signature        dom_signature,
--   upvotes_count    INT,
--   downvotes_count  INT,
  replies_count    INT NOT NULL DEFAULT 0,
  created_at       TIMESTAMP DEFAULT current_timestamp,
  updated_at       TIMESTAMP
);
--;;

CREATE TRIGGER trg_user_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE PROCEDURE set_updated_at_column();
--;;

CREATE UNIQUE INDEX idx_users_id ON users (id);
--;;

CREATE UNIQUE INDEX idx_username ON users (username);
--;;