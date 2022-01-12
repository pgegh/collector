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


(ns collector.client-http.mappers
  (:require [clojure.spec.alpha :as s]
            [clojure.test :refer [is]]
            [collector.core.constructors :refer [create-initial-database]]
            [collector.core.core-api :refer [add-video]]
            [collector.client-http.specs :refer :all]
            [collector.core.specs :refer :all])
  (:import (java.text SimpleDateFormat)))

(defn get-entries
  {:test (fn []
           (is (= (get-entries (create-initial-database))
                  []))
           (let [database (-> (create-initial-database)
                              (add-video "tt0000000" "test1")
                              (add-video "tt0000001" "test2")
                              (update-in [:categories :audios] #(assoc % "au0000000" {:name "test3"})))]
             (is (some #{{:id "tt0000000" :name "test1" :category :videos}} (get-entries database)))
             (is (some #{{:id "tt0000001" :name "test2" :category :videos}} (get-entries database)))
             (is (some #{{:id "au0000000" :name "test3" :category :audios}} (get-entries database)))
             ))}
  [database]
  {:pre  [(s/valid? :collector.core.specs/database database)]
   :post [(s/valid? :collector.client-http.specs/entries %)]}
  (let [categories (-> (:categories database)
                       (keys))]
    (reduce
      (fn [acc category] (into acc (let [ids (-> (get-in database [:categories category])
                                                 (keys))]
                                     (reduce
                                       (fn [acc id] (conj acc {:id       id
                                                               :name     (get-in database [:categories category id :name])
                                                               :category category}))
                                       []
                                       ids))))
      []
      categories)))

(defn db->client-db
  [database]
  {:pre  [(s/valid? :collector.core.specs/database database)]
   :post [(s/valid? :collector.client-http.specs/client-db %)]}
  {:selected-category "All"
   :db-date-created   (.format (SimpleDateFormat. "dd/MM/yyyy") (:date-created database))
   :db-file-name      (:file-name database)
   :db-date-updated   (.format (SimpleDateFormat. "dd/MM/yyyy") (:date-updated database))
   :entries           (get-entries database)
   :categories        (into [] (keys (:categories database)))})


(defn video->client-video
  [video]
  video)

(defn filenames->client-filenames
  [filenames]
  filenames)