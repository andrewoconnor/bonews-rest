CREATE DOMAIN dom_vote SMALLINT CHECK (
  VALUE = 1   OR
  VALUE = -1
);
--;;

CREATE TABLE bulbs (
  reply_id         INT NOT NULL REFERENCES replies(id),
  user_id          INT NOT NULL REFERENCES users(id),
  PRIMARY KEY      (reply_id, user_id),
  vote             dom_vote NOT NULL,
  created_at       TIMESTAMP DEFAULT current_timestamp,
  updated_at       TIMESTAMP
);
--;;

CREATE TRIGGER trg_bulb_updated_at
BEFORE UPDATE ON bulbs
FOR EACH ROW
EXECUTE PROCEDURE set_updated_at_column();
--;;

CREATE OR REPLACE FUNCTION votes_counter_cache() RETURNS TRIGGER AS
$$
DECLARE
  table_name text := quote_ident(TG_ARGV[0]);
  fk_name text := quote_ident(TG_ARGV[1]);
  fk_changed boolean := false;
  counter_name text;
  fk_value integer;
  record record;
BEGIN
  IF TG_OP = 'UPDATE' OR TG_OP = 'INSERT' THEN
    IF NEW.vote = 1 THEN
      IF table_name = 'replies' THEN
        counter_name = 'upvotes_count';
      ELSEIF table_name = 'users' THEN
        counter_name = 'upvotes_given_count';
      END IF;
    ELSEIF NEW.vote = -1 THEN
      IF table_name = 'replies' THEN
        counter_name = 'downvotes_count';
      ELSEIF table_name = 'users' THEN
        counter_name = 'downvotes_given_count';
      END IF;
    END IF;
  ELSEIF TG_OP = 'DELETE' THEN
    IF OLD.vote = 1 THEN
      IF table_name = 'replies' THEN
        counter_name = 'upvotes_count';
      ELSEIF table_name = 'users' THEN
        counter_name = 'upvotes_given_count';
      END IF;
    ELSEIF OLD.vote = -1 THEN
      IF table_name = 'replies' THEN
        counter_name = 'downvotes_count';
      ELSEIF table_name = 'users' THEN
        counter_name = 'downvotes_given_count';
      END IF;
    END IF;
  END IF;

  IF TG_OP = 'UPDATE' THEN
    record := NEW;
    EXECUTE 'SELECT ($1).' || fk_name || ' != ' || '($2).' || fk_name
    INTO fk_changed
    USING OLD, NEW;
  END IF;

  IF TG_OP = 'DELETE' OR fk_changed THEN
    record := OLD;
    EXECUTE 'SELECT ($1).' || fk_name INTO fk_value USING record;
    PERFORM increment_counter(table_name, counter_name, fk_value, -1);
  END IF;

  IF TG_OP = 'INSERT' OR fk_changed THEN
    record := NEW;
    EXECUTE 'SELECT ($1).' || fk_name INTO fk_value USING record;
    PERFORM increment_counter(table_name, counter_name, fk_value, 1);
  END IF;

  RETURN record;
END
$$
LANGUAGE plpgsql;
--;;

CREATE TRIGGER trg_replies_votes_counter
AFTER INSERT OR UPDATE OR DELETE ON bulbs
FOR EACH ROW
EXECUTE PROCEDURE votes_counter_cache('replies', 'reply_id');
--;;

CREATE TRIGGER trg_users_votes_counter
AFTER INSERT OR UPDATE OR DELETE ON bulbs
FOR EACH ROW
EXECUTE PROCEDURE votes_counter_cache('users', 'user_id');
--;;