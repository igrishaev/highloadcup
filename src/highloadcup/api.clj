(ns highloadcup.api
  (:require [highloadcup.db :as db]
            [highloadcup.spec :as spec]))

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

(defn fix-location [m]
  (update m :location :place))

(defn user-visits
  [request]
  (let [id (-> request :params :id read-string)
        opt (-> request :params)
        visits (db/user-visits id opt)]
    (json-response
     {:visits (->> visits
                   (sort-by :visited_at)
                   (map fix-location))})))

(def user-visits
  (-> user-visits
      (wrap-spec-params :opt.visits/params)))

(defn location-avg
  [request]
  (let [id (-> request :params :id Integer/parseInt)
        opt (-> request :params)
        avg (or (db/location-avg id opt) 0)]
    (json-response {:avg avg})))

(def location-avg
  (-> location-avg
      (wrap-spec-params :opt.avg/params)))
