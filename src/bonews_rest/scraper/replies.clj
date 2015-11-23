(ns bonews-rest.scraper.replies
  (:require [bonews-rest.scraper.utils :as utils]
            [net.cgrand.enlive-html :as html]
            [guangyin.core :as t]
            [guangyin.format :as f]
            [clojure.string :as str]))

(def link-href [[:a (html/attr? :href)]])

(defn get-reply-div
  [reply-url]
  (-> reply-url
      (utils/fetch-url)
      (html/select [:div.message-body])))

(defn get-reply-message
  [reply-div]
  (-> reply-div
      first
      (html/at [:form] nil)
      first
      (html/at [:div.message-options] nil)
      last))

(defn get-num-votes
  [vote-col]
  (-> vote-col
      first
      (:content)
      first
      (str/replace "\t" "")
      (str/replace "\n" "")
      Integer/parseInt))

(defn get-downvotes-col
  [reply-div]
  (-> reply-div
      first
      (html/select [:td#bo_knows_bulbs_vote_down])))

(defn get-upvotes-col
  [reply-div]
  (-> reply-div
      first
      (html/select [:td#bo_knows_bulbs_vote_up])))

(defn get-reply-data
  [reply-url]
  (let [reply-div  (get-reply-div reply-url)
        upvotes    (get-upvotes-col reply-div)
        downvotes  (get-downvotes-col reply-div)]
  (get-reply-message reply-div)))
  ; {
    ; :reply-num-upvotes      (get-num-votes upvotes)
    ; :reply-num-downvotes    (get-num-votes downvotes)
  ; }))