(ns bonews-rest.scraper.utils
  (:require [net.cgrand.enlive-html :as html]))

(defn fetch-url
  [url]
  (html/html-resource (java.net.URL. url)))