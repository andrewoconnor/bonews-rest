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


(defn get-thread-title
  [col]
  (html/select col [:h4 [:a (html/attr? :content)]]))

(defn get-thread-url
  [col]
  (let [title-node (html/select col [:h4 [:a (html/attr? :href)]])]
  (:href (first (rest (first (rest (first title-node))))))))

(defn get-cols
  [row]
  (let [cols (rest (html/select row [:td]))
        thread-title (get-thread-title (first cols))
        thread-url (get-thread-url (first cols))]
  {:url thread-url}))

(defn get-rows
  [table]
  (map-indexed vector
    (for [row (html/select table [:tr])
      :let [cols (get-cols row)]
      :when (seq cols)]
    cols)))