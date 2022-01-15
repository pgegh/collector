;; Copyright © 2021-2022 Hovig Manjikian
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

(ns collector.core.specs
  (:require [clojure.spec.alpha :as s]
            [collector.utils.auxiliary-functions :refer [current-year]])
  (:import (java.util Date)))

(s/def ::date #(instance? Date %))
(s/def ::date-created ::date)

(s/def :imdb-movie/id (s/and string?
                             #(re-find #"^tt[0-9]+$" %)))
(s/def :imdb-person/id (s/and string?
                              #(re-find #"^nm[0-9]+$" %)))
(s/def :imdb-company/id (s/and string?
                               #(re-find #"^co[0-9]+$" %)))

(s/def :audio/id (s/and string?
                        #(re-find #"^au[0-9]{7}$" %)))
(s/def :game/id (s/and string?
                       #(re-find #"^gm[0-9]{7}$" %)))
(s/def :book/id (s/and string?
                       #(re-find #"^bk[0-9]{7}$" %)))
(s/def :video/id (s/or :imdb :imdb-movie/id
                       :local (s/and string?
                                     #(re-find #"^vd[0-9]{7}$" %))))
(s/def :company/id (s/or :imdb :imdb-company/id
                         :local (s/and string?
                                       #(re-find #"^cm[0-9]{7}$" %))))
(s/def :person/id (s/or :imdb :imdb-person/id
                        :local (s/and string?
                                      #(re-find #"^pr[0-9]{7}$" %))))

(s/def :collector/id (s/or :a :audio/id
                           :b :book/id
                           :c :company/id
                           :d :game/id
                           :e :person/id
                           :f :video/id))

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
(s/def ::directors (s/coll-of :person/id :type set?))
(s/def ::writers (s/coll-of :person/id :type set?))
(s/def ::stars (s/coll-of :person/id :type set?))
(s/def ::actors (s/coll-of :person/id :type set?))
(s/def ::genres (s/coll-of ::genre :type set?))
(s/def ::established ::date)
(s/def ::company (s/keys :req-un [::name]
                         :opt-un [::established
                                  ::nationality]))
(s/def ::companies (s/coll-of :company/id :type set?))
(s/def ::country string?)
(s/def ::countries (s/coll-of ::country :type set?))
(s/def ::language string?)
(s/def ::languages (s/coll-of ::language :type set?))
(s/def ::nationality ::country)
(s/def ::gender #{:male :female})
(s/def ::occupation string?)
(s/def ::occupations (s/coll-of ::occupation :type set?))
(s/def ::person (s/keys :req-un [::name]
                        :opt-un [::gender
                                 ::birth
                                 ::occupations
                                 ::nationality]))
(s/def :database/persons (s/map-of :person/id ::person))
(s/def ::content-rating string?)
(s/def ::imdb-rating (s/and float?
                            #(<= 0.0 %)
                            #(>= 10.0 %)))
(s/def ::imdb-number-of-votes (s/and int? pos?))

(s/def ::artists (s/coll-of :person/id :type set?))
(s/def ::authors (s/coll-of :person/id :type set?))
(s/def ::rating (s/and int? #(>= % 0) #(<= % 10)))
(s/def ::runtime-seconds (s/and int? pos?))
(s/def ::genre string?)
(s/def ::album string?)
(s/def ::franchise string?)

(s/def ::audio (s/keys :req-un [::name]
                       :opt-un [::artists
                                ::year
                                ::image-url
                                ::runtime-seconds
                                ::genre
                                ::album
                                ::franchise
                                ::languages
                                ::rating]))
(s/def :book/category string?)
(s/def ::synopsis string?)
(s/def :book/categories (s/coll-of :book/category :type set?))
(s/def :book/edition (s/and int? pos?))
(s/def ::number-of-pages (s/and int? pos?))
(s/def ::isbn #(boolean (re-find #"^[0-9]{13}$" %)))

(s/def ::publisher ::company)

(s/def ::book (s/keys :req-un [::name]
                      :opt-un [::authors
                               ::publisher
                               ::year
                               :book/edition
                               :book/categories
                               ::franchise
                               ::synopsis
                               ::number-of-pages
                               ::language
                               ::isbn
                               ::rating]))

(s/def ::game (s/keys :req-un [::name]
                      :opt-un [::year
                               ::franchise
                               ::image-url
                               ::synopsis
                               ::directors
                               ::genres
                               ::companies
                               ::rating]))

(s/def ::video (s/keys :req-un [::name]
                       :opt-un [::original-title
                                ::type
                                ::year
                                ::image-url
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

(s/def :collector/entry (s/or :a ::audio
                              :b ::book
                              :c ::game
                              :d ::video))

(s/def :database/videos (s/map-of :video/id ::video))
(s/def ::date-updated ::date)



(s/def :database/audios (s/map-of :audio/id ::audio))
(s/def :database/books (s/map-of :book/id ::book))
(s/def :database/companies (s/map-of :company/id ::company))
(s/def :database/games (s/map-of :game/id ::game))
(s/def :database/videos (s/map-of :video/id ::video))
(s/def :database/persons (s/map-of :person/id ::person))

(s/def :entry/categories (s/keys :req-un [:database/audios
                                          :database/books
                                          :database/companies
                                          :database/games
                                          :database/videos
                                          :database/persons
                                          ]))

(s/def ::database (s/keys :req-un [::date-created
                                   ::date-updated
                                   ::file-name
                                   :entry/categories]))