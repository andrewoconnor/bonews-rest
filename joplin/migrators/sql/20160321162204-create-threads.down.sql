DROP INDEX idx_threads_id;
--;;

DROP TRIGGER trg_subforum_threads_counter ON threads CASCADE;
--;;

DROP TABLE threads CASCADE;
--;;