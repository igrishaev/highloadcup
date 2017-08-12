(ns highloadcup.spec
  (:require [clojure.spec.alpha :as s]))

(def invalid :clojure.spec.alpha/invalid)

(defn validate [spec value]
  (let [result (s/conform spec value)]
    (if (= result invalid)
      nil
      result)))

(def enum-gender #{"m" "f"})

(s/def :user/id int?)
(s/def :user/email string?)
(s/def :user/first_name string?)
(s/def :user/last_name string?)
(s/def :user/gender enum-gender)
(s/def :user/birth_date int?)

(s/def :user/create
  (s/keys :req-un [:user/id
                   :user/email
                   :user/first_name
                   :user/last_name
                   :user/gender
                   :user/birth_date]))

(s/def :user/update
  (s/keys :opt-un [:user/email
                   :user/first_name
                   :user/last_name
                   :user/gender
                   :user/birth_date]))

(s/def :location/id int?)
(s/def :location/place string?)
(s/def :location/country string?)
(s/def :location/city string?)
(s/def :location/distance int?)

(s/def :location/create
  (s/keys :req-un [:location/id
                   :location/place
                   :location/country
                   :location/city
                   :location/distance]))

(s/def :location/update
  (s/keys :opt-un [:location/place
                   :location/country
                   :location/city
                   :location/distance]))

(s/def :visit/id int?)
(s/def :visit/location int?)
(s/def :visit/user int?)
(s/def :visit/visited_at int?)
(s/def :visit/mark int?)

(s/def :visit/create
  (s/keys :req-un [:visit/id
                   :visit/location
                   :visit/user
                   :visit/visited_at
                   :visit/mark]))

(s/def :visit/update
  (s/keys :opt-un [:visit/location
                   :visit/user
                   :visit/visited_at
                   :visit/mark]))

(defn x-integer? [x]
  (if (integer? x)
    x
    (if (string? x)
      (try
        (Integer/parseInt x)
        (catch Exception e
          invalid))
      invalid)))

(def ->int (s/conformer x-integer?))

(s/def :opt.visits/fromDate ->int)
(s/def :opt.visits/toDate ->int)
(s/def :opt.visits/country string?)
(s/def :opt.visits/toDistance ->int)

(s/def :opt.visits/params
  (s/keys :opt-un [:opt.visits/fromDate
                   :opt.visits/toDate
                   :opt.visits/country
                   :opt.visits/toDistance]))


(s/def :opt.avg/fromDate ->int)
(s/def :opt.avg/toDate ->int)
(s/def :opt.avg/fromAge ->int)
(s/def :opt.avg/toAge ->int)
(s/def :opt.avg/gender enum-gender)

(s/def :opt.avg/params
  (s/keys :opt-un [:opt.avg/fromDate
                   :opt.avg/toDate
                   :opt.avg/fromAge
                   :opt.avg/toAge
                   :opt.avg/gender]))
