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

(ns collector.core.core
  (:require [clojure.test :refer [is]]
            [clojure.spec.alpha :as s]
            [collector.core.auxiliary-functions :refer [now]]
            [collector.core.constructors :refer [create-initial-database]]))



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