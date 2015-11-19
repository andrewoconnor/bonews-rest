(ns bonews-rest.scraper.signatures
  (:require [bonews-rest.scraper.utils :as utils]
            [net.cgrand.enlive-html :as html]
            [clj-webdriver.taxi :as web]))

(def search-forums-url "http://bo-ne.ws/forum/search.php")

(defn get-search-results
  [author-name]
  (web/set-driver! {:browser :firefox} search-forums-url)
  (web/to search-forums-url)
  (web/input-text "input#phorum_search_message" "No text.")
  (web/select-option {:xpath "//select[@name='match_type']"} {:value "PHRASE"})
  (web/input-text "input#phorum_search_author" author-name)
  (web/select-option {:xpath "//select[@name='match_forum[]']"} {:value "ALL"})
  (web/select-option {:xpath "//select[@name='match_threads']"} {:value "0"})
  (web/select-option {:xpath "//select[@name='match_dates']"} {:value "0"})
  (web/submit {:xpath "//input[@type='submit']"})
  (web/wait-until #(web/exists? "div.search-result"))
  (let [search-results-page (utils/parse-page-source (web/page-source))]
    (web/close)
    search-results-page
  ))

(defn get-reply-url
  [reply-url]
  (-> reply-url
      (:attrs)
      (:href)))

(defn get-reply-urls
  [search-results-page]
  (map-indexed vector
    (for [reply (-> search-results-page
                    (html/select [:div.search-result])
                    (html/select utils/link-label))
      :let [data (-> reply
                    (get-reply-url))]
      :when (seq data)]
    data)))