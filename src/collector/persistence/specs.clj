(ns collector.persistence.specs
  (:require [clojure.spec.alpha :as s]))

(s/def ::file-name (s/and string? #(re-find #"^[\w\-. ]+$" %)))
(s/def ::persist boolean?)
(s/def ::function fn?)
(s/def ::event (s/keys :req-un [::persist
                                ::function]))
