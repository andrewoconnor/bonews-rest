DROP INDEX username_idx;
--;;

DROP INDEX bo_id_idx;
--;;

DROP TRIGGER trg_user_updated_at ON users CASCADE;
--;;

DROP TABLE users CASCADE;
--;;

DROP DOMAIN dom_signature CASCADE;
--;;

DROP DOMAIN dom_username CASCADE;
--;;