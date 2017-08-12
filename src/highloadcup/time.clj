(ns highloadcup.time
  #_(:require [clj-time.core :as t]
            [clj-time.coerce :as c]))

#_(t/in-years (t/interval (t/date-time 1986) (t/date-time 1990))
              (t/date-time 1987))

#_(def msec-per-year
  (* 60 60 24 365 1000))

(def sec-per-year
  (* 60 60 24 365))

(defn get-timestamp []
  (quot (System/currentTimeMillis) 1000))

(defn get-age
  [timestamp]
  (let [ts-diff (- (get-timestamp) timestamp)]
    (quot ts-diff sec-per-year)))
