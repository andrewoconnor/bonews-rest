(ns clj.scraper.signatures
  (:require [clj.scraper.utils :as utils]
            [net.cgrand.enlive-html :as html]
            [clj-webdriver.taxi :as web]
            [clojure.string :as str]))

(def search-url-prefix "http://bo-ne.ws/forum/search.php?0,author=")
(def search-url-page-num ",page=")
(def search-url-suffix ",match_type=USER_ID,match_dates=0,match_threads=0")

(defn get-reply-div
  [reply-url]
  (-> reply-url
      (utils/fetch-url)
      (html/select [:div.message-body])))

(defn get-reply-message
  [reply-url]
  (-> reply-url
      (get-reply-div)
      first
      (html/at [:form] nil)
      first
      (html/at [:div.message-options] nil)
      last))

(defn get-posts-page-by-user
  [user-id page-num]
  (-> (str search-url-prefix user-id search-url-page-num page-num search-url-suffix)
      (utils/fetch-url)))

(defn get-reply-link
  [reply]
  (html/select reply utils/link-label))

(defn get-reply-url
  [reply-link]
  (get-in (first reply-link) [:attrs :href]))

(defn get-reply
  [replies-results-page]
  (html/select replies-results-page [:div.search-result]))

(defn not-nt-reply?
  [reply]
  (let [msg (-> reply (html/select [:blockquote]) first :content first)]
    (not= msg " n/t")))

(defn get-no-text-reply
  [user-id page-num]
  (let [replies-results-page (get-posts-page-by-user user-id page-num)
        replies              (get-reply replies-results-page)
        no-text-reply        (drop-while not-nt-reply? replies)]
    (if (first no-text-reply)
      (get-reply-url (get-reply-link (first no-text-reply)))
      (recur user-id (inc page-num)))))

(defn strip-msg-body-tags
  [signature]
  (let [ret (re-find #"(?s)<div class=\"message-body\">(.*)</div>" signature)]
    (if (nil? ret)
      signature
      (last ret))))

(defn strip-unmatched-html-tags
  [signature]
  (apply str (html/emit* (html/html-snippet signature))))

(defn strip-no-text
  [signature]
  (let [ret (re-find #"(?s)No text.<br />\s?+(.*)" signature)]
    (if (nil? ret)
      signature
      (last ret))))

(defn strip-breaks
  [signature]
  (let [ret (re-find #"^(?s)(?:<br />\n?\s?)*(.*)(?:<br />\n?\s?)*$" signature)]
    (if (nil? ret)
      signature
      (last ret))))

(defn clean-signature
  [signature]
  (-> signature
      (strip-msg-body-tags)
      (strip-unmatched-html-tags)
      (strip-no-text)
      (strip-breaks)
      (str/trim)))

(defn get-signature
  [user-id]
  (-> user-id
      (get-no-text-reply 1)
      (get-reply-message)
      (html/emit*)
      (->> (apply str))
      (clean-signature)))