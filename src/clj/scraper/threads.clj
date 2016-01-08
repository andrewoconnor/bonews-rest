(ns clj.scraper.threads
  (:require [clj.scraper.utils :as utils]
            [clj.scraper.replies :as br]
            [clj.scraper.bulbs :as bulbs]
            [net.cgrand.enlive-html :as html]
            [guangyin.core :as t]
            [guangyin.format :as f]
            [clojure.string :as str]
            [clj-webdriver.taxi :as web]
            [clj-webdriver.firefox :as ff]))

(def url-prefix "http://bo-ne.ws/forum/read.php?")

(def link-href [[:a (html/attr? :href)]])

(def link-label [:h4 [:a (html/attr? :href)]])

(def bo-time-formatter (f/date-time-formatter "MMMM dd, yyyy hh:mma"))

(def bo-ffprofile (doto (ff/new-profile)
                        (ff/set-preferences {"browser.migration.version" 9001 
                                             "permissions.default.image" 2})))

(defn get-thread-url
  "Construct URL for a single thread of a subforum"
  [subforum-id thread-id]
  (str url-prefix subforum-id "," thread-id))

(defn get-replies-table
  [thread-url]
  (-> thread-url
      (utils/fetch-url)
      (html/select [:div#phorum :table])
      last))

(defn get-reply-title
  [cols]
  (-> cols
      first
      (html/select link-label)
      last
      (:content)
      first))

(defn get-reply-url
  [cols]
  (-> cols
      first
      (html/select link-href)
      first
      (get-in [:attrs :href])))

(defn get-reply-id
  [reply-url]
  (-> reply-url
      (str/split #"\-")
      last
      (Integer/parseInt)))

(defn get-reply-post-time
  [cols]
  @(->  cols
        last
        (:content)
        first
        (t/local-date-time bo-time-formatter)))

(defn get-thread-id
  [reply-url]
  (-> reply-url
      (str/split #"\,")
      second
      (Integer/parseInt)))

(defn get-indent-level
  [cols]
  (-> cols
      first
      (html/select [:h4])
      first
      (get-in [:attrs :style])
      (->> (re-find #"padding-left: (\d+)px;"))
      last
      (Integer/parseInt)
      (/ 10)))

(defn get-parent-id
  [indent-level reply-levels]
  (if (= indent-level 0)
    nil
    (get reply-levels (dec indent-level))))

(defn get-parent-ids
  [indents reply-levels]
  (when (first indents)
    (let [indent        (first   indents)
          indent-level  (first   indent)
          reply-id      (second  indent)
          new-levels    (assoc   reply-levels indent-level reply-id)]
      (cons (get-parent-id indent-level reply-levels)
            (get-parent-ids (rest indents) new-levels)))))

(defn set-parent-id
  [reply parent-id]
  (-> reply
      (assoc :parent-id parent-id)
      (dissoc :indent)))

(defn rm-nil-parent-id
  [replies]
  (let [first-reply     (first replies)
        thread-replies  (rest  replies)]
    (cons (dissoc first-reply :parent-id) thread-replies)))

(defn set-parent-ids
  [replies parents]
  (when (first replies)
    (let [reply     (first replies)
          parent-id (first parents)]
      (cons (set-parent-id reply parent-id)
            (set-parent-ids (rest replies) (rest parents))))))

(defn combine-users
  [user bulbs]
  (if (some? (:users bulbs))
    (into (list user) (:users bulbs))
    (list user)))

(defn rm-nil-bulb
  [reply]
  (if (nil? (reply :bulbs))
    (dissoc reply :bulbs)
    reply))

(defn get-data-helper
  [rows]
  (for [row rows
    :let [cols         (utils/get-cols        row)
          reply-url    (get-reply-url         cols)
          reply-id     (get-reply-id          reply-url)
          reply-title  (get-reply-title       cols)
          post-time    (get-reply-post-time   cols)
          bulbs        (bulbs/get-data        reply-url)
          user-url     (utils/get-user-url    cols)
          user-id      (utils/get-user-id     user-url)
          username     (utils/get-username    cols)
          reply        {
            :id         reply-id
            :indent     [(get-indent-level cols) reply-id]
            :title      reply-title
            :url        reply-url
            :post-time  post-time
            :user       user-id
            :bulbs      (dissoc bulbs :users)
          }
          user         {
            :id         user-id
            :name       username
            :url        user-url
          }
          users        (combine-users user bulbs)]]
    [(rm-nil-bulb reply) users]))

(defn collate-users
  [tdata]
  (distinct (reduce into '() (map second tdata))))

(defn get-data-by-url
  [url]
  (web/set-driver! {:browser           :firefox
                    :profile           bo-ffprofile})
  (let [table      (get-replies-table  url)
        rows       (utils/get-rows     table)
        thread-id  (get-thread-id      url)
        tdata      (get-data-helper    rows)
        replies    (map first          tdata)
        indents    (map #(get % :indent) replies)
        parents    (get-parent-ids indents [])
        users      (collate-users      tdata)
        data       {
                      :thread {
                        :id       thread-id
                        :replies  (map :id replies)
                      }
                      :replies (rm-nil-parent-id (set-parent-ids replies parents))
                      :users   users
                    }]
    (web/close)
    data))

; (defn get-data-by-ids
;   [subforum-id thread-id]
;   (let [url    (get-thread-url     subforum-id thread-id)
;         table  (get-replies-table  url)
;         rows   (utils/get-rows     table)]
;     {
;       :thread   (get-thread-data   rows thread-id)
;       :replies  (get-replies-data  rows)
;       :users    (get-users-data    rows)
;     }))

(defn get-data
  ([url]
    (get-data-by-url url))
  ([subforum-id thread-id]
    nil))
    ; (get-data-by-ids subforum-id thread-id)))