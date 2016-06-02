(ns bonews-rest.scraper.replies
  (:require [bonews-rest.scraper.utils :as utils]
            [bonews-rest.scraper.signatures :as sig]
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
  (let [ret (re-find #"^(?s)(?:<br />\n?\s?)*(.*?)(?:<br />\n?\s?)*$" msg)]
    (if (nil? ret)
      msg
      (last ret))))

(defn stripper
  [sig msg]
  (let [rows (if (nil? sig) nil (str/split-lines sig))]
    (if (> (count rows) 1)
      (let [prefix  (str/trim (first rows))
            suffix  (str/trim (last rows))
            matcher (re-pattern (str "(?s)" prefix "(.*)" suffix))
            ret     (if (nil? msg) nil (re-find matcher msg))]
        (if (nil? ret)
          msg
          (str/replace msg (first ret) "")))
      msg)))

(defn strip-edited-x-times
  [msg]
  (let [ret (re-find #"^(?s)(.*)Edited \d+ time\(s\)\. (?:.*)\." msg)]
    (if (nil? ret)
      msg
      (last ret))))

(defn strip-unmatched-html-tags
  [signature]
  (apply str (html/emit* (html/html-snippet signature))))

(defn get-reply-data
  [reply-url user-id]
  (let [reply-div  (get-reply-div reply-url)
        msg        (get-message reply-div)
        message    (strip-unmatched-html-tags (strip-breaks (strip-edited-x-times msg)))
        signature  (sig/get-signature-by-id user-id)
        multi-strip (strip-breaks (stripper signature message))]
    (if (and (= (count multi-strip) (count message)) signature)
      (strip-breaks (str/replace message (or signature "")  ""))
      multi-strip)))