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
            [collector.core.constructors :refer [create-video
                                                 create-initial-database]]))

(defn create-empty-database
  "Creates a new empty database"
  {:test (fn []
           (is (s/valid? :collector.core.specs/database (create-empty-database (now) "clojure__test.db")))
           (is (= (create-empty-database #inst"2021-12-26T22:07:05.047-00:00" "clojure__test.db")
                  {:date-created #inst"2021-12-26T22:07:05.047-00:00"
                   :date-updated #inst"2021-12-26T22:07:05.047-00:00"
                   :file-name    "clojure__test.db"
                   :categories   {:audios    {}
                                  :books     {}
                                  :companies {}
                                  :games     {}
                                  :videos    {}
                                  :persons   {}}})))}
  [date filename]
  {:pre  [(s/valid? :collector.core.specs/date date)
          (s/valid? :collector.persistence.specs/database-file-name filename)]
   :post [(s/valid? :collector.core.specs/database %)]}
  (create-initial-database date filename))

(defn get-video
  "Returns the video from tha database if it exists, otherwise nil."
  {:test (fn []
           (is (= (get-video (-> (create-initial-database)
                                 (update-in [:categories :videos] #(assoc % "tt0000000" {:name "test"})))
                             "tt0000000")
                  {:name "test"}))
           (is (nil? (get-video (create-initial-database) "tt000000"))))}
  [database id]
  {:pre  [(s/valid? :collector.core.specs/database database)
          (s/valid? :video/id id)]
   :past [(or (s/valid? :collector.core.specs/video %) (nil? %))]}
  (get-in database [:categories :videos id]))

(defn add-video
  "Adds a new video to the state"
  {:test (fn []
           (let [db (create-initial-database)]
             (is (= (-> (add-video db "tt0000000" "video0")
                        (get-in [:categories :videos "tt0000000"]))
                    {:name "video0"}))
             (is (= (-> (add-video db "tt0000000" "video0")
                        (add-video "tt0000001" "video1")
                        (get-in [:categories :videos]))
                    {"tt0000000" {:name "video0"}
                     "tt0000001" {:name "video1"}}))
             (is (error? #"^A video with the same ID exists! ID:"
                         #(-> (add-video db "tt0000000" "video0")
                              (add-video "tt0000000" "video1"))))))}
  ([database id name & kvs]
   {:pre  [(s/valid? :collector.core.specs/database database)
           (s/valid? :video/id id)
           (s/valid? :collector.core.specs/name name)]
    :post [(s/valid? :collector.core.specs/database %)]}
   (let [video (apply create-video name kvs)]
     (if (get-video database id)
       (error "A video with the same ID exists! ID:" id)
       (update-in database [:categories :videos] #(assoc % id video))))))

(defn update-video
  "Updates the information of a video in the database with the provided values.
   The video must exist in the database. An updated database will be returned"
  {:test (fn []
           (is (= (-> (create-initial-database)
                      (update-in [:categories :videos] #(assoc % "tt0000000" {:name "original"}))
                      (update-video "tt0000000" :name "updated" :original-title "updated")
                      (get-video "tt0000000"))
                  {:name "updated" :original-title "updated"}))
           (is (error? #"The video you are trying to update does not exist in the database! Video ID:"
                       #(-> (create-initial-database)
                            (update-video "tt0000000" :name "updated")))))}
  [database id & kvs]
  {:pre  [(s/valid? :collector.core.specs/database database)
          (s/valid? :video/id id)]
   :post [(s/valid? :collector.core.specs/database %)]}
  (if-not (get-video database id)
    (error "The video you are trying to update does not exist in the database! Video ID:" id)
    (update-in database [:categories :videos id] #(apply assoc % kvs))))

(defn remove-video
  "Removes the given video from the database. The video must exist in the database.
  The updated database will be returned"
  {:test (fn []
           (is (= (-> (create-initial-database)
                      (update-in [:categories :videos] #(assoc % "tt0000000" {:name "test"}))
                      (remove-video "tt0000000")
                      (get-in [:categories :videos]))
                  {}))
           (is (error? #"^The selected video does not exist! Video ID:"
                       #(-> (create-initial-database)
                            (remove-video "tt0000000")))))}
  [database id]
  {:pre  [(s/valid? :collector.core.specs/database database)
          (s/valid? :video/id id)]
   :post [(s/valid? :collector.core.specs/database %)]}
  (if (get-video database id)
    (update-in database [:categories :videos] #(dissoc % id))
    (error "The selected video does not exist! Video ID:" id)))