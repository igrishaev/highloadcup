(ns highloadcup.warmup
  (:require [clj-http.client :as client]
            [highloadcup.conf :refer [conf]]))

(defn get-url [path]
  (format "http://127.0.0.1:%d%s"
          (:server-port conf)
          path))

(def base-params
  {:as :json
   :throw-exceptions false})

(defn run [n]

  (doseq [id (take n (next (range)))]

    (let [url (get-url (format "/users/%d" id))]
      (client/get url base-params))

    (let [url (get-url (format "/locations/%d" id))]
      (client/get url base-params))

    (let [url (get-url (format "/visits/%d" id))]
      (client/get url base-params))

    (let [url (get-url (format "/users/%d/visits" id))]
      (client/get url base-params))

    (let [url (get-url (format "/locations/%d/avg" id))]
      (client/get url base-params))))
