(ns bonews-rest.routes.home
  (:use org.httpkit.server)
  (:require [bonews-rest.home :refer [my-comp thread]]
            [bidi.ring :refer [make-handler resources-maybe]]
            [bidi.bidi :as bidi]
            [ring.util.response :refer [response content-type]]
            [net.cgrand.enlive-html :as html]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [include-js include-css]]
            [rum.core :as rum]
            [ring.util.response :as res]
            [cheshire.core :refer :all]))

(def sample-thread
  (read-string
    (slurp "resources/sample_thread.edn")))

(reset! thread (parse-string (generate-string sample-thread)))

(def loading-page
  (html
    [:html
     [:head
      [:meta {:charset "utf-8"}]
      [:meta {:name "viewport"
              :content "width=device-width, initial-scale=1"}]
      (include-css "/app.css")]
     [:body
      [:div#app
       (rum/render-html (my-comp @thread))]
      (include-js "/js/app.js")]]))

(def not-found-page
  (html
    [:html
     [:head
      [:meta {:charset "utf-8"}]
      [:meta {:name "viewport"
              :content "width=device-width, initial-scale=1"}]]
     [:body
      [:h3 "Well this is embarrassing, this page does not exist..."]]]))

(defn thread-handler
  [request]
  (res/response loading-page))

(defn not-found-handler
  [request]
  {:status 404 :body not-found-page})

(defn api-handler
  [request]
  (content-type (response (generate-string sample-thread)) "application/json"))

(def routes
  ["/" [
        ["api" api-handler]
        [["threads/" [#"\d+" :id]] thread-handler]
        ["" (resources-maybe {:prefix "public/"})]
        [true not-found-handler]
        ]])

(def handler
  (make-handler routes))