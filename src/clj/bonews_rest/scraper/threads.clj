(ns bonews-rest.scraper.threads
  (:use [clj-webdriver.driver :only [init-driver]])
  (:require [bonews-rest.scraper.utils :as utils]
            [bonews-rest.scraper.replies :as replies]
            [bonews-rest.scraper.bulbs :as bulbs]
            [net.cgrand.enlive-html :as html]
            [guangyin.core :as date-time]
            [guangyin.format :as fdt]
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

(def bo-time-formatter (fdt/date-time-formatter "MMMM dd, yyyy hh:mma"))

(def phantomjs-path (System/getenv "PHANTOMJS_PATH"))

(defn get-thread-url
  "Construct URL for a single thread of a subforum"
  [subforum-id thread-id]
  (str url-prefix subforum-id "," thread-id))

(defn get-replies-table
  [page]
  (-> page
      (html/select [:div#phorum :> :table.list])
      first))

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
        (date-time/local-date-time bo-time-formatter)))

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

(defn get-reply-user
  [user-id username]
  {:id   user-id
   :name username})

;(defn get-reply
;  [id title message time user-id bulbs parent]
;  {:id      id
;   :title   title
;   :message message
;   :time    time
;   :user    user-id
;   :bulbs   bulbs
;   :parent  parent})

(defn get-thread-instance
  [row]
  (let [cols        (utils/get-cols row)
        user-url    (utils/get-user-url cols)
        user-id     (utils/get-user-id user-url)
        username    (utils/get-username cols)
        user        (get-reply-user user-id username)
        reply-url   (get-reply-url cols)
        reply-id    (get-reply-id reply-url)
        title       (get-reply-title cols)
        message     (replies/get-reply-data reply-url user-id)
        time        (get-reply-post-time cols)
        bulbs-data  (bulbs/get-data reply-url)
        users       (:users bulbs-data)
        bulbs       (dissoc bulbs-data :users)
        parent      nil
        reply       (struct replies/reply reply-id title message time user-id bulbs parent)
        indent      (get-indent-level cols)
        parents     {indent (:id reply)}]
    {:replies (vec (list reply))
     :users   (set (into users (list user)))
     :parents parents}))

(defn get-parent-indent-level
  [replies]
  (dec (first (keys (:parents replies)))))

(defn get-parent
  [thread replies]
  (get (:parents thread) (get-parent-indent-level replies)))

(defn get-reply-with-parent
  [thread replies]
  (let [parent (get-parent thread replies)]
    (assoc-in replies [:replies 0 :parent] parent)))

(defn get-thread-data
  [thread replies]
  (merge-with into thread (get-reply-with-parent thread replies)))

(defn get-data-by-url
  [url]
  (let [page      (utils/fetch-url url)
        table     (get-replies-table page)
        rows      (utils/get-rows table)
        thread-id (get-thread-id url)
        replies   (map get-thread-instance rows)
        thread    (reduce get-thread-data {} replies)]
    thread))

(defn set-web-driver
  []
  (web/set-driver!
    (init-driver
      {:webdriver
       (PhantomJSDriver.
         (doto (DesiredCapabilities.)
           (.setCapability "phantomjs.binary.path" phantomjs-path)
           (.setCapability "phantomjs.page.settings.loadImages" false)
           (.setCapability "takesScreenshot" false)))})))

(defn get-data
  ([url]
    (set-web-driver)
    (let [starttime    (System/nanoTime)
          data         (get-data-by-url url)]
      (printf "Scrape completed in %ss.%n" (/ (- (System/nanoTime) starttime) 1e9))
      data))
  ([subforum-id thread-id]
    nil))