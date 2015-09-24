(ns bonews-rest.scraper.utils
  (:require [net.cgrand.enlive-html :as html]))

(defn fetch-url
  [url]
  (html/html-resource (java.net.URL. url)))

(defn get-rows
  [table]
  (map-indexed vector
    (for [tr (html/select table [:tr])
      :let [cols (rest (html/select tr [:td]))]
      :when (seq cols)]
    [cols])))
