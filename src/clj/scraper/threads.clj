(ns clj.scraper.threads
  (:use [clj-webdriver.driver :only [init-driver]])
  (:require [clj.scraper.utils :as utils]
            [clj.scraper.replies :as replies]
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

(def phantomjs-path (System/getenv "PHANTOMJS_PATH"))

(defn get-thread-url
  "Construct URL for a single thread of a subforum"
  [subforum-id thread-id]
  (str url-prefix subforum-id "," thread-id))

(defn get-replies-table
  [thread-url]
  (-> thread-url
      (utils/fetch-url)
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
        (t/local-date-time bo-time-formatter)))

(defn get-reply-user
  [cols]
  (let [user-url (utils/get-user-url cols)
        user-id  (utils/get-user-id user-url)
        username (utils/get-username cols)]
    (list
      {:id   user-id
       :name username
       :url  user-url})))

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

(defn get-reply-data
  [reply-url cols id bulbs]
  (let [user    (get-reply-user cols)
        user-id (get (first user) :id)
        reply   {:id      id
                 :title   (get-reply-title cols)
                 :message (replies/get-reply-data reply-url user-id)
                 :time    (get-reply-post-time cols)
                 :user    (:id (first user))}
        bulbs   (dissoc bulbs :users)]
    (if (empty? bulbs)
      {:reply reply
       :user  user}
      {:reply (assoc reply :bulbs bulbs)
       :user  user})))

(defn get-reply
  [row]
  (let [cols      (utils/get-cols row)
        reply-url (get-reply-url cols)
        id        (get-reply-id reply-url)
        indent    (get-indent-level cols)
        bulbs     (bulbs/get-data reply-url)
        rdata     (get-reply-data reply-url cols id bulbs)
        reply     (:reply rdata)
        user      (:user rdata)
        users     (into (:users bulbs) user)
        parents   {indent id}]
    {:replies (vec (list reply))
     :users   (set users)
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
  (let [table     (get-replies-table url)
        rows      (utils/get-rows table)
        thread-id (get-thread-id url)
        replies   (map get-reply rows)
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