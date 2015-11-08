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
      last))

(defn get-reply-post-time
  [cols]
  (-> cols
      last
      (:content)
      first
      (t/local-date-time custom-formatter)))

(defn get-replies-data
  [cols]
  (let [reply-url (get-reply-url cols)]
    {
      :reply-title           (get-reply-title cols)
      :reply-url             reply-url
      :reply-id              (get-reply-id reply-url)
      :reply-post-time       (get-reply-post-time cols)
      :author-name           (utils/get-author-name cols)
      :author-url            (utils/get-author-url cols)
    }))