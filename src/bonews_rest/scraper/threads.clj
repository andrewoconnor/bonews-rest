(ns bonews-rest.scraper.threads
  (:require [bonews-rest.scraper.utils :as utils]
            [net.cgrand.enlive-html :as html]))

(def movies-subforum-url "http://bo-ne.ws/forum/read.php?14")

(defn get-thread-url
  "Construct URL for a single thread of a subforum"
  [subforum thread-id]
  (str subforum "," thread-id))

(defn get-threads-table
  [thread-url]
  (-> thread-url
      (utils/fetch-url)
      (html/select [:div#phorum :table])))