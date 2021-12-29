(ns collector.client-http.edn-api
  (:require [clojure.spec.alpha :as s]
            [collector.client-http.mappers :refer [db->client-db]]
            [collector.persistence.persistence-api :refer [handle-event]]))

(defonce database-atom (atom nil))
(defonce database-file-name-atom (atom nil))

(defn load-database!
  [database-file-name]
  {:pre [(s/valid? :collector.persistence.specs/file-name database-file-name)]}
  (let [database (as-> (reset! database-file-name-atom database-file-name) $
                       (reset! database-atom (handle-event $)))]
    (time (db->client-db database))))

(defn add-movie!
  [imdb-movie-id title]
  (time (db->client-db (swap! database-atom #(handle-event % @database-file-name-atom {:type :add-movie
                                                                                       :args [imdb-movie-id title]})))))

(defn remove-movie!
  [imdb-movie-id]
  (time (db->client-db (swap! database-atom #(handle-event % @database-file-name-atom {:type :remove-movie
                                                                                       :args [imdb-movie-id]})))))