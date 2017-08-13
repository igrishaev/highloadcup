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
  (fn [{body :body :as request}]
    (if (spec/validate spec body)
      (handler request)
      (json-response 400 {}))))

(defn wrap-spec-params
  [handler spec]
  (fn [{params :params :as request}]
    (if-let [new-params (spec/validate spec params)]
      (handler (assoc request :params new-params))
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
    (do (db/update-user id fields)
        (json-response {}))
    (json-response 404 {})))

(def update-user
  (-> update-user
      (wrap-spec-body :user/update)))

(defn update-location
  [{fields :body} id]
  (if-let [_ (db/get-location id)]
    (do (db/update-location id fields)
        (json-response {}))
    (json-response 404 {})))

(def update-location
  (-> update-location
      (wrap-spec-body :location/update)))

(defn update-visit
  [{fields :body} id]
  (if-let [_ (db/get-visit id)]
    (do (db/update-visit id fields)
        (json-response {}))
    (json-response 404 {})))

(def update-visit
  (-> update-visit
      (wrap-spec-body :visit/update)))

(defn create-user
  [{fields :body}]
  (create-user fields)
  (json-response {}))

(def create-user
  (-> create-user
      (wrap-spec-body :user/create)))

(defn create-location
  [{fields :body}]
  (create-location fields)
  (json-response {}))

(def create-location
  (-> create-location
      (wrap-spec-body :location/create)))

(defn create-visit
  [{fields :body}]
  (create-visit fields)
  (json-response {}))

(def create-visit
  (-> create-visit
      (wrap-spec-body :visit/create)))

(defn user-visits
  [request]
  (let [id (-> request :params :id read-string)
        opt (-> request :params)
        visits (db/user-visits id opt)]
    (json-response
     {:visits (sort-by :visited_at visits)})))

(def user-visits
  (-> user-visits
      (wrap-spec-params :opt.visits/params)))

(defn location-avg
  [request]
  (let [id (-> request :params :id Integer/parseInt)
        opt (-> request :params)
        visits (db/location-visits id opt)

        avg (if (empty? visits)
              0
              (let [sum (apply + (map :mark visits))
                    cnt (count visits)]
                (/ sum cnt)))

        rounded (if (> avg 0)
                  (->> avg (format "%.5f") read-string)
                  avg)]

    (json-response {:avg rounded})))

(def location-avg
  (-> location-avg
      (wrap-spec-params :opt.avg/params)))
