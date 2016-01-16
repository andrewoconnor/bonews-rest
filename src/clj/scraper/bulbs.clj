(ns clj.scraper.bulbs
  (:require [clj.scraper.utils :as utils]
            [net.cgrand.enlive-html :as html]
            [clj-webdriver.taxi :as web]))

(def link-href [[:a (html/attr? :href)]])

(defn can-scrape-bulbs?
  []
  (web/click "input.bo_knows_bulbs_view_votes")
  (web/visible? "div.bo_knows_bulbs_voters"))

(defn get-page-source
  [reply-url]
  (web/to reply-url)
  (web/wait-until #(can-scrape-bulbs?))
  (utils/parse-page-source (web/page-source)))

(defn get-username
  [list-item]
  (-> list-item
      (:content)
      last
      (:content)
      first))

(defn get-user-id
  [list-item]
  (-> list-item
      (:content)
      first
      (html/select link-href)
      first
      (get-in [:attrs :href])
      (utils/get-user-id)))

(defn get-user-url
  [user-id]
  (str "http://bo-ne.ws/forum/profile.php?0," user-id))

(defn get-upvotes-list
  [reply-page]
  (-> reply-page
      (html/select [:div.bo_knows_bulbs_voters html/first-child :ul.bo_knows_bulbs_up])
      first))

(defn get-downvotes-list
  [reply-page]
  (-> reply-page
      (html/select [:div.bo_knows_bulbs_voters html/first-child :ul.bo_knows_bulbs_down])
      first))

;(defn get-vote-data
;  [list-item]
;  (get-user-id list-item))

(defn get-list-items
  [ulist]
  (-> ulist
      (html/select [:li])
      rest))

(defn user-helper
  [list-item]
  (let [user-id (get-user-id list-item)]
    {
      :id    user-id
      :name  (get-username list-item)
      :url   (get-user-url user-id)
    }))

(defn get-user-data
  [upvotes-list downvotes-list]
  ;(distinct (concat
  (set (into
    (for [list-item (get-list-items upvotes-list)]
      (user-helper list-item))
    (for [list-item (get-list-items downvotes-list)]
      (user-helper list-item))
  )))

(defn votes-helper
  [ulist]
  ;(seq
  (for [list-item (get-list-items ulist)
    :let [data (get-user-id list-item)]]
  data)
    ;)
  )

(defn get-votes-data
  [reply-page]
  (let [upvotes-list    (get-upvotes-list    reply-page)
        downvotes-list  (get-downvotes-list  reply-page)]
    {
      :upvotes {
        :users (votes-helper upvotes-list)
      }
      :downvotes {
        :users (votes-helper downvotes-list)
      }
      :users (get-user-data upvotes-list downvotes-list)
    }))

(defn has-bulbs?
  [reply-url]
  (-> reply-url
      (utils/fetch-url)
      (html/select [:input.bo_knows_bulbs_view_votes])
      (empty?)))

(defn get-data
  [reply-url]
  (if (has-bulbs? reply-url)
    nil
    (-> reply-url
        get-page-source
        get-votes-data
        utils/remove-nils)))