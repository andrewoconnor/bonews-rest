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

(defn get-thread-num-replies
  [col]
  (Integer/parseInt (first (:content col))))

(defn get-thread-last-update
  [col]
  (first (:content col)))

(defn get-author-url
  [col]
  (let [title-node (html/select col [[:a (html/attr? :href)]])]
  (:href (:attrs (last title-node)))))

(defn get-thread-author
  [col]
  (let [title-node (html/select col [[:a (html/attr? :href)]])]
  (first (:content (last title-node)))))

(defn get-thread-title
  [col]
  (let [title-node (html/select col [:h4 [:a (html/attr? :href)]])]
  (first (:content (first title-node)))))

(defn get-thread-url
  [col]
  (let [title-node (html/select col [:h4 [:a (html/attr? :href)]])]
  (:href (:attrs (first title-node)))))

(defn get-cols
  [row]
  (let [cols (rest (html/select row [:td]))]
    {:thread-url            (get-thread-url (first cols))
     :thread-title          (get-thread-title (first cols))
     :thread-num-replies    (get-thread-num-replies (fnext cols))
     :thread-last-update    (get-thread-last-update (last cols))
     :author                (get-thread-author (first cols))
     :author-url            (get-author-url (first cols))}))

(defn get-rows
  [table]
  (map-indexed vector
    (for [row (rest (html/select table [:tr]))
      :let [cols (get-cols row)]
      :when (seq cols)]
    cols)))