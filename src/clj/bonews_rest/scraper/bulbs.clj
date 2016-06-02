(ns bonews-rest.scraper.bulbs
  (:require [bonews-rest.scraper.utils :as utils]
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

(defn get-list-items
  [ulist]
  (-> ulist
      (html/select [:li])
      rest))

(defn user-data-helper
  [list-item]
  (let [user-id (get-user-id list-item)]
    {
      :id    user-id
      :username  (get-username list-item)
     }))

(defn get-user-data
  [upvotes-list downvotes-list]
  (set
    (into
      (map user-data-helper (get-list-items upvotes-list))
      (map user-data-helper (get-list-items downvotes-list)))))

(defn get-user-ids
  [ulist]
  (seq (map get-user-id (get-list-items ulist))))

(defn get-votes-data
  [reply-url reply-id]
  (let [reply-page      (get-page-source reply-url)
        upvotes-list    (get-upvotes-list reply-page)
        downvotes-list  (get-downvotes-list reply-page)
        upvotes-users   (get-user-ids upvotes-list)
        downvotes-users (get-user-ids downvotes-list)
        upvotes         (map #(assoc {:reply_id reply-id :vote 1} :user_id %) upvotes-users)
        downvotes       (map #(assoc {:reply_id reply-id :vote -1} :user_id %) downvotes-users)]
    {:bulbs (into upvotes downvotes)
     :users (get-user-data upvotes-list downvotes-list)}))

(defn has-bulbs?
  [reply-url]
  (-> reply-url
      (utils/fetch-url)
      (html/select [:input.bo_knows_bulbs_view_votes])
      (empty?)))

(defn get-data-helper
  [reply-url reply-id]
  (if (has-bulbs? reply-url)
    nil
    (-> reply-url
        (get-votes-data reply-id)
        (utils/remove-nils))))

(defn get-data
  [reply-url reply-id]
  (utils/try-times 3 (get-data-helper reply-url reply-id)))