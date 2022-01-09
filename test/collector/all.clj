(ns collector.all
  (:require [clojure.test :refer :all]
            [collector.client-http.mappers]
            [collector.core.constructors]
            [collector.core.core]
            [collector.core.core-api]
            [collector.core.specs]
            [collector.persistence.persistence]
            [collector.persistence.persistence-api]
            [collector.persistence.specs]
            [collector.utils.auxiliary-functions]))

(deftest a-test
  "Bootstrapping with the required namespaces, finds all the firestone.* namespaces (except this one),
         requires them, and runs all their tests."
  (let [namespaces (->> (all-ns)
                        (map str)
                        (filter (fn [x] (re-matches #"collector\..*" x)))
                        (remove (fn [x] (= "collector.all" x)))
                        (map symbol))]
    (is (successful? (time (apply run-tests namespaces))))))
