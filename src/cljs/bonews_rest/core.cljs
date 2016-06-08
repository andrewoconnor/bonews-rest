(ns bonews-rest.core
  (:require [rum.core :as rum]
            [bonews-rest.home :refer [my-comp]]))


;(reset! thread {"replies" [{"id" 1} {"id" 2} {"id" 3} {"id" 4}]})

;(defn mount! []
;  (rum/mount (my-comp thread) (.getElementById js/document "app")))
;
;(.addEventListener
;  js/window
;  "DOMContentLoaded"
;  mount!)