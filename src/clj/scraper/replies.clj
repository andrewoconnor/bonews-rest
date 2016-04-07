(ns clj.scraper.replies
  (:require [clj.scraper.utils :as utils]
            [clj.scraper.signatures :as sig]
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

(defn strip-msg-body-tags
  [signature]
  (let [ret (re-find #"(?s)<div class=\"message-body\">(.*)</div>" signature)]
    (if (nil? ret)
      signature
      (last ret))))

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

(defn custom-trim
  [my-str]
  (str/replace my-str #"(\s\s)+" ""))

(defn get-reply-data
  [reply-url user-id]
  (let [reply-div  (get-reply-div reply-url)
        msg        (str/trim (strip-msg-body-tags (apply str (html/emit* (get-reply-message reply-div)))))
        signature  (sig/get-signature user-id)]
    (str/replace msg signature "")))