(ns collector.client-http.mappers
  (:require [clojure.data.json :refer [write-str]]))

(defn db->client-db
  [database]
  (write-str database))

(defn movie->client-movie
  [movie]
  (write-str movie))

(defn filenames->client-filenames
  [filenames]
  (write-str filenames))