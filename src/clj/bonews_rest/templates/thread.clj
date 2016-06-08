(ns bonews-rest.templates.thread
  (require [net.cgrand.enlive-html :as html]
           [clojure.string :as str]))

;(defn get-username
;  [reply users]
;  (let [author-id  (:user_id reply)
;        author     (first (filter #(= (:id %) author-id) users))]
;    (:username author)))
;
;(html/defsnippet reply-snippet "_reply.html" [:div.reply]
;  [reply users]
;  [:div.reply-title] (html/content (:title reply))
;  [:span.user] (html/content (get-username reply users))
;  [:div.reply-message] (html/html-content (:message reply)))
;
;(html/defsnippet thread-snippet "_thread.html" [:div.thread]
;  [thread]
;  [:ul [:li]]
;  (let [users (:users thread)
;        replies (:replies thread)]
;    (html/clone-for [reply replies]
;      [:li] (html/html-content (reduce str (html/emit* (reply-snippet reply users)))))))
;
;(html/deftemplate threads-page "home.html"
;  []
;  [:body] (html/html-content (reduce str (html/emit* (first (thread-snippet sample-thread))))))