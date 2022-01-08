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


(ns collector.client-http.specs
  (:require [clojure.spec.alpha :as s]))

(s/def ::category #{"All" "Movies"})
(s/def ::id string?)
(s/def ::name string?)
(s/def ::entry (s/keys :req-un [::id
                                ::name
                                ::category]))
(s/def ::entries (s/coll-of ::entry :kind vector?))
(s/def ::client-db (s/keys :req-un [::category
                                    ::entries]))