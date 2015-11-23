(ns bonews-rest.scraper.bulbs
  (:require [bonews-rest.scraper.utils :as utils]
            [net.cgrand.enlive-html :as html]
            [clj-webdriver.taxi :as web]))

(def link-href [[:a (html/attr? :href)]])

; (defn get-reply-page
;   [reply-url]
;   (web/set-driver! {:browser :firefox} reply-url)
;   (web/to reply-url)
;   (web/click "input.bo_knows_bulbs_view_votes")
;   (web/wait-until #(web/visible? "div.bo_knows_bulbs_voters"))
;   (let [reply-page (utils/parse-page-source (web/page-source))]
;     (web/close)
;     reply-page
;   ))

(defn get-page-source
  [reply-url]
  (web/set-driver! {:browser :firefox} reply-url)
  (web/to reply-url)
  (web/click "input.bo_knows_bulbs_view_votes")
  (web/wait-until #(web/visible? "div.bo_knows_bulbs_voters"))
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
      (:attrs)
      (:href)
      (utils/get-user-id)))

(defn get-user-url
  [list-item]
  (str "http://bo-ne.ws/forum/"
    (-> list-item
        (:content)
        first
        (html/select link-href)
        first
        (:attrs)
        (:href))))

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
  (get-user-id list-item))

(defn get-list-items
  [ulist]
  (-> ulist
      (html/select [:li])
      rest))

(defn user-helper
  [list-item]
  {
    :id    (get-user-id   list-item)
    :name  (get-username  list-item)
    :url   (get-user-url  list-item)
  })

(defn get-user-data
  [upvotes-list downvotes-list]
  (distinct (concat 
    (for [list-item (get-list-items upvotes-list)
      :let [data (user-helper list-item)]]
    data)
    (for [list-item (get-list-items downvotes-list)
      :let [data (user-helper list-item)]]
    data))))

(defn votes-helper
  [ulist]
  (seq
    (for [list-item (get-list-items ulist)
      :let [data (get-vote-data list-item)]]
    data)))

(defn get-votes-data
  [reply-page]
  (let [upvotes-list    (get-upvotes-list   reply-page)
        downvotes-list  (get-downvotes-list reply-page)]
    (-> {
          :bulbs {
            :upvotes {
              :users (votes-helper upvotes-list)
            }
            :downvotes {
              :users (votes-helper downvotes-list)
            }
          }
          :users (get-user-data upvotes-list downvotes-list)
        }
        (utils/remove-nils))))


(defn get-data
  [reply-url]
  (-> reply-url
      (get-page-source)
      (get-votes-data)))

