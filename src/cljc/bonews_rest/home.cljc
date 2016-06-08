(ns bonews-rest.home
  (:require
    #?@(:clj [[rum.core :as rum]
              [ajax.core :as ajax]
              [clojure.core.async :as async]]
        :cljs [[rum.core :as rum]
               [ajax.core :as ajax]
               [cljs.core.async :as async]])))

(defonce thread (atom {}))

(rum/defc user []
  [:span])

(rum/defc reply-item
  [reply]
  [:li
   [:div
    [:span (get reply "id")]]])

(rum/defc replies-list
  [replies]
  [:ul
   (for [reply replies]
     (rum/with-key (reply-item reply) (get reply "id")))])

(rum/defc my-comp < rum/static
  [state]
  [:div [:h2 "Welcome to serverside react"]
   (replies-list (get state "replies"))])

(def home-page (my-comp @thread))

(defn- on-change-thread-state
  [_kwd _the-atom _old-state new-state]
  #?(:cljs (rum/request-render
             (rum/mount (my-comp new-state) (.getElementById js/document "app")))
     :clj nil))

(add-watch thread :main on-change-thread-state)