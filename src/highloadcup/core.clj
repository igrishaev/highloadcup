(ns highloadcup.core
  (:gen-class)
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.coercions :refer [as-int]]
            [mount.core :as mount]
            [clojure.spec.alpha :as s]
            [highloadcup.spec :as spec]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.keyword-params
             :refer [wrap-keyword-params]]
            [ring.middleware.json :refer
             [wrap-json-response wrap-json-body]]))

(def db (atom {}))

(swap! db assoc-in [:users 1]
       {:id 1
        :email "johndoe@gmail.com"
        :first_name "John"
        :last_name "Doe"
        :gender "m"
        :birth_date -1613433600})

(swap! db assoc-in [:visits 5]
       {:id 1
        :location 44
        :user 4
        :visited_at 123123123123
        :mark 2})

(swap! db assoc-in [:visits 6]
       {:id 2
        :location 44
        :user 1
        :visited_at 532523423
        :mark 5})

(defn json-response
  ([body]
   (json-response 200 body))
  ([status body]
   {:status status
    :body body}))

(defn html-response
  ([body]
   (html-response 200 body))
  ([status body]
   {:status status
    :body body}))

(defn get-user-by-id
  [user-id]
  (if-let [user (get-in @db [:users user-id])]
    (json-response user)
    (html-response 400 "")))

(defn get-location-by-id [loc-id]
  (json-response {:location 42}))

(defn get-visit-by-id [loc-id]
  (json-response {:visit 42}))

(defn spec-wrapper [handler spec]
  (fn [{body :body :as request}]
    (if (spec/validate spec body)
      (handler request)
      (json-response 400 {}))))

(defn create-new-user [request]
  (let [fields (:body request)
        id (:id fields)
        user (assoc fields :id id)]
    (swap! db assoc-in [:users id] user)
    (json-response user)))

(def create-new-user*
  (-> create-new-user
      (spec-wrapper :user/create)))

(defn update-user
  [{body :body} user-id]
  (swap! db update-in [:users user-id] merge body)
  (json-response {}))

(defn get-visits [user-id]
  (let [pred (every-pred
              (fn [visit] (-> visit :user (= user-id))))
        visits (filter pred (-> @db :visits vals))]
    (json-response {:visits visits})))

(defn location-avg [loc-id]
  (let [pred (every-pred
              (fn [visit] (-> visit :location (= loc-id))))
        visits (filter pred (-> @db :visits vals))
        sum (apply + (map :mark visits))
        cnt (count visits) ;; zero
        result (/ sum cnt) ;; rount to 5

        ]
    (json-response {:avg result}))

  )


(defroutes api-routes
  (GET "/users/:id" [id :<< as-int] (get-user-by-id id))
  (GET "/locations/:id" [id :<< as-int] (get-location-by-id id))
  (GET "/visits/:id" [id :<< as-int] (get-visit-by-id id))

  (GET "/locations/:id/avg" [id :<< as-int] (location-avg id))

  (GET "/users/:id/visits" [id :<< as-int] (get-visits id))

  (POST "/users/:id" [id :<< as-int :as request]
        (update-user request id))

  (POST "/users/new" request
        (create-new-user* request)))

(def api-routes*
  (-> api-routes
      wrap-keyword-params
      (wrap-json-body {:keywords? true})
      wrap-json-response))

(def jetty-params
  {:port 3333
   :join? false})

(mount/defstate server
  :start (run-jetty api-routes* jetty-params)
  :stop (.stop server))

(defn start! []
  (mount/start #'server))

(defn stop! []
  (mount/stop #'server))

(defn -main
  [& args])
