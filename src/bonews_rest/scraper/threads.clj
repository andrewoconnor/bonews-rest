(ns bonews-rest.scraper.threads
  (:require [bonews-rest.scraper.utils :as utils]
            [net.cgrand.enlive-html :as html]
            [guangyin.core :as t]
            [guangyin.format :as f]
            [clojure.string :as str]))

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
                        :id         (get-reply-id         reply-url)
                        :title      (get-reply-title      cols)
                        :url        reply-url
                        :post-time  (get-reply-post-time  cols)
                        :user       (utils/get-user-id    user-url)}]]
    replies)))

(defn get-users-data
  [rows]
  (distinct 
    (seq
      (for [row rows
        :let [cols       (utils/get-cols      row)
              reply-url  (get-reply-url       cols)
              user-url   (utils/get-user-url  cols)
              users      {
                          :id    (utils/get-user-id   user-url)
                          :name  (utils/get-username  cols)
                          :url   user-url}]]
      users))))

(defn get-data
  ([url]
    (let [table      (get-replies-table  url)
          rows       (utils/get-rows     table)
          thread-id  (get-thread-id      url)]
    {
      :thread   (get-thread-data   rows thread-id)
      :replies  (get-replies-data  rows)
      :users    (get-users-data    rows)
    }))
  ([subforum-id thread-id]
    (let [url    (get-thread-url     subforum-id thread-id)
          table  (get-replies-table  url)
          rows   (utils/get-rows     table)]
    {
      :thread   (get-thread-data   rows thread-id)
      :replies  (get-replies-data  rows)
      :users    (get-users-data    rows)
    })))