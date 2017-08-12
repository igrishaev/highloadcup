(ns highloadcup.spec
  (:require [clojure.spec.alpha :as s]))

(def invalid :clojure.spec.alpha/invalid)

(defn validate [spec value]
  (if (= (s/conform spec value) invalid)
    nil
    value))

(s/def :user/id int?)
(s/def :user/email string?)
(s/def :user/first_name string?)
(s/def :user/last_name string?)
(s/def :user/gender #{"m" "f"})
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
