(ns collector.core-api
  (:require [clojure.test :refer [is]]
            [clojure.spec.alpha :as s]
            [collector.auxiliary-functions :refer [error
                                                        now]]
            [collector.constructors :refer [create-movie
                                                 create-initial-database]]
            [collector.core :refer [get-movie]]))

(defn add-movie
  "Adds a movie to the state"
  {:test (fn []
           (let [db (create-initial-database (now))
                 mv0 (create-movie "movie0")
                 mv1 (create-movie "movie1")]
             (is (= (-> (add-movie db mv0 "tt0000000")
                        (get-in [:movies-db "tt0000000"]))
                    {:title "movie0"}))
             (is (= (-> (add-movie db mv0 "tt0000000")
                        (add-movie mv1 "tt0000001")
                        (get :movies-db))
                    {"tt0000000" {:title "movie0"}
                     "tt0000001" {:title "movie1"}}))
             (is (thrown? Exception (-> (add-movie db mv0 "tt0000000")
                                        (add-movie mv1 "tt0000000"))))))}
  [database movie imdb-movie-id]
  {:pre  [(s/valid? :collector.specs/database database)
          (s/valid? :collector.specs/movie movie)
          (s/valid? :collector.specs/imdb-movie-id imdb-movie-id)]
   :post [(s/valid? :collector.specs/database database)]}
  (if (:movies-db database)
    (if (get-movie database imdb-movie-id)
      (error "A movie with the same ID exists! ID:" imdb-movie-id)
      (update database :movies-db #(assoc % imdb-movie-id movie)))
    (assoc database :movies-db {imdb-movie-id movie})))