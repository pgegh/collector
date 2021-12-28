;; Copyright © 2021 Hovig Manjikian
;;
;; This file is part of collector.
;;
;; collector is free software: you can redistribute it and/or modify
;; it under the terms of the GNU General Public License as published by
;; the Free Software Foundation, either version 3 of the License, or
;; (at your option) any later version.
;;
;; collector is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU General Public License for more details.
;;
;; You should have received a copy of the GNU General Public License
;; along with json.  If not, see <https://www.gnu.org/licenses/>.

(ns collector.core.core-api
  (:require [clojure.test :refer [is]]
            [clojure.spec.alpha :as s]
            [collector.utils.auxiliary-functions :refer [error
                                                         error?
                                                         now]]
            [collector.core.constructors :refer [create-movie
                                                 create-initial-database]]))

(defn create-empty-database
  "Creates a new empty database"
  {:test (fn []
           (is (s/valid? :collector.core.specs/database (create-empty-database (now))))
           (is (= (create-empty-database #inst"2021-12-26T22:07:05.047-00:00")
                  {:date-created #inst"2021-12-26T22:07:05.047-00:00"})))}
  [date]
  {:pre  [(s/valid? :collector.core.specs/date date)]
   :post [(s/valid? :collector.core.specs/database %)]}
  (create-initial-database date))

(defn get-movie
  "Returns the movie from tha database if it exists, otherwise nil."
  {:test (fn []
           (is (= (get-movie {:date-created (now)
                              :movies-db    {"tt0000000" {:title "test"}}}
                             "tt0000000")
                  {:title "test"}))
           (is (nil? (get-movie {:date-created (now)
                                 :movies-db    {}} "tt000000")))
           (is (nil? (get-movie {:date-created (now)} "tt0000000"))))}
  [database imdb-movie-id]
  {:pre  [(s/valid? :collector.core.specs/database database)
          (s/valid? :collector.core.specs/imdb-movie-id imdb-movie-id)]
   :past [(or (s/valid? :collector.core.specs/movie %) (nil? %))]}
  (get-in database [:movies-db imdb-movie-id]))

(defn add-movie
  "Adds a new movie to the state"
  {:test (fn []
           (let [db (create-initial-database (now))]
             (is (= (-> (add-movie db "tt0000000" "movie0")
                        (get-in [:movies-db "tt0000000"]))
                    {:title "movie0"}))
             (is (= (-> (add-movie db "tt0000000" "movie0")
                        (add-movie "tt0000001" "movie1")
                        (get :movies-db))
                    {"tt0000000" {:title "movie0"}
                     "tt0000001" {:title "movie1"}}))
             (is (error? #"^A movie with the same ID exists! ID:"
                         #(-> (add-movie db "tt0000000" "movie0")
                              (add-movie "tt0000000" "movie1"))))))}
  ([database imdb-movie-id title & kvs]
   {:pre  [(s/valid? :collector.core.specs/database database)
           (s/valid? :collector.core.specs/imdb-movie-id imdb-movie-id)
           (s/valid? :collector.core.specs/title title)]
    :post [(s/valid? :collector.core.specs/database %)]}
   (let [movie (apply create-movie title kvs)]
     (if (:movies-db database)
       (if (get-movie database imdb-movie-id)
         (error "A movie with the same ID exists! ID:" imdb-movie-id)
         (update database :movies-db #(assoc % imdb-movie-id movie)))
       (assoc database :movies-db {imdb-movie-id movie})))))

(defn update-movie
  "Updates the information of a movie in the database with the provided values.
   The movie must exist in the database. An updated database will be returned"
  {:test (fn []
           (is (= (-> (create-initial-database)
                      (assoc :movies-db {"tt0000000" {:title "original"}})
                      (update-movie "tt0000000" :title "updated" :original-title "updated")
                      (get-movie "tt0000000"))
                  {:title "updated" :original-title "updated"}))
           (is (error? #"The movie you are trying to update does not exist in the database! Movie ID:"
                       #(-> (create-initial-database)
                            (update-movie "tt0000000" :title "updated")))))}
  [database imdb-movie-id & kvs]
  {:pre  [(s/valid? :collector.core.specs/database database)
          (s/valid? :collector.core.specs/imdb-movie-id imdb-movie-id)]
   :post [(s/valid? :collector.core.specs/database %)]}
  (if-not (get-movie database imdb-movie-id)
    (error "The movie you are trying to update does not exist in the database! Movie ID:" imdb-movie-id)
    (update-in database [:movies-db imdb-movie-id] #(apply assoc % kvs))))

(defn remove-movie
  "Removes the given movie from the database. The movie must exist in the database.
  The updated database will be returned"
  {:test (fn []
           (is (= (-> (create-initial-database)
                      (assoc :movies-db {"tt0000000" {:title "test"}})
                      (remove-movie "tt0000000")
                      (get :movies-db))
                  {}))
           (is (error? #"^The selected movie does not exist! Movie ID:"
                       #(-> (create-initial-database)
                            (remove-movie "tt0000000")))))}
  [database imdb-movie-id]
  {:pre  [(s/valid? :collector.core.specs/database database)
          (s/valid? :collector.core.specs/imdb-movie-id imdb-movie-id)]
   :post [(s/valid? :collector.core.specs/database %)]}
  (if (get-movie database imdb-movie-id)
    (update database :movies-db #(dissoc % imdb-movie-id))
    (error "The selected movie does not exist! Movie ID:" imdb-movie-id)))