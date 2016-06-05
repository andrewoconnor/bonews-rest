(ns bonews-rest.routes.home
  (:use org.httpkit.server)
  (:require [bonews-rest.templates.thread :as thread]
            [bidi.ring :refer [make-handler resources]]
            [bidi.bidi :as bidi]
            [net.cgrand.enlive-html :as html]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [include-js include-css]]
            [rum.core :as rum]
            [ring.util.response :as res]
            [bonews-rest.home :refer [home-page items]]))


(reset! items (-> 10 range vec))

(def loading-page
  (html
    [:html
      [:head
        [:meta {:charset "utf-8"}]
        [:meta {:name "viewport"
                :content "width=device-width, initial-scale=1"}]]
      [:body
       [:div#app
        (rum/render-html home-page)]
        (include-js "js/app.js")]]))

(defn thread-handler
  [request]
  (res/response loading-page))

(def routes
  ["" [
       ["/thread.html" thread-handler]
       ["" (resources {:prefix "public"})]
       ]])

(def handler
    (make-handler routes))