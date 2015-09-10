(ns macaw.frequency
  (:require
   [clojure.set :as set]
   [clojure.string :as string]
   [clj-fuzzy.stemmers :as stem]))

(def log-of-2 (Math/log 2))

(defn log2
  [n]
  (/ (Math/log n) log-of-2))

(defn tokenize
  [pod]
  (map
   string/lower-case
   (re-seq #"[^ \n\r\t]+" pod)))

(defn ignoring?
  [token]
  (re-find #"^@|^#|^https?:|['\"0-9]" token))

(defn clean-token
  [token]
  (let [clean (string/replace token #"[\"~`!@#$%^*()\[\]+=_:;<>,.?/|\\]" "")]
    clean))

(defn tokenize-stream
  [stream]
  (if (seq? stream)
    (remove
     empty?
     (map
      clean-token
      (remove
       ignoring?
       (mapcat tokenize stream))))))

(defn token-frequencies
  [stream]
  (frequencies
   (tokenize-stream stream)))

(defn mass-frequencies
  [mass]
  (into
   {}
   (map
    (fn [[id stream]]
      [id (token-frequencies stream)])
    mass)))

(defn total-appearances
  [freqs]
  (frequencies
   (mapcat
    keys
    (vals freqs))))

(defn merge-frequencies
  [mass]
  (reduce
   (fn [merged freq]
     (merge-with + merged freq))
   {} (vals mass)))

(defn scale-frequencies
  [freqs]
  (let [most (first (sort > (vals freqs)))]
    (into
     {}
     (map
      (fn [[token freq]]
        [token (int (Math/floor (- 0.5 (log2 (/ freq most)))))])
      freqs))))

(defn scale-mass
  [mass]
  (let [freqs (merge-frequencies mass)]
    (scale-frequencies freqs)))

(defn compare-frequencies
  [a b]
  (let [common (set/intersection (set (keys a)) (set (keys b)))
        a-common (select-keys a common)
        b-common (select-keys b common)
        joint (merge-with vector a-common b-common)
        discrepancies (filter (fn [[token [a b]]] (not= a b)) joint)
        sorted (sort-by (fn [[token [a b]]] (- b a)) > discrepancies)]
    sorted))

(defn progressive-filter
  [classes desired level bottom]
  (loop [level level]
    (let [found
          (filter
           (fn [[token [a b]]]
             (>= a level))
           classes)]
      (if (and (< (count found) desired) (> level bottom))
        (recur (dec level))
        found))))

(defn compare-masses
  [freqs a]
  (let [a-mass (select-keys freqs a)
        b-mass (apply dissoc freqs a)
        a-freqs (scale-mass a-mass)
        b-freqs (scale-mass b-mass)
        compared (compare-frequencies a-freqs b-freqs)
        appearances (total-appearances a-mass)]
    (map
     first
     (sort-by
      last >
      (map
       (fn [[token [a b]]]
         [token (* (get appearances token) (- b a))])
       (progressive-filter compared 5 8 3))))))

(defn extract-distinct-tokens
  [mass submasses]
  (let [freqs (mass-frequencies mass)]
    (into
     {}
     (map
      (fn [[id submass]]
        [id (compare-masses freqs submass)])
      submasses))))
