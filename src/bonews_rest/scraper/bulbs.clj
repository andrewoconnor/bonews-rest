(ns bonews-rest.scraper.bulbs
  (:require [bonews-rest.scraper.utils :as utils]
            [clj-webdriver.taxi :as web]))

(defn get-reply-page
  [reply-url]
  (web/set-driver! {:browser :firefox} reply-url)
  (web/to reply-url)
  (web/click "input.bo_knows_bulbs_view_votes")
  (web/wait-until #(web/visible? "div.bo_knows_bulbs_voters"))
  (let [reply-page (utils/parse-page-source (web/page-source))]
    (web/close)
    reply-page
  ))