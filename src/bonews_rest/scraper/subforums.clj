(ns bonews-rest.scraper.subforums
  (:require [bonews-rest.scraper.utils :as utils]
            [net.cgrand.enlive-html :as html]
            [guangyin.core :as t]
            [guangyin.format :as f]))

(def list-threads-url "http://bo-ne.ws/forum/list.php?")

(def bugs-subforum-id 25)
(def movies-subforum-id 14)

(def link-href [[:a (html/attr? :href)]])
(def link-label [:h4 [:a (html/attr? :href)]])

(def custom-formatter (f/date-time-formatter "MM/dd/yyyy hh:mma"))

(defn url-by-page
  "Construct URL for a single page of a subforum"
  [subforum-id page]
  (str list-threads-url subforum-id ",page=" page))

(defn get-threads-table
  [subforum page]
  (-> (url-by-page subforum page)
      (utils/fetch-url)
      (html/select [:div#phorum :table])))

(defn get-thread-num-replies
  [cols]
  (-> cols
      fnext
      (:content)
      first
      Integer/parseInt))

(defn get-thread-last-update
  [cols]
  (-> cols
       last
       (:content)
       first
       (t/local-date-time custom-formatter)))

(defn get-author-url
  [cols]
  (-> cols
      first
      (html/select link-href)
      last
      (:attrs)
      (:href)))

(defn get-thread-author
  [cols]
  (-> cols
      first
      (html/select link-href)
      last
      (:content)
      first))

(defn get-thread-title
  [cols]
  (-> cols
      first
      (html/select link-label)
      first
      (:content)
      first))

(defn get-thread-url
  [cols]
  (-> cols
      first
      (html/select link-label)
      first
      (:attrs)
      (:href)))

(defn get-rows
  [table]
  (-> table
      (html/select [:tr])
      rest))

(defn get-cols
  [row]
  (-> row
      (html/select [:td])
      rest))

(defn get-thread-data
  [cols]
  {
    :thread-url            (get-thread-url cols)
    :thread-title          (get-thread-title cols)
    :thread-num-replies    (get-thread-num-replies cols)
    :thread-last-update    (get-thread-last-update cols)
    :author                (get-thread-author cols)
    :author-url            (get-author-url cols)
  })

(defn get-threads-data
  [table]
  (map-indexed vector
    (for [row (get-rows table)
      :let [threads-data (-> row
                          (get-cols)
                          (get-thread-data))]
      :when (seq threads-data)]
    threads-data)))