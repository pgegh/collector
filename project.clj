(defproject collector "0.2.0-SNAPSHOT"
  :description "An application for storing data about digital media"
  :url "https://github.com/pgegh/collector"
  :license {:name "GPLv3"
            :url  "https://www.gnu.org/licenses/gpl-3.0.html"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/data.json "2.4.0"]
                 [http-kit "2.5.3"]]
  :repl-options {:init-ns collector.core.core})
