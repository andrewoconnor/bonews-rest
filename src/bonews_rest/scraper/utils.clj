(ns bonews-rest.scraper.utils
  (:require [net.cgrand.enlive-html :as html]))

(defn fetch-url
  [url]
  (html/html-resource (java.net.URL. url)))

(defn get-rows
  [table]
  (for [tr (html/select table [:tr])
    :let [row (html/select tr [:td])]
    :when (seq row)]
  [tr row]))
