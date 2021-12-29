(ns collector.client-http.server
  (:require [org.httpkit.server :refer [run-server]]
            [collector.client-http.endpoints :refer [handler!]]))

(defonce server-atom (atom nil))

(defn server-started?
  []
  (boolean (deref server-atom)))

(defn start-server!
  []
  (if (server-started?)
    "The server is already started!"
    (reset! server-atom
            (run-server #'handler! {:port 8001}))))

(defn stop-server!
  []
  (if-not (server-started?)
    "The server is not started!"
    (let [stop-server-fn (deref server-atom)]
      (stop-server-fn :timeout 100)
      (reset! server-atom nil))))

(comment
  (start-server!)
  (server-started?)
  (stop-server!)
  )