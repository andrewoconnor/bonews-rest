(ns clj.scraper.replies
  (:require [clj.scraper.utils :as utils]
            [clj.scraper.signatures :as sig]
            [net.cgrand.enlive-html :as html]
            [guangyin.core :as t]
            [guangyin.format :as f]
            [clojure.string :as str]))

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

(defn strip-msg-body-tags
  [signature]
  (let [ret (re-find #"(?s)<div class=\"message-body\">(.*)</div>" signature)]
    (if (nil? ret)
      signature
      (last ret))))

(defn strip-no-text
  [signature]
  (let [ret (re-find #"(?s)(No text.)(<br />\s?)+(.*)" signature)]
    (if (nil? ret)
      signature
      (last ret))))

(defn get-message
  [reply-div]
  (-> reply-div
      (get-reply-message)
      (html/emit*)
      (->> (apply str))
      (strip-msg-body-tags)
      (strip-no-text)
      (str/trim)))

(defn strip-breaks
  [msg]
  (let [ret (re-find #"^(?:<br />\n?\s?)*(.*)(?:<br />\n?\s?)*$" msg)]
    (if (nil? ret)
      msg
      (last ret))))

(defn get-reply-data
  [reply-url user-id]
  (let [reply-div  (get-reply-div reply-url)
        msg        (get-message reply-div)
        signature  (sig/get-signature user-id)]
    (strip-breaks (str/replace msg signature ""))))