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


(ns collector.client-http.specs
  (:require [clojure.spec.alpha :as s]))

(s/def ::category #{:audios :books :companies :games :videos :persons})
(s/def ::selected-category #{"All" "Audios" "Books" "Companies" "Games" "Videos" "Persons"})
(s/def ::id (s/or :a :audio/id
                  :b :book/id
                  :c :company/id
                  :d :game/id
                  :c :video/id
                  :e :person/id))
(s/def ::name string?)
(s/def ::readable-date #(re-find #"^[0-9][0-9]/[0-9][0-9]/[0-9][0-9][0-9][0-9]$" %))
(s/def ::db-date-created ::readable-date)
(s/def ::db-file-name :collector.persistence.specs/database-file-name)
(s/def ::db-date-updated ::readable-date)
(s/def ::categories (s/coll-of keyword? :type vector?))
(s/def ::entry (s/keys :req-un [::id
                                ::name
                                ::category]))
(s/def ::entries (s/coll-of ::entry :kind vector?))
(s/def ::client-db (s/keys :req-un [::selected-category
                                    ::db-date-created
                                    ::db-file-name
                                    ::db-date-updated
                                    ::entries
                                    ::categories]))