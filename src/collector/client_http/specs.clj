(ns collector.client-http.specs
  (:require [clojure.spec.alpha :as s]))

(s/def ::category #{"All" "Movies"})
(s/def ::id string?)
(s/def ::name string?)
(s/def ::entry (s/keys :req-un [::id
                                ::name
                                ::category]))
(s/def ::entries (s/coll-of ::entry :kind vector?))
(s/def ::client-db (s/keys :req-un [::category
                                    ::entries]))