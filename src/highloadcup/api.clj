(ns highloadcup.api
  (:require [highloadcup.db :as db]
            [highloadcup.spec :as spec]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params
             :refer [wrap-keyword-params]]))

(defn json-response
  ([body]
   (json-response 200 body))
  ([status body]
   {:status status
    :body body}))

(defn wrap-spec-body
  [handler spec]
  (fn [{body :body :as request} & args]
    (if (spec/validate spec body)
      (apply handler request args)
      (json-response 400 {}))))

(defn wrap-spec-params
  [handler spec]
  (fn [{params :params :as request} & args]
    (if-let [new-params (spec/validate spec params)]
      (apply handler (assoc request :params new-params) args)
      (json-response 400 {}))))

(defn get-user
  [id]
  (if-let [user (db/get-user id)]
    (json-response user)
    (json-response 404 {})))

(defn get-location
  [id]
  (if-let [location (db/get-location id)]
    (json-response location)
    (json-response 404 {})))

(defn get-visit
  [id]
  (if-let [visit (db/get-visit id)]
    (json-response visit)
    (json-response 404 {})))

(defn update-user
  [{fields :body} id]
  (if-let [_ (db/get-user id)]
    (do (db/upsert-entity :user (assoc fields :id id))
        (json-response {}))
    (json-response 404 {})))

(def update-user
  (-> update-user
      (wrap-spec-body :user/update)))

;; todo check if need existance
(defn update-location
  [{fields :body} id]
  (if-let [_ (db/get-location id)]
    (do (db/upsert-entity :location (assoc fields :id id))
        (json-response {}))
    (json-response 404 {})))

(def update-location
  (-> update-location
      (wrap-spec-body :location/update)))

(defn update-visit
  [{fields :body} id]
  (if-let [_ (db/get-visit id)]
    (do (db/upsert-entity :visit (assoc fields :id id))
        (json-response {}))
    (json-response 404 {})))

(def update-visit
  (-> update-visit
      (wrap-spec-body :visit/update)))

(defn create-user
  [{fields :body}]
  (db/upsert-entity :user fields)
  (json-response {}))

(def create-user
  (-> create-user
      (wrap-spec-body :user/create)))

(defn create-location
  [{fields :body}]
  (db/upsert-entity :location fields)
  (json-response {}))

(def create-location
  (-> create-location
      (wrap-spec-body :location/create)))

(defn create-visit
  [{fields :body}]
  (db/upsert-entity :visit fields)
  (json-response {}))

(def create-visit
  (-> create-visit
      (wrap-spec-body :visit/create)))

(defn fix-location-place
  [{{place :place} :location :as m}]
  (-> m
      (assoc :place place)
      (dissoc :location)))

(defn make-visits-map [row]
  (zipmap [:mark :visited_at :place] row))

(defn user-visits
  [request] ;; todo exists fn
  (let [id (-> request :params :id read-string)]
    (if (db/get-user id)
      (let [opt (-> request :params)
            visits (db/user-visits id opt)]
        (json-response
         {:visits (->> visits
                       (map make-visits-map)
                       (sort-by :visited_at))}))
      (json-response 404 {}))))

(def user-visits
  (-> user-visits
      (wrap-spec-params :opt.visits/params)
      wrap-keyword-params
      wrap-params))

(defn smart-round [val]
  (read-string (format "%.5f" val)))

(defn location-avg
  [request]
  (let [id (-> request :params :id read-string)]
    (if (db/get-location id)
      (let [opt (-> request :params)
            res (db/location-avg id opt)
            avg (if res (smart-round res) 0)]
        (json-response {:avg avg}))
      (json-response 404 {}))))

(def location-avg
  (-> location-avg
      (wrap-spec-params :opt.avg/params)
      wrap-keyword-params
      wrap-params))
