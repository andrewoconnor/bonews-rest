DROP INDEX idx_replies_id;
--;;

DROP TRIGGER trg_user_replies_counter ON replies CASCADE;
--;;

DROP TRIGGER trg_thread_replies_counter ON replies CASCADE;
--;;

DROP TABLE replies CASCADE;
--;;