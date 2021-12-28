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
           (let [processed-event (process-event "#inst \"2021-12-26T22:07:05.047-00:00\" create-empty-database #inst \"2021-12-26T22:07:05.047-00:00\"")
                 date (:date processed-event)
                 function (:function processed-event)]
             (is (s/valid? :collector.core.specs/date date))
             (is (fn? function)))
           (let [processed-event (process-event "#inst \"2021-12-26T22:07:05.047-00:00\" add-movie % \"tt0000000\" \"test\"")
                 date (:date processed-event)
                 function (:function processed-event)]
             (is (s/valid? :collector.core.specs/date date))
             (is (fn? function)))
           (is (error? #"^Invalid event name! Event name:" #(process-event "#inst \"2021-12-26T22:07:05.047-00:00\" get-movie #inst \"2021-12-26T22:07:05.047-00:00\""))))}
  [event-string]
  {:pre  [(string? event-string)
          (boolean (re-find #"^#inst \"[0-9T:\.\-]+\" " event-string))]
   :post [(fn? (:function %))
          (s/valid? :collector.core.specs/date (:date %))]}
  (let [date-string (re-find #"^#inst \"[0-9T:\.\-]+\" " event-string)
        date (read-string date-string)
        function-string (clojure.string/replace event-string date-string "")
        function-name (re-find #"^[a-zA-Z\-]+" function-string)
        namespaced-function-name (case function-name
                                   "create-empty-database" (pr-str `create-empty-database)
                                   "add-movie" (pr-str `add-movie)
                                   "remove-movie" (pr-str `remove-movie)
                                   "update-movie" (pr-str `update-movie)
                                   (error "Invalid event name! Event name:" function-name))
        namespaced-function (eval (read-string (str "#("
                                                    (clojure.string/replace function-string function-name namespaced-function-name)
                                                    ")")))]
    {:date date :function namespaced-function}))

(defn load-database-file
  "Loads the given database file. The file must exist. Returns the loaded database."
  {:test (fn []
           (spit "test.db" "#inst \"2021-12-27T22:07:05.047-00:00\" create-empty-database #inst\"2021-12-26T22:07:05.047-00:00\"\n")
           (is (= (load-database-file "test.db")
                  {:date-created #inst"2021-12-26T22:07:05.047-00:00"}))
           (spit "test.db" "#inst \"2021-12-27T22:07:05.047-00:00\" add-movie % \"tt0000000\" \"test\"\n" :append true)
           (is (= (load-database-file "test.db")
                  {:date-created #inst"2021-12-26T22:07:05.047-00:00"
                   :date-updated #inst"2021-12-27T22:07:05.047-00:00"
                   :movies-db    {"tt0000000" {:title "test"}}}))
           (io/delete-file "test.db"))}
  [database-file-name]
  {:pre  [(s/valid? :collector.persistence.specs/file-name database-file-name)
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
           (spit "test.db" "create-something\n")
           (persist-event "test.db"
                          {:type :add-movie :args [:x 1 "y" true]}
                          #inst"2021-12-27T22:07:05.047-00:00")
           (is (= (slurp "test.db")
                  "create-something\n#inst \"2021-12-27T22:07:05.047-00:00\" add-movie % :x 1 \"y\" true\n"))
           (io/delete-file "test.db"))}
  [database-file-name event date]
  {:pre  [(s/valid? :collector.persistence.specs/file-name database-file-name)
          (.exists (io/file database-file-name))
          (s/valid? :collector.persistence.specs/event event)
          (s/valid? :collector.core.specs/date date)]
   :post [(nil? %)]}
  (spit database-file-name
        (str (pr-str date)
             (case (:type event)
               :add-movie " add-movie % "
               :update-movie " update-movie % "
               :remove-movie " remove-movie % ")
             (apply pr-str (:args event))
             "\n")
        :append true))