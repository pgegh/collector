(ns collector.client-http.endpoints
  (:require [clojure.data.json :refer [read-str]]
            [collector.client-http.edn-api :refer [load-database!
                                                   add-movie!
                                                   get-movie!
                                                   remove-movie!
                                                   update-movie!
                                                   get-available-database-files!]]))


(defn create-response
  [client-state]
  (println client-state)
  {:status  200
   :headers {"Content-Type"                 "application/edn; charset=utf-8"
             "Access-Control-Allow-Origin"  "*"
             "Access-Control-Allow-Methods" "*"}
   :body    client-state})

(defn handler!
  [request]
  ;(pprint request)
  (let [uri (:uri request)
        ; when body contains params we will extract them
        params (when-let [body-as-stream (:body request)]
                 (-> body-as-stream
                     (slurp)
                     (read-str :key-fn keyword
                               :value-fn (fn [_ value] (str value)))))]
    (println uri)
    (println params)
    (cond (= uri "/load-database")
          (create-response (load-database! (:database-file-name params)))

          (= uri "/add-movie")
          (let [imdb-movie-id (:imdb-movie-id params)
                title (:title params)]
            (create-response (add-movie! imdb-movie-id title)))

          (= uri "/remove-movie")
          (let [imdb-movie-id (:imdb-movie-id params)]
            (create-response (remove-movie! imdb-movie-id)))

          (= uri "/update-movie")
          (let [imdb-movie-id (:imdb-movie-id params)
                title (:title params)]
            (create-response (update-movie! imdb-movie-id title)))

          (= uri "/get-movie")
          (create-response (get-movie! (:imdb-movie-id params)))

          (= uri "/get-available-database-files")
          (create-response (get-available-database-files!))

          :else
          {:status  404
           :headers {"Content-Type" "text/html"}
           :body    "<h1>Missing endpoint!</h1>"})))