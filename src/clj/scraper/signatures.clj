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
  (if reply-url
    (-> reply-url
        (utils/fetch-url)
        (html/select [:div.message-body]))
    nil))


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

(defn get-replies
  [replies-results-page]
  (html/select replies-results-page [:div.search-result]))

(defn not-nt-reply?
  [reply]
  (let [msg (-> reply (html/select [:blockquote]) first :content first)]
    (not= msg " n/t")))

(defn no-results?
  [replies-results-page]
  (-> replies-results-page
      (html/select [:div.information :h4])
      first))

(defn get-no-text-url
  [user-id page-num]
  (let [replies-results-page (get-posts-page-by-user user-id page-num)
        replies              (get-replies replies-results-page)
        no-text-reply        (drop-while not-nt-reply? replies)]
    (if (no-results? replies-results-page)
      nil
      (if (first no-text-reply)
        (get-reply-url (get-reply-link (first no-text-reply)))
        (recur user-id (inc page-num))))))

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
  (let [between-breaks #"^(?s)(?:<br />\n?\s?)*(.*?)(?:<br />\n?\s?)*$"
        ret (re-find between-breaks signature)]
    (if (nil? ret)
      signature
      (last ret))))

(defn strip-edit
  [signature]
  (let [ret (re-find #"(?s)(.*)Edited \d+ time\(s\)" signature)]
    (if (nil? ret)
      signature
      (last ret))))

(defn clean-signature
  [signature]
  (-> signature
      (strip-msg-body-tags)
      (strip-unmatched-html-tags)
      (strip-edit)
      (strip-no-text)
      (strip-breaks)
      (str/trim)))

(defn get-nt-reply-url
  [user-id]
  (get-no-text-url user-id 1))

(defn get-nt-reply-by-url
  [nt-reply-url]
  (-> nt-reply-url
      (get-reply-message)
      (html/emit*)))

(defn get-nt-reply
  [user-id]
  (-> user-id
      (get-nt-reply-url)
      (get-nt-reply-by-url)))

(defn not-blank?
  [no-text-reply signature]
  (and (not= no-text-reply nil) (not= signature "")))

(defn get-signature
  [no-text-reply signature]
  (if (not-blank? no-text-reply signature)
    signature
    nil))

(defn get-signature-by-url
  [no-reply-url]
  (let [no-text-reply (get-nt-reply-by-url no-reply-url)
        signature     (clean-signature (apply str no-text-reply))
        sig           (get-signature no-text-reply signature)]
    sig))

(defn get-signature-by-id
  [user-id]
  (let [no-text-reply (get-nt-reply user-id)
        signature     (clean-signature (apply str no-text-reply))
        sig           (get-signature no-text-reply signature)]
    sig))