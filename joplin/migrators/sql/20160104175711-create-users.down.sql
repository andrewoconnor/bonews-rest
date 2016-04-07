DROP INDEX idx_username;
--;;

DROP INDEX idx_users_id;
--;;

DROP TRIGGER trg_user_updated_at ON users CASCADE;
--;;

DROP TABLE users CASCADE;
--;;

DROP DOMAIN dom_signature CASCADE;
--;;

DROP DOMAIN dom_username CASCADE;
--;;