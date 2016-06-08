(ns bonews-rest.home
  (:require
    #?@(:clj  [[rum.core :as rum]
               [ajax.core :as ajax]
               [clojure.core.async :as async]]
        :cljs [[rum.core :as rum]
               [ajax.core :as ajax]
               [cljs.core.async :as async]])))

(defonce thread (atom {}))

(rum/defc user []
  [:span])

(rum/defc reply-item
  [reply user]
  [:li
   [:div.reply
    [:div.user (get user "username")]
    [:div.post_time (get reply "post_time")]
    [:div.title (get reply "title")]
    [:div.message (get reply "message")]]])

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