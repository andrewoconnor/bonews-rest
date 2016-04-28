(ns clj.scraper.users
  (:require [clj.queries :as queries]
            [clj.scraper.utils :as utils]
            [clj.scraper.signatures :as sig]
            [net.cgrand.enlive-html :as html]
            [clojure.string :as str]))

(def users-list-prefix "http://bo-ne.ws/forum/addon.php?0,module=user_list,page=")

(defn get-users-list
  [page-num]
  (utils/fetch-url (str users-list-prefix page-num)))

(defn get-users-table
  [user-list-page]
  (-> user-list-page
      (html/select [:table.user_list])
      last))

(defn get-user-id
  [cols]
  (-> cols
      first
      (:content)
      first
      (->> (re-find #"\d+"))
      (Integer/parseInt)))

(defn get-username
  [cols]
  (-> cols
      second
      (:content)
      second
      (:content)
      first
      (str/trim)))

(defn get-user
  [row]
  (let [cols         (utils/get-cols row)
        id           (get-user-id cols)
        name         (get-username cols)
        nt-reply-url (sig/get-no-text-url id 1)
        signature    (sig/get-signature-by-url nt-reply-url)]
    {:id           id
     :username     name
     :signature    signature
     :nt_reply_url nt-reply-url}))

(defn get-data
  [page-num]
  (let [table (get-users-table (get-users-list page-num))
        rows  (utils/get-rows table)
        users (map get-user rows)]
    (try (map queries/create-user! users) (catch Exception e (.getNextException e)))))

;(map queries/create-user! users)