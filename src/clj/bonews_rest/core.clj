(ns bonews-rest.core
  (:gen-class)
  (:use org.httpkit.server)
  (:require [bonews-rest.alias :refer [load-config]]
            [bonews-rest.routes.home :as routes]
            [clojure.java.io :as io]
            [joplin.repl :as repl
             :refer [migrate rollback seed reset create pending]]
            [ragtime.strategy :as strategy]))

(def config (load-config "joplin.edn"))

(defonce server (atom nil))

(defn stop-server
  []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil)))

(defn start-server
  []
  (reset! server (run-server routes/handler {:port 3000})))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
