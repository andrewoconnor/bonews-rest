(ns seeds.sql
    (:require [clojure.java.jdbc :as j]
              [bonews-rest.utils.lorem_ipsum :as lorem]))

(defn run [target & args]
  (j/with-db-connection [db {:connection-uri (-> target :db :url)}]
                        (j/insert! db :users {:id 1 :username "test1"})
                        (j/insert! db :users {:id 42 :username "test42"})
                        (j/insert! db :users {:id 41 :username "test40"})
                        (j/insert! db :users {:id 23 :username "airjordan23"})

                        (j/insert! db :subforums {:id 7 :name "Off Topic News"})
                        (j/insert! db :subforums {:id 8 :name "Breaking News"})
                        (j/insert! db :subforums {:id 9 :name "Sports"})
                        (j/insert! db :subforums {:id 10 :name "Arts"})
                        (j/insert! db :subforums {:id 11 :name "In House"})
                        (j/insert! db :subforums {:id 12 :name "Polls"})
                        (j/insert! db :subforums {:id 13 :name "Video Games"})
                        (j/insert! db :subforums {:id 14 :name "Movies"})
                        (j/insert! db :subforums {:id 15 :name "Television"})
                        (j/insert! db :subforums {:id 16 :name "Music"})
                        (j/insert! db :subforums {:id 17 :name "Books"})
                        (j/insert! db :subforums {:id 18 :name "Food"})
                        (j/insert! db :subforums {:id 19 :name "Politics"})
                        (j/insert! db :subforums {:id 20 :name "Education"})
                        (j/insert! db :subforums {:id 21 :name "Crime"})
                        (j/insert! db :subforums {:id 22 :name "Images and Videos"})
                        (j/insert! db :subforums {:id 23 :name "Science"})
                        (j/insert! db :subforums {:id 24 :name "The Economy"})
                        (j/insert! db :subforums {:id 25 :name "Bugs"})
                        (j/insert! db :subforums {:id 29 :name "Personal"})
                        (j/insert! db :subforums {:id 30 :name "Borting Up With The Dinosaurs"})
                        (j/insert! db :subforums {:id 33 :name "Health & Beauty"})
                        (j/insert! db :subforums {:id 34 :name "The World"})
                        (j/insert! db :subforums {:id 35 :name "Horseshit!"})

                        (j/insert! db :threads {:id 1234 :subforum_id 12})

                        (let [thread-id 1234]
                          (loop [id 1234]
                            (when (< id 1254)
                              (j/insert! db :replies {:id id
                                                      :thread_id thread-id
                                                      :user_id 42
                                                      :parent_id (if (= id 1234) nil (dec id))
                                                      :title (lorem/random-sentence)
                                                      :message (lorem/random-paragraph)})
                              (recur (inc id)))))


                        (j/insert! db :bulbs {:reply_id 1234 :user_id 1 :vote 1})
                        (j/insert! db :bulbs {:reply_id 1234 :user_id 41 :vote -1})
                        ))
