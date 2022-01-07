(ns collector.client-http.edn-api
  (:require [clojure.spec.alpha :as s]
            [clojure.data.json :refer [write-str]]
            [collector.client-http.mappers :refer [db->client-db
                                                   movie->client-movie
                                                   filenames->client-filenames]]
            [collector.core.core-api :refer [get-movie]]
            [collector.persistence.persistence-api :refer [handle-event
                                                           get-available-database-files]]))

(defonce database-atom (atom nil))
(defonce database-file-name-atom (atom nil))

(defn load-database!
  [database-file-name]
  {:pre [(s/valid? :collector.persistence.specs/file-name database-file-name)]}
  (let [database (as-> (reset! database-file-name-atom database-file-name) $
                       (reset! database-atom (handle-event $)))]
    (time (write-str (db->client-db database)))))

(defn add-movie!
  [imdb-movie-id title]
  (time (db->client-db (swap! database-atom #(handle-event % @database-file-name-atom {:type :add-movie
                                                                                       :args [imdb-movie-id title]})))))

(defn remove-movie!
  [imdb-movie-id]
  (time (db->client-db (swap! database-atom #(handle-event % @database-file-name-atom {:type :remove-movie
                                                                                       :args [imdb-movie-id]})))))

(defn update-movie!
  [imdb-movie-id title]
  (time (db->client-db (swap! database-atom #(handle-event % @database-file-name-atom {:type :update-movie
                                                                                       :args [imdb-movie-id :title title]})))))

(defn get-movie!
  [imdb-movie-id]
  (time (movie->client-movie (get-movie @database-atom imdb-movie-id))))

(defn get-available-database-files!
  []
  (time (filenames->client-filenames (get-available-database-files))))