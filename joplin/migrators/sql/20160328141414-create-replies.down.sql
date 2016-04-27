DROP INDEX idx_reply_user_id;
--;;

DROP INDEX idx_reply_thread_id;
--;;

DROP INDEX idx_replies_id;
--;;

DROP TRIGGER trg_user_replies_counter ON replies CASCADE;
--;;

DROP TRIGGER trg_thread_replies_counter ON replies CASCADE;
--;;

DROP TRIGGER trg_reply_updated_at ON replies CASCADE;
--;;

DROP TABLE replies CASCADE;
--;;