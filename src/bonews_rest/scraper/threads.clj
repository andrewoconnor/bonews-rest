(ns bonews-rest.scraper.threads
  (:require [bonews-rest.scraper.utils :as utils]
            [bonews-rest.scraper.replies :as br]
            [bonews-rest.scraper.bulbs :as bulbs]
            [net.cgrand.enlive-html :as html]
            [guangyin.core :as t]
            [guangyin.format :as f]
            [clojure.string :as str]
            [clj-webdriver.taxi :as web]))

(def url-prefix "http://bo-ne.ws/forum/read.php?")

(def link-href [[:a (html/attr? :href)]])
(def link-label [:h4 [:a (html/attr? :href)]])

(def custom-formatter (f/date-time-formatter "MMMM dd, yyyy hh:mma"))

(defn get-thread-url
  "Construct URL for a single thread of a subforum"
  [subforum-id thread-id]
  (str url-prefix subforum-id "," thread-id))

(defn get-replies-table
  [thread-url]
  (-> thread-url
      (utils/fetch-url)
      (html/select [:div#phorum :table])
      last))

(defn get-reply-title
  [cols]
  (-> cols
      first
      (html/select link-label)
      last
      (:content)
      first))

(defn get-reply-url
  [cols]
  (-> cols
      first
      (html/select link-href)
      first
      (:attrs)
      (:href)))

(defn get-reply-id
  [reply-url]
  (-> reply-url
      (str/split #"\-")
      last
      (Integer/parseInt)))

(defn get-reply-post-time
  [cols]
  (-> cols
      last
      (:content)
      first
      (t/local-date-time custom-formatter)))

(defn get-thread-id
  [reply-url]
  (-> reply-url
      (str/split #"\,")
      second
      (Integer/parseInt)))

(defn get-thread-data
  [rows thread-id]
  {
    :id thread-id
    :replies 
      (seq 
        (for [row rows
          :let [cols        (utils/get-cols  row)
                reply-url   (get-reply-url   cols)
                replies     (get-reply-id    reply-url)]]
      replies))
  }
)

(defn get-replies-data
  [rows]
  (seq 
    (for [row rows
      :let [cols       (utils/get-cols      row)
            reply-url  (get-reply-url       cols)
            user-url   (utils/get-user-url  cols)
            replies    {
                        :id         (get-reply-id          reply-url)
                        :title      (get-reply-title       cols)
                        ; :body       (br/get-reply-data    reply-url)
                        :bulbs      (bulbs/get-data        reply-url)
                        :url        reply-url
                        :post-time  @(get-reply-post-time  cols)
                        :user       (utils/get-user-id     user-url)}]]
    replies)))

(defn get-users-data
  [rows]
  (for [row rows
    :let [cols       (utils/get-cols      row)
          reply-url  (get-reply-url       cols)
          user-url   (utils/get-user-url  cols)
          users      {
                      :id    (utils/get-user-id   user-url)
                      :name  (utils/get-username  cols)
                      :url   user-url}]]
  users))

(defn collate-users
  [rows replies]
  (distinct (flatten (conj (get-users-data rows)
    (for [reply replies
      :let [users (:users (:bulbs reply))]
      :when (contains? (:bulbs reply) :users)]
    users)))))

(defn get-data-by-ids
  [subforum-id thread-id]
  (let [url    (get-thread-url     subforum-id thread-id)
        table  (get-replies-table  url)
        rows   (utils/get-rows     table)]
    {
      :thread   (get-thread-data   rows thread-id)
      :replies  (get-replies-data  rows)
      :users    (get-users-data    rows)
    }))

(defn get-data-by-url
  [url]
  (let [table       (get-replies-table  url)
        rows        (utils/get-rows     table)
        thread-id   (get-thread-id      url)
        replies     (get-replies-data   rows)]
    {
      :thread   (get-thread-data   rows thread-id)
      :replies  replies
      :users    (collate-users     rows replies)
    }))

(defn rm-users-from-bulb
  [reply]
  (update-in reply [:bulbs] dissoc :users))

(defn clean-replies
  [replies]
  (utils/remove-nils (map rm-users-from-bulb replies)))

(defn clean-data
  [data]
  (let [replies          (:replies data)
        cleaned-replies  (clean-replies replies)]
  (assoc data :replies cleaned-replies)))

(defn get-data
  ([url]
    (clean-data (get-data-by-url url)))
  ([subforum-id thread-id]
    (get-data-by-ids subforum-id thread-id)))