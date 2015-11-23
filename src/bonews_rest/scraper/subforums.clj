(ns bonews-rest.scraper.subforums
  (:require [bonews-rest.scraper.utils :as utils]
            [net.cgrand.enlive-html :as html]
            [guangyin.core :as t]
            [guangyin.format :as f]
            [clojure.string :as str]))

(def list-threads-url "http://bo-ne.ws/forum/list.php?")

(def bugs-subforum-id 25)
(def movies-subforum-id 14)

(def link-href [[:a (html/attr? :href)]])
(def link-label [:h4 [:a (html/attr? :href)]])

(def custom-formatter (f/date-time-formatter "MM/dd/yyyy hh:mma"))

(defn url-by-page
  "Construct URL for a single page of a subforum"
  [subforum-id page-num]
  (str list-threads-url subforum-id ",page=" page-num))

(defn get-threads-table
  [subforum page]
  (-> (url-by-page subforum page)
      (utils/fetch-url)
      (html/select [:div#phorum :table])))

(defn get-thread-num-replies
  [cols]
  (-> cols
      (nth 2)
      (:content)
      first
      (utils/parse-num-with-comma)))

(defn get-thread-last-update
  [cols]
  (-> cols
      last
      (:content)
      first
      (t/local-date-time custom-formatter)))

(defn get-thread-title
  [cols]
  (-> cols
      second
      (html/select link-label)
      first
      (:content)
      first))

(defn get-thread-url
  [cols]
  (-> cols
      second
      (html/select link-label)
      first
      (:attrs)
      (:href)))

(defn get-thread-suffix
  [thread-url]
  (-> thread-url
      (str/split #"\?")
      second
      (str/split #"\,")))

(defn get-thread-id
  [thread-suffix]
  (-> thread-suffix
      second
      Integer/parseInt))

(defn get-subforum-id
  [thread-suffix]
  (-> thread-suffix
      first
      Integer/parseInt))

(defn get-thread-data
  [cols]
  (let [thread-url    (get-thread-url cols)
        thread-suffix (get-thread-suffix thread-url)
        author-url    (utils/get-user-url cols)]
    {
      :thread-title          (get-thread-title cols)
      :thread-url            thread-url
      :thread-id             (get-thread-id thread-suffix)
      :thread-num-replies    (get-thread-num-replies cols)
      :thread-last-update    (get-thread-last-update cols)
      :subforum-id           (get-subforum-id thread-suffix)
      :author-name           (utils/get-username cols)
      :author-url            author-url
      :author-id             (utils/get-user-id author-url)
    }))