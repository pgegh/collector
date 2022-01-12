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


(ns collector.client-http.edn-api
  (:require [clojure.spec.alpha :as s]
            [clojure.data.json :refer [write-str]]
            [collector.client-http.mappers :refer [db->client-db
                                                   video->client-video
                                                   filenames->client-filenames]]
            [collector.core.core-api :refer [get-video]]
            [collector.persistence.persistence-api :refer [handle-event
                                                           get-available-database-files]]))

(defonce database-atom (atom nil))
(defonce database-file-name-atom (atom nil))

(defn load-database!
  [database-file-name]
  {:pre [(s/valid? :collector.persistence.specs/file-name database-file-name)]}
  (let [extended-database-file-name (if (re-find #".[dD][bB]$" database-file-name)
                                      database-file-name
                                      (str database-file-name ".db"))
        database (as-> (reset! database-file-name-atom extended-database-file-name) $
                       (reset! database-atom (handle-event $)))]
    (time (write-str (db->client-db database)))))

(defn add-video!
  [id name]
  (time (write-str (db->client-db (swap! database-atom #(handle-event % @database-file-name-atom {:type :add-video
                                                                                                  :args [id name]}))))))

(defn remove-video!
  [id]
  (time (write-str (db->client-db (swap! database-atom #(handle-event % @database-file-name-atom {:type :remove-video
                                                                                                  :args [id]}))))))

(defn update-video!
  [id name]
  (time (write-str (db->client-db (swap! database-atom #(handle-event % @database-file-name-atom {:type :update-video
                                                                                                  :args [id :name name]}))))))

(defn get-video!
  [id]
  (time (write-str (video->client-video (get-video @database-atom id)))))

(defn get-available-database-files!
  []
  (time (write-str (filenames->client-filenames (get-available-database-files)))))