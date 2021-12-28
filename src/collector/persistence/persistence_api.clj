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

(ns collector.persistence.persistence-api
  (:require [clojure.test :refer [is]]
            [clojure.spec.alpha :as s]
            [clojure.java.io :as io]
            [collector.core.core-api :refer :all]
            [collector.persistence.persistence :refer [load-database-file
                                                       persist-event]]
            [collector.utils.auxiliary-functions :refer [now]]))

(defn handle-event
  "Executes the given event. If execution is successful will append the event to the database-file.
  If both execution and persistence are successful the new updated database will be returned,
  otherwise exception will be thrown."
  {:test (fn []
           (is (s/valid? :collector.core.specs/database (handle-event "test.db")))
           (is (.exists (io/file "test.db")))
           (is (s/valid? :collector.core.specs/database (handle-event "test.db")))
           (io/delete-file "test.db")
           (is (= (-> (handle-event "test.db")
                      (handle-event "test.db" {:type :add-movie :args ["tt0000000" "test1"]})
                      (handle-event "test.db" {:type :add-movie :args ["tt0000001" "test2"]})
                      (handle-event "test.db" {:type :remove-movie :args ["tt0000001"]})
                      (get :movies-db))
                  {"tt0000000" {:title "test1"}}))
           (is (= (-> (handle-event "test.db")
                      (:movies-db))
                  {"tt0000000" {:title "test1"}}))
           (io/delete-file "test.db"))}
  ([database-file-name]
   {:pre  [(s/valid? :collector.persistence.specs/file-name database-file-name)]
    :post [(s/valid? :collector.core.specs/database %)]}
   (if (.exists (io/file database-file-name))
     (load-database-file database-file-name)
     (let [date (now)
           database (create-empty-database date)]
       (spit database-file-name (str (pr-str date) " create-empty-database " (pr-str date) "\n"))
       database)))
  ([database database-file-name event]
   {:pre  [(s/valid? :collector.core.specs/database database)
           (s/valid? :collector.persistence.specs/file-name database-file-name)
           (.exists (io/file database-file-name))
           (s/valid? :collector.persistence.specs/event event)]
    :post [(s/valid? :collector.core.specs/database %)]}
   (let [date (now)
         updated-database (assoc database :date-updated date)
         resulting-database (case (:type event)
                              :add-movie (apply add-movie updated-database (:args event))
                              :update-movie (apply update-movie updated-database (:args event))
                              :remove-movie (apply remove-movie updated-database (:args event)))]
     (persist-event database-file-name event date)
     resulting-database)))