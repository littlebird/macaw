(defproject aviary/macaw "0.0.8"
  :description "analyze word corpuses for distinctnesses"
  :url "http://github.com/littlebird/macaw"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-fuzzy "0.1.8"]
                ;; [clj-fuzzy "0.4.0"]
                 ]
  :plugins [[s3-wagon-private "1.1.2"]]
  :repositories ^:replace
  [["central" {:url "http://repo1.maven.org/maven2"}]
   ["clojure" {:url "http://build.clojure.org/releases"}]
   ["clojure-snapshots" {:url "http://build.clojure.org/snapshots"}]
   ["clojars" {:url "http://clojars.org/repo/"}]
   ["private" {:url "s3p://littlebird-maven/releases/"
               :creds :gpg
               :sign-releases false}]])
