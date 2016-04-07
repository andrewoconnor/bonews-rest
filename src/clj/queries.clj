(ns clj.queries
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

(defqueries "sql/users.sql" {:connection dev-db-spec})

(defqueries "sql/replies.sql" {:connection dev-db-spec})