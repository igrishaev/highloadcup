(ns highloadcup.api
  (:require [highloadcup.db :as db]
            [highloadcup.spec :as spec]))

(defn json-response
  ([body]
   (json-response 200 body))
  ([status body]
   {:status status
    :body body}))

(defn get-user-by-id
  [id]
  (if-let [user (db/get-user-by-id id)]
    (json-response user)
    (json-response 400 {})))

(defn get-location-by-id
  [id]
  (if-let [location (db/get-location-by-id id)]
    (json-response location)
    (json-response 400 {})))

(defn get-visit-by-id
  [id]
  (if-let [visit (db/get-visit-by-id id)]
    (json-response visit)
    (json-response 400 {})))
