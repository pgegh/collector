(ns collector-core.specs
  (:require [clojure.spec.alpha :as s])
  (:import (java.util Date)))

(s/def ::date #(instance? Date %))
(s/def ::date-created ::date)
(s/def ::initial-database (s/keys :req-un [::date-created]))