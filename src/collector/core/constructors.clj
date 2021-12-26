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

(ns collector.core.constructors
  (:require [clojure.test :refer [is]]
            [clojure.spec.alpha :as s]
            [collector.utils.auxiliary-functions :refer [now]]))

(defn create-initial-database
  "Creates an empty state for the entire application"
  {:test (fn []
           (is (s/valid? :collector.core.specs/initial-database (create-initial-database (now))))
           (is (s/valid? :collector.core.specs/initial-database (create-initial-database))))}
  ([]
   {:post [s/valid? :collector.core.specs/date %]}
   (create-initial-database (now)))
  ([date]
   {:pre  [s/valid? :collector.core.specs/date date]
    :post [s/valid? :collector.core.specs/initial-database %]}
   {:date-created date}))

(defn create-movie
  "Creates a movie element"
  {:test (fn []
           (is (= (create-movie "movie-title")
                  {:title "movie-title"}))
           (is (= (create-movie "movie-title"
                                :year 1994)
                  {:title "movie-title"
                   :year  1994})))}
  [title & kvs]
  {:pre  [s/valid? :collector.core.specs/title title]
   :post [s/valid? :collector.core.specs/movie]}
  (let [movie {:title title}]
    (if (empty? kvs)
      movie
      (apply assoc movie kvs))))