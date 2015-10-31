(ns bonews-rest.scraper.forums
  (:require [bonews-rest.scraper.utils :as utils]
            [net.cgrand.enlive-html :as html]))

(def bugs-subforum-url "http://bo-ne.ws/forum/list.php?25")
(def link-href [[:a (html/attr? :href)]])
(def link-label [:h4 [:a (html/attr? :href)]])

(defn url-by-page
  "Construct URL for a single page of a subforum"
  [subforum page]
  (str subforum ",page=" page))

(defn get-thread-table
  [subforum page]
  (-> (url-by-page subforum page)
      (utils/fetch-url)
      (html/select [:div#phorum :table])))

(defn get-thread-num-replies
  [col]
  (-> col
      fnext
      (:content)
      first
      Integer/parseInt))

(defn get-thread-last-update
  [col]
  (-> col
      last
      (:content)
      first))

(defn get-author-url
  [col]
  (-> col
      first
      (html/select link-href)
      last
      (:attrs)
      (:href)))

(defn get-thread-author
  [col]
  (-> col
      first
      (html/select link-href)
      last
      (:content)
      first))

(defn get-thread-title
  [col]
  (-> col
      first
      (html/select link-label)
      first
      (:content)
      first))

(defn get-thread-url
  [col]
  (-> col
      first
      (html/select link-label)
      first
      (:attrs)
      (:href)))

(defn get-rows
  [table]
  (-> table
      (html/select [:tr])
      rest))

(defn get-cols
  [row]
  (-> row
      (html/select [:td])
      rest))

(defn get-thread-data
  [cols]
  {
    :thread-url            (get-thread-url cols)
    :thread-title          (get-thread-title cols)
    :thread-num-replies    (get-thread-num-replies cols)
    :thread-last-update    (get-thread-last-update cols)
    :author                (get-thread-author cols)
    :author-url            (get-author-url cols)
  })

(defn get-page-data
  [table]
  (map-indexed vector
    (for [row (get-rows table)
      :let [page-data (-> row
                          (get-cols)
                          (get-thread-data))]
      :when (seq page-data)]
    page-data)))