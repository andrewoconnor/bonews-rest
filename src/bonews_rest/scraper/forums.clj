(ns bonews-rest.scraper.forums
  (:require [bonews-rest.scraper.utils :as utils]
            [net.cgrand.enlive-html :as html]
            [guangyin.core :as t]
            [guangyin.format :as f]
            [clojure.string :as str]))

(def list-forums-url "http://bo-ne.ws/forum/index.php")

(def link-label [:h3 [:a (html/attr? :href)]])

(def custom-formatter (f/date-time-formatter "MMMM dd, yyyy hh:mma"))

(defn get-subforums-table
  []
  (-> list-forums-url
      (utils/fetch-url)
      (html/select [:div#phorum :table])
      first))

(defn get-subforum-name
  [cols]
  (-> cols
      first
      (html/select link-label)
      first
      (:content)
      first))

(defn get-subforum-url
  [cols]
  (-> cols
      first
      (html/select link-label)
      first
      (:attrs)
      (:href)))

(defn get-subforum-id
  [cols]
  (-> cols
      (get-subforum-url)
      (str/split #"\?")
      second
      Integer/parseInt))

(defn get-subforum-num-topics
  [cols]
  (-> cols
      fnext
      (:content)
      first
      (str/trim)
      (str/replace "," "")
      Integer/parseInt))

(defn get-subforum-num-posts
  [cols]
  (-> (nth cols 2)
      (:content)
      first
      (str/trim)
      (str/replace "," "")
      Integer/parseInt))

(defn get-subforum-last-update
  [cols]
  (-> (nth cols 3)
      (:content)
      first
      (t/local-date-time custom-formatter)))

(defn get-rows
  [table]
  (-> table
      (html/select [:tr])
      rest))

(defn get-cols
  [row]
  (-> row
      (html/select [:td])))

(defn get-subforum-data
  [cols]
  {
    :subforum-name         (get-subforum-name cols)
    :subforum-url          (get-subforum-url cols)
    :subforum-id           (get-subforum-id cols)
    :subforum-num-topics   (get-subforum-num-topics cols)
    :subforum-num-posts    (get-subforum-num-posts cols)
    :subforum-last-update  (get-subforum-last-update cols)
  })

(defn get-subforums-data
  [table]
  (map-indexed vector
    (for [row (get-rows table)
      :let [subforums-data (-> row
                              (get-cols)
                              (get-subforum-data))]
      :when (seq subforums-data)]
    subforums-data)))