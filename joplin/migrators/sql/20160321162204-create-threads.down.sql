DROP INDEX idx_threads_id;
--;;

DROP TRIGGER trg_subforum_threads_counter ON threads CASCADE;
--;;

DROP TRIGGER trg_thread_updated_at ON threads CASCADE;
--;;

DROP TABLE threads CASCADE;
--;;