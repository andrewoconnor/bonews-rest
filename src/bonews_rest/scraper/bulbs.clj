(ns bonews-rest.scraper.bulbs
  (:require [clj-webdriver.taxi :as web]))


(defn get-reply-page
  [reply-url]
  (web/set-driver! {:browser :firefox} reply-url)
  (web/to reply-url))