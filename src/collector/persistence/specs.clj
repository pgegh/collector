(ns collector.persistence.specs
  (:require [clojure.spec.alpha :as s]))

(s/def ::file-name (s/and string? #(re-find #"^[\w\-. ]+$" %)))
(s/def ::args vector?)
(s/def ::type #{:add-movie
                :update-movie
                :remove-movie})
(s/def ::event (s/keys :req-un [::type
                                ::args]))
