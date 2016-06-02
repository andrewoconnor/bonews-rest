(ns bonews-rest.queries
  (:require [yesql.core :refer [defqueries]]))

(def dev-pghost (System/getenv "DEV_PGHOST"))
(def dev-pgport (System/getenv "DEV_PGPORT"))
(def dev-dbname (System/getenv "DEV_DBNAME"))
(def dev-subname (str "//" dev-pghost ":" dev-pgport "/" dev-dbname))
(def dev-pguser (System/getenv "DEV_PGUSER"))

(def dev-db-spec {:classname "org.postgresql.Driver"
                  :subprotocol "postgresql"
                  :subname dev-subname
                  :user dev-pguser})

(defqueries "users.sql" {:connection dev-db-spec})
(defqueries "threads.sql" {:connection dev-db-spec})
(defqueries "replies.sql" {:connection dev-db-spec})
(defqueries "bulbs.sql" {:connection dev-db-spec})