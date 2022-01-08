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