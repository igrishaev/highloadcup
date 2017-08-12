(ns highloadcup.api
  (:require [highloadcup.db :as db]
            [highloadcup.spec :as spec]))

(defn json-response
  ([body]
   (json-response 200 body))
  ([status body]
   {:status status
    :body body}))

(defn spec-wrapper [handler spec]
  (fn [{body :body :as request}]
    (if (spec/validate spec body)
      (handler request)
      (json-response 400 {}))))

(defn get-user
  [id]
  (if-let [user (db/get-user id)]
    (json-response user)
    (json-response 400 {})))

(defn get-location
  [id]
  (if-let [location (db/get-location id)]
    (json-response location)
    (json-response 400 {})))

(defn get-visit
  [id]
  (if-let [visit (db/get-visit id)]
    (json-response visit)
    (json-response 400 {})))

(defn update-user
  [{fields :body} id]
  (if-let [_ (db/get-user id)]
    (do (db/update-user id fields)
        (json-response {}))
    (json-response 404 {})))

(def update-user
  (-> update-user
      (spec-wrapper :user/update)))

(defn update-location
  [{fields :body} id]
  (if-let [_ (db/get-location id)]
    (do (db/update-location id fields)
        (json-response {}))
    (json-response 404 {})))

(def update-location
  (-> update-location
      (spec-wrapper :location/update)))

(defn update-visit
  [{fields :body} id]
  (if-let [_ (db/get-visit id)]
    (do (db/update-visit id fields)
        (json-response {}))
    (json-response 404 {})))

(def update-visit
  (-> update-visit
      (spec-wrapper :visit/update)))

(defn create-user
  [{fields :body}]
  (create-user fields)
  (json-response {}))

(def create-user
  (-> create-user
      (spec-wrapper :user/create)))

(defn create-location
  [{fields :body}]
  (create-location fields)
  (json-response {}))

(def create-location
  (-> create-location
      (spec-wrapper :location/create)))

(defn create-visit
  [{fields :body}]
  (create-visit fields)
  (json-response {}))

(def create-visit
  (-> create-visit
      (spec-wrapper :visit/create)))
