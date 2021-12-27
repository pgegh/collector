(ns collector.persistence.persistence
  (:require [clojure.test :refer [is]]
            [clojure.spec.alpha :as s]
            [clojure.java.io :as io]
            [collector.core.core-api :refer :all]
            [collector.utils.auxiliary-functions :refer [error
                                                         error?]]))

(defn add-function-namespace
  "Finds what name space the function has. This is to help `eval` to execute the function.
  This approach is better than storing the namespace in the database-file, so that the program is more dynamic."
  {:test (fn []
           (is (= (add-function-namespace "create-empty-database arg1 arg2")
                  (str "(" (pr-str `create-empty-database) " arg1 arg2)")))
           (is (= (add-function-namespace "add-movie arg1 arg2")
                  (str "(" (pr-str `add-movie) " arg1 arg2)")))
           (is (= (add-function-namespace "remove-movie arg1 arg2")
                  (str "(" (pr-str `remove-movie) " arg1 arg2)")))
           (is (= (add-function-namespace "update-movie arg1 arg2")
                  (str "(" (pr-str `update-movie) " arg1 arg2)")))
           (is (error? #"^Invalid event name! Event name:" #(add-function-namespace "Invalid-event"))))}
  [function-string]
  {:pre  [(string? function-string)]
   :post [(string? %)
          (boolean (re-find #"^\(.+\)$" %))]}
  (let [function-name (re-find #"^[a-zA-Z\-]+" function-string)
        namespaced-function-name (case function-name
                                   "create-empty-database" (pr-str `create-empty-database)
                                   "add-movie" (pr-str `add-movie)
                                   "remove-movie" (pr-str `remove-movie)
                                   "update-movie" (pr-str `update-movie)
                                   (error "Invalid event name! Event name:" function-name))]
    (str "(" (clojure.string/replace function-string function-name namespaced-function-name) ")")))

(defn load-database-file
  "Loads the given database file. The file must exist. Returns the loaded database."
  {:test (fn []
           (spit "test.db" "create-empty-database #inst\"2021-12-26T22:07:05.047-00:00\"\n")
           (is (= (load-database-file "test.db")
                  {:date-created #inst"2021-12-26T22:07:05.047-00:00"}))
           ;; todo: test with multiple events.
           (io/delete-file "test.db"))}
  [database-file-name]
  {:pre  [(s/valid? :collector.persistence.specs/file-name database-file-name)
          (.exists (io/file database-file-name))]
   :post [s/valid? :collector.core.specs/database %]}
  (as-> (slurp database-file-name) $
        (clojure.string/split-lines $)
        (map add-function-namespace $)
        (reduce (fn [acc event] ((eval (read-string event)) acc)) (eval (read-string (first $))) (drop 1 $))))