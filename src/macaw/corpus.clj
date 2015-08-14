(ns macaw.corpus
  (:require
   [clojure.set :as set]
   [clojure.string :as string]
   [clj-fuzzy.stemmers :as stem]))

(def frequency-list "resources/frequencies/english.txt")

(defn stemmize
  [s]
  (let [tokens (string/split s #"[ -_]+")
        stems (map stem/porter tokens)]
    (set stems)))

(defn match-all-stems?
  [canon hopeful]
  (= canon (set/intersection canon (stemmize hopeful))))

(defn parse-frequency
  [line]
  (let [[word frequency] (string/split line #" ")]
    [word (Integer/parseInt frequency)]))

(defn load-frequencies
  []
  (let [raw (slurp frequency-list)
        lines (string/split raw #"\r\n")]
    (into {} (map parse-frequency lines))))

(defn group-stems
  [freqs]
  (let [groups (group-by (comp stem/porter first) freqs)]
    (into
     {}
     (map
      (fn [[stem freqs]]
        [stem (reduce + (map last freqs))])
      groups))))

(defn scale-stems
  [stems]
  (let [rank (sort-by last > stems)
        highest (-> rank first last)]
    (into
     {}
     (map
      (fn [[stem freq]]
        [stem (float (/ freq highest))])
      rank))))

(defn parse-line
  [line]
  (let [tokens (re-seq #"[a-zA-Z']" line)]))

(defn count-line
  [freqs line]
  (let [tokens (parse-line line)]
    (merge-with + freqs (frequencies tokens))))

