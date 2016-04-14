(ns clj.scraper.utils
  (:require [net.cgrand.enlive-html :as html]
            [clojure.string :as str]
            [clojure.walk :as walk]))

(def link-href [[:a (html/attr? :href)]])
(def link-label [:h4 [:a (html/attr? :href)]])

(defn fetch-url
  [url]
  (html/html-resource (java.net.URL. url)))

(defn parse-page-source
  [page-source]
  (html/html-resource (java.io.StringReader. page-source)))

(defn parse-num-with-comma
  [num-with-comma]
  (-> num-with-comma
      (str/trim)
      (str/replace "," "")
      (Integer/parseInt)))

(defn remove-nils
  "remove pairs of key-value that has nil value from a (possibly nested) map. also transform map to nil if all of its value are nil" 
  [nm]
  (walk/postwalk 
    (fn [el]
      (if (map? el)
      ; (let [m (into {} (remove (comp nil? second) el))]
        (let [m (apply dissoc el (map first (filter (comp nil? second) el)))]
          (when (seq m)
            m))
        el))
    nm))

(defn get-username
  [cols]
  (-> cols
      second
      (html/select link-href)
      last
      (:content)
      first))

(defn get-user-url
  [cols]
  (str "http://bo-ne.ws/forum/profile.php?0,"
    (-> cols
        second
        (html/select link-href)
        last
        (:attrs)
        (:href)
        (str/split #"\,")
        second)))

(defn get-user-id
  [user-url]
  (-> user-url
      (str/split #"\,")
      second
      (Integer/parseInt)))

(defn get-rows
  [table]
  (-> table
      (html/select [:tr])
      rest))

(defn get-cols
  [row]
  (-> row
      (html/select [:td])))

(defn get-data
  [table type-of-data]
  (seq
    (for [row (get-rows table)
      :let [cols (get-cols row)
            data (type-of-data cols)]
      :when (seq data)]
    data)))


(defn try-times*
  "Executes thunk. If an exception is thrown, will retry. At most n retries
  are done. If still some exception is thrown it is bubbled upwards in
  the call chain."
  [n thunk]
  (loop [n n]
    (if-let [result (try [(thunk)]
                      (catch Exception e
                        (when (zero? n)
                          (throw e))))]
      (result 0)
      (recur (dec n)))))

(defmacro try-times
  "Executes body. If an exception is thrown, will retry. At most n retries
  are done. If still some exception is thrown it is bubbled upwards in
  the call chain."
  [n & body]
  `(try-times* ~n (fn [] ~@body)))