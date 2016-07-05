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
(defonce selected-reply (atom int))

(defn get-user
  [obj users]
  (first (filter #(= (get % "id") (get obj "user_id")) users)))

(defn get-votes
  [reply bulbs]
  (let [votes (filter #(= (get % "reply_id") (get reply "id")) bulbs)
        upvotes (filter #(= (get % "vote") 1) votes)
        downvotes (filter #(= (get % "vote") -1) votes)]
    {:upvotes upvotes
     :downvotes downvotes}))

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
  [reply users bulbs]
  (let [user (get-user reply users)
        votes (get-votes reply bulbs)]
    [:li
     [:div.reply
      [:div.reply_header
       [:div.reply_header_left
        [:span.user (get user "username")]
        (if (pos? (count votes))
          [:div.bulbs
           (if (pos? (count (:upvotes votes)))
             [:div.green_bulbs (count (:upvotes votes))]
             nil)
           (if (pos? (count (:downvotes votes)))
             [:div.red_bulbs (count (:downvotes votes))]
             nil)]
          nil)]
       [:div.post_time (post-time reply)]]
      [:div.title (get reply "title")]
      (if (str/blank? (get reply "message"))
        nil
        [:div.message {:dangerouslySetInnerHTML {:__html (get reply "message")}}])]]))

(rum/defc replies-list
  [state]
  (let [replies (get state "replies")
        users   (get state "users")
        bulbs   (get state "bulbs")]
    [:ul
     (for [reply replies
           :let [id   (get reply "id")]]
       (rum/with-key (reply-item reply users bulbs) id))]))

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