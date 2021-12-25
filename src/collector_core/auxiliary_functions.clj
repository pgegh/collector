;; Copyright © 2021 Hovig Manjikian
;;
;; This file is part of collector-core.
;;
;; collector-core is free software: you can redistribute it and/or modify
;; it under the terms of the GNU General Public License as published by
;; the Free Software Foundation, either version 3 of the License, or
;; (at your option) any later version.
;;
;; collector-core is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU General Public License for more details.
;;
;; You should have received a copy of the GNU General Public License
;; along with json.  If not, see <https://www.gnu.org/licenses/>.

(ns collector-core.auxiliary-functions
  (:require [clojure.test :refer [is]])
  (:import (java.util Date Calendar)
           (java.text SimpleDateFormat)))

(defn now
  "Returns a Java object representing the current date"
  {:test (fn []
           ;; Should return a Java object
           (is (re-find #"^#inst " (pr-str (new Date)))))}
  []
  (new Date))

(defn current-year
  "Returns the current year as Int"
  {:test (fn []
           (let [year (current-year)]
             (is (and (int? year)
                      (< 2020 year)))))}
  []
  (.get (Calendar/getInstance)
        (Calendar/YEAR)))

(defn parse-date
  "Parses a date form a string"
  {:test (fn []
           (is (= (parse-date "yyyy-MM-dd" "2014-12-23") #inst"2014-12-22T23:00:00.000-00:00")))}
  [pattern date-string]
  (.parse
    (SimpleDateFormat. pattern)
    date-string))