(ns bonews-rest.scraper.bulbs
  (:require [bonews-rest.scraper.utils :as utils]
            [net.cgrand.enlive-html :as html]
            [clj-webdriver.taxi :as web]))

(def link-href [[:a (html/attr? :href)]])

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

(defn get-username
  [list-item]
  (-> list-item
      (:content)
      last
      (:content)
      first))

(defn get-userid
  [list-item]
  (-> list-item
      (:content)
      first
      (html/select link-href)
      first
      (:attrs)
      (:href)
      (utils/get-author-id)))

(defn get-upvotes-list
  [reply-page]
  (-> reply-page
      (html/select [:ul.bo_knows_bulbs_up])))

(defn get-downvotes-list
  [reply-page]
  (-> reply-page
      (html/select [:ul.bo_knows_bulbs_down])))

(defn get-vote-data
  [list-item]
  (get-userid list-item))
  ; {
  ;   :username (get-username list-item)
  ;   :userid   (get-userid   list-item)
  ; })

(defn get-list-items
  [ulist]
  (-> ulist
      (html/select [:li])
      rest))

(defn votes-helper
  [ulist]
  (seq
    (for [list-item (get-list-items ulist)
      :let [data (get-vote-data list-item)]]
      ; :when (seq data)]
    data)))

(defn get-votes-data
  [reply-page]
  (let [upvotes-list    (get-upvotes-list   reply-page)
        downvotes-list  (get-downvotes-list reply-page)]
    {
      :upvotes   (votes-helper upvotes-list)
      :downvotes (votes-helper downvotes-list)
    }))

