(ns bonews-rest.scraper.forums
  (:require [bonews-rest.scraper.utils :as utils]
            [net.cgrand.enlive-html :as html]))

(def bugs-subforum-url "http://bo-ne.ws/forum/list.php?25")

(defn url-by-page
  "Construct URL for a single page of a subforum"
  [subforum page]
  (str subforum ",page=" page))

(defn get-thread-table
  [subforum page]
  (-> (url-by-page subforum page)
      (utils/fetch-url)
      (html/select [:div#phorum :table])))

(defn get-thread-rows
  "Returns all rows in the threads table (except the header row)"
  [table]
  (utils/get-rows table))

(defn get-thread-columns
  "Returns all columns in the thread row (except the first spacer column)"
  [rows]
  ())
