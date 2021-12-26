(ns collector.core.specs
  (:require [clojure.spec.alpha :as s]
            [collector.core.auxiliary-functions :refer [current-year]])
  (:import (java.util Date)))

(s/def ::date #(instance? Date %))
(s/def ::date-created ::date)
(s/def ::initial-database (s/keys :req-un [::date-created]))

(s/def ::imdb-movie-id (s/and string?
                              #(re-find #"^tt[0-9]+$" %)))
(s/def ::title string?)
(s/def ::name string?)
(s/def ::birth ::date)
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
                               #(re-find #"^nm[0-9]+$" %)))
(s/def ::directors (s/coll-of ::imdb-person-id :type set?))
(s/def ::writers (s/coll-of ::imdb-person-id :type set?))
(s/def ::stars (s/coll-of ::imdb-person-id :type set?))
(s/def ::actors (s/coll-of ::imdb-person-id :type set?))
(s/def ::genres string?)
(s/def ::imdb-company-id (s/and string?
                                #(re-find #"^co[0-9]+$" %)))
(s/def ::companies (s/coll-of ::imdb-company-id :type set?))
(s/def ::country string?)
(s/def ::countries (s/coll-of ::country :type set?))
(s/def ::language string?)
(s/def ::languages (s/coll-of ::language :type set?))
(s/def ::nationality ::country)
(s/def ::gender #{:male :female})
(s/def ::established ::date)
(s/def ::occupation string?)
(s/def ::person (s/keys :req-un [::name]
                        :opt-un [::gender
                                 ::birth
                                 ::occupation
                                 ::nationality]))
(s/def ::persons-db (s/map-of ::imdb-person-id ::person))
(s/def ::content-rating string?)
(s/def ::imdb-rating (s/and float?
                            #(<= 0.0 %)
                            #(>= 10.0 %)))
(s/def ::imdb-number-of-votes (s/and int? pos?))


(s/def ::movie (s/keys :req-un [::title]
                       :opt-un [::original-title
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
                                ::imdb-number-of-votes]))
(s/def ::movies-db (s/map-of ::imdb-movie-id ::movie))
(s/def ::date-updated ::date)


(s/def ::company (s/keys :req-un [::name]
                         :opt-un [::established
                                  ::nationality]))
(s/def ::companies-db (s/map-of ::imdb-company-id ::company))

(s/def ::database (s/keys :req-un [::date-created]
                          :opt-un [::date-updated
                                   ::movies-db
                                   ::persons-db
                                   ::companies-db]))