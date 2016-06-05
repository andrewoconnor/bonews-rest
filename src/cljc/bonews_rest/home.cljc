(ns bonews-rest.home
  (:require
    [rum.core :as rum]))


(defonce items (atom (vec nil)))

(rum/defc item [i] [:li i])

(rum/defc item-list [items]
          [:ul (for [x (range (count items))]
                 (rum/with-key (item x) x))])

(rum/defc my-comp []
          [:div [:h2 "Welcome to serverside react"]
           (item-list @items)])

(def home-page (my-comp))