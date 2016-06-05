(ns bonews-rest.home
  (:require
    [rum.core :as rum]))


(defonce items (atom (vec nil)))

(rum/defc item-list [items]
          [:ul (repeat (count items) [:li "word up"])])

(rum/defc my-comp []
          [:div [:h2 "Welcome to serverside react"]
           (item-list @items)])

(def home-page (my-comp))