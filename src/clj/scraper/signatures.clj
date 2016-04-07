(ns clj.scraper.signatures
  (:require [clj.scraper.utils :as utils]
            [net.cgrand.enlive-html :as html]
            [clj-webdriver.taxi :as web]
            [clojure.string :as str]))

(def search-url-prefix "http://bo-ne.ws/forum/search.php?0,author=")
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
  [user-id]
  (-> (str search-url-prefix user-id search-url-suffix)
      (utils/fetch-url)))

(defn not-news-post?
  [reply-msg]
  (let [ret (re-find #"Quote" (first reply-msg))]
    (if (nil? ret)
      true
      false)))

  ;(not (every? identity (map = reply-msg " \n\nQuote"))))

(defn get-reply
  [replies-results-page]
  (take 10 (html/select replies-results-page [:div.search-result])))

(defn get-reply-link
  [reply]
  (html/select reply utils/link-label))

(defn get-reply-url
  [reply-link]
  (get-in (first reply-link) [:attrs :href]))

(defn get-reply-msg
  [reply]
  (:content (last (html/select reply [:blockquote]))))

(defn get-subforum
  [reply]
  (:content (last (html/select reply [html/last-child]))))

(defn get-replies-list
  [user-id]
  (let [replies-results-page (get-posts-page-by-user user-id)]
    (for [reply (get-reply replies-results-page)
      :let  [reply-link  (get-reply-link  reply)
             reply-url   (get-reply-url   reply-link) 
             reply-msg   (get-reply-msg   reply)
             subforum    (get-subforum    reply)]
      :when (and (not-news-post? reply-msg) (not= subforum "Bugs"))]
      reply-url)))

(defn get-replies
  [user-id]
  (for [reply (get-replies-list user-id)
    :let [message (get-reply-message reply)]]
    (apply str (html/emit* message))))


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
  (let [ret (re-find #"(?s)(No text.)(<br />\s?)+(.*)" signature)]
    (if (nil? ret)
      signature
      (last ret))))

(defn strip-leading-period
  [signature]
  (let [ret (re-find #"(?s)^.(.*)$" signature)]
    (if (nil? ret)
      signature
      (last ret))))

(defn clean-signature
  [signature]
  (-> signature
      (strip-msg-body-tags)
      (strip-no-text)
      ;(strip-leading-period)
      (strip-unmatched-html-tags)
      (str/trim)))

(defn get-signature
  [user-id]
  (->>  user-id
        (get-replies)
        (utils/combinations 2)
        (map utils/longest-common-substrings)
        (utils/most-frequent-n 1)
        ffirst
        (clean-signature)
        ;(strip-unmatched-html-tags)
        ;(str/trim)
        ;(custom-trim)
        ;(clean-signature)
        ))

  ; (utils/most-frequent-n 1
  ;   (for [combo (utils/combinations 2 (get-replies user-id))
  ;     :let [first_str   (first  combo)
  ;           second_str  (second combo)]]
  ;     (utils/longest-common-substrings first_str second_str))))
  ; (utils/most-frequent-n 1 (map #(utils/longest-common-substrings %) (utils/combinations 2 (get-replies user-id)))))
  



; (def search-forums-url "http://bo-ne.ws/forum/search.php")

; (defn get-search-results
;   [author-name]
;   (web/set-driver! {:browser :firefox} search-forums-url)
;   (web/to search-forums-url)
;   (web/input-text "input#phorum_search_message" "No text.")
;   (web/select-option {:xpath "//select[@name='match_type']"} {:value "PHRASE"})
;   (web/input-text "input#phorum_search_author" author-name)
;   (web/select-option {:xpath "//select[@name='match_forum[]']"} {:value "ALL"})
;   (web/select-option {:xpath "//select[@name='match_threads']"} {:value "0"})
;   (web/select-option {:xpath "//select[@name='match_dates']"} {:value "0"})
;   (web/submit {:xpath "//input[@type='submit']"})
;   (web/wait-until #(web/exists? "div.search-result"))
;   (let [search-results-page (utils/parse-page-source (web/page-source))]
;     (web/close)
;     search-results-page
;   ))

; (defn get-reply-url
;   [reply-url]
;   (-> reply-url
;       (:attrs)
;       (:href)))

; (defn get-reply-urls
;   [search-results-page]
;   (map-indexed vector
;     (for [reply (-> search-results-page
;                     (html/select [:div.search-result])
;                     (html/select utils/link-label))
;       :let [data (-> reply
;                     (get-reply-url))]
;       :when (seq data)]
;     data)))

