(ns clj.scraper.threads
  (:use [clj-webdriver.driver :only [init-driver]])
  (:require [clj.scraper.utils :as utils]
            [clj.scraper.replies :as br]
            [clj.scraper.bulbs :as bulbs]
            [net.cgrand.enlive-html :as html]
            [guangyin.core :as t]
            [guangyin.format :as f]
            [clojure.string :as str]
            [clj-webdriver.taxi :as web]
            [clojure.set :refer [union]]
            ;[clj-webdriver.firefox :as ff]
            )
  (:import [org.openqa.selenium.phantomjs PhantomJSDriver]
           [org.openqa.selenium.remote DesiredCapabilities]))

(def url-prefix "http://bo-ne.ws/forum/read.php?")

(def link-href [[:a (html/attr? :href)]])

(def link-label [:h4 [:a (html/attr? :href)]])

(def bo-time-formatter (f/date-time-formatter "MMMM dd, yyyy hh:mma"))

;(def bo-ffprofile (doto (ff/new-profile)
;                        (ff/set-preferences {"browser.migration.version" 9001
;                                             "permissions.default.image" 2})))

(def phantomjs-path (System/getenv "PHANTOMJS_PATH"))

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

(defn rm-nil-bulb
  [reply]
  (if (nil? (:bulbs reply))
    (dissoc reply :bulbs)
    reply))

(defn rm-nil-parent-id
  [reply]
  (if (nil? (:parent-id reply))
    (dissoc reply :parent-id)
    reply))

(defn rm-nil-keys
  [reply]
  (-> reply
      (rm-nil-bulb)
      (rm-nil-parent-id)))

;(defn combine-users
;  [thread-data user bulb-users]
;  (let [users (:users thread-data)]
;    (-> users
;        (union (into #{} (list user)))
;        (union bulb-users))))
;
;(defn combine-replies
;  [thread-data reply]
;  (->> reply
;       (rm-nil-keys)
;       (list)
;       (into (:replies thread-data))))
;
;(defn update-thread-data
;  [thread-data replies users]
;  (-> thread-data
;      (assoc :replies replies)
;      (assoc :users   users)))

;(defn get-data-helper
;  [row]
;  (let [cols           (utils/get-cols       row)
;        reply-url      (get-reply-url        cols)
;        reply-id       (get-reply-id         reply-url)
;        reply-title    (get-reply-title      cols)
;        post-time      (get-reply-post-time  cols)
;        reply          {:id         reply-id}]))

(defn get-thread-data
  [row]
  (let [cols      (utils/get-cols row)
        reply-url (get-reply-url cols)]
    {:id      (get-reply-id reply-url)
     :title   (get-reply-title cols)
     :indent  (get-indent-level cols)
     :bulbs   (bulbs/get-data reply-url)}))
        ;
        ;reply-id     (get-reply-id reply-url)
        ;reply-title  (get-reply-title cols)
        ;bulbs        (bulbs/get-data reply-url)
        ;]


;(defn get-data-helper
;  [rows reply-parents thread-data]
;  (when (first rows)
;    (let [row            (first                rows)
;          cols           (utils/get-cols       row)
;          reply-url      (get-reply-url        cols)
;          reply-id       (get-reply-id         reply-url)
;          reply-title    (get-reply-title      cols)
;          post-time      (get-reply-post-time  cols)
;          bulbs          (bulbs/get-data       reply-url)
;          user-url       (utils/get-user-url   cols)
;          user-id        (utils/get-user-id    user-url)
;          username       (utils/get-username   cols)
;          indent-level   (get-indent-level     cols)
;          reply-parents  (assoc reply-parents indent-level reply-id)
;          parent-id      (get-parent-id indent-level reply-parents)
;          user           {:id         user-id
;                          :name       username
;                          :url        user-url}
;          bulb-users     (:users bulbs)
;          bulbs          (dissoc bulbs :users)
;          reply          {:id         reply-id
;                          :parent-id  parent-id
;                          :title      reply-title
;                          :url        reply-url
;                          :post-time  post-time
;                          :user-id    user-id
;                          :bulbs      bulbs}
;          replies        (combine-replies thread-data reply)
;          users          (combine-users thread-data user bulb-users)
;          thread-data    (update-thread-data thread-data replies users)]
;      (merge thread-data (get-data-helper (rest rows) reply-parents thread-data)))))


(defn get-and-rm-users
  [reply]
  (let [users  (get-in reply [:bulbs :users])
        reply1 (update-in reply [:bulbs] dissoc :users)]
    {:replies (vec (list reply1))
     :users   (set users)
     :parents [(:indent reply1)]}))

(defn get-data-by-url
  [url]
  ; {:browser :firefox :profile bo-ffprofile}

  (let [table        (get-replies-table url)
        rows         (utils/get-rows table)
        thread-id    (get-thread-id url)
        data         (map get-thread-data rows)
        data1        (map get-and-rm-users data)
        comb-data    (apply merge-with into data1)

        ;users        (reduce union (map #(get-in % [:bulbs :users]) data))
        ;replies      (map #(update-in % [:bulbs] dissoc :users) data)
        ;thread       {:id thread-id
        ;              :replies replies
        ;              :users users}
        ;LAST
        ;thread-data  (get-data-helper rows [] {:replies [] :users #{}})
        ;replies      (:replies thread-data)
        ;users        (:users   thread-data)
        ;data         {:thread {:id       thread-id
        ;                       :replies  (map :id replies)}
        ;              :replies replies
        ;              :users   users}
        ]

    ;(web/close)

    ;(assoc thread :users users)
    comb-data
    ; data
    ))

(defn set-web-driver
  []
  (web/set-driver!
    (init-driver
      {:webdriver
       (PhantomJSDriver.
         (doto (DesiredCapabilities.)
           (.setCapability "phantomjs.binary.path" phantomjs-path)
           (.setCapability "phantomjs.page.settings.loadImages" false)))})))

(defn get-data
  ([url]
    (set-web-driver)
    (let [starttime    (System/nanoTime)
          data         (get-data-by-url url)]
      (printf "Scrape completed in %ss.%n" (/ (- (System/nanoTime) starttime) 1e9))
      data))
  ([subforum-id thread-id]
    nil))