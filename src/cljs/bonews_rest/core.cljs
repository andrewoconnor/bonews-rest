(ns bonews-rest.core
  (:require [rum.core :as rum]
            [bonews-rest.home :refer [items my-comp]]))


(defn mount! []
  (reset! items (-> 10 range vec))
  (rum/mount (my-comp) (.getElementById js/document "app")))

(.addEventListener
  js/window
  "DOMContentLoaded"
  mount!)