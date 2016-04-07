CREATE TABLE threads (
  id               INT UNIQUE NOT NULL,
  subforum_id      INT NOT NULL REFERENCES subforums(id),
  replies_count    SMALLINT NOT NULL DEFAULT 0,
  created_at       TIMESTAMP DEFAULT current_timestamp,
  updated_at       TIMESTAMP
);
--;;

CREATE TRIGGER trg_subforum_threads_counter
AFTER INSERT OR UPDATE OR DELETE ON threads
FOR EACH ROW
EXECUTE PROCEDURE counter_cache('subforums', 'threads_count', 'subforum_id');
--;;

CREATE UNIQUE INDEX idx_threads_id ON threads (id);
--;;