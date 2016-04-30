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
  vote_type text;
  counter_name text;
  step integer;
  reply_id integer;
  reply_author integer;
  fk_value integer;
  vote integer;
  record record;
BEGIN
  IF TG_OP = 'UPDATE' OR TG_OP = 'INSERT' THEN
    record := NEW;
    step = 1;
  ELSEIF TG_OP = 'DELETE' THEN
    record := OLD;
    step = -1;
  END IF;

  vote = record.vote;
  reply_id = record.reply_id;

  IF vote = 1 THEN
    vote_type = 'upvotes_';
  ELSEIF vote = -1 THEN
    vote_type = 'downvotes_';
  END IF;

  IF table_name = 'users' THEN
    counter_name = vote_type || 'given_';
    SELECT user_id INTO reply_author FROM replies WHERE id = reply_id;
  ELSE
    counter_name = vote_type;
  END IF;

  counter_name = counter_name || 'count';

  IF TG_OP = 'UPDATE' THEN
    EXECUTE 'SELECT ($1).' || fk_name || ' != ' || '($2).' || fk_name
    INTO fk_changed
    USING OLD, NEW;
  END IF;

  IF TG_OP = 'INSERT' OR TG_OP = 'DELETE' OR fk_changed THEN
    EXECUTE 'SELECT ($1).' || fk_name INTO fk_value USING record;
    PERFORM increment_counter(table_name, counter_name, fk_value, step);
    IF table_name = 'users' THEN
      counter_name = vote_type || 'received_count';
      PERFORM increment_counter(table_name, counter_name, reply_author, step);
    END IF;
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