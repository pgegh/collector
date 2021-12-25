(ns collector-core.specs
  (:require [clojure.spec.alpha :as s]
            [collector-core.auxiliary-functions :refer [current-year]])
  (:import (java.util Date)))

(s/def ::date #(instance? Date %))
(s/def ::date-created ::date)
(s/def ::initial-database (s/keys :req-un [::date-created]))

(s/def ::imdb-movie-id (s/and string?
                              #(re-find #"^tt[0-9]+$")))
(s/def ::title string?)
(s/def ::original-title string?)
(s/def ::type string?)
(s/def ::year (s/and int?
                     #(< 1900 %)
                     #(>= (current-year) %)))
(s/def ::image-url uri?)
(s/def ::release-date ::date)
(s/def ::runtime-minutes (s/and int? pos?))
(s/def ::plot string?)
(s/def ::imdb-person-id (s/and string?
                               #(re-find #"^nm[0-9]+$")))
(s/def ::directors (s/coll-of ::imdb-person-id :type set?))
(s/def ::writers (s/coll-of ::imdb-person-id :type set?))
(s/def ::stars (s/coll-of ::imdb-person-id :type set?))
(s/def ::actors (s/coll-of ::imdb-person-id :type set?))
(s/def ::genres string?)
(s/def ::imdb-company-id (s/and string?
                                #(re-find #"^co[0-9]+$")))
(s/def ::companies (s/coll-of ::imdb-company-id :type set?))
(s/def ::country string?)
(s/def ::countries (s/coll-of ::country :type set?))
(s/def ::language string?)
(s/def ::languages (s/coll-of ::language :type set?))
(s/def ::content-rating string?)
(s/def ::imdb-rating (s/and float?
                            #(<= 0.0 %)
                            #(>= 10.0 %)))
(s/def ::imdb-number-of-votes (s/and int? pos?))


(s/def ::movie (s/keys :req-un [::imdb-movie-id]
                       :opt-un [::title
                                ::original-title
                                ::type
                                ::year
                                ::image-url
                                ::release-date
                                ::runtime-minutes
                                ::plot
                                ::directors
                                ::writers
                                ::stars
                                ::actors
                                ::genres
                                ::companies
                                ::countries
                                ::languages
                                ::content-rating
                                ::imdb-rating
                                ::imdb-number-of-votes
                                ]))