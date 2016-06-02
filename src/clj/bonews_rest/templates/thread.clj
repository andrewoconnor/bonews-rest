(ns bonews-rest.templates.thread
  (require [net.cgrand.enlive-html :as html]))

(defn get-thread-title
  [thread]
  (:title (first (:replies thread))))

(defn get-username-by-id
  [thread]
  (let [main-reply (first (:replies thread))
        author-id  (:user_id main-reply)
        users      (:users thread)
        author     (first (filter #(= (:id %) author-id) users))]
    (:username author)))

(html/deftemplate thread-page "thread.html"
  [thread]
  [:title] (html/content (get-thread-title thread))
  [:h1] (html/content (get-thread-title thread))
  [:span.author] (html/content (get-username-by-id thread))
  [:div.thread-body] (html/content (:message (first (:replies thread)))))

(def sample-thread
  {:thread {:id 560002, :subforum_id 13, :user_id 538},
   :replies [{:id 560002,
              :title "Total Warhammer: War",
              :message "Anyone else get this yet? I've been having an absolute blast so far. They really toned down the empire-management side of things (no more squalor, food etcetera), which I think is unfortunate, but the battles are great and the way the factions play off each other is excellent.<br />
                      <br />
                      Lotta people have been complaining of agent spam by the AI, but I haven't seen too much of that, luckily.<br />
                      <br />
                      I'm currently on a campaign as the Vampires and have been pretty much rolling everyone from the first turn on, but even so, the way the campaign map is structured means that you're never truly safe and the loss of just one major army (or even just one Legendary Lord) can be devastating, as you can never afford to have more than 3-4 full stacks in the field. It keeps things exciting and makes almost every battle feel meaningful.<br />
                      <br />
                      Also I can't tell if this game takes itself really seriously or is making fun of it's own rediculousness, which is a big plus for me!",
              :post_time #inst"2016-05-27T02:06:00.000000000-00:00",
              :user_id 538,
              :parent_id nil}
             {:id 560009,
              :title "fuck that dumb anime shit, I want Medieval 3",
              :message "",
              :post_time #inst"2016-05-27T04:13:00.000000000-00:00",
              :user_id 63,
              :parent_id 560002}
             {:id 560237,
              :title "Attila was pretty decent. Hopefully the Charlemagne expansion and this make good templates for a new Medieval game",
              :message "",
              :post_time #inst"2016-05-27T17:13:00.000000000-00:00",
              :user_id 418,
              :parent_id 560009}
             {:id 560667,
              :title "For real, I think this might be the best TW since Medieval 2, even is the campaign is less focused than the Shogun 2 one, and I can imagine the fantasy setting is not really everyone's bag.",
              :message "",
              :post_time #inst"2016-05-30T10:56:00.000000000-00:00",
              :user_id 538,
              :parent_id 560237}
             {:id 560111,
              :title "No rat men and no squalor = no way",
              :message "Every single time I play a Total War game I end up figuring out which units do best when you're automating the battles so that I can just stomp around the empire-management side.",
              :post_time #inst"2016-05-27T10:50:00.000000000-00:00",
              :user_id 976,
              :parent_id 560002}
             {:id 560112,
              :title "again i am disappointed by not motto thread",
              :message "",
              :post_time #inst"2016-05-27T10:53:00.000000000-00:00",
              :user_id 567,
              :parent_id 560111}
             {:id 560217,
              :title "Okay... so lets say I'm planning a to commission a family crest....",
              :message "What's the heraldric term for \"rampant but with a huge erect dick?\"",
              :post_time #inst"2016-05-27T15:42:00.000000000-00:00",
              :user_id 976,
              :parent_id 560112}
             {:id 560117,
              :title "People are laughing about it because 20 seconds into the \"Making of\" video, it's revealed they're using models from Mantic Games (who makes Kings of War)",
              :message "",
              :post_time #inst"2016-05-27T10:59:00.000000000-00:00",
              :user_id 515,
              :parent_id 560002}
             {:id 560156,
              :title "please please please provide the source for this because it is fucking tasty like fresh bread",
              :message "",
              :post_time #inst"2016-05-27T12:40:00.000000000-00:00",
              :user_id 243,
              :parent_id 560117}
             {:id 560162,
              :title "There are screenshots on the Kings of War Fanatics webpage, but the Making Of video itself was pulled, re-edited, and reuploaded after yanking the closeups of the Mantic models.",
              :message "It's kind of understandable because they stopped production of Tomb Kings, and Mantic had better models anyway.<br />
                      <br />
                      (Unsubstantiated rumor is they invited players to bring in their armies to play games at HQ, and the pictures may have been of some of those models.)",
              :post_time #inst"2016-05-27T13:02:00.000000000-00:00",
              :user_id 515,
              :parent_id 560156}
             {:id 560666,
              :title "It's sad that Games Workshop's business practices suck so badly, 'cause the game is legit good and really shouldn't  have to suffer from its association with GW.",
              :message "",
              :post_time #inst"2016-05-30T10:52:00.000000000-00:00",
              :user_id 538,
              :parent_id 560117}],
   :bulbs [{:reply_id 560112, :vote 1, :user_id 35}
           {:reply_id 560112, :vote 1, :user_id 28}
           {:reply_id 560112, :vote 1, :user_id 243}
           {:reply_id 560112, :vote 1, :user_id 976}
           {:reply_id 560117, :vote 1, :user_id 243}
           {:reply_id 560162, :vote 1, :user_id 243}
           {:reply_id 560162, :vote 1, :user_id 226}],
   :users [{:id 976, :username "Nü", :signature nil, :nt_reply_url nil}
           {:id 538, :username "Versorium", :signature nil, :nt_reply_url nil}
           {:id 567, :username "couch", :signature nil, :nt_reply_url nil}
           {:id 63, :username "The Vengeful Spirit of Benjamin J. Bocephus", :signature nil, :nt_reply_url nil}
           {:id 35, :username "Xiphias", :signature nil, :nt_reply_url nil}
           {:id 243, :username "railgun", :signature nil, :nt_reply_url nil}
           {:id 418, :username "shutup", :signature nil, :nt_reply_url nil}
           {:id 515, :username "Redshirt", :signature nil, :nt_reply_url nil}
           {:id 28, :username "Oh No", :signature nil, :nt_reply_url nil}
           {:id 226, :username "SharoKham", :signature nil, :nt_reply_url nil}]})