(ns bonews-rest.routes.home
  (:use org.httpkit.server)
  (:require [bonews-rest.templates.thread :as thread]
            [bidi.ring :refer (make-handler)]
            [ring.util.response :as res]))

(defn index-handler
  [request]
  (res/response "Homepage"))

(defn thread-handler
  [request]
  (res/response (reduce str (thread/thread-page thread/sample-thread))))

(def handler
  (make-handler ["/" {"index.html" index-handler
                      "thread.html" thread-handler}]))