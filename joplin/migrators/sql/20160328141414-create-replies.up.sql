CREATE TABLE replies (
  id               INT UNIQUE NOT NULL,
  thread_id        INT NOT NULL REFERENCES threads(id),
  user_id          INT NOT NULL REFERENCES users(id),
  parent_id        INT REFERENCES replies(id),
  title            TEXT NOT NULL,
  message          TEXT,
  post_time        TIMESTAMP,
  upvotes_count    SMALLINT NOT NULL DEFAULT 0,
  downvotes_count  SMALLINT NOT NULL DEFAULT 0,
  created_at       TIMESTAMP DEFAULT current_timestamp,
  updated_at       TIMESTAMP
);
--;;

CREATE TRIGGER trg_reply_updated_at
BEFORE UPDATE ON replies
FOR EACH ROW
EXECUTE PROCEDURE set_updated_at_column();
--;;

CREATE TRIGGER trg_thread_replies_counter
AFTER INSERT OR UPDATE OR DELETE ON replies
FOR EACH ROW
EXECUTE PROCEDURE counter_cache('threads', 'replies_count', 'thread_id');
--;;

CREATE TRIGGER trg_user_replies_counter
AFTER INSERT OR UPDATE OR DELETE ON replies
FOR EACH ROW
EXECUTE PROCEDURE counter_cache('users', 'replies_count', 'user_id');
--;;

CREATE UNIQUE INDEX idx_replies_id ON replies (id);
--;;

CREATE INDEX idx_reply_thread_id ON replies (thread_id);
--;;

CREATE INDEX idx_reply_user_id ON replies (user_id);
--;;