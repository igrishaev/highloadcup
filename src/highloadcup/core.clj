(ns highloadcup.core
  (:gen-class)
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.coercions :refer [as-int]]
            [highloadcup.api :as api]
            [mount.core :as mount]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.keyword-params
             :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.json :refer
             [wrap-json-response wrap-json-body]]))

(defroutes api-routes
  (GET "/users/:id" [id :<< as-int]
       (api/get-user id))

  (GET "/locations/:id" [id :<< as-int]
       (api/get-location id))

  (GET "/visits/:id" [id :<< as-int]
       (api/get-visit id))

  (GET "/locations/:id/avg" [id :<< as-int :as request]
       (api/location-avg request))

  (GET "/users/:id/visits" [id :<< as-int :as request]
       (api/user-visits request))

  (POST "/users/:id" [id :<< as-int :as request]
        (api/update-user request id))

  (POST "/locations/:id" [id :<< as-int :as request]
        (api/update-location request id))

  (POST "/visits/:id" [id :<< as-int :as request]
        (api/update-visit request id))

  (POST "/users/new" request (api/create-user request))

  (POST "/locations/new" request (api/create-location request))

  (POST "/visits/new" request (api/create-visit request))

  (route/not-found ""))

(def api-routes*
  (-> api-routes
      wrap-keyword-params
      wrap-params
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
  [& args]
  (start!))
