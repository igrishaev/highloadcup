(ns highloadcup.server
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.coercions :refer [as-int]]
            [highloadcup.conf :refer [conf]]
            [highloadcup.api :as api]
            [mount.core :as mount]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.json :refer
             [wrap-json-response wrap-json-body]]

            [highloadcup.db :as db]

            )
  (:import org.rapidoid.setup.On
           org.rapidoid.http.ReqHandler)

  )

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
      (wrap-json-body {:keywords? true})
      wrap-json-response))

(def json-handler
  (reify ReqHandler
    (execute [_ ^Object req]
      {:foo 999})))

(def json-handler2
  (reify ReqHandler
    (execute [_ ^Object req]
      (db/get-user 1))))
(defn on-start []
  (-> "/foo"
      On/get
      (.json json-handler))
  (-> "/bar/{id}"
      On/get
      (.json json-handler2))
  nil)

(defn on-stop []
  (.shutdown (On/setup)))

(mount/defstate
  ^{:on-reload :noop}
  server
  :start (on-start)
  :stop (on-stop))

(defn start []
  (mount/start #'server))

(defn stop []
  (mount/stop #'server))
