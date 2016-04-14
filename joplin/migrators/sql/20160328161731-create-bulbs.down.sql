-- DROP TRIGGER trg_users_votes_counter ON bulbs CASCADE;
--;;

DROP TRIGGER trg_replies_votes_counter ON bulbs CASCADE;
--;;

DROP FUNCTION votes_counter_cache() CASCADE;
--;;

DROP TRIGGER trg_bulb_updated_at ON bulbs CASCADE;
--;;

DROP TABLE bulbs CASCADE;
--;;

DROP DOMAIN dom_vote CASCADE;
--;;