(ns clj.scraper.forums
  (:require [clj.scraper.utils :as utils]
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
      (get-in [:attrs :href])))

(defn get-subforum-id
  [subforum-url]
  (-> subforum-url
      (str/split #"\?")
      second
      Integer/parseInt))

(defn get-subforum-num-topics
  [cols]
  (-> cols
      fnext
      (:content)
      first
      (utils/parse-num-with-comma)))

(defn get-subforum-num-posts
  [cols]
  (-> cols
      (nth 2)
      (:content)
      first
      (utils/parse-num-with-comma)))

(defn get-subforum-last-update
  [cols]
  (-> cols
      (nth 3)
      (:content)
      first
      (t/local-date-time custom-formatter)))

(defn get-subforum-data
  [cols]
  (let [subforum-url (get-subforum-url cols)]
    {
      :subforum-name           (get-subforum-name cols)
      :subforum-url            subforum-url
      :subforum-id             (get-subforum-id subforum-url)
      :subforum-num-topics     (get-subforum-num-topics cols)
      :subforum-num-posts      (get-subforum-num-posts cols)
      :subforum-last-update    (get-subforum-last-update cols)
    }))