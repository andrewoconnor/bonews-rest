(ns seeds.sql
    (:require [clojure.java.jdbc :as j]))

(defn run [target & args]
      (j/with-db-connection [db {:connection-uri (-> target :db :url)}]
                            (j/insert! db :users {:bo_id 1 :username "test1"})
                            (j/insert! db :users {:bo_id 42 :username "test42"})
                            (j/insert! db :users {:bo_id 41 :username "test40"})
                            (j/insert! db :users {:bo_id 23 :username "airjordan23"})))
