(ns bonews-rest.scraper.utils
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

(defn find-lcs [^String s1 ^String s2 ^objects table]
  (second  
    (let [^chars arr1 (.toCharArray s1) ^chars arr2 (.toCharArray s2)]
      (binding [*unchecked-math* true])
        (areduce arr1 i ret1 [0 #{}]
          (areduce arr2 j ret2 ret1 
            (let [[z result] ret2] 
              (if (= ^char (aget arr1 i) ^char (aget arr2 j))
                (let [curr-row     ^longs (aget table i)
                      curr-longest (aset-long curr-row j 
                                     (if (or (zero? i) (zero? j))
                                       1 
                                       (inc (-> ^longs (aget table (dec i)) 
                                                (aget (dec j)))))) 
                      longer-than-z? (> curr-longest z)
                      z              (if longer-than-z? curr-longest z)
                      result         (if longer-than-z? #{} result)]
                  [z (if (>= curr-longest z) 
                       (conj result (.substring s1 (inc (- i z)) (inc i))) 
                       result)])
                [z result])))))))

(defn longest-common-substrings
  "Returns vector of longest common substrings for the two input strings."
  [^String s1 ^String s2]
    (vec  
      (find-lcs s1 s2 (make-array Long/TYPE (count s1) (count s2)))))