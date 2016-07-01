(ns bonews-rest.home
  #?(:cljs (:require-macros [enfocus.macros :as em]))
  (:require
    #?@(:clj  [[net.cgrand.enlive-html :as html]
               [rum.core :as rum]
               [ajax.core :as ajax]
               [clojure.core.async :as async]
               [clojure.string :as str]
               [clj-time.core :as timec]
               [clj-time.format :as timef]]
        :cljs [[enfocus.core :as html]
               [rum.core :as rum]
               [ajax.core :as ajax]
               [cljs.core.async :as async]
               [clojure.string :as str]
               [cljs-time.core :as timec]
               [cljs-time.format :as timef]])))

(defonce thread (atom {}))

(rum/defc user []
  [:span])

(defn post-time
  [reply]
  (let [timestamp (get reply "post_time")]
    #?(:cljs (timef/unparse (timef/formatter "MMM dd YYYY, h:mm A")
                            (timef/parse (timef/formatters :date-time-no-ms)
                                         timestamp))
       :clj (timef/unparse (timef/formatter "MMM dd YYYY, h:mm aa")
                           (timef/parse (:date-time-no-ms timef/formatters)
                                        timestamp)))))

(rum/defc reply-item
  [reply user]
  [:li
   [:div.reply
    [:div.reply_header
     [:div.reply_header_left
      [:span.user (get user "username")]
      [:div.bulbs
       [:div.green_bulbs 61]
       [:div.red_bulbs 2]]]
     [:div.post_time (post-time reply)]]
    [:div.title
     [:span.title_text (get reply "title")]]
     [:div.message {:dangerouslySetInnerHTML {:__html (get reply "message")}}]]])

(rum/defc replies-list
  [state]
  (let [replies (get state "replies")
        users   (get state "users")
        bulbs   (get state "bulbs")]
    [:ul
     (for [reply replies
           :let [id   (get reply "id")
                 user (first (filter #(= (get % "id") (get reply "user_id")) users))]]
       (rum/with-key (reply-item reply user) id))]))

(rum/defc my-comp
  [state]
  [:div [:h2 "Welcome to serverside react"]
   (replies-list state)])

(defn- on-change-thread-state
  [_kwd _the-atom _old-state new-state]
  #?(:cljs (rum/request-render
             (rum/mount (my-comp new-state) (.getElementById js/document "app")))
     :clj nil))

(add-watch thread :main on-change-thread-state)

(defn get-thread []
  (ajax/GET "http://localhost:3000/api"
            {:handler (fn [response] (reset! thread response))}))

#?(:cljs (js/setInterval get-thread 5000))