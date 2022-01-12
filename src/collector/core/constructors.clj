;; Copyright © 2021-2022 Hovig Manjikian
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
           (is (s/valid? :collector.core.specs/database (create-initial-database (now) "clojure__test.db")))
           (is (s/valid? :collector.core.specs/database (create-initial-database))))}
  ([]
   {:post [s/valid? :collector.core.specs/date %]}
   (create-initial-database (now) "clojure__test.db"))
  ([date filename]
   {:pre  [(s/valid? :collector.core.specs/date date)
           (s/valid? :collector.persistence.specs/database-file-name filename)]
    :post [s/valid? :collector.core.specs/database %]}
   {:date-created date
    :date-updated date
    :file-name    filename
    :categories   {:audios    {}
                   :books     {}
                   :companies {}
                   :games     {}
                   :videos    {}
                   :persons   {}}}))

(defn create-video
  "Creates a video entry"
  {:test (fn []
           (is (= (create-video "video-name")
                  {:name "video-name"}))
           (is (= (create-video "video-name"
                                :year 1994)
                  {:name "video-name"
                   :year 1994})))}
  [name & kvs]
  {:pre  [s/valid? :collector.core.specs/name name]
   :post [s/valid? :collector.core.specs/video]}
  (let [video {:name name}]
    (if (empty? kvs)
      video
      (apply assoc video kvs))))