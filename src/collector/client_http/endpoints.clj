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


(ns collector.client-http.endpoints
  (:require [clojure.data.json :refer [read-str]]
            [collector.client-http.edn-api :refer [load-database!
                                                   add-video!
                                                   get-entry!
                                                   remove-video!
                                                   update-video!
                                                   get-available-database-files!]]))

(def allowed-ports "http://localhost:8000")

(defn allow-origin-response
  []
  {:status  200
   :headers {"Access-Control-Allow-Origin"  allowed-ports
             "Access-Control-Allow-Methods" "*"
             "Access-Control-Allow-Headers" "*"}})

(defn create-response
  [client-state]
  (println client-state)
  {:status  200
   :headers {"Content-Type"                 "application/json; charset=utf-8"
             "Access-Control-Allow-Origin"  allowed-ports
             "Access-Control-Allow-Methods" "*"
             "Access-Control-Allow-Headers" "*"}
   :body    client-state})

(defn handler!
  [request]
  (if (= (:request-method request) :options)
    (allow-origin-response)
    (let [uri (:uri request)
          ; when query exists
          query (:query-string request)
          ; when body contains params we will extract them
          params (when-let [body-as-stream (:body request)]
                   (-> body-as-stream
                       (slurp)
                       (read-str :key-fn keyword
                                 :value-fn (fn [_ value] (str value)))))]
      (println "Query:" query)
      (println "Uri:" uri)
      (println "Params:" params)
      (cond (= uri "/load-database")
            (create-response (load-database! (:database-file-name params)))

            (= uri "/add-video")
            (let [id (:id params)
                  title (:name params)]
              (create-response (add-video! id title)))

            (= uri "/remove-video")
            (let [id (:id params)]
              (create-response (remove-video! id)))

            (= uri "/update-video")
            (let [id (:id params)
                  title (:title params)]
              (create-response (update-video! id title)))

            (= uri "/get-entry")
            (create-response (get-entry! query))

            (= uri "/get-available-database-files")
            (create-response (get-available-database-files!))

            :else
            {:status  404
             :headers {"Content-Type" "text/html"}
             :body    "<h1>Missing endpoint!</h1>"}))))
