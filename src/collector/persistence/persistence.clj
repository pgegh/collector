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

(ns collector.persistence.persistence
  (:require [clojure.test :refer [is]]
            [clojure.spec.alpha :as s]
            [clojure.java.io :as io]
            [collector.core.core-api :refer :all]
            [collector.utils.auxiliary-functions :refer [error
                                                         error?]]))

(defn process-event
  "Finds what name space the function has. This is to help `eval` to execute the function.
  This approach is better than storing the namespace in the database-file, so that the program is more dynamic."
  {:test (fn []
           (let [processed-event (process-event "#inst \"2021-12-26T22:07:05.047-00:00\" create-empty-database #inst \"2021-12-26T22:07:05.047-00:00\" \"clojure__test.db\"")
                 date (:date processed-event)
                 function (:function processed-event)]
             (is (s/valid? :collector.core.specs/date date))
             (is (fn? function)))
           (let [processed-event (process-event "#inst \"2021-12-26T22:07:05.047-00:00\" add-video % \"tt0000000\" \"test\"")
                 date (:date processed-event)
                 function (:function processed-event)]
             (is (s/valid? :collector.core.specs/date date))
             (is (fn? function)))
           (is (error? #"^Invalid event name! Event name:" #(process-event "#inst \"2021-12-26T22:07:05.047-00:00\" get-video #inst \"2021-12-26T22:07:05.047-00:00\""))))}
  [event-string]
  {:pre  [(string? event-string)
          (boolean (re-find #"^#inst \"[0-9T:\.\-]+\" " event-string))]
   :post [(fn? (:function %))
          (s/valid? :collector.core.specs/date (:date %))]}
  (let [date-string (re-find #"^#inst \"[0-9T:\.\-]+\" " event-string)
        date (read-string date-string)
        function-string (clojure.string/replace event-string #"^#inst \"[0-9T:\.\-]+\" " "")
        function-name (re-find #"^[a-zA-Z\-]+" function-string)
        namespaced-function-name (case function-name
                                   "create-empty-database" (pr-str `create-empty-database)
                                   "add-video" (pr-str `add-video)
                                   "remove-video" (pr-str `remove-video)
                                   "update-video" (pr-str `update-video)
                                   (error "Invalid event name! Event name:" function-name))
        namespaced-function (eval (read-string (str "#("
                                                    (clojure.string/replace function-string function-name namespaced-function-name)
                                                    ")")))]
    {:date date :function namespaced-function}))

(defn load-database-file
  "Loads the given database file. The file must exist. Returns the loaded database."
  {:test (fn []
           (spit "clojure__test.db" "#inst \"2021-12-27T22:07:05.047-00:00\" create-empty-database #inst\"2021-12-26T22:07:05.047-00:00\" \"clojure__test.db\"\n")
           (is (= (-> (load-database-file "clojure__test.db")
                      (get :date-created))
                  #inst"2021-12-26T22:07:05.047-00:00"))
           (spit "clojure__test.db" "#inst \"2021-12-27T22:07:05.047-00:00\" add-video % \"tt0000000\" \"test\"\n" :append true)
           (is (= (-> (load-database-file "clojure__test.db")
                      (get-video "tt0000000"))
                  {:name "test"}))
           (io/delete-file "clojure__test.db"))}
  [database-file-name]
  {:pre  [(s/valid? :collector.persistence.specs/database-file-name database-file-name)
          (.exists (io/file database-file-name))]
   :post [(s/valid? :collector.core.specs/database %)]}
  (as-> (slurp database-file-name) $
        (clojure.string/split-lines $)
        (map process-event $)
        (reduce (fn [acc event] (-> acc
                                    (assoc :date-updated (:date event))
                                    ((:function event))))
                ((:function (first $)))
                (drop 1 $))))

(defn persist-event
  "Will append the database file with a new event"
  {:test (fn []
           (spit "clojure__test.db" "create-something\n")
           (persist-event "clojure__test.db"
                          {:type :add-video :args [:x 1 "y" true]}
                          #inst"2021-12-27T22:07:05.047-00:00")
           (is (= (slurp "clojure__test.db")
                  "create-something\n#inst \"2021-12-27T22:07:05.047-00:00\" add-video % :x 1 \"y\" true\n"))
           (io/delete-file "clojure__test.db"))}
  [database-file-name event date]
  {:pre  [(s/valid? :collector.persistence.specs/database-file-name database-file-name)
          (.exists (io/file database-file-name))
          (s/valid? :collector.persistence.specs/event event)
          (s/valid? :collector.core.specs/date date)]
   :post [(nil? %)]}
  (spit database-file-name
        (str (pr-str date)
             (case (:type event)
               :add-video " add-video % "
               :update-video " update-video % "
               :remove-video " remove-video % ")
             (apply pr-str (:args event))
             "\n")
        :append true))