(ns bonews-rest.scraper.forums
  (:require [bonews-rest.scraper.utils :as utils]
            [net.cgrand.enlive-html :as html]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clojure.string :as str]))

(def list-forums-url "http://bo-ne.ws/forum/index.php")

(def link-label [:h3 [:a (html/attr? :href)]])

(def custom-formatter (f/formatter "MMM dd, yyyy hh:mmaa"))

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
      (:attrs)
      (:href)))

(defn get-subforum-id
  [cols]
  (-> cols
      (get-subforum-url)
      (str/split #"\?")
      second
      Integer/parseInt))

(defn get-subforum-num-topics
  [cols]
  (-> cols
      fnext
      (:content)
      first
      (str/trim)
      (str/replace "," "")
      Integer/parseInt))

(defn get-subforum-num-posts
  [cols]
  (-> (nth cols 2)
      (:content)
      first
      (str/trim)
      (str/replace "," "")
      Integer/parseInt))

(defn get-subforum-last-update
  [cols]
  (->>  (nth cols 3)
        (:content)
        first
        (f/parse custom-formatter)))

(defn get-rows
  [table]
  (-> table
      (html/select [:tr])
      rest))

(defn get-cols
  [row]
  (-> row
      (html/select [:td])))

(defn get-subforum-data
  [cols]
  {
    :subforum-name         (get-subforum-name cols)
    :subforum-url          (get-subforum-url cols)
    :subforum-id           (get-subforum-id cols)
    :subforum-num-topics   (get-subforum-num-topics cols)
    :subforum-num-posts    (get-subforum-num-posts cols)
    :subforum-last-update  (get-subforum-last-update cols)
  })

(defn get-subforums-data
  [table]
  (map-indexed vector
    (for [row (get-rows table)
      :let [subforums-data (-> row
                              (get-cols)
                              (get-subforum-data))]
      :when (seq subforums-data)]
    subforums-data)))

; (def bugs-subforum-id 25)
; (def movies-subforum-id 14)

; (def link-href [[:a (html/attr? :href)]])
; (def link-label [:h4 [:a (html/attr? :href)]])

; (def custom-formatter (f/formatter "MM/dd/yyyy hh:mmaa"))

; (defn url-by-page
;   "Construct URL for a single page of a subforum"
;   [subforum-id page]
;   (str list-threads-url subforum-id ",page=" page))

; (defn get-threads-table
;   [subforum page]
;   (-> (url-by-page subforum page)
;       (utils/fetch-url)
;       (html/select [:div#phorum :table])))

; (defn get-thread-num-replies
;   [cols]
;   (-> cols
;       fnext
;       (:content)
;       first
;       Integer/parseInt))

; (defn get-thread-last-update
;   [cols]
;   (->> cols
;        last
;        (:content)
;        first
;        (f/parse custom-formatter)))

; (defn get-author-url
;   [cols]
;   (-> cols
;       first
;       (html/select link-href)
;       last
;       (:attrs)
;       (:href)))

; (defn get-thread-author
;   [cols]
;   (-> cols
;       first
;       (html/select link-href)
;       last
;       (:content)
;       first))

; (defn get-thread-title
;   [cols]
;   (-> cols
;       first
;       (html/select link-label)
;       first
;       (:content)
;       first))

; (defn get-thread-url
;   [cols]
;   (-> cols
;       first
;       (html/select link-label)
;       first
;       (:attrs)
;       (:href)))

; (defn get-rows
;   [table]
;   (-> table
;       (html/select [:tr])
;       rest))

; (defn get-cols
;   [row]
;   (-> row
;       (html/select [:td])
;       rest))

; (defn get-thread-data
;   [cols]
;   {
;     :thread-url            (get-thread-url cols)
;     :thread-title          (get-thread-title cols)
;     :thread-num-replies    (get-thread-num-replies cols)
;     :thread-last-update    (get-thread-last-update cols)
;     :author                (get-thread-author cols)
;     :author-url            (get-author-url cols)
;   })

; (defn get-threads-data
;   [table]
;   (map-indexed vector
;     (for [row (get-rows table)
;       :let [page-data (-> row
;                           (get-cols)
;                           (get-thread-data))]
;       :when (seq page-data)]
;     page-data)))