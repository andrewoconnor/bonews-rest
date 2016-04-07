DROP TRIGGER trg_replies_votes_counter ON bulbs CASCADE;
--;;

DROP FUNCTION votes_counter_cache() CASCADE;
--;;

DROP TABLE bulbs CASCADE;
--;;

DROP DOMAIN dom_vote CASCADE;
--;;