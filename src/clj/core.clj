(ns clj.core
  (:gen-class)
  (:require [clj.alias :refer [load-config]]
            [clojure.java.io :as io]
            [joplin.repl :as repl
             :refer [migrate rollback seed reset create pending]]
            [ragtime.strategy :as strategy]))

(def config (load-config "joplin.edn"))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
