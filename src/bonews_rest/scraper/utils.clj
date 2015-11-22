(ns bonews-rest.scraper.utils
  (:require [net.cgrand.enlive-html :as html]
            [clojure.string :as str]))

(def link-href [[:a (html/attr? :href)]])
(def link-label [:h4 [:a (html/attr? :href)]])

(defn fetch-url
  [url]
  (html/html-resource (java.net.URL. url)))

(defn parse-page-source
  [page-source]
  (html/html-resource (java.io.StringReader. page-source)))

(defn parse-num-with-comma
  [num-with-comma]
  (-> num-with-comma
      (str/trim)
      (str/replace "," "")
      (Integer/parseInt)))

(defn get-username
  [cols]
  (-> cols
      second
      (html/select link-href)
      last
      (:content)
      first))

(defn get-user-url
  [cols]
  (-> cols
      second
      (html/select link-href)
      last
      (:attrs)
      (:href)))

(defn get-user-id
  [user-url]
  (-> user-url
      (str/split #"\,")
      second
      (Integer/parseInt)))

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
  [table type-of-data]
  (seq
    (for [row (get-rows table)
      :let [data (-> row
                    (get-cols)
                    (type-of-data))]
      :when (seq data)]
    data)))