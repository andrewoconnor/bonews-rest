(ns bonews-rest.scraper.utils
  (:require [net.cgrand.enlive-html :as html]))

(defn fetch-url
  [url]
  (html/html-resource (java.net.URL. url)))

(defn get-rows
  [table]
  (-> table
      (html/select [:tr])
      rest))

(defn get-cols
  [row]
  (-> row
      (html/select [:td])))

(defn get-data
  [get-table type-of-data]
  (map-indexed vector
    (for [row (get-rows get-table)
      :let [data (-> row
                    (get-cols)
                    (type-of-data))]
      :when (seq data)]
    data)))