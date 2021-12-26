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

(ns collector.core.auxiliary-functions
  (:require [clojure.test :refer [is]])
  (:import (java.util Date Calendar)
           (java.text SimpleDateFormat)
           (clojure.lang ArraySeq)))

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
           (is (= (->> (parse-date "yyyy-MM-dd" "2014-12-23")
                       (.format (SimpleDateFormat. "yyyy.MM.dd")))
                  "2014.12.23")))}
  [pattern date-string]
  (.parse
    (SimpleDateFormat. pattern)
    date-string))

(defn error
  "Throws an exception with a message"
  {:test (fn []
           (is (= (try
                    (error "Error test!")
                    (catch Exception e
                      (ex-message e)))
                  "Error test!"))
           (is (= (try
                    (error "Error" "test!" 1 :mode)
                    (catch Exception e
                      (ex-message e)))
                  "Error test! 1 :mode")))}
  [& error-message]
  {:pre  [(instance? ArraySeq error-message)]
   :post [(instance? Exception %)]}
  (let [e (clojure.string/join " " (map str error-message))]
    (throw (new Exception e))))
