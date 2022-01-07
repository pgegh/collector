(ns collector.client-http.mappers
  (:require [clojure.spec.alpha :as s]
            [collector.client-http.specs :refer :all]
            [collector.core.specs :refer :all]))

(defn db->client-db
  [database]
  {:pre  [(s/valid? :collector.core.specs/database database)]
   :post [(s/valid? :collector.client-http.specs/client-db %)]}
  {:category "All"
   :entries  (if-not (and (:movies-db database)
                          (> (count (:movies-db database)) 0))
               []
               (->> (get database :movies-db)
                    (keys)
                    (map (fn [key] {:id       key
                                    :name     (get-in database [:movies-db key :title])
                                    :category "Movies"}))
                    (into [])))
   })

(defn movie->client-movie
  [movie]
  movie)

(defn filenames->client-filenames
  [filenames]
  filenames)