(ns bonews-rest.core
  (:require [rum.core :as rum]
            [bonews-rest.home :refer [items my-comp]]))


(defn mount! []
  (rum/mount (my-comp) (.getElementById js/document "app")))

(defn init! []
  (reset! items (-> 10 range vec))
  (mount!))

(.addEventListener
  js/window
  "DOMContentLoaded"
  init!)