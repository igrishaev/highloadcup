(ns highloadcup.server
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.coercions :refer [as-int]]
            [highloadcup.conf :refer [conf]]
            [highloadcup.api :as api]
            [mount.core :as mount]
            [org.httpkit.server :refer [run-server]]
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

(defn get-server-opt []
  {:port (:server-port conf)
   :ip (:server-ip conf)
   :thread (:server-threads conf)})

(mount/defstate
  ^{:on-reload :noop}
  server
  :start (run-server #'api-routes* (get-server-opt))
  :stop (server :timeout 100))

(defn start []
  (mount/start #'server))

(defn stop []
  (mount/stop #'server))
