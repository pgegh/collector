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