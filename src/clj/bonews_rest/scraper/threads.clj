(ns bonews-rest.scraper.threads
  (:use [clj-webdriver.driver :only [init-driver]])
  (:require [bonews-rest.scraper.utils :as utils]
            [bonews-rest.scraper.replies :as replies]
            [bonews-rest.scraper.bulbs :as bulbs]
            [bonews-rest.queries :as queries]
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
  (utils/ldt-to-timestamp
    @(-> cols
         last
         (:content)
         first
         (date-time/local-date-time bo-time-formatter))))

(defn get-thread-id
  [reply-url]
  (-> reply-url
      (str/split #"\,")
      second
      (Integer/parseInt)))

(defn get-subforum-id
  [reply-url]
  (->> reply-url
       (re-find #"\?(\d+)\,")
       last
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
  {:id       user-id
   :username username
   :signature nil
   :nt_reply_url nil})

(defn get-reply
  [id title message time user-id parent]
  {:id      id
   :title   title
   :message message
   :post_time time
   :user_id    user-id
   :parent_id  parent})

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
        bulbs       (bulbs/get-data reply-url reply-id)
        users       (map #(merge % {:signature nil :nt_reply_url nil}) (:users bulbs))
        parent      nil
        reply       (get-reply reply-id title message time user-id parent)
        indent      (get-indent-level cols)
        parents     {indent (:id reply)}]
    {:replies (vec (list reply))
     :users   (set (into users (list user)))
     :bulbs   (vec (:bulbs bulbs))
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
    (assoc-in replies [:replies 0 :parent_id] parent)))

(defn get-thread-data
  [thread replies]
  (merge-with into thread (get-reply-with-parent thread replies)))

(defn get-data-by-url
  [url]
  (let [page        (utils/fetch-url url)
        table       (get-replies-table page)
        rows        (utils/get-rows table)
        subforum-id (get-subforum-id url)
        thread-id   (get-thread-id url)
        thread      (map get-thread-instance rows)
        tdata       (reduce get-thread-data {} thread)
        replies     (:replies tdata)]
    {
     :thread {:id          thread-id
              :subforum_id subforum-id
              :user_id     (:user_id (first replies))}
     :replies (:replies tdata)
     :bulbs   (:bulbs tdata)
     :users   (vec (:users tdata))
     }))

(defn format-reply
  [thread-data]
  (let [thread-id (:id (:thread thread-data))
        replies   (:replies thread-data)
        new-reps  (map #(assoc (dissoc % :bulbs) :thread_id thread-id) replies)]
    (vec new-reps)))

(defn insert-users
  [thread]
  (try
    (doall (map queries/create-user! (:users thread)))
    (catch Exception e (.getNextException e))))

(defn insert-thread
  [thread]
  (try
    (queries/create-thread! (:thread thread))
    (catch Exception e (.getNextException e))))

(defn insert-replies
  [thread]
  (try
    (doall (map queries/create-reply! (format-reply thread)))
    (catch Exception e (.getNextException e))))

(defn insert-bulbs
  [thread]
  (try
    (doall (map queries/create-bulb! (:bulbs thread)))
    (catch Exception e (.getNextException e))))

(defn insert-thread-into-db
  [thread]
  (do
    (insert-users thread)
    (insert-thread thread)
    (insert-replies thread)
    (insert-bulbs thread)))

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