(ns bonews-rest.scraper.signatures
  (:require [bonews-rest.scraper.utils :as utils]
            [net.cgrand.enlive-html :as html]
            [clj-webdriver.taxi :as web]
            [clojure.string :as str]))

(def search-url-prefix "http://bo-ne.ws/forum/search.php?0,author=")
(def search-url-suffix ",match_type=USER_ID,match_dates=0,match_threads=0")

(defn get-posts-page-by-user
  [user-id]
  (-> (str search-url-prefix user-id search-url-suffix)
      (utils/fetch-url)))

(defn not-news-post?
  [reply-msg]
  (not (every? identity (map = reply-msg " \n\nQuote"))))

(defn get-replies-list
  [user-id]
  (let [replies-results-page (get-posts-page-by-user user-id)]
    (for [reply (html/select replies-results-page [:div.search-result])
      :let  [reply-link  (html/select reply utils/link-label)
             reply-url   (get-in (first reply-link) [:attrs :href])
             reply-msg   (:content (last (html/select reply [:blockquote])))
             subforum    (:content (last (html/select reply [html/last-child])))]
      :when (and (not-news-post? reply-msg) (not= subforum "Bugs"))]
      reply-url)))

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

