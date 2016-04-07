CREATE DOMAIN dom_subforum_name TEXT CHECK (
    LENGTH(VALUE) > 1    AND
    LENGTH(VALUE) < 200
);
--;;

CREATE TABLE subforums (
  id               INT UNIQUE NOT NULL,
  name             dom_subforum_name UNIQUE NOT NULL,
  threads_count    INT NOT NULL DEFAULT 0,
  created_at       TIMESTAMP DEFAULT current_timestamp,
  updated_at       TIMESTAMP
);
--;;

CREATE UNIQUE INDEX idx_subforums_id ON subforums (id);
--;;